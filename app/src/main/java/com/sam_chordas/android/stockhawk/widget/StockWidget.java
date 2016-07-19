package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;


/**
 * Implementation of App Widget functionality.
 */
public class StockWidget extends AppWidgetProvider {

    private static final String CLICK_ACTION = "FETCH_DATA";

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {


        // Construct the RemoteViews object
        RemoteViews remoteViews = createAppWidgetRemoteViews(context,appWidgetId);
        //remoteViews.setTextViewText(R.id.appwidget_text, widgetText);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(CLICK_ACTION)) {
            final String symbol = intent.getStringExtra("symbol");
            final String extraInfo = intent.getStringExtra("info");
           Log.d("Yolopad","Item Touched");
            Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, DetailActivity.class);
            i.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("ticker", symbol);
            i.putExtra("info",extraInfo);
            context.startActivity(i);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Toast.makeText(context, "Widget was created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Toast.makeText(context, "Widget was deleted", Toast.LENGTH_SHORT).show();
    }

    private RemoteViews createAppWidgetRemoteViews(Context context, int appWidgetId){
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
        Intent intent = new Intent(context,WidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.widget_list,intent);

        // Intent to launch Main Activity when upper portion is touched
        Intent mainIntent = new Intent(context, MyStocksActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        //Intent for specific items in the list
        Intent clickIntentTemplate = new Intent(context,DetailActivity.class);
        clickIntentTemplate.setAction(StockWidget.CLICK_ACTION);
        clickIntentTemplate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        clickIntentTemplate.setData(Uri.parse(clickIntentTemplate.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
        //views.setEmptyView(R.id.widget_list, R.id.widget_empty);

        return remoteViews;
    }
}

