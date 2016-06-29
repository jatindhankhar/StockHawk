package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;



public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getIntent().
    }

}
