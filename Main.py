import ML
import ImageResize
import pandas as pd
import base64
from socketserver import BaseRequestHandler,ThreadingTCPServer
import cv2
from skimage.measure import compare_ssim

class CompareImage():
    def compare_image(self, path_image1, path_image2):

        imageA = cv2.imread(path_image1)
        imageB = cv2.imread(path_image2)

        grayA = cv2.cvtColor(imageA, cv2.COLOR_BGR2GRAY)
        grayB = cv2.cvtColor(imageB, cv2.COLOR_BGR2GRAY)

        (score, diff) = compare_ssim(grayA, grayB, full=True)
        # print("SSIM: {}".format(score))
        return score
    
def getKeysByValue(dictOfElements, valueToFind):
    listOfItems = dictOfElements.items()
    for item  in listOfItems:
        if item[1] == valueToFind:
            listOfKey=item[0]
    return  listOfKey

def sort_by_value(d):
   items=d.items()
   backitems=[[v[1],v[0]] for v in items]
   backitems.sort()
   return [ backitems[i][1] for i in range(0,len(backitems))]

class Handler(BaseRequestHandler):
    def handle(self):
        address,pid = self.client_address
        BUF_SIZE=10240
        print('%s connected!'%address)
        global imagebytes
        imagebytess = bytearray()
        while True:
            socketdata = self.request.recv(BUF_SIZE)
            if len(socketdata)>0:
                #print('receive=',print(socketdata))
                print('receive')
                imagebytess.extend(socketdata)
            else:
                print('get it!')
                #server.shutdown()
                imagebytes = imagebytess
                with open('image.jpg','wb') as f:
                    f.write(imagebytes)
                    f.close
                MLResult = ML.AutoMlFunc('image.jpg')
                data={}
                for result in MLResult:
                    data.update({result.display_name:result.classification.score})
                MAX1=max(data["polo"],data["t_shirt"],data["sleeveless"],data["jacket"],data["stripe"])
                label1=getKeysByValue(data, MAX1)
                if(label1=="t_shirt"):
                    MAX2=max(data["nikelogo"],data["simplecolor"],data["mark"])
                    if(MAX2>=0.5):
                        label2=getKeysByValue(data, MAX2)
                        if(label2=="nikelogo"):
                            csvpath = ".\\csv\\tshirt_nike.csv"
                        else:
                            csvpath=".\\csv\\tshirt"+"_"+label2+".csv"
                    else:
                        csvpath = ".\\csv\\" + "tshirt.csv"
                elif(label1=="polo"):
                    MAX2 = max(data["long"], data["stripe"], data["simplecolor"])
                    if (MAX2 >= 0.2):
                        label2 = getKeysByValue(data, MAX2)
                        csvpath = ".\\csv\\" + label1 + "_" + label2 + ".csv"
                    else:
                        csvpath = ".\\csv\\" + label1 + ".csv"
                elif(label1=="stripe"):
                    if(data["long"]>=0.2):
                        csvpath = ".\\csv\\" + label1 + "_" + "long.csv"
                    else:
                        csvpath = ".\\csv\\" + label1 + ".csv"
                elif(label1=="jacket"):
                    #MAX2 = max(data["long"])
                    if (data["long"] >= 0.2):
                        csvpath = ".\\csv\\" + label1 + "_" + "long.csv"
                    else:
                        csvpath = ".\\csv\\" + label1 + ".csv"
                pf = pd.read_csv(csvpath)
                df = pd.DataFrame(pf)
                df['Score'] = 0
                compare_image = CompareImage()
                ImageResize.convertjpg('image.jpg',".\\")
                for index,row in pf.iterrows():
                    img_path=row["Imagepath"]
                    df.loc[index,"Score"]=compare_image.compare_image('image.jpg', img_path)
                df.sort_values(by=['Score'], ascending=False,inplace=True)
                for index,row in pf.iterrows():
                    if(index>29):
                        df.drop(index,axis=0,inplace=True)
                print (df['Imagepath'])            
                for i,imgpath in enumerate(df['Imagepath']):
                    global imgbyte
                    with open(imgpath,'rb') as img:
                        imgbyte = img.read()
                        img.close()
                    df.iloc[i,2] = base64.encodebytes(imgbyte).decode("utf-8")
                jsonData = df.to_json(orient='records', lines=False)
                self.request.sendall(jsonData.encode('UTF-8'))
                print('send it!')
                self.request.close
                print('close')
                print (MLResult)
                print (df['Product'])
                print (df['Score'])
                break

if __name__ == '__main__':
    HOST = '************'
    PORT = 8787
    ADDR = (HOST,PORT)
    server = ThreadingTCPServer(ADDR,Handler) 
    print('listening')
    server.serve_forever() 


