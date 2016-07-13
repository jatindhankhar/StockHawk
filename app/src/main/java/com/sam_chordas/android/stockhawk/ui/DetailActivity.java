package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class DetailActivity extends AppCompatActivity {

    private JSONObject extraInfo;
    private String name;
    private String symbol;
    private ArrayList<String> dates;
    private ArrayList<Float>  values;
    private LineChart lineChart;
    private FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lineChart = (LineChart) findViewById(R.id.chart);
        fab = (FloatingActionButton)  findViewById(R.id.fabShare);
        fab.setOnClickListener(shareImage);
        String info = getIntent().getStringExtra("info");
        try {
            extraInfo = new JSONObject(info);
            name = extraInfo.getString("Name");
            symbol = extraInfo.getString("symbol");
        } catch (JSONException e) {
            e.printStackTrace();

        }
        getSupportActionBar().setTitle(name);
        fetchData();


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
                    }
                }
               else
                {

                }
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

            }
        });

    }



    private View.OnClickListener shareImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(DetailActivity.this, "Hello", Toast.LENGTH_SHORT).show();

        }
    };
}
