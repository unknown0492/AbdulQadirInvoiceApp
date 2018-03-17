package com.silentcoders.abdulqadir.invoiceapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.silentcoders.classlibrary.UtilNetwork;
import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;
import com.silentcoders.classlibrary.UtilURL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import static com.silentcoders.abdulqadir.invoiceapp.Constants.DB_NAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.havePrivilege;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.logout;

public class Decision extends AppCompatActivity {

    Context context = this;
    final static String TAG = "Decision";

    Button bt_make_bill, bt_view_bill, bt_data_entry, bt_fetch_bills, bt_push_bills;

    SQLiteDatabase sqldb;
    SharedPreferences spfs;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_decision );

        init();
    }

    private void init(){

        clearCache();

        initViews();

        checkPrivileges();

        addEventListeners();

    }

    private void initViews(){
        bt_make_bill = (Button) findViewById( R.id.bt_make_bills );
        bt_view_bill = (Button) findViewById( R.id.bt_view_bills );
        bt_data_entry = (Button) findViewById( R.id.bt_data_entry );
        bt_fetch_bills = (Button) findViewById( R.id.bt_fetch_bills );
        bt_push_bills = (Button) findViewById( R.id.bt_push_bills );

        sqldb = UtilSQLite.makeDatabase( DB_NAME, context );
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );
    }

    private void checkPrivileges(){

        if( havePrivilege( sqldb, spfs, "make_bill" ) ) bt_make_bill.setVisibility( View.VISIBLE );
        else bt_make_bill.setVisibility( View.GONE );

        if( havePrivilege( sqldb, spfs, "view_bill" ) ) bt_view_bill.setVisibility( View.VISIBLE );
        else bt_view_bill.setVisibility( View.GONE );

        if( havePrivilege( sqldb, spfs, "data_entry" ) ) bt_data_entry.setVisibility( View.VISIBLE );
        else bt_data_entry.setVisibility( View.GONE );



    }

    public void clearCache() {
        File[] directory = getCacheDir().listFiles();
        if( directory != null ){
            for ( File file : directory ){
                file.delete();
            }
        }
    }

    private void addEventListeners(){

        syncBillButtonClickEvent();

        makeBillButtonClickEvent();

        viewBillButtonClickEvent();

        dataEntryButtonClickEvent();

    }

    private void makeBillButtonClickEvent(){

        bt_make_bill.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                Intent in = new Intent( context, MakeBill_Phase1Activity.class );
                startActivity( in );
            }

        });
    }

    private void viewBillButtonClickEvent(){

        bt_view_bill.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {
                Intent in = new Intent( context, ViewBillsActivity.class );
                startActivity( in );
            }

        });
    }

    private void dataEntryButtonClickEvent(){

        bt_data_entry.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                Dialog dialog = new Dialog( context );
                dialog.setCancelable( true );
                dialog.setContentView( R.layout.layout_data_entry_choice );

                Button bt_carton = (Button) dialog.findViewById( R.id.bt_add_carton );
                Button bt_item = (Button) dialog.findViewById( R.id.bt_add_item );

                bt_carton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick( View view ) {
                        Intent in = new Intent( context, CartonActivity.class );
                        startActivity( in );
                    }

                });

                bt_item.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick( View view ) {
                        Intent in = new Intent( context, ItemActivity.class );
                        startActivity( in );
                    }

                });

                dialog.show();

            }

        });
    }

    private void syncBillButtonClickEvent(){

        bt_push_bills.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                AlertDialog.Builder ab = new AlertDialog.Builder( context );
                ab.setTitle( "Caution !" );
                ab.setMessage( "This will replace the data on the server of the bills created from this shop. Do you want to Continue ?" );
                ab.setPositiveButton( "Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick( DialogInterface dialogInterface, int i ) {




                        new AsyncTask<Void,Void,String>() {

                            @Override
                            protected String doInBackground(Void... voids) {
                                // Prepare the data in json format to send to the server
                                JSONObject jsonObject;
                                JSONArray jsonArray = null;
                                String sql = "SELECT * FROM bills";
                                Cursor c = UtilSQLite.executeQuery(sqldb, sql, false);
                                try {
                                    jsonArray = new JSONArray();
                                    while (c.moveToNext()) {
                                        jsonObject = new JSONObject();
                                        jsonObject.put("id", c.getString(c.getColumnIndex("id")));
                                        jsonObject.put("user_id", c.getString(c.getColumnIndex("user_id")));
                                        jsonObject.put("timestamp", c.getString(c.getColumnIndex("timestamp")));
                                        jsonObject.put("customer_name", c.getString(c.getColumnIndex("customer_name")));
                                        jsonObject.put("place", c.getString(c.getColumnIndex("place")));
                                        jsonObject.put("remarks", c.getString(c.getColumnIndex("remarks")));
                                        jsonObject.put("salesman", c.getString(c.getColumnIndex("salesman")));
                                        jsonObject.put("invoice_by", c.getString(c.getColumnIndex("invoice_by")));
                                        jsonObject.put("packed_by", c.getString(c.getColumnIndex("packed_by")));
                                        jsonObject.put("cartons_meta", c.getString(c.getColumnIndex("cartons_meta")));
                                        jsonObject.put("item_meta", c.getString(c.getColumnIndex("item_meta")));
                                        jsonObject.put("bill_status", c.getString(c.getColumnIndex("bill_status")));
                                        jsonObject.put("bill_extras", c.getString(c.getColumnIndex("bill_extras")));
                                        jsonArray.put(jsonObject);
                                    }



                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                return UtilNetwork.makeRequestForData(UtilURL.getWebserviceURL(context), "POST", UtilURL.getURLParamsFromPairs(
                                        new String[][]{{"what_do_you_want", "push_bills"},
                                                {"bills_json", jsonArray.toString()}} ), context);

                            }

                            @Override
                            protected void onPostExecute( String s ) {
                                super.onPostExecute( s );

                                Log.d( TAG, s );
                                Toast.makeText( context, "Pushed bills to the server !", Toast.LENGTH_LONG ).show();

                            }
                        }.execute();



                    }

                });

                ab.setNegativeButton( "No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick( DialogInterface dialogInterface, int i ) {

                    }

                });

                ab.show();

                Toast.makeText( context, "push", Toast.LENGTH_LONG ).show();
            }

        });

        bt_fetch_bills.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {
                Toast.makeText( context, "fetch", Toast.LENGTH_LONG ).show();
            }

        });
    }




    // MENU
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.decision_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        switch( item.getItemId() ){
            case R.id.menu_logout:
                logout( context );
                finish();
                Intent in = new Intent( context, LoginActivity.class );
                startActivity( in );
                return( true );
            case R.id.menu_exit:
                finish();
                return( true );

        }
        return( super.onOptionsItemSelected( item ) );
    }


}
