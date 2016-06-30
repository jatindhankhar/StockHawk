package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;

import org.json.JSONException;
import org.json.JSONObject;


public class DetailActivity extends AppCompatActivity {

    private JSONObject extraInfo;
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String info = getIntent().getStringExtra("info");
        try {
            extraInfo = new JSONObject(info);
            name = extraInfo.getString("Name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getSupportActionBar().setTitle(name);
        //http://stackoverflow.com/questions/29486659/how-to-get-yahoo-finance-stock-data

    }

}
