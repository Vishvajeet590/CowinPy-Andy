package com.vishvajeet590.python.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;





public class SMSBraodcast extends BroadcastReceiver {
    //private static String sKey = "B31F2A75FBF94099";
    private static String sKey = "";

    private static String ivParameter = "1234567890123456";
    public static String sms = "android.provider.Telephony.SMS_RECEIVED";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BroadcastReceiver","INSIDE");
        if (intent.getAction().equals(sms)){
            Bundle bundle = intent.getExtras();
            Object[] ob = (Object[]) bundle.get("pdus");
            SmsMessage[] message = new SmsMessage[ob.length];
            String val = "";
            for (int i =0 ;i<ob.length;i++){
                message[i]= SmsMessage.createFromPdu((byte[]) ob[i]);
                if (message[i].getOriginatingAddress().equals("JD-NHPSMS") || message[i].getOriginatingAddress().equals("AX-NHPSMS") || message[i].getOriginatingAddress().contains("NHPSMS") ){
                    Pattern pattern = Pattern.compile("(\\d{6})");
                    Matcher matcher = pattern.matcher(message[i].getMessageBody());

                    if (matcher.find()) {
                        val = matcher.group(0);
                        String encOTP ="";

                        try {
                            sKey = readKey(context);
                            // encOTP = encrypt(val,sKey);
                             //Log.d("KEYSIS","LATEST KEY :"+sKey);
                            //Log.d("BroadcastReceiver","enc = "+encOTP);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("BroadcastReceiver","err ="+e.getMessage() );

                        }

                        String ip = readIp(context);
                        String port = readPort(context);
                        OtpSender sender = new OtpSender();
                        sender.execute(val,ip,port);// 6 digit number
                        Log.d("BroadcastReceiver","OUT");
                    }
                    break;

                }



            }

            Toast.makeText(context, "SMS RECIEVED from :"+ message[0].getOriginatingAddress()+"  \n body = \n"+message[0].getMessageBody(), Toast.LENGTH_SHORT).show();




        }
    }







    public class OtpSender extends AsyncTask<String,Void,Void> {
        Socket socket;
        DataOutputStream dos;
        PrintWriter pw;

        OutputStreamWriter osw;


        @Override
        protected Void doInBackground(String... strings) {
            String otp = strings[0];
            String ip = strings[1];
            String port = strings[2];
            try {
                Log.d("BroadcastReceiver","In BAck");
                socket = new Socket(ip,Integer.parseInt(port));
                pw = new PrintWriter(socket.getOutputStream());
                pw.write(otp);
                pw.close();
                socket.close();
            } catch (IOException e) {
                Log.d("BroadcastReceiver","ERR = "+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }




    }



    public static String encrypt(String sSrc,String sKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());//  use cbc mode ， a vector iv is required to increase the strength of the encryption algorithm
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
        // return new BASE64Encoder().encode(encrypted);// base64 is used here for transcoding 。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new String(Base64.getEncoder().encode(encrypted), "UTF-8");
        }
        else return null;
    }


    public String readKey(Context context){
        File file  = new File(context.getFilesDir(),"Key.txt");
        String text="";
        FileInputStream fis = null;
        Log.d("KEYSIS","TEXT");
        if (file.exists()){
            Log.d("KEYSIS","GOING IN");
            try {
                Log.d("KEYSIS","INSIDE TRY IN");
                fis = context.openFileInput("Key.txt");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                Log.d("KEYSIS","GOING to READ");
                while ((text = br.readLine()) != null) {
                    sb.append(text).append("\n");
                }
                text = sb.toString().trim();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return text;

        }
        else {
            Toast.makeText(context, "KEY NOT FOUND GENERATE AES KEY", Toast.LENGTH_SHORT).show();
            return null;
        }


    }

    public String readIp(Context context){
        File file  = new File(context.getFilesDir(),"IPBOOK.txt");
        String text="";
        FileInputStream fis = null;
        Log.d("KEYSIS","TEXT");
        if (file.exists()){
            Log.d("KEYSIS","GOING IN");
            try {
                Log.d("KEYSIS","INSIDE TRY IN");
                fis = context.openFileInput("IPBOOK.txt");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                Log.d("KEYSIS","GOING to READ");
                while ((text = br.readLine()) != null) {
                    sb.append(text).append("\n");
                }
                text = sb.toString().trim();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return text;

        }
        else {
            Toast.makeText(context, "IP NOT FOUND SAVE IP", Toast.LENGTH_SHORT).show();
            return null;
        }


    }



    public String readPort(Context context){
        File file  = new File(context.getFilesDir(),"PORTBOOK.txt");
        String text="";
        FileInputStream fis = null;
        Log.d("KEYSIS","TEXT");
        if (file.exists()){
            Log.d("KEYSIS","GOING IN");
            try {
                Log.d("KEYSIS","INSIDE TRY IN");
                fis = context.openFileInput("PORTBOOK.txt");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                Log.d("KEYSIS","GOING to READ");
                while ((text = br.readLine()) != null) {
                    sb.append(text).append("\n");
                }
                text = sb.toString().trim();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return text;

        }
        else {
            Toast.makeText(context, "PORT NOT FOUND SAVE PORT", Toast.LENGTH_SHORT).show();
            return null;
        }


    }



}
