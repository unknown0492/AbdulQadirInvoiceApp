package com.silentcoders.abdulqadir.invoiceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.silentcoders.abdulqadir.adapters.BillAdapter;
import com.silentcoders.abdulqadir.classes.Bill;
import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;
import com.silentcoders.customitems.CustomToast;

import org.json.JSONArray;

import static com.silentcoders.abdulqadir.invoiceapp.Constants.DB_NAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_USERNAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.havePrivilege;

public class ViewBillsActivity extends AppCompatActivity {

    Context context = this;
    final static String TAG = "ViewBillsActivity";

    SharedPreferences spfs;
    SQLiteDatabase sqldb;

    ListView lv_bills;
    SearchView sv_search_bills;
    String search_by = "";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_view_bills );

        init();

    }

    private void init(){

        initViews();

    }

    private void initViews(){

        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );
        sqldb = UtilSQLite.makeDatabase( DB_NAME, context );

        lv_bills = (ListView) findViewById( R.id.lv_bills );

        getBillsIntoList( "" );

    }

    Bill bills[] = null;
    int bill_selected_index = 0;

    private void getBillsIntoList( String search ){


        new AsyncTask<String,Void,String>(){

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = new ProgressDialog( context );
                progressDialog.setCancelable( false );
                progressDialog.setMessage( "Loading Bills..." );
                progressDialog.show();
            }


            @Override
            protected String doInBackground(String... args) {

                String search = args[ 0 ];
                //Log.d( TAG, "search : " +search );

                String sql = null;
                if( havePrivilege( sqldb, spfs, "list_all_bills" ) )
                    sql = String.format( "SELECT * FROM bills WHERE (customer_name LIKE '%s') OR (place LIKE '%s') ORDER BY timestamp", "%"+search_by+"%", "%"+search_by+"%" );
                else
                    sql = String.format( "SELECT * FROM bills WHERE ( (customer_name LIKE '%s') OR (place LIKE '%s') ) AND ( user_id='%s' ) ORDER BY timestamp", "%"+search_by+"%", "%"+search_by+"%", UtilSharedPreferences.getSharedPreference( spfs, KEY_USERNAME, "-1" ) );

                Log.d( TAG, sql );

                Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
                int total_items = c.getCount();

                bills = new Bill[ total_items ];
                int i = 0;
                while( c.moveToNext() ){
                    bills[ i ] = new Bill();
                    bills[ i ].setId( c.getString( c.getColumnIndex( "id" ) ) );
                    bills[ i ].setTimestamp( c.getString( c.getColumnIndex( "timestamp" ) ) );
                    bills[ i ].setDate( LocalFunctions.convertMillisToDate( Long.parseLong( c.getString( c.getColumnIndex( "timestamp" ) ) ) ) );
                    bills[ i ].setCustomerName( c.getString( c.getColumnIndex( "customer_name" ) ) );
                    bills[ i ].setPlace( c.getString( c.getColumnIndex( "place" ) ) );
                    bills[ i ].setRemarks( c.getString( c.getColumnIndex( "remarks" ) ) );
                    bills[ i ].setSalesman( c.getString( c.getColumnIndex( "salesman" ) ) );
                    bills[ i ].setInvoiceBy( c.getString( c.getColumnIndex( "invoice_by" ) ) );
                    bills[ i ].setPackedBy( c.getString( c.getColumnIndex( "packed_by" ) ) );
                    bills[ i ].setCartonsMeta( c.getString( c.getColumnIndex( "cartons_meta" ) ) );
                    bills[ i ].setItemMeta( c.getString( c.getColumnIndex( "item_meta" ) ) );
                    bills[ i ].setBillStatus( c.getString( c.getColumnIndex( "bill_status" ) ) );
                    bills[ i ].setUserID( c.getString( c.getColumnIndex( "user_id" ) ) );
                    bills[ i ].setBillExtras( c.getString( c.getColumnIndex( "bill_extras" ) ) );
                    try{
                        JSONArray jsonArray = new JSONArray( c.getString( c.getColumnIndex( "cartons_meta" ) ) );
                        bills[ i ].setTotalCartons( String.valueOf( jsonArray.length() ) );
                    }
                    catch ( Exception e ){
                        e.printStackTrace();
                    }
                    i++;
                }

                lv_bills.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick( AdapterView<?> adapterView, View view, int pos, long l ) {

                        bill_selected_index = pos;
                        Log.d( TAG, "Index : "+bill_selected_index );

                        return false;
                    }

                });

                lv_bills.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick( AdapterView<?> adapterView, View view, int pos, long l ) {
                        Intent in = new Intent( context, BillViewActivity.class );
                        in.putExtra( "who_called", "viewbill" );
                        in.putExtra( "id", bills[ pos ].getId() );
                        startActivity( in );
                    }

                });


                return null;
            }

            @Override
            protected void onPostExecute( String args ) {
                super.onPostExecute( args );
                progressDialog.dismiss();

                BillAdapter ba = new BillAdapter( context, -1, bills );
                lv_bills.setAdapter( ba );
                registerForContextMenu( lv_bills );

                // Register the adapter

            }


        }.execute( search_by );

    }

    public void searchViewListener(){

        sv_search_bills.setOnQueryTextListener( new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit( String query ) {
                return false;
            }

            @Override
            public boolean onQueryTextChange( String newText ) {

                search_by = newText;
                //if( search_by.length() <= 1 )
                //    return false;

                search_by = newText;
                getBillsIntoList( "" );

                return false;
            }

        });

    }

    public void deleteBill(){
        //Toast.makeText( context, "aa", Toast.LENGTH_SHORT ).show();

        String bill_id = bills[ bill_selected_index ].getId();

        String sql = String.format( "DELETE FROM bills WHERE id='%s'", bill_id );
        // Log.d( TAG, sql );

        UtilSQLite.executeQuery( sqldb, sql, true );

        getBillsIntoList( "" );

    }

    public void editBill(){

        String bill_id = bills[ bill_selected_index ].getId();

        Intent in = new Intent( context, MakeBill_Phase1Activity.class );
        in.putExtra( "id", bill_id );
        startActivity( in );

    }




    // MENU
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.bill_search, menu );

        sv_search_bills = (SearchView) menu.findItem( R.id.sv_search_bills ).getActionView();
        searchViewListener();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        switch( item.getItemId() ){

            case android.R.id.home:
                Intent in = new Intent( context, Decision.class );
                startActivity( in );
                finish();
                return true;

        }
        return( super.onOptionsItemSelected( item ) );
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu( menu, v, menuInfo );

        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.bill_edit_delete, menu );

    }

    public boolean onContextItemSelected( MenuItem item ) {
        //Toast.makeText( context, "cc", Toast.LENGTH_SHORT ).show();
        //find out which menu item was pressed
        switch ( item.getItemId() ) {

            case R.id.menu_edit_bill:
                editBill();
                return true;

            case R.id.menu_delete_bill:
                //Toast.makeText( context, "bb", Toast.LENGTH_SHORT ).show();
                if( havePrivilege( sqldb, spfs, "delete_bill" ) ) {
                    deleteBill();
                }
                else{
                    CustomToast.showCustomToast( context, "error", "You dont have privileges to delete the Bill !", Toast.LENGTH_SHORT );
                }
                return true;

            default:
                return false;
        }
    }


    @Override
    protected void onNewIntent( Intent intent ) {
        super.onNewIntent( intent );

        getBillsIntoList( "" );
    }
}
