# CowinPy-Android

Its an easier way to run python script to Covid vaccine slot book developed by [@pallupz](https://github.com/pallupz/covid-vaccine-booking) . In earlier version Script have to be run continously on PC/Laptop, But by using Chaquopy we can do that on Android devices. It will be way less power consuming and help folks not having access to Computer Laptops.

You can dowload apk from [Here](https://github.com/Vishvajeet590/CowinPy-Andy/blob/master/APK/CowinPy.apk)<br />
You can dowload Beta apk from [Here](https://github.com/Vishvajeet590/CowinPy-Andy/blob/master/APK/CowinPy%20Test.apk)<br />



**If possible notify if it works or if any problem immediately create new issue. Also, STAR this repository with others if you liked. It keeps me motivated**



# Update ✍ : 

<pre>
Master Branch
V1.1 : Automatically detects SMS from COWIN and enters it.<br />
     : Beep alert sounds are now suported on Android.<br />
     : User can continue without giving permission to SMS and type OTP manually.
</pre>

<pre>
Dev Branch
V2.0 : Added feature to save JSON for diffrent profiles configuration.
     : Added feature to send OTP to PC version of script.
     : Fixed crashing bugs.
</pre>

# Screenshots 📱 :

<img src="https://user-images.githubusercontent.com/42716731/121064853-b69b4780-c7e5-11eb-9bb7-1317c406bbbe.png" width="250">         <img src="https://user-images.githubusercontent.com/42716731/121064869-bbf89200-c7e5-11eb-8e70-736d350ca904.png" width="250">         

<img src="https://user-images.githubusercontent.com/42716731/121639208-d367a180-caa9-11eb-8879-9defade8afda.png" width="250">         <img src="https://user-images.githubusercontent.com/42716731/121639083-a6b38a00-caa9-11eb-9461-9b7761361003.png" width="250">


<img src="https://user-images.githubusercontent.com/42716731/120796719-c8b48600-c558-11eb-89d1-d325ae77bfec.png" width="550">   



<img src="https://user-images.githubusercontent.com/42716731/120796625-a6bb0380-c558-11eb-9faa-e7d084eb01cc.png" width="550">   



<img src="https://user-images.githubusercontent.com/42716731/120796764-d702a200-c558-11eb-8045-1a40ed26c965.png" width="550">   


# Setup process for Otp to Pc feature
Note : Pc and android device must be connected to a common network (Same wifi or Connect pc with hotspot of Android device )

Step 1 : Download Beta APK from [Here](https://github.com/Vishvajeet590/CowinPy-Andy/blob/master/APK/CowinPy%20Test.apk) and Zip containg all files from OtpTOPc folder from [Here](https://github.com/Vishvajeet590/CowinPy-Andy/blob/master/Otp2Pc/Otp2Pc.rar). Run OtpServer.py in terminal.

<img src="https://user-images.githubusercontent.com/42716731/121730370-39d1db80-cb0d-11eb-9926-c6b0fb10763c.png" width="750">

Step 2 : Open app on your device and press Laptop icon on top right corner. Enter the details in App and hit Handshake. When you see "You are connected", press Save in app.<br />
         ( I have not added many checks (LAZY) so please enter as they appear on screen ) 

<img src="https://user-images.githubusercontent.com/42716731/121730648-9a611880-cb0d-11eb-94e9-7b3a45016bb6.png" width="250">

That's it now run the main.py in terminal.🤞

# BONUS : Use Otp2Pc in your project 🤘

Download otpServer.py file from [Here](https://github.com/Vishvajeet590/CowinPy-Andy/blob/master/Otp2Pc) and add it to your working directory
```python
from otpServer import getOtp

# use this function whereever you want OTP

OTP = getOtp()

```

