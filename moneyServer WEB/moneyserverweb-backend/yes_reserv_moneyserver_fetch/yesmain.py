import pdfplumber

def is_debit(currCrDrString):
    if currCrDrString[-2] == 'D':
        return True
    return False

def extract_amount(currCrDrString):
    amount = ''
    i = 0
    while currCrDrString[i] != ' ':
        amount+=currCrDrString[i]
        i+=1
    return amount

def pull_gen_yes_txns_from_statement(pdf_path,pdf_password):
    try:
        with pdfplumber.open(pdf_path, password=pdf_password) as pdf:
            total_pages = len(pdf.pages)
            return_data = []
            for currpagenumber in range(total_pages):
                page = pdf.pages[currpagenumber].extract_table()
                if page == None:
                    continue
                index = 0
                for item in page:
                    print(item)
                    if index == 0:
                        index+=1
                        continue
                    if item[0] == None and 'Cashback' in item[1]:
                        break
                    currDataString = item[0] + " - " + item[1]
                    currCrDrString = item[3]
                    isDebit = is_debit(currCrDrString)
                    if isDebit == True :   
                        amountString = extract_amount(currCrDrString)
                        return_data.append(["{:.2f}".format(float(amountString.replace(",", ""))),currDataString])
                    index+=1
            return return_data
    except Exception as e:
        print(f"An unexpected error occurred: {e}")