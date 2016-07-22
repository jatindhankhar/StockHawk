package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;

import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;


public class DetailActivity extends AppCompatActivity {

    private JSONObject extraInfo;
    private String name;
    private String symbol;
    private ArrayList<String> dates;
    private ArrayList<Float>  values;
    private LineChart lineChart;
    private FloatingActionButton fab;
    private View errorLayout;
    private CircularProgressBar circularProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lineChart = (LineChart) findViewById(R.id.chart);
        lineChart.setVisibility(View.GONE);
        fab = (FloatingActionButton)  findViewById(R.id.fabShare);
        fab.setVisibility(View.GONE);
        String info = getIntent().getStringExtra("info");
        try {
            extraInfo = new JSONObject(info);
            name = extraInfo.getString("Name");
            symbol = extraInfo.getString("symbol");
        } catch (JSONException e) {
            e.printStackTrace();

        }
        errorLayout = (View)findViewById(R.id.error_layout);
        circularProgressBar = (CircularProgressBar) findViewById(R.id.circularProgressBar);
        getSupportActionBar().setTitle(name);
        fetchData();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("name",name);
        outState.putStringArrayList("dates",dates);
        float[] valuesArray = new float[values.size()];
        for (int i = 0; i < valuesArray.length; i++) {
            valuesArray[i] = values.get(i);
        }
        outState.putFloatArray("values", valuesArray);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("name")) {

            name = savedInstanceState.getString("name");
            dates = savedInstanceState.getStringArrayList("dates");
            values = new ArrayList<>();

            float[] valuesArray = savedInstanceState.getFloatArray("values");
            for (float f : valuesArray) {
                values.add(f);
            }
            drawGraph();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void fetchData()
    {
        /* Thanks
        http://www.mkyong.com/java/java-how-to-get-current-date-time-date-and-calender/
        http://stackoverflow.com/questions/16392892/how-to-reduce-one-month-from-current-date-and-stored-in-date-variable-using-java
         */
        // Date Logic
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String endDate = sdf.format(cal.getTime());
        cal.add(Calendar.MONTH,-1); //adding -1 month
        String startDate = sdf.format(cal.getTime());

        // Query Building
        //http://stackoverflow.com/questions/29486659/how-to-get-yahoo-finance-stock-data
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
        urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol=\"" + symbol + "\" and startDate=\"" + startDate + "\" and endDate=\"" + endDate + "\""));
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        // Request Building
        OkHttpClient okHttpClient = new OkHttpClient();
        Log.d("Yolopad", "Url is " + urlStringBuilder.toString());
        final Request request = new Request.Builder().url(urlStringBuilder.toString()).build();
        // https://github.com/codepath/android_guides/wiki/Using-OkHttp#asynchronous-network-calls
       okHttpClient.newCall(request).enqueue(new Callback() {
           @Override
           public void onFailure(Request request, IOException e) {
            handleError();
           }

           @Override
           public void onResponse(Response response) throws IOException {
                if(response.isSuccessful())
                {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        // Thanks http://stackoverflow.com/questions/14898768/how-to-access-nested-elements-of-json-object-using-getjsonarray-method
                        JSONArray quotes = jsonObject.getJSONObject("query").getJSONObject("results").getJSONArray("quote");
                        dates = new ArrayList<>();
                        values = new ArrayList<>();
                        // JsonArray doesn't support foreach :(
                        for(int i=0;i<quotes.length();i++)
                        {

                            dates.add(quotes.getJSONObject(i).get("Date").toString());
                            values.add(Float.parseFloat(quotes.getJSONObject(i).get("Close").toString()));
                        }
                        Log.d("Date is ",dates.get(0));
                        Log.d("Quote is "," " + values.get(0));
                        drawGraph();


                    } catch (JSONException e) {
                        e.printStackTrace();
                        handleError();
                    }
                }
               else
                {
                    handleError();
                }
           }
       });




    }

    private void handleSuccess() {
        DetailActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                circularProgressBar.setVisibility(View.GONE);
                lineChart.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(shareImage);
                errorLayout.setVisibility(View.GONE);
            }
        });

    }

    private void handleError() {
        DetailActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                circularProgressBar.setVisibility(View.GONE);
                ((TextView)errorLayout.findViewById(R.id.error_text)).setText("There was some error");
                errorLayout.setVisibility(View.VISIBLE);
            }
        });


    }

    public void drawGraph()
    {
        // Thanks http://stackoverflow.com/a/34880488
        DetailActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Entry> entries = new ArrayList<>();
                for(int i=0;i<values.size();i++)
                {
                    entries.add(new Entry(values.get(i), i));
                }
                LineDataSet dataSet = new LineDataSet(entries," Stock Price");
                //Dataset customization
                dataSet.setColors(ColorTemplate.PASTEL_COLORS);
                dataSet.setDrawCircles(true);
                dataSet.setDrawFilled(true);
                dataSet.setValueTextSize(12f);
                int legendColors = ColorTemplate.COLORFUL_COLORS[2];
                dataSet.setValueTextColor(legendColors);
                // Customization Ends
                LineData data = new LineData(dates,dataSet);
                //data.setValueTextColor(ColorTemplate.rgb("#000000"));
                lineChart.setData(data);
                lineChart.fitScreen();
                lineChart.setTouchEnabled(true);
                // Thanks http://stackoverflow.com/a/28633547
                lineChart.getXAxis().setTextSize(12f);
                lineChart.getXAxis().setTextColor(ColorTemplate.getHoloBlue());
                lineChart.getAxisLeft().setTextSize(12f);
                lineChart.getAxisLeft().setTextColor(ColorTemplate.getHoloBlue());
                lineChart.getAxisRight().setTextSize(12f);
                lineChart.getAxisRight().setTextColor(ColorTemplate.getHoloBlue());
                lineChart.animateY(1000, Easing.EasingOption.EaseOutCirc);
                lineChart.setDescription("Stock Price for " + symbol);
                //lineChart.setMarkerView();

                // Set Marker View
                CustomMarkerView cmv = new CustomMarkerView(getBaseContext(),R.layout.custom_marker_view_layout);
                lineChart.setMarkerView(cmv);

                // X -Axis specific customizations
                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
                xAxis.setAvoidFirstLastClipping(true);
                // X - axis labels are cut off
                // Bug  Already reported :( https://github.com/PhilJay/MPAndroidChart/issues/1484
                handleSuccess();
            }
        });

    }



    private View.OnClickListener shareImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(DetailActivity.this, "Hello", Toast.LENGTH_SHORT).show();
            // save bitmap to cache directory
            try {

                File cachePath = new File(getBaseContext().getCacheDir(), "images");
                cachePath.mkdirs(); // don't forget to make the directory
                FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
                lineChart.getChartBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Share the Image
            File imagePath = new File(getBaseContext().getCacheDir(), "images");
            File newFile = new File(imagePath, "image.png");
            Uri contentUri = FileProvider.getUriForFile(getBaseContext(), "com.sam_chordas.android.stockhawk.fileprovider", newFile);

            if (contentUri != null) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Stock Price for " + name);
                startActivity(Intent.createChooser(shareIntent, "Choose an app"));

            }
        }
    };
}
