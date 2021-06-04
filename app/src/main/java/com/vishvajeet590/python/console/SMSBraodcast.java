package com.vishvajeet590.python.console;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSBraodcast extends BroadcastReceiver {
    public static String sms = "android.provider.Telephony.SMS_RECEIVED";
    @Override
    public void onReceive(Context context, Intent intent) {
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
                        ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(null,val);
                        clipboard.setPrimaryClip(clip);

                        if (clipboard == null) return;
                        ClipData clip1 = clipboard.getPrimaryClip();
                        if (clip == null) return;
                        ClipData.Item item = clip1.getItemAt(0);
                        if (item == null) return;
                        CharSequence textToPaste = item.getText();
                        if (textToPaste == null) return;
                    }

                    Log.e("Broadcast", "__________GCM Broadcast");
                    Bundle extras = intent.getExtras();
                    Intent ie = new Intent("SMSBraodcast");
                    // Data you need to pass to activity
                    ie.putExtra("message", val);

                    context.sendBroadcast(ie);
                    break;

                }



            }

            //Toast.makeText(context, "SMS RECIEVED from :"+ message[0].getOriginatingAddress()+"  \n body = \n"+message[0].getMessageBody(), Toast.LENGTH_SHORT).show();




        }
    }



    public class OtpSender extends AsyncTask<String,Void,Void> {
        Socket socket;
        DataOutputStream dos;
        PrintWriter pw;

        @Override
        protected Void doInBackground(String... strings) {
            String otp = strings[0];
            try {
                socket = new Socket("192.168.0.109",1456);
                pw = new PrintWriter(socket.getOutputStream());
                pw.write(otp);
                pw.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
