package com.silentcoders.abdulqadir.invoiceapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;
import com.silentcoders.customitems.CustomToast;

import java.util.Calendar;

import static com.silentcoders.abdulqadir.invoiceapp.Constants.DB_NAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_USERNAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.convertDateToMillis;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.generateRandomPrimaryKey;

public class MakeBill_Phase1Activity extends AppCompatActivity {

    Context context = this;
    final static String TAG = "MakeBillP1";

    SQLiteDatabase sqldb;
    SharedPreferences spfs;

    EditText et_date, et_customer_name, et_place, et_remarks;
    DatePickerDialog datePickerDialog;

    Button bt_next;
    String id = null;

    @Override
    protected void onResume() {
        super.onResume();

        Intent in = getIntent();
        try{
            id = in.getStringExtra( "id" );
            if( id != null ) {
                String sql = String.format( "SELECT * FROM bills WHERE id='%s'", id );
                Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
                c.moveToNext();

                et_customer_name.setText( c.getString( c.getColumnIndex( "customer_name" ) ) );
                et_date.setText( LocalFunctions.convertMillisToDate( Long.parseLong( c.getString( c.getColumnIndex( "timestamp" ) ) ) ) );
                et_place.setText( c.getString( c.getColumnIndex( "place" ) ) );
                et_remarks.setText( c.getString( c.getColumnIndex( "remarks" ) ) );
            }
        }
        catch ( Exception e ){
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_make_bill_phase1 );

        Log.d( TAG, "onCreate()" );

        init();
    }

    private void init(){
        initActionBar();

        initViews();
    }

    private void initActionBar(){
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle( "Make Bill" );
    }

    private void initViews(){

        sqldb = UtilSQLite.makeDatabase( DB_NAME, context );
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );

        et_date = (EditText) findViewById( R.id.et_date );
        et_customer_name = (EditText) findViewById( R.id.et_customer_name );
        et_customer_name.setText( generateRandomPrimaryKey( 5 ) );
        et_place = (EditText) findViewById( R.id.et_place );
        et_place.setText( "Mumbai" );
        et_remarks = (EditText) findViewById( R.id.et_remarks );

        initDatePickerDialog();

        bt_next = (Button) findViewById( R.id.bt_next );
        setNextButtonClickListener();


    }

    private void initDatePickerDialog(){

        Calendar cal = Calendar.getInstance();
        int year = cal.get( Calendar.YEAR );
        int month = cal.get( Calendar.MONTH );
        int date = cal.get( Calendar.DATE );

        datePickerDialog = new DatePickerDialog( context, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet( DatePicker datePicker, int i, int i1, int i2 ) {
                datePicker.getYear();
                datePicker.getMonth();
                datePicker.getDayOfMonth();

                String date = String.format( "%d-%d-%d", datePicker.getDayOfMonth(), datePicker.getMonth()+1, datePicker.getYear() );
                et_date.setText( date );
            }

        }, year, month, date );

        et_date.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {
                datePickerDialog.show();
            }

        });

        String default_date = String.format( "%d-%d-%d", date, month+1, year );
        et_date.setText( default_date );

    }

    private void setNextButtonClickListener(){

        bt_next.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                String date = et_date.getText().toString().trim();
                String customer_name = et_customer_name.getText().toString().trim();
                String place = et_place.getText().toString().trim();
                String remarks = et_remarks.getText().toString().trim();

                if( date.equals( "" ) ||
                        customer_name.equals( "" ) ||
                        place.equals( "" ) ){
                    CustomToast.showCustomToast( context, "error", "Date, Name and Place cannot be empty !", Toast.LENGTH_LONG );
                    return;
                }

                String bill_status = "incomplete";
                String sql = null;
                String timestamp = String.valueOf( convertDateToMillis( date ) );
                if( id == null ) {
                    id = generateRandomPrimaryKey( 5 );
                    sql = String.format( "INSERT INTO bills( 'id', 'user_id', 'timestamp', 'customer_name', 'place', 'remarks', 'bill_status', 'cartons_meta', 'item_meta', 'bill_extras' ) " +
                                    "VALUES( '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s' )",
                            id, UtilSharedPreferences.getSharedPreference( spfs, KEY_USERNAME, "-1" ), timestamp, customer_name, place, remarks, bill_status, "[]", "[]", "[]" );
                }
                else{
                    sql = String.format( "UPDATE bills SET 'timestamp'='%s', 'customer_name'='%s', 'place'='%s', 'remarks'='%s', 'bill_status'='%s' WHERE id='%s'",
                            timestamp, customer_name, place, remarks, bill_status, id );
                }
                Log.i( TAG, sql );


                UtilSQLite.executeQuery( sqldb, sql, true );

                // Open Next Activity
                Intent in = new Intent( context, MakeBill_Phase2Activity.class );
                in.putExtra( "id", id );
                startActivity( in );


            }

        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d( TAG, "onNewIntent()" );
    }

    // MENU
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.make_bill_phase1_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        switch( item.getItemId() ){

            case android.R.id.home:
                confirmGoingBack();

                return true;

        }
        return( super.onOptionsItemSelected( item ) );
    }

    private void confirmGoingBack(){

        if( id != null ){
            AlertDialog.Builder ab = new AlertDialog.Builder( context );
            ab.setTitle( "Caution" );
            ab.setMessage( "If you go back, this bill will be marked as INCOMPLETE and will appear in the View Bills list. Do you want to continue going back ?" );
            ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick( DialogInterface dialogInterface, int i ) {

                    Intent in = new Intent( context, Decision.class );
                    startActivity( in );
                    finish();

                }

            });
            ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Do Nothing;
                }

            });

            ab.show();
            return;
        }
        NavUtils.navigateUpFromSameTask( MakeBill_Phase1Activity.this );

    }

}
