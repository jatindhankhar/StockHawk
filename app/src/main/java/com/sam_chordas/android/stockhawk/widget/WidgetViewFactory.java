package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;

/**
 * Created by jatin on 16/7/16.
 */
public class WidgetViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;
    private int mWidgetId,isUp;
    private String symbol,extraInfo,bidPrice,change;


    public WidgetViewFactory(Context context, Intent intent) {
        mContext = context;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if(mCursor != null) mCursor.close();
        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP,QuoteColumns.EXTRAINFO},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);

    }

    @Override
    public void onDestroy() {
        if(mCursor != null)
        {
             mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        Log.d("Yolopad","Getting data");
        // Data fields for holding with some assumptions
        isUp = 1;
        if(mCursor.moveToPosition(position))
        {
            symbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
            bidPrice = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE));
            change = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE));
            extraInfo = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.EXTRAINFO));
            isUp = mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP));
        }
        // Inflate (is this right term in given context ?) a remote view from layout
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.stock_widget_item);
        rv.setTextViewText(R.id.stock_symbol,symbol);
        rv.setTextViewText(R.id.bid_price,bidPrice);
        rv.setTextViewText(R.id.change,change);

        if(isUp == 1)
        {
        rv.setInt(R.id.change,"setBackgroundResource",R.drawable.percent_change_pill_green);
        }
        else {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }
        //Setup and associate an intent with each widget collection item
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("symbol",symbol);
        fillInIntent.putExtra("info",extraInfo);
        rv.setOnClickFillInIntent(R.id.stock_widget_item,fillInIntent);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.stock_widget_item);
    }

    @Override
    public int getViewTypeCount() {
        //Since we are only returning only one remote view
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
