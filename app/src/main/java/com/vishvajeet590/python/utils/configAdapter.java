package com.vishvajeet590.python.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.console.R;
import com.vishvajeet590.python.console.MainActivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class configAdapter extends RecyclerView.Adapter<configAdapter.configViewHolder> {
    private ArrayList<dataModel> models;
    private Context context1;
    private RecyclerviewClickInterface clickInterface;

    public configAdapter(ArrayList<dataModel> modelArrayList, Context context , RecyclerviewClickInterface recyclerviewClickInterface){
        models = modelArrayList;
        context1 = context;
        clickInterface = recyclerviewClickInterface;
    }

    public  class configViewHolder extends RecyclerView.ViewHolder{
        TextView name,ben,search,vacc;
        ConstraintLayout layout;

        public configViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.config);
            ben = itemView.findViewById(R.id.benif);
            search = itemView.findViewById(R.id.searcht);
            layout = itemView.findViewById(R.id.item_cons);
            vacc = itemView.findViewById(R.id.vac);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickInterface.onItemClick(getAdapterPosition());
                }
            });
        }
    }



    @Override
    public configViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        return new configViewHolder(view);
    }

    @Override
    public void onBindViewHolder(configAdapter.configViewHolder holder, int position) {
        dataModel model = models.get(position);
        holder.name.setText(model.name);
        holder.ben.setText("Ben : "+model.benificiary);
        holder.search.setText("Search type : "+model.search);
        holder.vacc.setText("Vaccine : "+model.vacType);
        



    }

    @Override
    public int getItemCount() {
        return models.size();
    }




    public void addToNewRun(ArrayList<dataModel> list,int pos){
        dataModel model = list.get(0);
//String name, String benificiary, String search, String number, String vacType, String pincodes, String state, String district, String availibility, String refresh, String startDay, String cost, String auto_book
        String text = model.name+"%"+model.benificiary+"%"+model.search+"%"+model.number+"%"+model.vacType+"%"+model.pincodes+"%"+model.state+"%"+model.district+"%"+model.availibility+"%"+model.refresh+"%"+model.startDay+"%"+model.cost+"%"+model.auto_book;
        FileOutputStream fos = null;
        try {

            fos = context1.openFileOutput("RUNNING.txt", MODE_PRIVATE);
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



