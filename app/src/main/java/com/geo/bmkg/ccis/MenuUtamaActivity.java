package com.geo.bmkg.ccis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MenuUtamaActivity extends Activity {

    //Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_utama);

    }

    public void showStasiunKlimatologi(View view)
    {
        Intent intent = new Intent(MenuUtamaActivity.this, StasiunKlimatologiActivity.class);
        startActivity(intent);
    }

    public void showProyeksiIklim(View view)
    {
        Intent intent = new Intent(MenuUtamaActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
