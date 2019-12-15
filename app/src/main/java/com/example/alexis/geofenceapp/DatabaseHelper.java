package com.example.alexis.geofenceapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.sql.Timestamp;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG ="DatabaseHelper";
    public static final String DATABASE_NAME="Geo.db";

    //Table
    public static final String TABLE_NAME="POI_TABLE";
    public static final String COL00="ID";
    public static final String COL11="NAME";
    public static final String COL44="LAT";
    public static final String COL55="LONG";

    public static final String TABLE_NAME3="HISTORY";
    public static final String COL10="ID";
    public static final String COL21="TIMESTAMP";
    public static final String COL32="NAME";
    public static final String COL43="LAT";
    public static final String COL54="LONG";


    public DatabaseHelper(Context context){
        super(context,DATABASE_NAME ,null,2);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable= "CREATE TABLE "+ TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT , "+
                " NAME TEXT, LAT REAL, LONG REAL)";
        db.execSQL(createTable);
        String createTable3= "CREATE TABLE "+ TABLE_NAME3 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT , "+
                " TIMESTAMP TEXT, NAME TEXT, LAT REAL, LONG REAL)";
        db.execSQL(createTable3);




    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME3);
        onCreate(db);
    }
    //Add Data Functions
    public boolean addData(String name,Double lat,Double longe){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(COL11,name);
        contentValues.put(COL44,lat);
        contentValues.put(COL55,longe);
        //Log.d(TAG,"????????????>??"+lat);
        //Toast.makeText(DatabaseHelper.this,"Data Successfully,Inserted Over SpeedLimit!",Toast.LENGTH_LONG).show();

        //DB insert check
        long result=db.insert(TABLE_NAME,null,contentValues);
        if (result==-1){
            return false;
        }else{
            return true;
        }

    }
    public boolean addData3(String timestamp, String name, Double lat, Double longe){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();

        contentValues.put(COL21,timestamp);
        contentValues.put(COL32,name);
        contentValues.put(COL43,lat);
        contentValues.put(COL54,longe);

        //DB insert check
        long result=db.insert(TABLE_NAME3,null,contentValues);
        if (result==-1){
            return false;
        }else{
            return true;
        }

    }


    //Show Data Functions
    public Cursor showData(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor data =db.rawQuery("SELECT * FROM "+ TABLE_NAME,null);
        //Cursor data=db.rawQuery("delete from "+ TABLE_NAME,null);// In case of mistake in database
        return data;
    }
    public Cursor showData3(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor data =db.rawQuery("SELECT * FROM "+ TABLE_NAME3,null);
        //Cursor data=db.rawQuery("delete from "+ TABLE_NAME3,null); //In case of mistake in database
        return data;
    }
    //Top 3 show from history
    public Cursor showData2(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor data =db.rawQuery("SELECT NAME,COUNT(NAME) AS value FROM "+ TABLE_NAME3+" GROUP BY NAME ORDER BY value DESC LIMIT 3",null);
        //Cursor data=db.rawQuery("delete from "+ TABLE_NAME,null);// In case of mistake in database
        return data;
    }
    //Check if POI Name already exists
    public boolean check(String name){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor = null;
        String sql ="SELECT 1 FROM "+TABLE_NAME+" WHERE " + COL32 + "='" + name + "'";
        cursor= db.rawQuery(sql,null);
        //Log("Cursor Count : " + cursor.getCount());

        if(cursor.getCount()>0){
            cursor.close();
            return false;
        }else{
            cursor.close();
            return true;
        }
        //cursor.close();
    }
    public boolean deleteData(String name){
        SQLiteDatabase db=this.getWritableDatabase();
        //Cursor cursor = null;
        Cursor cursor = null;
        String sql ="SELECT 1 FROM "+TABLE_NAME+" WHERE " + COL32 + "='" + name + "'";
        cursor= db.rawQuery(sql,null);
        //Log("Cursor Count : " + cursor.getCount());

        if(cursor.getCount()>0){
            db.delete(TABLE_NAME, "NAME = ?",new String[] {name});
            cursor.close();
            return true;
        }else{
            cursor.close();
            return false;
        }
        //db.delete(TABLE_NAME, "NAME = ?",new String[] {name});

    }

}
