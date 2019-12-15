package com.example.alexis.geofenceapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private GoogleMap mMap;
    //Play services location
    private static final int MY_PERMISSION_REQUEST_CODE=1234; //Extreme safe code
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST=4321; //Extreme safe code#2

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    //Intervals
    private static int UPDATE_INTERVAL=5000; //5 secs
    private static int FASTEST_INTERVAL=3000; //3secs
    private static int DISPLACEMENT=10; //metatopish

    //DatabaseRef-GeoFire
    DatabaseReference ref;
    GeoFire geoFire; //GeoFire is an open-source library for Android/Java that allows you to store and query a set of keys based on their geographic location.

    //Marker
    Marker currentMarker;
    //VerticalSeekBar
    VerticalSeekBar vSeekBar;

    //var
    boolean flag;

    //DB-SQLite
    DatabaseHelper Geo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Database-geoFire
        ref= FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire=new GeoFire(ref);
        //VerticalSeekBar Listener
        vSeekBar=(VerticalSeekBar)findViewById(R.id.verticalSeekBar);
        vSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(progress),2000,null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //SQLite Database
        Geo=new DatabaseHelper(this);

        setUpLocation();



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length> 0&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            //Request runtime permission
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else{
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }

    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null){
            final double latitude=mLastLocation.getLatitude();
            final double longitude=mLastLocation.getLongitude();

            //Update Firebase
            geoFire.setLocation("You", new GeoLocation(latitude, longitude),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            //Add Marker(Move from last location)
                            if(currentMarker!= null){
                                currentMarker.remove(); //remove old marker
                            }
                            currentMarker=mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude,longitude))
                            .title("You")); //You is the Key that updates location in GeoFire

                            //Move Camera to this Position
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),12.0f));
                        }
                    });


            Log.d("P15074/121",String.format("Your location was changed : %f / %f",latitude,longitude));



        }else{
            Log.d("P15074/121", "Can't get your location");
        }

    }


    private void createLocationRequest() {
        //mLocationRequest=new LocationRequest();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);// location accuracy is our priority
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }

    private void buildGoogleApiClient() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode=GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!= ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else{
                Toast.makeText(this,"This device is not supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //------O parakatw kwdikas prostithetai automata apo ta services,alla den to xreiazomaste gia auto einai se sxolia------
        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        //------------------------------------------------------------------------------------

        //BASIC IDEA
        //make poiList with all added pois inside
        //then make a for loop or foreach and put the below query inside and do it for every poi

        try{
            addPoiOnMap();

        }catch(Exception e){
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }

        //createPoint(new LatLng(37.7533,-122.4056));

        final ListsPoi[] listsPoi ;
        listsPoi=addPoiOnMap();

        try {
            for (int i = 0; i < listsPoi.length; i++) {
                final int j=i;
                //GeoQuery
                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(listsPoi[i].latitude, listsPoi[i].longtitude), 0.5f); //0.5f=500m
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        //sendNotification("The User",String.format("%s entered the Point Of Interest Area",key));
                        flag = true;
                        sendToast(flag);
                        //Get timestamp
                        Date date = new Date();
                        Timestamp t =new Timestamp(date.getTime());
                        String timestamp=String.valueOf(t);
                        //Toast.makeText(MapsActivity.this,""+timestamp,Toast.LENGTH_LONG).show();

                        //History (HISTORY table[TABLE_NAME3]):insert

                        boolean insertData=Geo.addData3(timestamp,listsPoi[j].name,listsPoi[j].latitude,listsPoi[j].longtitude);
                        if(insertData){
                            //Toast.makeText(MainActivity.this,"Data Successfully,Inserted Over SpeedLimit!",Toast.LENGTH_LONG).show();
                        }else{
                            //Toast.makeText(MainActivity.this,"Something went wrong ,Unlucky,mate,Keep Running fast:(",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onKeyExited(String key) {
                        //sendNotification("The User",String.format("%s is no longer in the Point Of Interest Area",key));
                        flag = false;
                        sendToast(flag);
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        Log.d("MOVE", String.format("%s moved within the point of interest area[%f/%f]", key, location.latitude, location.longitude));

                    }

                    @Override
                    public void onGeoQueryReady() {

                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Log.e("ERROR", "" + error);
                    }
                });
            }
        }
        catch(Exception e){
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }



    }





    //Instead of sendToast(cause it didnt work properly)
    private void sendToast(boolean flag) {
        if(flag){
            Toast.makeText(this, "User entered a POI's area",
                    Toast.LENGTH_LONG).show();
        }
        else{
            //Toast.makeText(this, "User is no longer in a POI's area",
                //    Toast.LENGTH_LONG).show();
        }
    }

    //Returns an array with all poi(name,lat,long)
    private ListsPoi[] addPoiOnMap(){
       // mMap.clear();

        Cursor data=Geo.showData();
        if(data.getCount()==0){
            //display("Error","No Data Found");

            return new ListsPoi[0];
        }
        //StringBuffer buffer=new StringBuffer();
        int counter=data.getCount();
        int i=0;
        ListsPoi[] list=new ListsPoi[counter];
        String name;
        double latitude;
        double longtitude;

        while(data.moveToNext()){

            list[i]=new ListsPoi();
            name=data.getString(1);
            latitude=data.getDouble(2);
            longtitude=data.getDouble(3);



            list[i].insert(name,latitude,longtitude);
            //Toast.makeText(this,""+list[i].latitude,Toast.LENGTH_LONG).show();

            createPoint(new LatLng(list[i].latitude, list[i].longtitude));



            i=i+1;
        }
        return list;


    }
    //Create Points Of Interest
    public void createPoint(LatLng latLng){
        //LatLng poi=new LatLng(lat,longe);
        //Toast.makeText(this,"yo",Toast.LENGTH_LONG).show();
        mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(500)//meters
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f)
        );
    }
    //User notification function
    private void sendNotification(String title, String format) {
        Notification.Builder builder =new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(format);
        NotificationManager manager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent=new Intent(this,MapsActivity.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,intent ,PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification =builder.getNotification();
        notification.flags|=Notification.FLAG_AUTO_CANCEL;
        notification.defaults|=Notification.DEFAULT_SOUND;

        manager.notify(new Random().nextInt(),notification);


    }

    //Implemented Methods-----
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);


    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();
    }
}
