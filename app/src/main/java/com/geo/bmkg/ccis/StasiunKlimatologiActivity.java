package com.geo.bmkg.ccis;

//import android.support.v7.app.AppCompatActivity;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StasiunKlimatologiActivity extends Activity implements OnInfoWindowClickListener {

    private LatLng defaultLatLng = new LatLng(-2.239236, 120.600938);
    private GoogleMap map;
    private int zoomLevel = 3;
    Intent intent;
    static final int MY_PERMISSIONS_REQUEST_GOOGLEMAP_ACCESS = 0;
    //static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    String contents = "";
    InputStream is = null;
    BufferedReader reader = null;
    String line = "";
    String[] rawdata = null;
    String latitude = "";
    String longitude = "";
    String[] position = null;
    String stationcode = "";
    String snippet = "";
    String nid = "";
    AssetManager mgr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stasiun_klimatologi_activity_main);
        this.setTitle("CCIS Mobile App - [Stasiun Klimatologi]");

        mgr = getAssets();


        if (Build.VERSION.SDK_INT < 23) {

            try {
                try {
                    is = mgr.open("stasiun");
                } catch (IOException e) {

                }

                reader = new BufferedReader(new InputStreamReader(is));
                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                if (map != null) {
                    map.getUiSettings().setCompassEnabled(true);
                    map.setTrafficEnabled(true);
                    map.setMyLocationEnabled(true);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, zoomLevel));

                    while ((line = reader.readLine()) != null) {

                        rawdata = line.split("_");
                        stationcode = rawdata[1].trim();
                        nid = rawdata[2].trim();
                        snippet = "Station Code " + stationcode + " nid " + nid;

                        position = rawdata[3].trim().split(",");
                        latitude = position[1].trim();
                        longitude = position[0].trim();

                        map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                                .title(rawdata[0])
                                .snippet(snippet)
                                .icon(BitmapDescriptorFactory.defaultMarker()));


                    }
                    map.setOnInfoWindowClickListener(this);

                }
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ignored) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }

        }
        else {

            if (ContextCompat.checkSelfPermission(StasiunKlimatologiActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(StasiunKlimatologiActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(StasiunKlimatologiActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_GOOGLEMAP_ACCESS);

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GOOGLEMAP_ACCESS:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        try {
                            is = mgr.open("stasiun");
                        } catch (IOException e) {

                        }

                        reader = new BufferedReader(new InputStreamReader(is));
                        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                        if (map != null) {
                            map.getUiSettings().setCompassEnabled(true);
                            map.setTrafficEnabled(true);
                            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                StasiunKlimatologiActivity.this.finish();
                                System.exit(0);
                                //return;
                            }
                            map.setMyLocationEnabled(true);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, zoomLevel));

                            while ((line = reader.readLine()) != null) {

                                rawdata = line.split("_");
                                stationcode = rawdata[1].trim();
                                nid = rawdata[2].trim();
                                snippet = "Station Code " + stationcode + " nid " + nid;

                                position = rawdata[3].trim().split(",");
                                latitude = position[1].trim();
                                longitude = position[0].trim();

                                map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                                        .title(rawdata[0])
                                        .snippet(snippet)
                                        .icon(BitmapDescriptorFactory.defaultMarker()));


                            }
                            map.setOnInfoWindowClickListener(this);

                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException ignored) {
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }


                } else {
                    StasiunKlimatologiActivity.this.finish();
                    System.exit(0);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }


    @Override
    public void onPause() {
        if (map != null) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            map.setMyLocationEnabled(false);
            map.setTrafficEnabled(false);
        }
        super.onPause();
    }



    @Override
    public void onInfoWindowClick(Marker marker) {
        intent = new Intent(this, LineCharUIActivity.class);
        intent.putExtra("snippet", marker.getSnippet());
        intent.putExtra("title", marker.getTitle());
        intent.putExtra("position", marker.getPosition());
        
        startActivity(intent);
    }
}
