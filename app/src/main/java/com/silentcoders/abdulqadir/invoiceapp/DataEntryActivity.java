package com.silentcoders.abdulqadir.invoiceapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class DataEntryActivity extends AppCompatActivity {

    Context context = this;
    final static String TAG = "DataEntryActivity";

    Button bt_make_bill, bt_view_bill, bt_data_entry, bt_fetch_bills, bt_push_bills;

    SQLiteDatabase sqldb;
    SharedPreferences spfs;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_data_entry );
    }
}
