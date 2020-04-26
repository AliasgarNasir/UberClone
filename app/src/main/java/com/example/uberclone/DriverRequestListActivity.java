package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap mMap;

    private ListView listView;
    private ArrayList<String> nearbyDriverRequests;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        btnGetRequests = findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);

        listView = findViewById(R.id.requestListView);
        nearbyDriverRequests = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearbyDriverRequests);
        listView.setAdapter(adapter);

    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.driverLogoutItem){

            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Intent intent = new Intent(DriverRequestListActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateRequestListView(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

        }else if(Build.VERSION.SDK_INT >= 23){

            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(DriverRequestListActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2000);

            }else{

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                assert currentDriverLocation != null;
                updateRequestListView(currentDriverLocation);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                assert currentDriverLocation != null;
                updateRequestListView(currentDriverLocation);
            }
        }
    }

    private void updateRequestListView(Location driverLocation) {

        if (driverLocation != null) {

            nearbyDriverRequests.clear();

            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(),driverLocation.getLongitude());

            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");

            requestCarQuery.whereNear("passengerLocation",driverCurrentLocation);
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null){
                        if (objects.size() > 0) {
                            for (ParseObject nearRequest : objects) {

                                Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo((ParseGeoPoint) nearRequest.get("passengerLocation"));

                                float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10) / 10;
                                nearbyDriverRequests.add("There are " + roundedDistanceValue + " miles to " + nearRequest.get("username"));
                            }
                        }else{
                            Toast.makeText(DriverRequestListActivity.this, "Sorry, No Request.", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                     }


                }
            });

        }
    }






}


