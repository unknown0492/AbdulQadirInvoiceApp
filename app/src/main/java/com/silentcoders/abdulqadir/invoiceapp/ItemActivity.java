package com.silentcoders.abdulqadir.invoiceapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.silentcoders.abdulqadir.adapters.ItemAdapter;
import com.silentcoders.abdulqadir.classes.Item;
import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;
import com.silentcoders.customitems.CustomToast;

import static android.widget.Toast.LENGTH_LONG;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.DB_NAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_SHOP_ID;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_USERNAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.generateRandomPrimaryKey;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.havePrivilege;
import static com.silentcoders.classlibrary.UtilString.getRandomName;

public class ItemActivity extends AppCompatActivity {

    Context context = this;
    final static String TAG = "ItemActivity";

    SQLiteDatabase sqldb;
    SharedPreferences spfs;

    ListView lv_items;
    Dialog item_dialog;

    EditText et_item_name, et_item_code, et_item_price, et_item_discount, et_final_price;
    RadioButton rb_per_piece, rb_per_dozen;
    RadioGroup rg_qty_type;

    Button bt_save_item;
    Item[] items = null;

    SearchView sv_search_items;

    int item_selected_index = -1;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_item );

        init();
    }

    private void init(){
        initActionBar();

        initViews();

        addEvents();

        reloadItemsInListView( "" );
    }

    private void initActionBar(){
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle( "Item" );
    }

    private void initViews(){

        sqldb = UtilSQLite.makeDatabase( DB_NAME, context );
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );

        lv_items = (ListView) findViewById( R.id.lv_items );


    }

    private void addEvents(){


    }

    private void setPriceEditableListener(){

        et_item_price.setOnFocusChangeListener( new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange( View view, boolean hasFocus ) {

                if( hasFocus ){
                    return;
                }

                String str_item_price = et_item_price.getText().toString();
                String str_item_discount = et_item_discount.getText().toString();

                if( str_item_price.equals( "" ) ){
                    et_item_price.setText( "0" );
                    str_item_price = "0";
                }
                if( str_item_discount.equals( "" ) ){
                    et_item_discount.setText( "0" );
                    str_item_discount = "0";
                }

                calculateFinalPrice( str_item_price, str_item_discount );

            }

        });

        et_item_price.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                String str_item_price = et_item_price.getText().toString();
                String str_item_discount = et_item_discount.getText().toString();

                if( str_item_price.equals( "" ) ){
                    str_item_price = "0";
                }
                if( str_item_discount.equals( "" ) ){
                    str_item_discount = "0";
                }

                calculateFinalPrice( str_item_price, str_item_discount );

                return false;
            }

        });

        et_item_discount.setOnFocusChangeListener( new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange( View view, boolean hasFocus ) {

                if( hasFocus ){
                    return;
                }

                String str_item_price = et_item_price.getText().toString();
                String str_item_discount = et_item_discount.getText().toString();

                if( str_item_price.equals( "" ) ){
                    et_item_price.setText( "0" );
                    str_item_price = "0";
                }
                if( str_item_discount.equals( "" ) ){
                    et_item_discount.setText( "0" );
                    str_item_discount = "0";
                }

                calculateFinalPrice( str_item_price, str_item_discount );

            }

        });

        et_item_discount.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                String str_item_price = et_item_price.getText().toString();
                String str_item_discount = et_item_discount.getText().toString();

                if( str_item_price.equals( "" ) ){
                    str_item_price = "0";
                }
                if( str_item_discount.equals( "" ) ){
                    str_item_discount = "0";
                }

                calculateFinalPrice( str_item_price, str_item_discount );

                return false;
            }

        });



    }

    private void calculateFinalPrice( String str_item_price, String str_item_discount ){

        float item_price = Float.parseFloat( str_item_price );
        float item_discount = Float.parseFloat( str_item_discount );
        float final_price = item_price - item_discount;

        et_final_price.setText( final_price + "" );
    }

    private void addItem(){
        item_dialog = new Dialog( context );
        item_dialog.setCancelable( true );
        item_dialog.setTitle( "Add Item" );
        item_dialog.setContentView( R.layout.layout_item_dialog);

        et_item_name = (EditText) item_dialog.findViewById( R.id.et_item_name );
        et_item_code = (EditText) item_dialog.findViewById( R.id.et_item_code );
        et_item_price = (EditText) item_dialog.findViewById( R.id.et_item_price );
        et_item_discount = (EditText) item_dialog.findViewById( R.id.et_item_discount );
        setPriceEditableListener();
        et_final_price = (EditText) item_dialog.findViewById( R.id.et_final_price );

        et_item_name.setText( getRandomName( 5 ) );
        et_item_code.setText( "101" );
        et_item_price.setText( 12 + "" );
        et_item_discount.setText( 5 + "" );
        et_final_price.setText( (12-5) + "" );

        rg_qty_type = (RadioGroup) item_dialog.findViewById( R.id.rg_qty_type );
        rb_per_piece = (RadioButton) item_dialog.findViewById( R.id.rb_per_piece );
        rb_per_dozen = (RadioButton) item_dialog.findViewById( R.id.rb_per_dozen );

        bt_save_item = (Button) item_dialog.findViewById( R.id.bt_save_item );
        bt_save_item.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                String item_name = et_item_name.getText().toString().trim();
                String item_code = et_item_code.getText().toString().trim();

                if( item_code.equals( "" ) ||
                        item_name.equals( "" ) ){
                    CustomToast.showCustomToast( context, "error", "Item Name/Number cannot be empty !", LENGTH_LONG );
                    return;
                }

                String price = et_item_price.getText().toString().trim();
                String discount = et_item_discount.getText().toString().trim();
                String final_price = et_final_price.getText().toString().trim();

                price = price.equals( "" )?"0":price;
                discount = discount.equals( "" )?"0":discount;
                final_price = final_price.equals( "" )?"0":final_price;

                String qty_type = "per_piece";

                if( rb_per_piece.isChecked() ){
                    qty_type = "per_piece";
                }
                if( rb_per_dozen.isChecked() ){
                    qty_type = "dozen";
                }

                String sql_shop_id = String.format( "SELECT * FROM user_shops WHERE user_id='%s'", UtilSharedPreferences.getSharedPreference( spfs, KEY_USERNAME, "-1" ).toString() );
                // Log.d( TAG, sql_shop_id );
                Cursor c = UtilSQLite.executeQuery( sqldb, sql_shop_id, false );
                if( c.getCount() == 0 ){
                    CustomToast.showCustomToast( context, "error", "Error occurred... Please logout and login again !", LENGTH_LONG );
                    return;
                }
                c.moveToNext();
                String shop_id = c.getString( c.getColumnIndex( "shop_id" ) );

                String id = generateRandomPrimaryKey( 5 );
                String sql = String.format( "INSERT INTO items( 'id', 'item_name', 'item_code', 'price', 'discount', 'final_price', 'qty_type', 'shop_id' ) VALUES(" +
                        "'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s' )",
                        id, item_name, item_code, price, discount, final_price, qty_type, shop_id );
                UtilSQLite.executeQuery( sqldb, sql, true );

                CustomToast.showCustomToast( context, "success", "Item created !", LENGTH_LONG );

                item_dialog.dismiss();

                reloadItemsInListView( "" );

            }

        });

        item_dialog.show();

    }



    private void reloadItemsInListView( String search_by ){

        new AsyncTask<String,Void,String>(){

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = new ProgressDialog( context );
                progressDialog.setCancelable( false );
                progressDialog.setMessage( "Loading Items..." );
                progressDialog.show();
            }



            @Override
            protected String doInBackground(String... args) {

                String search = args[ 0 ];
                //Log.d( TAG, "search : " +search );

                String sql = null;
                if( havePrivilege( sqldb, spfs, "list_all_items" ) )
                    sql = String.format( "SELECT * FROM items WHERE (item_name LIKE '%s') OR (item_code LIKE '%s') ORDER BY item_name", "%"+search+"%", "%"+search+"%" );
                else
                    sql = String.format( "SELECT * FROM items WHERE ( (item_name LIKE '%s') OR (item_code LIKE '%s') ) AND ( shop_id='%s' ) ORDER BY item_name", "%"+search+"%", "%"+search+"%", UtilSharedPreferences.getSharedPreference( spfs, KEY_SHOP_ID, "-1" ) );

                //Log.d( TAG, sql );

                Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
                int total_items = c.getCount();

                items = new Item[ total_items ];
                int i = 0;
                while( c.moveToNext() ){
                    items[ i ] = new Item();
                    items[ i ].setId( c.getString( c.getColumnIndex( "id" ) ) );
                    items[ i ].setItemName( c.getString( c.getColumnIndex( "item_name" ) ) );
                    items[ i ].setItemCode( c.getString( c.getColumnIndex( "item_code" ) ) );
                    items[ i ].setPrice( c.getString( c.getColumnIndex( "price" ) ) );
                    items[ i ].setDiscount( c.getString( c.getColumnIndex( "discount" ) ) );
                    items[ i ].setFinalPrice( c.getString( c.getColumnIndex( "final_price" ) ) );
                    items[ i ].setQuantityType( c.getString( c.getColumnIndex( "qty_type" ) ) );
                    items[ i ].setShopId( c.getString( c.getColumnIndex( "shop_id" ) ) );
                    i++;
                }



                lv_items.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick( AdapterView<?> adapterView, View view, int pos, long l ) {

                        item_selected_index = pos;
                        Log.d( TAG, "Index : "+item_selected_index );

                        return false;
                    }

                });


                return null;
            }

            @Override
            protected void onPostExecute(String args) {
                super.onPostExecute(args);
                progressDialog.dismiss();

                ItemAdapter ia = new ItemAdapter( context, -1, items );
                lv_items.setAdapter( ia );
                registerForContextMenu( lv_items );
            }


        }.execute( search_by );



    }

    private void editItem(){

        item_dialog = new Dialog( context );
        item_dialog.setCancelable( true );
        item_dialog.setTitle( "Edit Item" );
        item_dialog.setContentView( R.layout.layout_item_dialog );

        et_item_name = (EditText) item_dialog.findViewById( R.id.et_item_name );
        et_item_code = (EditText) item_dialog.findViewById( R.id.et_item_code );
        et_item_price = (EditText) item_dialog.findViewById( R.id.et_item_price );
        et_item_discount = (EditText) item_dialog.findViewById( R.id.et_item_discount );
        setPriceEditableListener();
        et_final_price = (EditText) item_dialog.findViewById( R.id.et_final_price );

        String sql = String.format( "SELECT * FROM items WHERE id='%s'", items[ item_selected_index ].getId() );
        Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
        if( c.getCount() == 0 ){
            CustomToast.showCustomToast( context, "error", "Invalid Item !", Toast.LENGTH_LONG );
            return;
        }
        c.moveToNext();

        et_item_name.setText( c.getString( c.getColumnIndex( "item_name" ) ) );
        et_item_code.setText( c.getString( c.getColumnIndex( "item_code" ) ) );
        et_item_price.setText( c.getString( c.getColumnIndex( "price" ) ) );
        et_item_discount.setText( c.getString( c.getColumnIndex( "discount" ) ) );
        et_final_price.setText( c.getString( c.getColumnIndex( "final_price" ) ) );

        rg_qty_type = (RadioGroup) item_dialog.findViewById( R.id.rg_qty_type );
        rb_per_piece = (RadioButton) item_dialog.findViewById( R.id.rb_per_piece );
        rb_per_dozen = (RadioButton) item_dialog.findViewById( R.id.rb_per_dozen );

        String str_qty_type = c.getString( c.getColumnIndex( "qty_type" ) );

        if( str_qty_type.equals( "per_piece" ) ){
            rb_per_piece.setChecked( true );
        }else rb_per_piece.setChecked( false );

        if( str_qty_type.equals( "dozen" ) ){
            rb_per_dozen.setChecked( true );
        }else rb_per_dozen.setChecked( false );

        bt_save_item = (Button) item_dialog.findViewById( R.id.bt_save_item );
        bt_save_item.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                String item_name = et_item_name.getText().toString().trim();
                String item_code = et_item_code.getText().toString().trim();

                if( item_code.equals( "" ) ||
                        item_name.equals( "" ) ){
                    CustomToast.showCustomToast( context, "error", "Item Name/Number cannot be empty !", LENGTH_LONG );
                    return;
                }

                String price = et_item_price.getText().toString().trim();
                String discount = et_item_discount.getText().toString().trim();
                String final_price = et_final_price.getText().toString().trim();

                price = price.equals( "" )?"0":price;
                discount = discount.equals( "" )?"0":discount;
                final_price = final_price.equals( "" )?"0":final_price;

                String qty_type = "per_piece";

                if( rb_per_piece.isChecked() ){
                    qty_type = "per_piece";
                }
                if( rb_per_dozen.isChecked() ){
                    qty_type = "dozen";
                }

                String id = items[ item_selected_index ].getId();
                String sql = String.format( "UPDATE items SET 'item_name'='%s', 'item_code'='%s', 'price'='%s', 'discount'='%s', 'final_price'='%s', 'qty_type'='%s' WHERE id='%s'",
                        item_name, item_code, price, discount, final_price, qty_type, id );
                UtilSQLite.executeQuery( sqldb, sql, true );

                CustomToast.showCustomToast( context, "success", "Item updated !", LENGTH_LONG );

                item_dialog.dismiss();

                reloadItemsInListView( "" );

            }

        });

        item_dialog.show();

    }

    private void deleteItem(){

        String id = items[ item_selected_index ].getId();
        String sql = String.format( "DELETE FROM items WHERE id='%s'", id );
        UtilSQLite.executeQuery( sqldb, sql, true );

        CustomToast.showCustomToast( context, "success", "Item deleted !", LENGTH_LONG );

        reloadItemsInListView( "" );

    }

    private void searchViewListener(){

        sv_search_items.setOnQueryTextListener( new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange( String newText ) {

                if( newText.contains( "'" ) || newText.contains( "\"" ) )
                    return false;

                reloadItemsInListView( newText );
                return false;
            }

        });

    }



    // MENU
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.item_menu, menu );

        sv_search_items = (SearchView) menu.findItem( R.id.sv_search_items ).getActionView();
        searchViewListener();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        switch( item.getItemId() ){

            case R.id.menu_add_item:
                addItem();
                return( true );
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask( this );
                return true;
            case R.id.menu_exit:
                finish();
                return( true );

        }
        return( super.onOptionsItemSelected( item ) );
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu( menu, v, menuInfo );

        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.item_edit_delete, menu );



    }

    public boolean onContextItemSelected( MenuItem item ) {
        //find out which menu item was pressed
        switch ( item.getItemId() ) {

            case R.id.menu_edit_item:
                if( havePrivilege( sqldb, spfs, "edit_item" ) ) {
                    editItem();
                }
                else{
                    CustomToast.showCustomToast( context, "error", "You dont have privileges to edit the Item !", LENGTH_LONG );
                }
                return true;

            case R.id.menu_delete_item:
                if( havePrivilege( sqldb, spfs, "delete_item" ) ) {
                    deleteItem();
                }
                else{
                    CustomToast.showCustomToast( context, "error", "You dont have privileges to delete the Item !", LENGTH_LONG );
                }
                return true;

            default:
                return false;
        }
    }


}
