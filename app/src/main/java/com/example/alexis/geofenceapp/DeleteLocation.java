package com.example.alexis.geofenceapp;

import android.database.Cursor;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DeleteLocation extends AppCompatActivity {
    //Db
    DatabaseHelper Geo;
    Button confirmDel;
    EditText dName;
    Button view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_location);

        //DB onCreate
        Geo=new DatabaseHelper(this);
        dName=(EditText) findViewById(R.id.dName);
        confirmDel=(Button) findViewById(R.id.confirmDel);

        DeleteData();
        //SQLite Database
        Geo=new DatabaseHelper(this);

        //history Button
        view=(Button) findViewById(R.id.viewButton);
        ViewAll();
    }
    //It's made basically to help user see all poi before he deletes one
    private void ViewAll() {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor data = Geo.showData();
                if (data.getCount() == 0) {
                    //display("Error","No Data Found");
                    return;
                }
                int counter=0;
                StringBuffer buffer = new StringBuffer();
                while (data.moveToNext()) {
                    counter++;

                    buffer.append("No."+counter+":" + "\n");
                    buffer.append("NAME: " + data.getString(1) + "\n");
                    buffer.append("LATITUDE :" + data.getString(2) + "\n");
                    buffer.append("LONGTITUDE :" + data.getString(3) + "\n");
                    buffer.append("--------------" + "\n");

                }
                display("All Stored Locations", buffer.toString());
            }
        });
    }

    private void DeleteData() {
        confirmDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=dName.getText().toString();
                try{
                    if(Geo.deleteData(name)) {

                        Toast.makeText(DeleteLocation.this, "Data Successfully,Deleted!", Toast.LENGTH_LONG).show();
                            //obj.createPoint(new LatLng(lat,longe));
                    } else {
                        Toast.makeText(DeleteLocation.this, "There is not a POI with that name", Toast.LENGTH_LONG).show();
                    }

                }catch (Exception e){
                    Toast.makeText(DeleteLocation.this, ""+e, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void display(String title,String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}
