package com.geo.bmkg.ccis;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class NewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        TextView txtStasiun = (TextView) findViewById(R.id.stasiun);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String title = extras.getString("title");
            txtStasiun.setText(title);
            this.setTitle("Indikator Iklim Ekstrim-"+title);
        }


    }
}
