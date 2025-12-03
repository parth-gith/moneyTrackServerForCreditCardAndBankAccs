import pdfplumber
import sys
import re
import requests
import json
import datetime
import os


def extract_date(currDataString):
    dateString = ""
    for c in currDataString:
        if c != '|':
            dateString+=c
        else:
            break
    return dateString
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
def extract_platform(currDataString):
    currDataStringALPHA = re.sub(r'\d', '', currDataString).lower()
    if "swiggy" in currDataStringALPHA or "instamart" in currDataStringALPHA or 'bundl' in currDataStringALPHA:
        return "Swiggy"
    elif "zomato" in currDataStringALPHA or 'bistro' in currDataStringALPHA:
        return "Zomato"
def is_date_between(target_date,start_date,end_date):
    target =  datetime.datetime.strptime(target_date, "%d/%m/%Y")
    start =  datetime.datetime.strptime(start_date, "%d/%m/%Y")
    end =  datetime.datetime.strptime(end_date, "%d/%m/%Y")
    if start <= target <= end:
        return [True,False,False]
    else:
        if target < start:
            # tuple of bool , before start, after end
            return [False,True,False]
        elif target > end:
            return [False,False,True]
def fetch_swiggy_txndb(cookie,start_date,end_date):
    swiggyTxnHashMap = {}
    url = "https://www.swiggy.com/dapi/order/all?order_id="
    headers = {
        "Cookie": cookie,
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36",
        "content-type":"application/json",
        "referer":"https://www.swiggy.com/my-account"
    }
    try:
        isFirstResp = True
        lastRespId = ""
        stopFetch = False
        while stopFetch == False:
            if isFirstResp == True:
                response = requests.get(url, headers=headers)
                isFirstResp = False
            else:
                response = requests.get(url+str(lastRespId), headers=headers)
            response.raise_for_status()
            swiggy_orderes_json_data = response.json()
            orders_array = swiggy_orderes_json_data["data"]["orders"]
            for order in orders_array:
                lastRespId = order["order_id"]
                order_date = str(datetime.datetime.strptime(order["order_time"][0:10], '%Y-%m-%d').strftime('%d/%m/%Y'))
                isBetween,isBeforeStart,isAfterEnd = is_date_between(order_date,start_date,end_date)
                if isBetween == True:
                    order_amount = "{:.2f}".format(float(order["payment_info_v2"]["amount_collected"]["units"]))
                    order_restaurant_address = order["restaurant_name"] + ", " + order["restaurant_locality"] + ", " + order["restaurant_area_name"]
                    order_items = order["order_items"]
                    order_items_num = len(order_items)
                    order_items_full_name = ""
                    for itemId in range(order_items_num):
                        order_items_full_name += order_items[itemId]["name"]
                        if itemId < order_items_num - 1 :
                            order_items_full_name += " & "
                    order_details_full = "Swiggy " + order_restaurant_address + " | " + order_items_full_name
                    if order_date in swiggyTxnHashMap :
                        if order_amount in swiggyTxnHashMap[order_date]:
                            swiggyTxnHashMap[order_date][order_amount].append([order_details_full,False])
                        else:
                            swiggyTxnHashMap[order_date][order_amount] = [[order_details_full,False]]
                    else:
                        swiggyTxnHashMap[order_date] = {order_amount: [[order_details_full, False]]}
                else:
                    if isBeforeStart == True:
                        stopFetch = True
                    elif isAfterEnd == True:
                        continue
        return swiggyTxnHashMap
    except Exception as e:
        print(f"An unexpected error occurred: {e}")
def sendToMoneyserver(txnmonthBill,txnyearBill,txnamt,txndetails):
    print("sending to moneyserver....")
    url = "http://ip:port/parth-moneyserver-services/moneyServer/CreditCardTxnDetails/txn"
    payload = {
        "txnBillingMonth": txnmonthBill,
        "txnBillingYear" : txnyearBill,
        "txnCCused" : "Swiggy HDFC Visa",
        "txnDetails" : txndetails,
        "txnAmount" : float(txnamt),
        "txnIsEmi" : False
    }
    response = requests.post(url, json=payload)
    print(f"Status Code: {response.status_code}")
    print(f"Response Content (JSON): {response.json()}")
    print("###################################################################################################")
def hdfc_swiggy_cc_statement_extractor(pdf_path,pdf_password,moneyserverMonth,moneyserverYear):
    try:
        with pdfplumber.open(pdf_path, password=pdf_password) as pdf:
            total_pages = len(pdf.pages)
            txnDataHashmap = {}
            start_date = ""
            end_date = ""
            for currpagenumber in range(total_pages):
                page = pdf.pages[currpagenumber].extract_table()
                if page == None:
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
                    print(currDataString)
                    isDebit = is_debit(currDataString)
                    if isDebit == True:
                        dateString = extract_date(currDataString)
                        end_date = dateString
                        if currpagenumber == 0 and index == 1:
                            start_date = dateString
                        amountString = extract_amount(currDataString)
                        platformString =  extract_platform(currDataString)
                        if dateString in txnDataHashmap :
                            txnDataHashmap[dateString].append([platformString,"{:.2f}".format(float(amountString.replace(",", ""))),False])
                        else:
                            txnDataHashmap[dateString] = [[platformString,"{:.2f}".format(float(amountString.replace(",", ""))),False]]
                    index+=1
            cookie = "cookie"
            swiggyTxnHashMap = fetch_swiggy_txndb(cookie,start_date,end_date)
            for cctxndate in txnDataHashmap:
                for cctxn in txnDataHashmap[cctxndate]:
                    if cctxn[0] == "Swiggy":
                        cctxnamt = cctxn[1]
                        if cctxndate in swiggyTxnHashMap:
                            if cctxnamt in swiggyTxnHashMap[cctxndate]:
                                indexx = 0
                                for txn in swiggyTxnHashMap[cctxndate][cctxnamt]:
                                    if txn[1] == False:
                                        swiggyTxnHashMap[cctxndate][cctxnamt][indexx][1] = True
                                        cctxn[2] = True
                                        cctxndetails = swiggyTxnHashMap[cctxndate][cctxnamt][indexx][0]
                                        sendToMoneyserver(moneyserverMonth,moneyserverYear,cctxnamt,cctxndetails)
                                    indexx += 1
            print("MANUAL ENTRY NEEDED FOR BELOW TXNS : ")
            return_data = []
            for cctxndate in txnDataHashmap:
                for cctxn in txnDataHashmap[cctxndate]:
                    if cctxn[2] == False:
                        print(str(cctxndate) + " " + str(cctxn[0]) + " " + str(cctxn[1]))
                        return_data.append([str(cctxn[1]),str(cctxndate) + " " + str(cctxn[0])])
            return return_data
    except Exception as e:
        print(f"An unexpected error occurred: {e}")



# if __name__ == "__main__":
#     pdf_path = "credit card statement pdf" 
#     pdf_password = "pass" 
#     hdfc_swiggy_cc_statement_extractor(pdf_path,pdf_password,"December","2025")

    