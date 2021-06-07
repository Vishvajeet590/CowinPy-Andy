package com.vishvajeet590.python.console;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vishvajeet590.python.utils.configAdapter;
import com.vishvajeet590.python.utils.dataModel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chaquo.python.console.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Profile extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mManaager;

    ImageView nodata;
    TextView noText,select;
    Button startFresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        recyclerView = findViewById(R.id.recycler);
        startFresh = findViewById(R.id.startFresh);
        noText = findViewById(R.id.notext);
        nodata = findViewById(R.id.noimage);
        select = findViewById(R.id.select);

        //ArrayList<dataModel> models = fetchdata();
        ArrayList<dataModel> models = fetchdata();
        if (models.size() == 0){
            recyclerView.setVisibility(View.GONE);
            select.setVisibility(View.GONE);
            nodata.setVisibility(View.VISIBLE);
            noText.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            select.setVisibility(View.VISIBLE);
            nodata.setVisibility(View.GONE);
            noText.setVisibility(View.GONE);
            recyclerView.setHasFixedSize(true);
            mManaager = new LinearLayoutManager(this);
            mAdapter = new configAdapter(models,this);
            recyclerView.setLayoutManager(mManaager);
            recyclerView.setAdapter(mAdapter);

        }

       /* models.add(new dataModel("Config 1","3","2","8707405904","0","none","35","1","1","5","2","0","n"));
        models.add(new dataModel("Config 1","3","2","8707405904","0","none","35","1","1","5","2","0","n"));
        models.add(new dataModel("Config 1","3","2","8707405904","0","none","35","1","1","5","2","0","n"));
        models.add(new dataModel("Config 1","3","2","8707405904","0","none","35","1","1","5","2","0","n"));
        models.add(new dataModel("Config 1","3","2","8707405904","0","none","35","1","1","5","2","0","n"));
*/


        startFresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewActivity();
            }
        });


    }


    public  ArrayList<dataModel> fetchdata() {
        FileInputStream fis = null;
        ArrayList<dataModel> dataModelArrayList = new ArrayList<>();
        try {
            String[] data = new String[13];


            fis = openFileInput("ALLCONFIGS.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            int c = 0;
            int con = 1;
            while ((text = br.readLine()) != null) {
                if (text.equals("NEW_DATA_FROM_HERE")){
                    String l = br.readLine();
                    Log.d("Line ",l);
                    StringTokenizer st = new StringTokenizer(l,"%");
                    while (st.hasMoreTokens()){
                        data[c] = st.nextToken();
                        c++;
                    }
                    c=0;
                    dataModelArrayList.add(new dataModel(data[0]+" "+con,data[1],data[2],data[3],data[4],data[5],data[6],data[7],data[8],data[9],data[10],data[11],data[12]));
                    con++;
                }

            }
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
        return dataModelArrayList;
    }



    public void openNewActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

