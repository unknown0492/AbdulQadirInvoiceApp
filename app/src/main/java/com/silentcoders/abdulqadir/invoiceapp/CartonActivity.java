package com.silentcoders.abdulqadir.invoiceapp;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.silentcoders.abdulqadir.adapters.CartonAdapter;
import com.silentcoders.abdulqadir.classes.Carton;
import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;
import com.silentcoders.classlibrary.UtilString;
import com.silentcoders.customitems.CustomToast;

import static com.silentcoders.abdulqadir.invoiceapp.Constants.DB_NAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.generateRandomPrimaryKey;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.havePrivilege;

public class CartonActivity extends AppCompatActivity {

    Context context = this;
    final static String TAG = "Decision";

    SQLiteDatabase sqldb;
    SharedPreferences spfs;

    Dialog carton_dialog;

    ListView lv_cartons;
    CartonAdapter cartonAdapter;
    Carton[] cartons;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_carton );

        init();


    }

    private void init(){
        initActionBar();

        initViews();

        getCartonsIntoList();

    }

    private void initActionBar(){
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle( "Carton" );
    }

    private void initViews(){

        sqldb = UtilSQLite.makeDatabase( DB_NAME, context );
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );



    }

    private void addCarton(){

        carton_dialog = new Dialog( context );
        carton_dialog.setCancelable( true );
        carton_dialog.setContentView( R.layout.layout_carton_dialog );

        Button bt_save_carton = (Button) carton_dialog.findViewById( R.id.bt_save_carton );
        final EditText et_carton_name = (EditText) carton_dialog.findViewById( R.id.et_carton_name );
        final EditText et_carton_price = (EditText) carton_dialog.findViewById( R.id.et_carton_price );
        TextView tv_title = (TextView) carton_dialog.findViewById( R.id.tv_title );

        tv_title.setText( "Create Carton" );

        et_carton_name.setText( UtilString.getRandomName( 5 ) );

        bt_save_carton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                String carton_name  = et_carton_name.getText().toString();
                String carton_price = et_carton_price.getText().toString();

                if( carton_name.equals( "" ) ||
                        carton_price.equals( "" ) ){
                    CustomToast.showCustomToast( context, "error","Carton Name/Price cannot be empty !", Toast.LENGTH_LONG );
                    return;
                }

                String pkey = generateRandomPrimaryKey( 5 );

                String sql = String.format( "INSERT INTO cartons( 'id', 'carton_name', 'carton_price' ) VALUES( '%s', '%s', '%s' )",
                        pkey, carton_name, carton_price );

                try {
                    UtilSQLite.executeQuery( sqldb, sql, true );
                    CustomToast.showCustomToast( context, "success","Carton created successfully !", Toast.LENGTH_LONG );
                    getCartonsIntoList();
                    carton_dialog.cancel();
                }
                catch ( Exception e ){
                    CustomToast.showCustomToast( context, "error","Carton creation failed !", Toast.LENGTH_LONG );
                }


            }

        });

        carton_dialog.show();
    }

    EditText et_carton_name;
    EditText et_carton_price;
    int carton_position = -1;

    private void editCarton(){

        carton_dialog = new Dialog( context );
        carton_dialog.setCancelable( true );
        carton_dialog.setContentView( R.layout.layout_carton_dialog );

        Button bt_save_carton = (Button) carton_dialog.findViewById( R.id.bt_save_carton );
        TextView tv_title = (TextView) carton_dialog.findViewById( R.id.tv_title );
        tv_title.setText( "Edit Carton" );

        et_carton_name = (EditText) carton_dialog.findViewById( R.id.et_carton_name );
        et_carton_price = (EditText) carton_dialog.findViewById( R.id.et_carton_price );
        if (carton_position != -1) {
            et_carton_name.setText( cartons[ carton_position ].getCartonName() );
            et_carton_price.setText( cartons[ carton_position ].getCartonPrice() );
        }

        bt_save_carton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                String carton_name  = et_carton_name.getText().toString();
                String carton_price = et_carton_price.getText().toString();

                if( carton_name.equals( "" ) ||
                        carton_price.equals( "" ) ){
                    CustomToast.showCustomToast( context, "error","Carton Name/Price cannot be empty !", Toast.LENGTH_LONG );
                    return;
                }

                String sql = String.format( "UPDATE cartons SET carton_name='%s', carton_price='%s' WHERE id='%s'",
                        carton_name, carton_price, cartons[ carton_position ].getId() );

                try {
                    UtilSQLite.executeQuery( sqldb, sql, true );
                    CustomToast.showCustomToast( context, "success","Carton updated successfully !", Toast.LENGTH_LONG );
                    getCartonsIntoList();
                    carton_dialog.cancel();
                }
                catch ( Exception e ){
                    CustomToast.showCustomToast( context, "error","Carton updation failed !", Toast.LENGTH_LONG );
                }


            }

        });
        carton_dialog.show();

    }

    private void deleteCarton(){

        String sql = String.format( "DELETE FROM cartons WHERE id='%s'",
                cartons[ carton_position ].getId() );

        try {
            UtilSQLite.executeQuery( sqldb, sql, true );
            CustomToast.showCustomToast( context, "success","Carton deleted successfully !", Toast.LENGTH_LONG );
            getCartonsIntoList();
        }
        catch ( Exception e ){
            CustomToast.showCustomToast( context, "error","Carton deletion failed !", Toast.LENGTH_LONG );
        }


    }

    public void getCartonsIntoList(){

        String sql = "SELECT * FROM cartons";
        Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
        if( c.getCount() == 0 ){
            Log.d( TAG, "No Cartons created yet !" );
            return;
        }

        int total_cartons = c.getCount();
        cartons = new Carton[ total_cartons ];
        int i = 0;
        while( c.moveToNext() ){
            cartons[ i ] = new Carton();
            cartons[ i ].setCartonName( c.getString( c.getColumnIndex( "carton_name" ) ) );
            cartons[ i ].setCartonPrice( c.getString( c.getColumnIndex( "carton_price" ) ) );
            cartons[ i ].setId( c.getString( c.getColumnIndex( "id" ) ) );
            cartons[ i ].setAIID( c.getString( c.getColumnIndex( "ai_id" ) ) );
            i++;
        }

        lv_cartons = (ListView) findViewById( R.id.lv_cartons );
        cartonAdapter = new CartonAdapter( context, 0, cartons );
        lv_cartons.setAdapter( cartonAdapter );

        lv_cartons.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick( AdapterView<?> adapterView, View view, int pos, long l ) {

                Log.d( TAG, "Index : "+cartons[ pos ].getCartonName() );
                carton_position = pos;

                return false;
            }

        });

        registerForContextMenu( lv_cartons );
    }


    // MENU
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.carton_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        switch( item.getItemId() ){

            case R.id.menu_add_carton:
                addCarton();
                return( true );
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
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
        inflater.inflate( R.menu.carton_edit_delete, menu );

    }

    public boolean onContextItemSelected( MenuItem item ) {
        //find out which menu item was pressed
        switch ( item.getItemId() ) {

            case R.id.menu_edit_carton:
                if( havePrivilege( sqldb, spfs, "edit_carton" ) ) {
                    editCarton();
                }
                else{
                    CustomToast.showCustomToast( context, "error", "You dont have privileges to edit the Carton !", Toast.LENGTH_LONG );
                }
                return true;

            case R.id.menu_delete_carton:
                if( havePrivilege( sqldb, spfs, "delete_carton" ) ) {
                    deleteCarton();
                }
                else{
                    CustomToast.showCustomToast( context, "error", "You dont have privileges to delete the Carton !", Toast.LENGTH_LONG );
                }
                return true;

            default:
                return false;
        }
    }
}
