import numpy as np
import pandas as pd
from datetime import datetime


def get_datetime_data(s: str):
    try:
        dt = datetime.strptime(s, "%d/%m/%y")
        return True,[dt.day, dt.strftime("%B"), dt.year]
    except Exception:
        return False, []
    
def pull_gen_hdfcsavings_txns_from_statement(xls_path):
    try:
        df = pd.read_excel(xls_path,header=20)
        return_data = []
        curr_seqno = 1
        prev_seqno_date = ''
        for i in df.itertuples():
            if pd.isna(i.Date):
                continue
            is_valid_date , date_data = get_datetime_data(i.Date)
            if is_valid_date == True:
                if i.Date != prev_seqno_date:
                    prev_seqno_date = i.Date
                    curr_seqno = 1
                elif i.Date == prev_seqno_date:
                    curr_seqno += 1
                amountString = ""
                if pd.isna(i._6):
                    amountString = '-' + str(i._5)
                elif pd.isna(i._5):
                    amountString = str(i._6)
                return_data.append([date_data[0],date_data[1],date_data[2],curr_seqno,amountString,str(i.Narration)])
        return return_data
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

        
            