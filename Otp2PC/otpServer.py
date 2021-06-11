from Crypto.Cipher import AES
import base64
import os
import socket 
from pathlib import Path
import os.path

BLOCK_SIZE = 16
PADDING = '\0'
pad_it = lambda s: s+(16 - len(s)%16)*PADDING
#key =b'B31F2A75FBF94099'
iv = b'1234567890123456'

def get_details(port):
    hostname=socket.gethostname()   
    IPAddr=socket.gethostbyname(hostname)
    key_file = Path("key/AES_Key.txt")
    if os.path.isfile(key_file):
        key = getKey().encode('utf-8')
    else:
        print('##########################################################################################')
        print('Details to enter in App to connect you Phone')
        print("IP Address = "+IPAddr)
        print("Port  = ",port)
        key_file = Path("key/AES_Key")
        key = input("Enter the 16 digit encyption key DISPLAYED on APP : ")
        writeKey(key)
        key = key.encode('utf-8')
        print('##########################################################################################')

    return key

# using the aes algorithm for decryption of encrypted OTP which is encoding as java using same AES
def pad_byte(b):
    bytes_num_to_pad = BLOCK_SIZE - (len(b) % BLOCK_SIZE)
    byte_to_pad = bytes([bytes_num_to_pad])
    padding = byte_to_pad * bytes_num_to_pad
    padded = b + padding
    return padded

# def encrypt_aes(sourceStr):
#     #  note that there segment_size=128
#     generator = AES.new(key, AES.MODE_CFB, iv, segment_size=128)
#     #  notice that i'm going to pass the clear text first utf-8  transcoding is called as a byte string padding  algorithm 
#     padded = pad_byte(sourceStr.encode('utf-8'))
#     crypt = generator.encrypt(padded)
#     cryptedStr = base64.b64encode(crypt)
#     return cryptedStr

def decrypt_aes(cryptedStr,keys):
    key = keys
    #  note that there segment_size=128
    generator = AES.new(key, AES.MODE_CFB, iv, segment_size=128)
    cryptedStr = base64.b64decode(cryptedStr)
    recovery = generator.decrypt(cryptedStr)
    #print(recovery)
    decryptedStr = recovery.rstrip(PADDING.encode('utf-8'))
    return decryptedStr

def next_free_port( port=1024, max_port=65535 ):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    while port <= max_port:
        try:
            sock.bind(('', port))
            sock.close()
            return port
        except OSError:
            port += 1
    raise IOError('no free ports')

def run_server(soc,key):
    print("Waiting for OTP......")
    otp = ''
    while True:
        c,adrr = soc.accept()
        mes = c.recv(16)
        otp = mes
        print("Your encrypted OTP is :",otp)
        c.close()
        otp = decrypt_aes(otp,key).decode("utf-8") 
        otp = otp[:6]
        if otp.isdigit and len(otp) == 6: 
            break
    soc.close()
    print("OTP recieved = ",otp)
    print("Server Stoped")
    return otp

def getKey():
    f = open("key\AES_Key.txt", "r+")
    k = f.read()
    return k

def writeKey(key):
     f = open("key\AES_Key.txt", "w+")
     f.write(key)

def getOtp():
    # Get free port number
    port = next_free_port()

    # Start socket 
    soc = socket.socket()
    soc.bind(('', port))
    soc.listen(5)

    # Provide and get req details
    key = get_details(port)

    #get Otp
    OTP = run_server(soc,key)
    return OTP

def handShake():
    p = next_free_port()
    hostname=socket.gethostname()   
    IPAddr=socket.gethostbyname(hostname)
    print('##########################################################################################')
    print('Details to enter in App to connect you Phone')
    print("IP Address = "+IPAddr)
    print("Port  = ",p)
    

   # k = get_details(p)
    sok = socket.socket()
    sok.bind(('', p))
    sok.listen(5)
    while True:
        print("#################  SERVER STARTED  #################")
        c,adrr = sok.accept()
        mes = c.recv(32)
        print(mes)
        c.close()
        if mes == b'HIMYPC':
            break
        else : 
            print("LETS TRY AGAIN")
    sok.close()
    print("YOU ARE CONNECTED")
    key_file = Path("key/AES_Key.txt")
    if os.path.isfile(key_file):
        print("Your encryption key is already stored")
    else:
        key = input("Enter the 16 digit encyption key DISPLAYED on APP : ")
        writeKey(key)



if __name__ == "__main__": 
    handShake()
