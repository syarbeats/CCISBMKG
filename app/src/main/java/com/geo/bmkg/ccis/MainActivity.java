package com.geo.bmkg.ccis;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;

import android.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainActivity extends android.support.v4.app.FragmentActivity {


	private GoogleMap mMap;
    private LatLng defaultLatLng = new LatLng(-2.239236, 120.600938);
    private int zoomLevel = 3;
    static final int MY_PERMISSIONS_REQUEST_GOOGLEMAP_ACCESS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT < 23) {
            setUpMapIfNeeded();
        }
        else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_GOOGLEMAP_ACCESS);

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
                    setUpMapIfNeeded();

                } else {
                    MainActivity.this.finish();
                    System.exit(0);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            } else {
            	Log.e("CCIS Mobile App", "Map was null!");
            }
        }       
    }
    
    private void setUpMap() {

                try{
                    TileProvider wmsTileProvider = TileProviderFactory.getOsgeoWmsTileProvider();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, zoomLevel));
                    mMap.addTileOverlay(new TileOverlayOptions().tileProvider(wmsTileProvider));
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }catch (Exception e)
                {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }finally {

                }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_indikatoriklimtropis:
                intent = new Intent(this, LineCharUIActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_help:
                return true;
            case R.id.menu_about:
                return true;
            case R.id.menu_stasiun_klimatologi:
                intent = new Intent(this, StasiunKlimatologiActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}