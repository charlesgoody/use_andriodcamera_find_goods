# -*- coding: utf-8 -*-

import pandas as pd

#df = pd.read_csv('.\\csv\\jacket.csv')
df = pd.read_csv('.\\tshirt_simplecolor.csv')
for i,imgpath in enumerate(df['Imagepath']):
    global imgbyte
    try:
        with open(imgpath,'rb') as img:
            imgbyte = img.read()
            img.close()
            print ('可'+df.loc[i,'Imagepath'])
    except FileNotFoundError:
        print ('不可'+df.loc[i,'Imagepath'])
        df.drop(i,axis = 0,inplace=True)
        
df.to_csv('.\\data.csv', encoding='utf_8_sig',index=False)