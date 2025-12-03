import pdfplumber
from fastapi import FastAPI, UploadFile, File
import shutil
import os
from swiggy_hdfc_moneyserver_fetch import swiggymain
import time

app = FastAPI()

@app.post("/parth-moneyserver-services/moneyServer-web/getAllTxns/{ccname}")
async def square(ccname: str,file: UploadFile = File(...),swiggyCard_year: str = None,swiggyCard_month: str = None):
    UPLOAD_DIR = os.path.join("moneyserverwebAPI", "uploads")
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    file_path = os.path.join(UPLOAD_DIR, file.filename)
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    pdf_path = file_path
    pdf_password = ""
    return_data = []
    if ccname == "rupayhdfc" or ccname == "regaliagoldhdfc" :
        pdf_password = "pass"
        return_data = pull_txns_from_statement(pdf_path,pdf_password)
        print(str(len(return_data)) + " data rows returned for card "+ ccname);
    elif ccname == "swiggyhdfc":
        pdf_password = "pass"
        return_data = swiggymain.hdfc_swiggy_cc_statement_extractor(pdf_path,pdf_password,swiggyCard_month,swiggyCard_year)
    os.remove(file_path)
    return return_data

def is_debit(currDataString):
    index = len(currDataString) - 1
    while(currDataString[index] != 'C'):
        index -= 1
    if currDataString[index-2] == '+':
        return False
    else:
        return True

def extract_amount(currDataString):
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
    while(currDataString[index] != ' '):
        amountString += currDataString[index]
        index += 1
    return amountString

def pull_txns_from_statement(pdf_path,pdf_password):
    try:
        with pdfplumber.open(pdf_path, password=pdf_password) as pdf:
            total_pages = len(pdf.pages)
            return_data = []
            for currpagenumber in range(total_pages):
                page = pdf.pages[currpagenumber].extract_table()
                if page == None:
                    continue
                if "Reward" in page[0][0]:
                    continue
                index = 0
                for item in page:
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
        print(f"An unexpected error occurred: {e}")


# if __name__ == "__main__":
#     pdf_path = "credit card statement pdf"
#     pdf_password = "pass" 
#     return_data = pull_txns_from_statement(pdf_path,pdf_password)
#     print(return_data)