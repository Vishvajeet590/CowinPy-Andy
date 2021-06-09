package com.vishvajeet590.python.console;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vishvajeet590.python.utils.RecyclerviewClickInterface;
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
import android.widget.Toast;

import com.chaquo.python.console.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Profile extends AppCompatActivity implements RecyclerviewClickInterface {

    RecyclerView recyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mManaager;
    ArrayList<dataModel> models;

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
         models = fetchdata();
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
            mAdapter = new configAdapter(models,this,this);
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
            String text ;
            int c = 0;
            int con = 1;
            Log.d("INSIDE","GOING INSIDE");

            while ((text = br.readLine()) != null) {
                if (text != null){
                    Log.d("INSIDE",text);
                    StringTokenizer st = new StringTokenizer(text, "<");
                    ArrayList<String> capsule = new ArrayList<>();
                    while (st.hasMoreTokens()) {
                        capsule.add(st.nextToken());
                    }

                    for (String xy : capsule) {
                        Log.d("INSIDE",xy);
                        StringTokenizer sta = new StringTokenizer(xy, "%");
                        try {
                            while (sta.hasMoreTokens()) {
                                data[c] = sta.nextToken();
                                c++;
                            }
                            c = 0;
                            dataModelArrayList.add(new dataModel(data[0] + " " + con, data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11], data[12]));
                            con++;
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                        finally {
                            Log.d("fetchdata", "fetchdata: error ");
                        }

                    }


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

    @Override
    public void onItemClick(int pos) {
        addToNewRun(models,pos);
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);

    }



    public void addToNewRun(ArrayList<dataModel> list,int pos){
        dataModel model = list.get(pos);
//String name, String benificiary, String search, String number, String vacType, String pincodes, String state, String district, String availibility, String refresh, String startDay, String cost, String auto_book
        String text = model.name+"%"+model.benificiary+"%"+model.search+"%"+model.number+"%"+model.vacType+"%"+model.pincodes+"%"+model.state+"%"+model.district+"%"+model.availibility+"%"+model.refresh+"%"+model.startDay+"%"+model.cost+"%"+model.auto_book;
        FileOutputStream fos = null;
        try {

            fos = openFileOutput("RUNNING.txt", MODE_PRIVATE);
            fos.write(text.getBytes());
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

    }

}

