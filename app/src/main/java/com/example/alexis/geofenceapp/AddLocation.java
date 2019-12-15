package com.example.alexis.geofenceapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddLocation extends AppCompatActivity {
    //Db
    DatabaseHelper Geo;
    Button confirm;
    EditText tName,tLat,tLong;
    MapsActivity obj;
    private int min;
    private int max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        //DB onCreate
        Geo=new DatabaseHelper(this);
        tName=(EditText) findViewById(R.id.tName);
        tLat=(EditText) findViewById(R.id.tLat);
        tLong=(EditText) findViewById(R.id.tLong);
        confirm=(Button) findViewById(R.id.confirm);
        tLat.setFilters(new InputFilter[]{new InputFilterMinMax("-90", "90")});
        tLong.setFilters(new InputFilter[]{new InputFilterMinMax("-180", "180")});

        AddData();
    }

    //DB_POI ADD with Button

    public void AddData(){
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name=tName.getText().toString();
                double lat=Double.parseDouble(String.valueOf(tLat.getText()));
                double longe=Double.parseDouble(String.valueOf(tLong.getText()));
                try{
                    if(Geo.check(name)) {
                        boolean insertData = Geo.addData(name, lat, longe);

                        if (insertData) {
                            Toast.makeText(AddLocation.this, "Data Successfully,Inserted!", Toast.LENGTH_LONG).show();
                            //obj.createPoint(new LatLng(lat,longe));
                        } else {
                            Toast.makeText(AddLocation.this, "Something went wrong ,Unlucky,mate:(", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(AddLocation.this, "Can't add POI. Another POI with the same name exists", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){
                    Toast.makeText(AddLocation.this, ""+e, Toast.LENGTH_LONG).show();
                }

            }
        });
    }



}
//Set Min/Max for lat/ long
class InputFilterMinMax implements InputFilter {
    private int min, max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
