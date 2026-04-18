import pdfplumber
from fastapi import FastAPI, UploadFile, File
import shutil
import os
from swiggy_hdfc_moneyserver_fetch import swiggymain
from yes_reserv_moneyserver_fetch import yesmain
from hdfc_savingsacc_moneyserver_fetch import hdfcsavingsmain
import time
import uuid
import requests
import json
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware


COOKIE_FILE = "swiggy_cookie.txt"
class CookieRequest(BaseModel):
    cookie: str

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/parth-moneyserver-services/moneyServer-web/updateSwiggyCookie/")
async def rectangle(req: CookieRequest):
    print(req.cookie)
    with open(COOKIE_FILE, "w") as f:
        f.write(req.cookie.strip())
    return {"message": "cookie-received"}

@app.post("/parth-moneyserver-services/moneyServer-web/getAllTxns/{ccname}")
async def square(ccname: str,file: UploadFile = File(...),card_year: str = None,card_month: str = None):
    UPLOAD_DIR = os.path.join("moneyserverwebAPI", "uploads")
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    file_path = os.path.join(UPLOAD_DIR, file.filename)
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    pdf_path = file_path
    pdf_password = ""
    return_data = []
    saveStateId = ""
    isBank = False
    swiggyCookieAlive = None
    if ccname == "hdfcsavings":
        isBank = True
        saveStateId = "hdfcsavings-bank"
    else:
        saveStateId = ccname.lower() + "-" + card_month.lower() + "-" + card_year
    saveStateExists , saveStateDataList = get_saveStateData_moneyserver(saveStateId,isBank)
    if saveStateExists == True:
        return_data = saveStateDataList
        return {
                "creditCardName": ccname,
                "swiggyCookieAlive": None,
                "rows": return_data
            }
    if ccname == "rupayhdfc" or ccname == "regaliagoldhdfc" :
        pdf_password = "pass"
        return_data = pull_gen_hdfc_txns_from_statement(pdf_path,pdf_password)
    elif ccname == "swiggyhdfc":
        pdf_password = "pass"
        swiggyCookieAlive, return_data = swiggymain.hdfc_swiggy_cc_statement_extractor(pdf_path,pdf_password,card_month,card_year)
        if swiggyCookieAlive == False:
            return {
                "creditCardName": ccname,
                "swiggyCookieAlive": swiggyCookieAlive,
                "rows": []
            }
    elif ccname == "yesreserv":
        pdf_password = "pass"
        return_data = yesmain.pull_gen_yes_txns_from_statement(pdf_path,pdf_password)
        total_sum = get_statement_total(return_data)
        return_data.insert(0,[str(total_sum),"#############################  THIS IS TOTAL SUM OF ALL TXN AMT !!! ###############################"])
    elif ccname == "hdfcsavings":
        xls_path = pdf_path
        return_data = hdfcsavingsmain.pull_gen_hdfcsavings_txns_from_statement(xls_path)
    os.remove(file_path)
    set_saveStateId_firstTime(return_data)
    return {
                "creditCardName": ccname,
                "swiggyCookieAlive": swiggyCookieAlive,
                "rows": return_data
            }

def get_saveStateData_moneyserver(saveStateId, isBank):
    try:
        url = "http://192.168.29.179:8080/parth-moneyserver-services/moneyServer/saveState/" + saveStateId
        response = requests.get(url)
        response.raise_for_status()
        resJson = response.json();
        if resJson["exist"] == False:
            return False,None
        else:
            list_data = []
            saveStateRows_data = resJson["data"]["saveStateRows"]
            saveStateRows_len = len(saveStateRows_data)
            for dataRowIndex in range(saveStateRows_len):
                if isBank == False:
                    list_data.append([saveStateRows_data[dataRowIndex]["txnAmount"], 
                                  saveStateRows_data[dataRowIndex]["txnDetail"], 
                                  saveStateRows_data[dataRowIndex]["saveStateRowId"]])
                else:
                    list_data.append([saveStateRows_data[dataRowIndex]["bankTxnDay"], 
                                  saveStateRows_data[dataRowIndex]["bankTxnMonth"], 
                                  saveStateRows_data[dataRowIndex]["bankTxnYear"],
                                  saveStateRows_data[dataRowIndex]["bankTxnSeqNo"],
                                  saveStateRows_data[dataRowIndex]["txnAmount"],
                                  saveStateRows_data[dataRowIndex]["saveStateRowId"]])
            return True, list_data
    except Exception as e:
        print(f"An unexpected error occurred during main.get_saveStateData_moneyserver(): {e}")
        return False,None
def set_saveStateId_firstTime(return_data):
    for txn in return_data:
        newid = uuid.uuid4()
        txn.append(newid)
    return return_data
def get_statement_total(return_data):
    total_sum = 0
    for txn in return_data:
        total_sum += float(txn[0])
    return total_sum
def is_debit(currDataString):
    try:
        index = len(currDataString) - 1
        while(currDataString[index] != 'C'):
            index -= 1
        if currDataString[index-2] == '+':
            return False
        else:
            return True
    except Exception as e:
        print(f"An unexpected error occurred during main.is_debit(): {e}")
        return True
def extract_amount(currDataString):
    try:
        index = 0
        while (
            index < len(currDataString) - 1 and
            not (
                index > 0 and
                currDataString[index - 1] == ' ' and 
                currDataString[index] == 'C' and 
                currDataString[index + 1] == ' '
            )
        ):
            index += 1
        index += 2
        amountString = ""
        if currDataString[index] == 'C':
            index += 2
        while(currDataString[index] != ' '):
            amountString += currDataString[index]
            index += 1
        return amountString
    except Exception as e:
        print(f"An unexpected error occurred during main.extract_amount(): {e}")
        return "0.0"
def pull_gen_hdfc_txns_from_statement(pdf_path,pdf_password):
    try:
        with pdfplumber.open(pdf_path, password=pdf_password) as pdf:
            total_pages = len(pdf.pages)
            return_data = []
            for currpagenumber in range(total_pages):
                words = pdf.pages[currpagenumber].extract_words()
                target_top_coord = None
                for word in words:
                    if word['text'] == 'Domestic':
                        target_top_coord = word['top']
                        break
                if target_top_coord is not None:
                    crop_bbox = (pdf.pages[currpagenumber].bbox[0], target_top_coord, pdf.pages[currpagenumber].bbox[2], pdf.pages[currpagenumber].bbox[3])
                    cropped_page = pdf.pages[currpagenumber].crop(crop_bbox)
                    page = cropped_page.extract_table()
                    if page == None:
                        continue
                    if "Reward" in page[0][0]:
                        continue
                    index = 0
                    for item in page:
                        print(item)
                        if index == 0:
                            index+=1
                            continue
                        if item[0] == None and 'Cashback' in item[1]:
                            break
                        currDataString = ""
                        if currpagenumber == 0 and index == 1:
                            currDataString = item[0][16:]
                        else:
                            currDataString = item[0]
                        isDebit = is_debit(currDataString)
                        if isDebit == True :   
                            amountString = extract_amount(currDataString)
                            return_data.append(["{:.2f}".format(float(amountString.replace(",", ""))),currDataString])
                        index+=1
            return return_data
    except Exception as e:
        print(f"An unexpected error occurred during main.pull_gen_hdfc_txns_from_statement(): {e}")


# if __name__ == "__main__":
#     pdf_path = "credit card statement pdf"
#     pdf_password = "pass" 
#     return_data = pull_txns_from_statement(pdf_path,pdf_password)
#     print(return_data)