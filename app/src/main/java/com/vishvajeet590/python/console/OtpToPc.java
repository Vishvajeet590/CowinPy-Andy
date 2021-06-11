package com.vishvajeet590.python.console;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.console.R;
import com.vishvajeet590.python.utils.dataModel;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class OtpToPc extends AppCompatActivity {
    private static String sKey = "B31F2A75FBF94099";
    private static String ivParameter = "1234567890123456";

    TextView key;
    EditText ipBox,portBox;
    Button handshakeBtn,saveBtn;
    ImageButton back;
    boolean handshake = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_to_pc);
        key = findViewById(R.id.key);
        ipBox = findViewById(R.id.ipbox);
        portBox = findViewById(R.id.portbox);
        handshakeBtn = findViewById(R.id.startHand);
        saveBtn = findViewById(R.id.saveBtn);
        back = findViewById(R.id.backbtn);


            sKey = readKey();

        if (sKey.length() == 16)
        key.setText(sKey);

        handshakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String  ip = ipBox.getText().toString();
                String  port = portBox.getText().toString();
                handShaker shaker = new handShaker();
                shaker.execute(ip,port);
                handshake = true;

                if (handshake == false){
                    Toast.makeText(OtpToPc.this, "Incorrect details.....", Toast.LENGTH_SHORT).show();
                }
                else if (handshake){
                    Toast.makeText(OtpToPc.this, "Now You can save these details", Toast.LENGTH_SHORT).show();
                }


            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handshake == false){
                    Toast.makeText(OtpToPc.this, "Start HANDSHAKE first", Toast.LENGTH_SHORT).show();
                }
                else if (handshake == true){
                    String  ip = ipBox.getText().toString();
                    String  port = portBox.getText().toString();
                    saveIP(ip);
                    savePort(port);
                    ipBox.setText("");
                    portBox.setText("");
                    handshake = false;
                    Toast.makeText(OtpToPc.this, "Ip = "+ip+" Port = "+port +"Saved successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewActivity();
            }
        });







    }


    public boolean startHandshake(String ip,int port){
        Socket socket;
        DataOutputStream dos;
        PrintWriter pw;
        boolean shake = false;
        try {
            Log.d("IPS","IP = "+ip+" port = "+port);
            socket = new Socket("192.168.0.109",1024);
            pw = new PrintWriter(socket.getOutputStream());
            pw.write("HIMYPC");
            pw.close();
            socket.close();
            shake = true;
        } catch (IOException e) {
            shake = false;
            e.printStackTrace();
        }
        return shake;

    }


    public void saveIP(String ip) {
        String text = ip;
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("IPBOOK.txt", MODE_PRIVATE);
            fos.write(text.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void savePort(String ip) {
        String text = ip;
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("PORTBOOK.txt", MODE_PRIVATE);
            fos.write(text.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    public static String generateKey(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 16;
        Random random = new Random();
        String generatedString = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            generatedString = random.ints(leftLimit, rightLimit + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }
        return generatedString;
    }


    public  String writeforkey(){
        String text = generateKey();
        FileOutputStream fos = null;
        try {

            fos = openFileOutput("Key.txt", MODE_PRIVATE);
            fos.write(text.getBytes());
            Log.d("KEYSIS","WRITE ="+text);
            // mEditText.getText().clear();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("DATA", "runner: "+e.toString());

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("DATA", "addToNewRun12333: "+e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.d("DATA", "addToNewRun: "+e.toString());
                    e.printStackTrace();
                }
            }
        }
        return text;

    }

    public static String encrypt(String sSrc) throws Exception {
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


    public String readKey(){
        File file  = new File(getFilesDir(),"Key.txt");
        String text="";
        FileInputStream fis = null;
        Log.d("KEYSIS","TEXT");
        if (file.exists()){
            Log.d("KEYSIS","GOING IN");
            try {
                Log.d("KEYSIS","INSIDE TRY IN");
                fis = openFileInput("Key.txt");
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
        else return text= writeforkey();


    }




    public class handShaker extends AsyncTask<String,Void,Void> {
        Socket socket;
        DataOutputStream dos;
        PrintWriter pw;


        @Override
        protected Void doInBackground(String... strings) {
            String ip = strings[0];
            int port = Integer.parseInt(strings[1]);
            try {
                Log.d("BroadcastReceiver","In BAck");
                socket = new Socket(ip,port);
                pw = new PrintWriter(socket.getOutputStream());
                pw.write("HIMYPC");
                pw.close();
                socket.close();
            } catch (IOException e) {
                Log.d("OTPTOPC","ERR do in back = "+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }




    }




    public void openNewActivity(){
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }


}