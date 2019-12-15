package com.example.alexis.geofenceapp;

import android.content.Intent;
import android.database.Cursor;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    //Buttons
    private Button mapButton;
    private Button addButton;
    private Button history;
    private Button delete;
    private Button top;
    //DB
    DatabaseHelper Geo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ChangeActivity to MapsActivity
        mapButton=(Button)findViewById(R.id.button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        //ChangeActivity to AddLocation
        addButton=(Button)findViewById(R.id.addButton);
        addButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,AddLocation.class);
                startActivity(intent);
            }
        }));

        //Delete Button
        delete=(Button)findViewById(R.id.deleteButton);
        delete.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2=new Intent(MainActivity.this,DeleteLocation.class);
                startActivity(intent2);
            }
        }));

        //SQLite Database
        Geo=new DatabaseHelper(this);

        //history Button
        history=(Button) findViewById(R.id.history);
        ViewHistory();

        //Top3 Button
        top=(Button)findViewById(R.id.topButton);
        ViewTop();


    }

    private void ViewTop() {
        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor data = Geo.showData2();
                if (data.getCount() == 0) {
                    //display("Error","No Data Found");
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                int counter=0;
                while (data.moveToNext()) {
                    counter++;

                    buffer.append("No."+counter+":"  + "\n");
                    buffer.append("NAME: " + data.getString(0) + "\n");
                    buffer.append(" TIMES:" + data.getString(1) + "\n");
                    buffer.append("--------------" + "\n");

                }
                display("Top 3 Visited Locations", buffer.toString());
            }
        });
    }

    public void openMap(){
        Intent intent=new Intent (this,MapsActivity.class);
        startActivity(intent);
    }
    //Inside TABLE_NAME3 its every time that user has been into a poi
    public void ViewHistory() {
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor data = Geo.showData3();
                if (data.getCount() == 0) {
                    //display("Error","No Data Found");
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                int counter=0;
                while (data.moveToNext()) {
                    counter++;
                   buffer.append("No."+counter+":"  + "\n");
                    buffer.append("TIMESTAMP: " + data.getString(1) + "\n");
                    buffer.append("POI :" + data.getString(2) + "\n");
                    buffer.append("LATITUDE :" + data.getString(3) + "\n");
                    buffer.append("LONGTITUDE :" + data.getString(4) + "\n");
                    buffer.append("--------------" + "\n");

                }
                display("History", buffer.toString());
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
