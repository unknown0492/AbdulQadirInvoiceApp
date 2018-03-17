package com.silentcoders.abdulqadir.invoiceapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.silentcoders.abdulqadir.adapters.BillDialogCartonAdapter;
import com.silentcoders.abdulqadir.adapters.SpinnerCartonAdapter;
import com.silentcoders.abdulqadir.classes.BillCartonView;
import com.silentcoders.abdulqadir.classes.BillItem;
import com.silentcoders.abdulqadir.classes.Carton;
import com.silentcoders.abdulqadir.classes.Item;
import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;
import com.silentcoders.customitems.CustomToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;

import static com.silentcoders.abdulqadir.invoiceapp.Constants.DB_NAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_USERNAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;

public class MakeBill_Phase2Activity extends AppCompatActivity {

    Context context = this;
    final static String TAG = "MakeBillP2";

    SQLiteDatabase sqldb;
    SharedPreferences spfs;

    String id = null;
    String salesman, invoice_by, packed_by;

    TextView tv_date, tv_customer_name, tv_place, tv_remarks, tv_total;
    Button bt_add_carton, bt_add_item, bt_finish;
    LinearLayout ll_bill_container, ll_carton_container;
    Carton cartons[];
    Item items[];
    BillDialogCartonAdapter billDialogCartonAdapter;

    float grand_total = 0;
    JSONArray cartonsMeta, itemsMeta;
    JSONObject cartonMetaObject, itemMetaObject;

    Vector<BillCartonView> all_cartons_vector;

    View item_dialog_layout;
    int item_sr_no = 1;
    int carton_sr_no = 1;

    BillItem tempBillItem;

    AutoCompleteTextView actv_item_name_new;// = (AutoCompleteTextView) main_dialog.findViewById( R.id.actv_item_name );
    AutoCompleteTextView actv_item_code_new;// = (AutoCompleteTextView) main_dialog.findViewById( R.id.actv_item_code );
    EditText et_price_new;// = (EditText) main_dialog.findViewById( R.id.et_item_price );
    EditText et_price_total_new;// = (EditText) main_dialog.findViewById( R.id.et_price_total );
    EditText et_discount_new;// = (EditText) main_dialog.findViewById( R.id.et_item_discount );
    EditText et_discount_total_new;// = (EditText) main_dialog.findViewById( R.id.et_discount_total );
    EditText et_final_price_new;// = (EditText) main_dialog.findViewById( R.id.et_final_price );
    EditText et_final_price_total_new;// = (EditText) main_dialog.findViewById( R.id.et_final_price_total );
    RadioGroup rg_qty_type_new;// = (RadioGroup) main_dialog.findViewById( R.id.rg_qty_type );
    RadioButton rb_per_piece_new;// = (RadioButton) main_dialog.findViewById( R.id.rb_per_piece );
    RadioButton rb_per_dozen_new;// = (RadioButton) main_dialog.findViewById( R.id.rb_per_dozen );
    EditText et_item_qty_new;// = (EditText) main_dialog.findViewById( R.id.et_item_qty );
    Spinner sp_cartons;// = (Spinner) main_dialog.findViewById( R.id.sp_cartons );
    ArrayAdapter<String> aa;
    SpinnerCartonAdapter spa, spa_move_items;
    TextView tv_dialog_type; // add/edit
    EditText et_sr_no;
    Button bt_add_item_new, bt_view_bill;

    View.OnLongClickListener itemLongClickListener;
    View.OnLongClickListener onCartonLongClickListener = null;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_make_bill_phase2 );

        init();

    }

    private void init(){

        initActionBar();

        //id = getIntent().getStringExtra( "id" );

        initViews();


        // Log.i( TAG, "id : " +id );

    }

    private void initActionBar(){
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle( "Make Bill" );
    }

    private void initViews(){

        sqldb = UtilSQLite.makeDatabase( DB_NAME, context );
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );

        all_cartons_vector = new Vector<BillCartonView>();

        tv_date = (TextView) findViewById( R.id.tv_date );
        tv_customer_name = (TextView) findViewById( R.id.tv_customer_name );
        tv_place = (TextView) findViewById( R.id.tv_place );
        tv_remarks = (TextView) findViewById( R.id.tv_remarks );
        tv_total = (TextView) findViewById( R.id.tv_total );

        cartonsMeta = new JSONArray();
        itemsMeta = new JSONArray();

        ll_bill_container = (LinearLayout) findViewById( R.id.ll_bill_container );
        bt_add_carton = (Button) findViewById( R.id.bt_add_carton );
        bt_add_item = (Button) findViewById( R.id.bt_add_item );
        bt_view_bill = (Button) findViewById( R.id.bt_view_bill );
        bt_finish = (Button) findViewById( R.id.bt_finish );
        viewBillButtonListener();
        finishButtonListener();


        // Elements of the Add Item Dialog
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
        item_dialog_layout = layoutInflater.inflate( R.layout.mkbill_add_item, null );


        addCartonButtonListener();
        addItemButtonListener();

        loadCartons();

        // This is for creating the adaptor and setting it onto the AutoCompleteTextView in the Add Item Dialog
        loadItemsIntoItemDialogAdatper();
    }

    private void loadBillInitials(){

        String sql = String.format( "SELECT * FROM bills WHERE id='%s'", id );
        Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
        c.moveToNext();

        tv_date.setText( LocalFunctions.convertMillisToDate( Long.parseLong( c.getString( c.getColumnIndex( "timestamp" ) ) ) ) );
        tv_customer_name.setText( c.getString( c.getColumnIndex( "customer_name" ) ) );
        tv_place.setText( c.getString( c.getColumnIndex( "place" ) ) );
        tv_remarks.setText( c.getString( c.getColumnIndex( "remarks" ) ) );
        tv_total.setText( "Rs." + Math.round( Math.ceil( grand_total ) ) + "/-" );

        salesman = c.getString( c.getColumnIndex( "salesman" ) );
        invoice_by = c.getString( c.getColumnIndex( "invoice_by" ) );
        packed_by = c.getString( c.getColumnIndex( "packed_by" ) );

    }

    private void loadBillData(){

        String sql = String.format( "Select cartons_meta,item_meta from bills WHERE id='%s'", id );
        Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
        c.moveToNext();

        all_cartons_vector.removeAllElements();
        all_cartons_vector = new Vector<BillCartonView>();
        ll_bill_container.removeAllViews();
        grand_total = 0;
        JSONArray jsonArray = null;
        JSONObject jsonObject = null;

        JSONArray jsonArray1 = null;
        JSONObject jsonObject1 = null;
        try {
            jsonArray = new JSONArray( c.getString( c.getColumnIndex( "cartons_meta" ) ) );
            jsonArray1 = new JSONArray( c.getString( c.getColumnIndex( "item_meta" ) ) );
            int j = 0;
            // This for loop is to iterate the Cartons
            for( int i = 0 ; i < jsonArray.length() ; i++ ){
                jsonObject = jsonArray.getJSONObject( i );
                createCarton( jsonObject.getString( "sr_no" ),
                        jsonObject.getString( "carton_name" ),
                        jsonObject.getString( "price" ),
                        jsonObject.getString( "temp_carton_id" ),
                        jsonObject.getString( "is_despatched" ) );

                // This for loop is to iterate the items inside the Current Carton
                // Iterate only till the Carton Temp ID matches with the Carton Temp id for the item
                try {
                    for (jsonObject1 = jsonArray1.getJSONObject(j); (j < jsonArray1.length()) && jsonObject.getString("temp_carton_id").equals(jsonObject1.getString("temp_carton_id")); ) {

                        createItemFromJSON(jsonObject1);
                        Log.d(TAG, jsonObject1.getString("item_name") + ", " + jsonObject.getString("carton_name") + ", " + j);
                        j++;
                        if ((j < jsonArray1.length()))
                            jsonObject1 = jsonArray1.getJSONObject(j);
                    }
                }
                catch( Exception e ){
                    Log.e( TAG, "Exception : no items inside the carton" );
                }
                /*if( !jsonObject.getString( "temp_carton_id" ).equals( jsonObject1.getString( "temp_carton_id" ) ) ){
                    j--;
                }*/
                carton_sr_no = Integer.parseInt( jsonObject.getString( "sr_no" ) ) + 1;
            }
        } catch ( JSONException e ) {
            e.printStackTrace();
        }


    }

    private void loadCartons(){
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
        billDialogCartonAdapter = new BillDialogCartonAdapter( context, cartons );

    }






    private boolean createCarton(final String sr_no,
                                 String str_carton_name,
                                 String str_carton_price,
                                 String carton_temp_id,
                                 final String is_despatched ){



        // This is the Custom View containing ONE CARTON information with items inside it
        final BillCartonView bcv = new BillCartonView( context );
        bcv.setCartonSrNo( sr_no );
        bcv.setTempID( carton_temp_id );
        bcv.setCartonName( str_carton_name );
        bcv.setCartonPrice( str_carton_price );
        bcv.setIsDespatched( is_despatched );
        bcv.addToCartonTotal( Float.parseFloat( str_carton_price ) );
        addToGrandTotal( Float.parseFloat( str_carton_price ) );
        // This is the custome method defined in BillCartonView and it will set a LongClickListener on the Carton Name View
        // And provide two options edit and delete carton information
        onCartonLongClickListener = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick( final View carton_view ) {
                // Toast.makeText( context, "HIIII", Toast.LENGTH_LONG ).show();

                // This dialog will have two buttons to prompt, Edit or Delete Carton
                final Dialog dialog1 = new Dialog( context );
                Button edit_carton = new Button( context );
                edit_carton.setText( "Edit Carton" );
                Button delete_carton = new Button( context );
                delete_carton.setText( "Delete Carton" );
                LinearLayout ll = new LinearLayout( context );
                ll.setOrientation( LinearLayout.HORIZONTAL );
                ll.setPadding( 10, 10, 10, 10 );
                ll.addView( edit_carton );
                ll.addView( delete_carton );
                dialog1.setContentView( ll );

                // Reading the Carton Name and Price from our CustomView
                final String str_carton_name1 = bcv.getCartonName();
                final String str_carton_sr_no1 = bcv.getCartonSrNo();
                final String str_carton_price1 = bcv.getCartonPrice();

                // This is the Edit Carton Button on the dialog, if we want to edit carton information
                edit_carton.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick( View view ) {

                        // This dialog is same as Add Carton layout, and will do the job of editing carton information such
                        // as name and its price
                        final Dialog dialog2 = new Dialog( context );
                        dialog2.setContentView( R.layout.mkbill_add_carton );
                        ListView lv_cartons = (ListView) dialog2.findViewById( R.id.lv_cartons );
                        final EditText et_csn1 = (EditText) dialog2.findViewById( R.id.et_carton_sr_no );
                        final EditText et_cn1 = (EditText) dialog2.findViewById( R.id.et_carton_name );
                        final EditText et_cp1 = (EditText) dialog2.findViewById( R.id.et_carton_price );
                        final TextView tv_is_desp1 = (TextView) dialog2.findViewById( R.id.tv_is_despatched );
                        Button bt_add_carton = (Button) dialog2.findViewById( R.id.bt_add_carton );
                        // This Add button on the Edit Carton Dialog, will edit the contents of the Carton inside the CustomView
                        bt_add_carton.setText( "Edit" );
                        bt_add_carton.setOnClickListener( new View.OnClickListener() {

                            @Override
                            public void onClick( View view ) {

                                String sr_no = et_csn1.getText().toString().trim();
                                String str_carton_name = et_cn1.getText().toString().trim();
                                String str_carton_price = et_cp1.getText().toString().trim();
                                String str_is_despatched = tv_is_desp1.getText().toString().trim();

                                if( sr_no.equals( "" )
                                        || str_carton_name.equals( "" )
                                        || str_carton_price.equals( "" ) ){
                                    CustomToast.showCustomToast( context, "error", "Sr No, Carton name & price cannot be empty !", Toast.LENGTH_LONG );
                                    return;
                                }

                                bcv.setCartonSrNo( sr_no );
                                bcv.setCartonName( str_carton_name );
                                // subtract previous value from the total
                                subtractFromGrandTotal( Float.parseFloat( bcv.getCartonPrice() ) );
                                bcv.subtractFromCartonTotal( Float.parseFloat( bcv.getCartonPrice() ) );
                                bcv.setCartonPrice( str_carton_price );
                                bcv.setIsDespatched( str_is_despatched );
                                // add new value of carton to the grand total
                                addToGrandTotal( Float.parseFloat( str_carton_price ) );
                                bcv.addToCartonTotal( Float.parseFloat( str_carton_price ) );
                                // ((TextView)carton_view).setText( str_carton_name );

                                dialog2.dismiss();
                                dialog1.dismiss();

                            }

                        });
                        // This adapter is reused to list the cartons inside the Edit Carton Dialog
                        lv_cartons.setAdapter( billDialogCartonAdapter );
                        lv_cartons.setOnItemClickListener( new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick( AdapterView<?> adapterView, View view, int position, long id ) {
                                et_cn1.setText( cartons[ position ].getCartonName() );
                                et_cp1.setText( cartons[ position ].getCartonPrice() );
                            }

                        });

                        et_csn1.setText( str_carton_sr_no1 );
                        et_cn1.setText( str_carton_name1 );
                        et_cp1.setText( str_carton_price1 );
                        tv_is_desp1.setText( is_despatched );


                        dialog2.show();

                    }

                });

                // Delete the carton from the Bill
                delete_carton.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick( View view ) {

                        bcv.subtractFromCartonTotal( Float.parseFloat( bcv.getCartonPrice() ) );
                        subtractFromGrandTotal( Float.parseFloat( bcv.getCarton().getCartonTotal() ) ); // Have to subtract the carton item prices
                        subtractFromGrandTotal( Float.parseFloat( bcv.getCartonPrice() ) ); // Have to subtract the carton price
                        ll_bill_container.removeView( bcv.getView() );

                        all_cartons_vector.remove( bcv );
                        amendCartonInDatabase();

                        dialog1.dismiss();

                    }

                });

                dialog1.show();

                return false;
            }

        };

        bcv.setCartonLongClickListener( onCartonLongClickListener );



        ll_bill_container.addView( bcv.getView() );
        all_cartons_vector.add( bcv );
        // Regenerate cartons jSONArray for this bill and update the column of cartons_meta
        amendCartonInDatabase();



        return true;
    }

    private void addCartonButtonListener( ){

        // This button is on the Bills Activity on the bottom left corner and will add new carton to the Bill
        bt_add_carton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                // Dialog which will List all the Cartons and Add button to add the carton to the bill
                final Dialog dialog = new Dialog( context );
                dialog.setContentView( R.layout.mkbill_add_carton );
                ListView lv_cartons = (ListView) dialog.findViewById( R.id.lv_cartons );
                final EditText et_csn = (EditText) dialog.findViewById( R.id.et_carton_sr_no );
                et_csn.setText( carton_sr_no + "" );
                final EditText et_cn = (EditText) dialog.findViewById( R.id.et_carton_name );
                final EditText et_cp = (EditText) dialog.findViewById( R.id.et_carton_price );
                final TextView tv_is_desp = (TextView) dialog.findViewById( R.id.tv_is_despatched );
                Button bt_add_carton = (Button) dialog.findViewById( R.id.bt_add_carton );
                lv_cartons.setAdapter( billDialogCartonAdapter );

                // This button is on the Dialog and will validate the carton data and add to the system
                bt_add_carton.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick( View view ) {

                        String str_carton_sr_no = et_csn.getText().toString().trim();
                        String str_carton_name = et_cn.getText().toString().trim();
                        String str_carton_price = et_cp.getText().toString().trim();
                        String str_is_despatched = tv_is_desp.getText().toString().trim();

                        if( str_carton_sr_no.equals( "" ) ||
                                str_carton_name.equals( "" )
                                || str_carton_price.equals( "" ) ){
                            CustomToast.showCustomToast( context, "error", "Sr No, Carton name & price cannot be empty !", Toast.LENGTH_LONG );
                            return;
                        }

                        createCarton( str_carton_sr_no,
                                str_carton_name,
                                str_carton_price,
                                LocalFunctions.generateRandomPrimaryKey( 5 ),
                                str_is_despatched
                                );

                        carton_sr_no = Integer.parseInt( str_carton_sr_no );
                        carton_sr_no++;








                        dialog.dismiss();

                    }

                });

                lv_cartons.setOnItemClickListener( new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick( AdapterView<?> adapterView, View view, int position, long id ) {
                        et_cn.setText( cartons[ position ].getCartonName() );
                        et_cp.setText( cartons[ position ].getCartonPrice() );
                    }

                });

                dialog.show();

            }

        });




    }






    private void addItemButtonListener(){

        bt_add_item.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                if( all_cartons_vector.size() == 0 ){
                    CustomToast.showCustomToast( context, "error", "Please create a Carton first !", Toast.LENGTH_LONG );
                    return;
                }

                showAddItemDialog( "add" );

            }

        });

    }

    private void showAddItemDialog(final String dialog_type ){

        final Dialog main_dialog = new Dialog( context );
        main_dialog.setContentView( R.layout.mkbill_add_item );
        actv_item_name_new = (AutoCompleteTextView) main_dialog.findViewById( R.id.actv_item_name );
        actv_item_code_new = (AutoCompleteTextView) main_dialog.findViewById( R.id.actv_item_code );
        //actv_item_code_new.setRawInputType(InputType.TEXT);
        et_price_new = (EditText) main_dialog.findViewById( R.id.et_price );
        et_price_total_new = (EditText) main_dialog.findViewById( R.id.et_price_total );
        et_discount_new = (EditText) main_dialog.findViewById( R.id.et_discount );
        et_discount_total_new = (EditText) main_dialog.findViewById( R.id.et_discount_total );
        et_final_price_new = (EditText) main_dialog.findViewById( R.id.et_final_price );
        et_final_price_total_new = (EditText) main_dialog.findViewById( R.id.et_final_price_total );
        rg_qty_type_new = (RadioGroup) main_dialog.findViewById( R.id.rg_qty_type );
        rb_per_piece_new = (RadioButton) main_dialog.findViewById( R.id.rb_per_piece );
        rb_per_dozen_new = (RadioButton) main_dialog.findViewById( R.id.rb_per_dozen );
        et_item_qty_new = (EditText) main_dialog.findViewById( R.id.et_item_qty );
        sp_cartons = (Spinner) main_dialog.findViewById( R.id.sp_cartons );
        //tv_dialog_type = (TextView) main_dialog.findViewById( R.id.tv_dialog_type );
        //tv_dialog_type.setText( "add" );

        et_sr_no = (EditText) main_dialog.findViewById( R.id.et_sr_no );
        et_sr_no.setText( String.valueOf( item_sr_no ) );
        //else et_sr_no.setText( String.valueOf( item_sr_no ) );

        bt_add_item_new = (Button) main_dialog.findViewById( R.id.bt_add_item );

        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
        final String carton_names[] = new String[ all_cartons_vector.size() ];
        String temp_carton_ids[] = new String[ all_cartons_vector.size() ];
        int j = all_cartons_vector.size() - 1;
        while( iterator.hasNext() ){
            BillCartonView billCartonView = iterator.next();
            carton_names[ j ] = billCartonView.getCartonName() + " " + billCartonView.getCartonSrNo();
            temp_carton_ids[ j ] = billCartonView.getTempID();
            Log.d( TAG, carton_names[ j ] + "," + temp_carton_ids[ j ] );
            j--;
        }

        spa = new SpinnerCartonAdapter( context, carton_names, temp_carton_ids );
        sp_cartons.setAdapter( spa );

        aa = new ArrayAdapter<String>( context, android.R.layout.simple_dropdown_item_1line, items[ 0 ].getAllItemNames( items ) );
        actv_item_name_new.setAdapter( aa );
        actv_item_name_new.setOnFocusChangeListener( new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange( View view, boolean hasFocus ) {

                if ( hasFocus )
                    return;

                // Retrieve only those items with the entered item name, if entered item name does not exist in db, i.e. rows = 0, then retrieve all the rows
                String item_name = actv_item_name_new.getText().toString().trim();
                String sql = "";
                if ( item_name.equals( "" ) || item_name.contains( "'" ) )
                    sql = "SELECT item_code FROM items";
                else {
                    sql = String.format("SELECT item_code FROM items WHERE item_name='%s'", item_name);
                    Log.d( TAG, sql );
                }
                Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
                if ( c.getCount() == 0 )
                    return;

                String[] temp_item_codes = new String[ c.getCount() ];
                int i = 0;
                while ( c.moveToNext() ) {
                    temp_item_codes[ i ] = c.getString( c.getColumnIndex( "item_code" ) );
                    i++;
                }
                ArrayAdapter<String> aa = new ArrayAdapter<String>( context, android.R.layout.simple_dropdown_item_1line, temp_item_codes );
                actv_item_code_new.setAdapter( aa );


                //String item_name = actv_item_name_new.getText().toString().trim();
                String item_code = actv_item_code_new.getText().toString().trim();
                if( item_name.equals( "" ) || item_code.equals( "" ) ){
                    return;
                }

                sql = String.format( "SELECT * FROM items WHERE item_name='%s' AND item_code='%s'", item_name, item_code );
                c = UtilSQLite.executeQuery( sqldb, sql, false );
                if( c.getCount() == 0 )
                    return;

                c.moveToNext();

                String str_qty_type = c.getString( c.getColumnIndex( "qty_type" ) );
                if( str_qty_type.equals( "per_piece" ) ) rb_per_piece_new.setChecked( true );
                else rb_per_piece_new.setChecked( false );
                if( str_qty_type.equals( "dozen" ) ) rb_per_dozen_new.setChecked( true );
                else rb_per_dozen_new.setChecked( false );

                et_price_new.setText( c.getString( c.getColumnIndex( "price" )) );
                et_discount_new.setText( c.getString( c.getColumnIndex( "discount" )) );
                et_final_price_new.setText( c.getString( c.getColumnIndex( "final_price" )) );

                calculatePricesForAddItemDialog();
            }

        });
        actv_item_code_new.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange( View view, boolean hasFocus ) {

                if( hasFocus )
                    return;

                String item_name = actv_item_name_new.getText().toString().trim();
                String item_code = actv_item_code_new.getText().toString().trim();
                if( item_name.equals( "" ) || item_code.equals( "" ) ){
                    return;
                }

                String sql = String.format( "SELECT * FROM items WHERE item_name='%s' AND item_code='%s'", item_name, item_code );
                Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
                if( c.getCount() == 0 )
                    return;

                c.moveToNext();

                String str_qty_type = c.getString( c.getColumnIndex( "qty_type" ) );
                if( str_qty_type.equals( "per_piece" ) ) rb_per_piece_new.setChecked( true );
                else rb_per_piece_new.setChecked( false );
                if( str_qty_type.equals( "dozen" ) ) rb_per_dozen_new.setChecked( true );
                else rb_per_dozen_new.setChecked( false );

                        /*calculatePricesForAddItemDialog( et_price_new, et_discount_new, et_final_price_new, et_item_qty_new,
                                et_price_total_new, et_discount_total_new, et_final_price_total_new, rg_qty_type_new, rb_per_piece_new, rb_per_dozen_new );*/

                //tv_qty_type_new.setText( c.getString( c.getColumnIndex( "qty_type" ) ) );

                et_price_new.setText( c.getString( c.getColumnIndex( "price" )) );
                et_discount_new.setText( c.getString( c.getColumnIndex( "discount" )) );
                et_final_price_new.setText( c.getString( c.getColumnIndex( "final_price" )) );



                calculatePricesForAddItemDialog(  );

            }
        });

        View.OnFocusChangeListener onfchl = new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange( View view, boolean hasFocus ) {

                        /*calculatePricesForAddItemDialog( et_price_new, et_discount_new, et_final_price_new, et_item_qty_new,
                                et_price_total_new, et_discount_total_new, et_final_price_total_new, rg_qty_type_new, rb_per_piece_new, rb_per_dozen_new );*/
                calculatePricesForAddItemDialog(  );

            }

        };

        View.OnKeyListener onkeyl = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                calculatePricesForAddItemDialog(  );

                return false;
            }
        };

        View.OnClickListener onclkl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText( context, "clicked", Toast.LENGTH_SHORT ).show();
                calculatePricesForAddItemDialog(  );
            }
        };

        et_discount_new.setOnFocusChangeListener( onfchl );
        et_final_price_new.setOnFocusChangeListener( onfchl );
        et_price_new.setOnFocusChangeListener( onfchl );
        et_item_qty_new.setOnFocusChangeListener( onfchl );

        et_discount_new.setOnKeyListener( onkeyl );
        et_final_price_new.setOnKeyListener( onkeyl );
        et_price_new.setOnKeyListener( onkeyl );
        et_item_qty_new.setOnKeyListener( onkeyl );

        rb_per_dozen_new.setOnClickListener( onclkl );
        rb_per_piece_new.setOnClickListener( onclkl );

        // Button on the dialog to add item to the carton
        bt_add_item_new.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                String str_sr_no = et_sr_no.getText().toString().trim();
                String str_item_name = actv_item_name_new.getText().toString().trim();
                String str_item_code = actv_item_code_new.getText().toString().trim();
                String str_price = et_price_new.getText().toString().trim();
                String str_price_total = et_price_total_new.getText().toString().trim();
                String str_discount = et_discount_new.getText().toString().trim();
                String str_discount_total = et_discount_total_new.getText().toString().trim();
                String str_final_price = et_final_price_new.getText().toString().trim();
                String str_final_price_total = et_final_price_total_new.getText().toString().trim();
                String str_quantity = et_item_qty_new.getText().toString().trim();
                View sp_selected_view = sp_cartons.getSelectedView();
                TextView tv_ct_name = (TextView) sp_selected_view.findViewById( R.id.tv_carton_name );
                String str_temp_carton_id = tv_ct_name.getTag().toString();
                // Log.d( TAG, "str_temp_carton_id : "+str_temp_carton_id );
                String str_qty_type = "per_piece";
                if( rb_per_piece_new.isChecked() ) str_qty_type = "per_piece";
                else str_qty_type = "dozen";

                if( str_sr_no.equals( "" ) ) { CustomToast.showCustomToast( context, "error", "Serial Number is required !", Toast.LENGTH_SHORT ); return; }
                if( str_item_name.equals( "" ) ) { CustomToast.showCustomToast( context, "error", "Item Name is required !", Toast.LENGTH_SHORT ); return; }
                if( str_item_code.equals( "" ) ) { CustomToast.showCustomToast( context, "error", "Item Number is required !", Toast.LENGTH_SHORT ); return; }
                if( str_price.equals( "" ) ) { CustomToast.showCustomToast( context, "error", "Price is required !", Toast.LENGTH_SHORT ); return; }
                if( str_discount.equals( "" ) ) { CustomToast.showCustomToast( context, "error", "Discount is required !", Toast.LENGTH_SHORT ); return; }
                if( str_final_price.equals( "" ) ) { CustomToast.showCustomToast( context, "error", "Final Price is required !", Toast.LENGTH_SHORT ); return; }
                if( str_quantity.equals( "" ) ) { CustomToast.showCustomToast( context, "error", "Quantity is required !", Toast.LENGTH_SHORT ); return; }

                calculatePricesForAddItemDialog();

                BillItem billItem = new BillItem();
                billItem.setItemSrNo( str_sr_no );
                billItem.setItemName( str_item_name );
                billItem.setItemCode( str_item_code );
                billItem.setItemPrice( str_price );
                billItem.setItemDiscount( str_discount );
                billItem.setItemFinalPrice( str_final_price );
                billItem.setItemPriceTotal( str_price_total );
                billItem.setItemDiscountTotal( str_discount_total );
                billItem.setItemFinalPriceTotal( str_final_price_total );
                billItem.setItemQtyType( str_qty_type );
                billItem.setItemQuantity( str_quantity );
                billItem.setCartonTempID( str_temp_carton_id );

                if( dialog_type.equals( "edit" ) ){
                    billItem.setBillItemID( tempBillItem.getBillItemID() );
                    editItemInsideTheBillCartonViewVector( billItem );
                }
                else{ // add
                    billItem.setBillItemID( LocalFunctions.generateRandomPrimaryKey( 5 ) );
                    // addItemToBillCartonView( billItem );
                    createItem( billItem );
                    item_sr_no++;
                }
                amendItemsInDatabase();




                main_dialog.dismiss();

            }

        });

        sp_cartons.setEnabled( true );

        if( dialog_type.equals( "edit" ) ){

            // CartonTempID, billItemID
            et_sr_no.setText( tempBillItem.getItemSrNo() );
            actv_item_name_new.setText( tempBillItem.getItemName() );
            actv_item_code_new.setText( tempBillItem.getItemCode() );
            et_price_new.setText( tempBillItem.getItemPrice() );
            et_discount_new.setText( tempBillItem.getItemDiscount() );
            et_final_price_new.setText( tempBillItem.getItemFinalPrice() );
            et_price_total_new.setText( tempBillItem.getItemPriceTotal() );
            et_discount_total_new.setText( tempBillItem.getItemDiscountTotal() );
            et_final_price_total_new.setText( tempBillItem.getItemFinalPriceTotal() );

            if( tempBillItem.getItemQtyType().equals( "per_piece" ) ) rb_per_piece_new.setChecked( true );
            else rb_per_dozen_new.setChecked( true );

            et_item_qty_new.setText( tempBillItem.getItemQuantity() );
            sp_cartons.setSelection( spa.getItemPositionByTag( tempBillItem.getCartonTempID() ) );
            sp_cartons.setEnabled( false );

        }


        main_dialog.show();

    }

    private void createItem( BillItem billItem ){
        addItemToBillCartonView( billItem );
    }

    private void createItemFromJSON( JSONObject jsonObject ){

        BillItem billItem = new BillItem();

        try {
            billItem.setItemSrNo( jsonObject.getString( "sr_no" ) );
            billItem.setItemName( jsonObject.getString( "item_name" ) );
            billItem.setItemCode( jsonObject.getString( "item_code" ) );
            billItem.setItemPrice( jsonObject.getString( "price" ) );
            billItem.setItemDiscount( jsonObject.getString( "discount" ) );
            billItem.setItemFinalPrice( jsonObject.getString( "final_price" ) );
            billItem.setItemPriceTotal( jsonObject.getString( "total_price" ) );
            billItem.setItemDiscountTotal( jsonObject.getString( "total_discount" ) );
            billItem.setItemFinalPriceTotal( jsonObject.getString( "total_final_price" ) );
            billItem.setItemQtyType( jsonObject.getString( "qty_type" ) );
            billItem.setItemQuantity( jsonObject.getString( "qty" ) );
            billItem.setCartonTempID( jsonObject.getString( "temp_carton_id" ) );
            billItem.setBillItemID( jsonObject.getString( "temp_item_id" ) );
            addItemToBillCartonView( billItem );

            item_sr_no = Integer.parseInt( jsonObject.getString( "sr_no"  ) ) + 1;
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }



    private void addItemToBillCartonView( BillItem billItem ){

        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
        int index = 0;
        BillCartonView bcv = null;
        View bill_item_placement = null;
        while( iterator.hasNext() ){
            bcv = iterator.next();
            if( bcv.getTempID().equals( billItem.getCartonTempID() ) ){

                LayoutInflater layoutInflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
                bill_item_placement = layoutInflater.inflate( R.layout.bill_item_placement, null );
                TextView tv_bill_item_id = (TextView) bill_item_placement.findViewById( R.id.tv_bill_item_id );
                TextView tv_sr_no = (TextView) bill_item_placement.findViewById( R.id.tv_sr_no );
                TextView tv_item_name = (TextView) bill_item_placement.findViewById( R.id.tv_item_name );
                TextView tv_item_code = (TextView) bill_item_placement.findViewById( R.id.tv_item_code );
                TextView tv_item_price = (TextView) bill_item_placement.findViewById( R.id.tv_item_price );
                TextView tv_item_discount = (TextView) bill_item_placement.findViewById( R.id.tv_item_discount );
                TextView tv_item_final_price = (TextView) bill_item_placement.findViewById( R.id.tv_final_price );
                TextView tv_item_quantity = (TextView) bill_item_placement.findViewById( R.id.tv_item_quantity );
                TextView tv_total_price = (TextView) bill_item_placement.findViewById( R.id.tv_total_price );
                TextView tv_total_discount = (TextView) bill_item_placement.findViewById( R.id.tv_total_discount );
                TextView tv_total_final_price = (TextView) bill_item_placement.findViewById( R.id.tv_total_final_price );

                tv_bill_item_id.setText( String.valueOf( billItem.getBillItemID() ) );
                tv_sr_no.setText( String.valueOf( billItem.getItemSrNo() ) );
                tv_item_name.setText( String.valueOf( billItem.getItemName() + billItem.getItemCode() ) );
                tv_item_code.setText( String.valueOf( billItem.getItemCode() ) );
                //tv_item_price.setText( String.valueOf( "Price : " + billItem.getItemPrice() ) + " /"+billItem.getItemQtyType() ); // another design
                tv_item_price.setText( String.valueOf( billItem.getItemPrice() ) );
                //tv_item_discount.setText( String.valueOf( "Discount : " + billItem.getItemDiscount() ) ); // another design
                tv_item_discount.setText( String.valueOf( billItem.getItemDiscount() ) );
                // tv_item_final_price.setText( String.valueOf( "Final Price : " +  billItem.getItemFinalPrice() ) ); // another design
                tv_item_final_price.setText( String.valueOf( billItem.getItemFinalPrice()  ) );
                // tv_item_quantity.setText( String.valueOf( billItem.getItemQuantity() + " pieces" ) );    // another design
                // tv_item_quantity.setText( String.valueOf( billItem.getItemQuantity() ) );
                tv_item_quantity.setText( String.valueOf( billItem.getItemQuantity() ) + "pc" );
                tv_total_price.setText( String.valueOf( billItem.getItemPriceTotal() ) );
                tv_total_discount.setText( String.valueOf( billItem.getItemDiscountTotal() ) );
                tv_total_final_price.setText( String.valueOf( billItem.getItemFinalPriceTotal() ) );

                // Log.d( TAG, "inside "+bcv.getTotalChildren() );

                break;
            }
            // Log.d( TAG, "outside" );
            index++;
        }
        itemLongClickListener = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick( final View view ) {

                final Dialog sub_dialog = new Dialog( context );
                LinearLayout ll = new LinearLayout( context );
                ll.setOrientation( LinearLayout.VERTICAL );
                ll.setPadding( 10, 10, 10, 10 );
                Button bt_edit_item = new Button( context );
                bt_edit_item.setText( "Edit Item" );
                final Button bt_delete_item = new Button( context );
                bt_delete_item.setText( "Delete Item" );
                //bt_delete_item.setPadding( 10, 10, 0, 0 );
                Button bt_move_item = new Button( context );
                bt_move_item.setText( "Move Item" );
                //bt_move_item.setPadding( 0, 10, 0, 0 );

                ll.addView( bt_edit_item );
                ll.addView( bt_delete_item );
                ll.addView( bt_move_item );

                sub_dialog.setContentView( ll );

                bt_edit_item.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick( View v ) {
                        sub_dialog.dismiss();

                        TextView tv_bill_item_id = (TextView) view.findViewById( R.id.tv_bill_item_id );
                        TextView tv_bill_item_name = (TextView) view.findViewById( R.id.tv_item_name );
                        String str_bill_item_id = tv_bill_item_id.getText().toString();

                        Log.d( TAG, "item id : " +str_bill_item_id+","+tv_bill_item_name.getText().toString() );
                        // Iterate through the bcv vector to find out this item's carton and item info
                        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
                        boolean item_found = false;
                        while( iterator.hasNext() ){
                            BillCartonView bcv = iterator.next();
                            if( bcv.getTotalChildren() > 2 ){
                                //Log.d( TAG, "inside children : " + bcv.getTotalChildren() );
                                // Skip the first and the last child, and iterate through the rest, because rest are the items
                                int total_items = bcv.getTotalChildren();
                                for( int i = 1; i < total_items - 1; i++ ){
                                    Log.d( TAG, "Looped "+i+" time" );
                                    //View temp_view = bcv.getItemAtPosition( i );
                                    //tv_bill_item_id = (TextView) temp_view.findViewById( R.id.tv_bill_item_id );
                                    //String bill_item_id = tv_bill_item_id.getText().toString().trim();
                                    tempBillItem = bcv.getItemDataAtPosition( i-1 );// i-1 because, we are skipping the first element
                                    Log.d( TAG, tempBillItem.getBillItemID() + "," + tempBillItem.getCartonTempID() + "," +tempBillItem.getItemName() + "," +tempBillItem.getItemQuantity() );
                                    if( str_bill_item_id.equals( tempBillItem.getBillItemID() ) ){
                                        Log.d( TAG, "Here is the item : "+tempBillItem.getBillItemID() + "," + tempBillItem.getCartonTempID() + "," +tempBillItem.getItemName() + "," +tempBillItem.getItemQuantity() );
                                        // tempBillItem = bcv.getItemDataAtPosition( i-1 );
                                        item_found = true;
                                        showAddItemDialog( "edit" );
                                        break;
                                    }
                                }
                            }
                            if( item_found ) break;
                        }
                        amendItemsInDatabase();
                    }

                });

                bt_delete_item.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick( View v ) {
                        sub_dialog.dismiss();

                        // Delete the item code here
                        // Toast.makeText( context, "Delete", Toast.LENGTH_SHORT ).show();
                        TextView tv_bill_item_id = (TextView) view.findViewById( R.id.tv_bill_item_id );
                        String str_bill_item_id = tv_bill_item_id.getText().toString();

                        Log.d( TAG, "item id : " +str_bill_item_id );
                        // Iterate through the bcv vector to find out this item's carton and item info
                        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
                        boolean item_found = false;
                        while( iterator.hasNext() ){
                            BillCartonView bcv = iterator.next();
                            if( bcv.getTotalChildren() > 2 ){
                                //Log.d( TAG, "inside children : " + bcv.getTotalChildren() );
                                // Skip the first and the last child, and iterate through the rest, because rest are the items
                                int total_items = bcv.getTotalChildren();
                                for( int i = 1; i < total_items - 1; i++ ){
                                    Log.d( TAG, "Looped "+i+" time" );
                                    //View temp_view = bcv.getItemAtPosition( i );
                                    //tv_bill_item_id = (TextView) temp_view.findViewById( R.id.tv_bill_item_id );
                                    //String bill_item_id = tv_bill_item_id.getText().toString().trim();
                                    tempBillItem = bcv.getItemDataAtPosition( i-1 );// i-1 because, we are skipping the first element

                                    if( str_bill_item_id.equals( tempBillItem.getBillItemID() ) ){
                                        Log.d( TAG, "Here is the item : "+tempBillItem.getBillItemID()+","+tempBillItem.getItemName() );
                                        // Remove the item from the vector
                                        bcv.subtractFromCartonTotal( Float.parseFloat( tempBillItem.getItemFinalPriceTotal() ) );
                                        bcv.subtractPiecesFromCarton( Integer.parseInt( tempBillItem.getItemQuantity() ) );
                                        subtractFromGrandTotal( Float.parseFloat( tempBillItem.getItemFinalPriceTotal() ) );
                                        bcv.removeItemAtPosition( i );
                                        item_found = true;

                                        break;
                                    }
                                }
                            }
                            if( item_found ) break;
                        }
                        amendItemsInDatabase();


                    }

                });

                bt_move_item.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick( View v ) {
                        //sub_dialog.dismiss();

                        // If there is only One carton, then give error, because it is not possible to move
                        if( all_cartons_vector.size() == 1 ){
                            CustomToast.showCustomToast( context, "error", "There is only one carton !", Toast.LENGTH_SHORT );
                            return;
                        }

                        TextView tv_bill_item_id = (TextView) view.findViewById( R.id.tv_bill_item_id );
                        final String str_bill_item_id = tv_bill_item_id.getText().toString();
                        String carton_temp_id = getCartonTempIDForItem( str_bill_item_id );

                        final Dialog moveDialog = new Dialog( context );
                        moveDialog.setContentView( R.layout.mkbil_move_item );
                        final EditText et_total_items = (EditText) moveDialog.findViewById( R.id.et_total_items );
                        final EditText et_movable_items = (EditText) moveDialog.findViewById( R.id.et_movable_items );
                        final Spinner spinner_cartons = (Spinner) moveDialog.findViewById( R.id.sp_cartons );
                        Button bt_move_items = (Button) moveDialog.findViewById( R.id.bt_move_items );
                        bt_move_items.setOnClickListener( new View.OnClickListener() {

                            @Override
                            public void onClick( View v ) {
                                //Toast.makeText( context, "Showing", Toast.LENGTH_SHORT ).show();
                                moveDialog.dismiss();

                                String str_total_items = et_total_items.getText().toString().trim();
                                String str_movable_items = et_movable_items.getText().toString().trim();
                                if( str_movable_items.equals( "" ) ||
                                        str_movable_items.equals( "0" ) ){
                                    CustomToast.showCustomToast( context, "error", "Please enter number of items to move !", Toast.LENGTH_SHORT );
                                    return;
                                }

                                // Case 2 - Move Some items to another carton
                                int int_total_items = Integer.parseInt( str_total_items );
                                int int_movable_items = Integer.parseInt( str_movable_items );
                                if( int_movable_items > int_total_items ){
                                    CustomToast.showCustomToast( context, "error", "Movable items cannot be more than Total items !", Toast.LENGTH_SHORT );
                                    return;
                                }

                                // Find the cartonID for the selected itemID
                                String str_from_carton_temp_id = getCartonTempIDForItem( str_bill_item_id );
                                // Get the BillItem for the carton-itemID pair
                                BillCartonView bcv_from = getBillCartonViewForItemID( str_from_carton_temp_id, str_bill_item_id );
                                BillCartonView bcv_to = null;//getBillCartonViewForItemID( str_from_carton_temp_id, str_bill_item_id );
                                try {
                                    bcv_to = (BillCartonView) bcv_from.cloneMe();
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }
                                BillItem ogBillItem = getBillItemForItemID( str_from_carton_temp_id, str_bill_item_id );
                                BillItem movableBillItem = null;//getBillItemForItemID( str_from_carton_temp_id, str_bill_item_id );
                                try {
                                    movableBillItem = (BillItem) ogBillItem.cloneMe();
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }



                                // Get the quantity, price, discount, final price and totalFinalPrice and perform the new calculation
                                int og_qty = Integer.parseInt( movableBillItem.getItemQuantity() );
                                int remaining_qty = og_qty - int_movable_items;

                                String[] movable_prices = calculatePricesForAddItemDialog(new String[]{
                                        movableBillItem.getItemPrice(),
                                        movableBillItem.getItemDiscount(),
                                        movableBillItem.getItemFinalPrice(),
                                        movableBillItem.getItemQtyType(),
                                        String.valueOf( int_movable_items )
                                });

                                float og_price = Float.parseFloat( movableBillItem.getItemPrice() );
                                float og_discount = Float.parseFloat( movableBillItem.getItemDiscount() );
                                float og_final_price = Float.parseFloat( movableBillItem.getItemFinalPrice() );
                                float og_total_price = Float.parseFloat( movableBillItem.getItemPriceTotal() );
                                float og_total_discount = Float.parseFloat( movableBillItem.getItemDiscountTotal() );
                                float og_total_final_price = Float.parseFloat( movableBillItem.getItemFinalPriceTotal() );

                                float movable_total_price = Float.parseFloat( movable_prices[ INDEX_PRICE_TOTAL ] );//int_movable_items * og_price;
                                float movable_total_discount = Float.parseFloat( movable_prices[ INDEX_DISCOUNT_TOTAL ] );//int_movable_items * og_discount;
                                float movable_total_final_price = Float.parseFloat( movable_prices[ INDEX_FINAL_PRICE_TOTAL ] );//int_movable_items * og_final_price;

                                // Adding the item to new carton
                                movableBillItem.setItemQuantity( String.valueOf( int_movable_items ) );
                                movableBillItem.setItemPriceTotal( String.valueOf( movable_total_price ) );
                                movableBillItem.setItemDiscountTotal( String.valueOf( movable_total_discount ) );
                                movableBillItem.setItemFinalPriceTotal( String.valueOf( Math.round( movable_total_final_price ) ) );
                                movableBillItem.setBillItemID( LocalFunctions.generateRandomPrimaryKey( 5 ) );

                                Log.d( TAG, "OG bill item id : "+ogBillItem.getBillItemID()+", movable bill item id : "+movableBillItem.getBillItemID() );

                                View sp_selected_view = spinner_cartons.getSelectedView();
                                TextView tv_ct_name = (TextView) sp_selected_view.findViewById( R.id.tv_carton_name );
                                String str_to_carton_id = tv_ct_name.getTag().toString();
                                movableBillItem.setCartonTempID( str_to_carton_id );
                                addItemToBillCartonView( movableBillItem );
                                bcv_to.addToCartonTotal( movable_total_final_price );
                                bcv_to.addPiecesToCarton( int_movable_items );



                                // Removing the items from the previous carton
                                String[] remaining_prices = calculatePricesForAddItemDialog(new String[]{
                                        ogBillItem.getItemPrice(),
                                        ogBillItem.getItemDiscount(),
                                        ogBillItem.getItemFinalPrice(),
                                        ogBillItem.getItemQtyType(),
                                        String.valueOf( remaining_qty )
                                });

                                float remaining_total_price = Float.parseFloat( remaining_prices[ INDEX_PRICE_TOTAL ] );//remaining_qty * og_price;//Math.abs( movable_total_price - og_total_price );
                                float remaining_total_discount = Float.parseFloat( remaining_prices[ INDEX_DISCOUNT_TOTAL ] );//remaining_qty * og_discount;//Math.abs( movable_total_discount - og_total_discount );
                                float remaining_total_final_price = Float.parseFloat( remaining_prices[ INDEX_FINAL_PRICE_TOTAL ] );//remaining_qty * og_final_price; //Math.abs( movable_total_final_price - og_total_final_price );
                                // Case 1 - Move All items to another carton
                                if( int_total_items == int_movable_items ){
                                    bt_delete_item.performClick();
                                }
                                else {
                                    ogBillItem.setItemPriceTotal(String.valueOf(remaining_total_price));
                                    ogBillItem.setItemDiscountTotal(String.valueOf(remaining_total_discount));
                                    ogBillItem.setItemFinalPriceTotal(String.valueOf(Math.round( remaining_total_final_price )));
                                    ogBillItem.setItemQuantity(String.valueOf(remaining_qty));
                                    bcv_from.subtractPiecesFromCarton( int_movable_items );
                                    bcv_from.subtractFromCartonTotal( movable_total_final_price );
                                    subtractFromGrandTotal( movable_total_final_price );
                                    //modifyItemInsideCarton( ogBillItem, bcv_from );
                                    editItemInsideTheBillCartonViewVector(ogBillItem);
                                }

                                Log.d( TAG, String.format( "quantity : %d, totalPrice : %f", remaining_qty, remaining_total_final_price ) );
                                Log.d( TAG, String.format( "quantity : %d, totalPrice : %f", int_movable_items, movable_total_final_price ) );

                                //bcv = getBillCartonViewForItemID( str_from_carton_temp_id, str_bill_item_id );



                                sub_dialog.dismiss();

                                amendItemsInDatabase();

                            }

                        });

                        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
                        String carton_names[] = new String[ all_cartons_vector.size() - 1 ];
                        String temp_carton_ids[] = new String[ all_cartons_vector.size() - 1 ];
                        //int j = all_cartons_vector.size() - 2;
                        int i = 0;
                        BillItem movableBillItem = null;
                        while( iterator.hasNext() ){
                            BillCartonView billCartonView = iterator.next();
                            Log.d( TAG, "Looping movableBillItem" );
                            if( billCartonView.getTempID().equals( carton_temp_id ) ) {
                                //et_total_items.setText( billCartonView.getItemDataAtPosition( i ).getItemQuantity() );
                                //i++;
                                continue;
                            }
                            carton_names[ i ] = billCartonView.getCartonName() + " " + billCartonView.getCartonSrNo();
                            temp_carton_ids[ i ] = billCartonView.getTempID();
                            Log.d( TAG, "Filling move item adapter : " + carton_names[ i ] + "," + temp_carton_ids[ i ] );
                            i++;
                        }

                        BillItem bi = getBillItemForItemID( null, str_bill_item_id );
                        et_total_items.setText( bi.getItemQuantity() );

                        spa_move_items = new SpinnerCartonAdapter( context, carton_names, temp_carton_ids );
                        spinner_cartons.setAdapter( spa_move_items );

                        moveDialog.show();

                    }

                });


                sub_dialog.show();

                return false;
            }

        };
        bill_item_placement.setOnLongClickListener( itemLongClickListener );

        bcv.addItemAtPosition( bill_item_placement, billItem );
        bcv.addToCartonTotal( Float.parseFloat( billItem.getItemFinalPriceTotal() ) );
        bcv.addPiecesToCarton( Integer.parseInt( billItem.getItemQuantity() ) );
        addToGrandTotal( Float.parseFloat( billItem.getItemFinalPriceTotal() ) );
        item_sr_no = Integer.parseInt( billItem.getItemSrNo() );
        //BillCartonView main_bcv = all_cartons_vector.get(index);
        //main_bcv.setView( bcv );

        //all_cartons_vector.add( index, main_bcv );

    }

    private void editItemInsideTheBillCartonViewVector( BillItem billItem ){


        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
        int index = 0;
        BillCartonView bcv = null;
        View bill_item_placement = null;
        boolean item_found = false;
        while( iterator.hasNext() ){
            bcv = iterator.next();
            if( bcv.getTempID().equals( billItem.getCartonTempID() ) ){

                // Log.d( TAG, "Got the CARTON" );
                // Loop through the items to fi
                for( int i = 1; i < bcv.getTotalChildren() - 1 ; i++ ){
                    BillItem bi = bcv.getItemDataAtPosition( i - 1 );
                    //View bip = bcv.getItemAtPosition( i - 1 );
                    if( bi.getBillItemID().equals( billItem.getBillItemID() ) ){
                        Log.d( TAG, "Item found !!" );
                        item_found = true;

                        LayoutInflater layoutInflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
                        View bip = layoutInflater.inflate( R.layout.bill_item_placement, null );
                        TextView tv_bill_item_id = (TextView) bip.findViewById( R.id.tv_bill_item_id );
                        TextView tv_sr_no = (TextView) bip.findViewById( R.id.tv_sr_no );
                        TextView tv_item_name = (TextView) bip.findViewById( R.id.tv_item_name );
                        TextView tv_item_code = (TextView) bip.findViewById( R.id.tv_item_code );
                        TextView tv_item_price = (TextView) bip.findViewById( R.id.tv_item_price );
                        TextView tv_item_discount = (TextView) bip.findViewById( R.id.tv_item_discount );
                        TextView tv_item_final_price = (TextView) bip.findViewById( R.id.tv_final_price );
                        TextView tv_item_quantity = (TextView) bip.findViewById( R.id.tv_item_quantity );
                        TextView tv_total_price = (TextView) bip.findViewById( R.id.tv_total_price );
                        TextView tv_total_discount = (TextView) bip.findViewById( R.id.tv_total_discount );
                        TextView tv_total_final_price = (TextView) bip.findViewById( R.id.tv_total_final_price );

                        tv_bill_item_id.setText( String.valueOf( billItem.getBillItemID() ) );
                        tv_sr_no.setText( String.valueOf( billItem.getItemSrNo() ) );
                        tv_item_name.setText( String.valueOf( billItem.getItemName() + billItem.getItemCode() ) );
                        tv_item_code.setText( String.valueOf( billItem.getItemCode() ) );
                        //tv_item_price.setText( String.valueOf( "Price : " + billItem.getItemPrice() ) + " /"+billItem.getItemQtyType() ); // another design
                        tv_item_price.setText( String.valueOf( billItem.getItemPrice() ) );
                        //tv_item_discount.setText( String.valueOf( "Discount : " + billItem.getItemDiscount() ) ); // another design
                        tv_item_discount.setText( String.valueOf( billItem.getItemDiscount() ) );
                        // tv_item_final_price.setText( String.valueOf( "Final Price : " +  billItem.getItemFinalPrice() ) ); // another design
                        tv_item_final_price.setText( String.valueOf( billItem.getItemFinalPrice()  ) );
                        // tv_item_quantity.setText( String.valueOf( billItem.getItemQuantity() + " pieces" ) );    // another design
                        // tv_item_quantity.setText( String.valueOf( billItem.getItemQuantity() ) );
                        Log.d( TAG, "pieces : "+billItem.getItemQuantity() );
                        tv_item_quantity.setText( String.valueOf( billItem.getItemQuantity() ) + "pc" );
                        tv_total_price.setText( String.valueOf( billItem.getItemPriceTotal() ) );
                        tv_total_discount.setText( String.valueOf( billItem.getItemDiscountTotal() ) );
                        tv_total_final_price.setText( String.valueOf( billItem.getItemFinalPriceTotal() ) );

                        bip.setOnLongClickListener( itemLongClickListener );

                        bcv.subtractFromCartonTotal( Float.parseFloat( bi.getItemFinalPriceTotal() ) );
                        bcv.addToCartonTotal( Float.parseFloat( billItem.getItemFinalPriceTotal() ) );

                        bcv.subtractPiecesFromCarton( Integer.parseInt( bi.getItemQuantity() ) );
                        bcv.addPiecesToCarton( Integer.parseInt( billItem.getItemQuantity() ) );

                        subtractFromGrandTotal( Float.parseFloat( bi.getItemFinalPriceTotal() ) );
                        addToGrandTotal( Float.parseFloat( billItem.getItemFinalPriceTotal() ) );

                        bcv.setItemAtPosition( bip, i );
                        bcv.setItemDataAtPosition( billItem, i - 1 );
                        Log.d( TAG, "Replacing itemdata at position : "+(i-1) );

                        break;
                    }
                }
                if( item_found ) break;

            }
            // Log.d( TAG, "outside" );
            index++;
        }

    }


    private void saveBill(){

    }

    private void cancelBill(){

    }



    private void loadItemsIntoItemDialogAdatper(){

        String sql = null;

        if( LocalFunctions.havePrivilege( sqldb, spfs, "list_all_items" ) )
            sql = "SELECT * FROM items";
        else {
            // Get the shop id for this user
            String SQL = String.format( "SELECT shop_id FROM user_shops WHERE user_id='%s'", UtilSharedPreferences.getSharedPreference( spfs, KEY_USERNAME, "-1" ) );
            Log.d( TAG, SQL );
            Cursor c = UtilSQLite.executeQuery( sqldb, SQL, false );
            c.moveToNext();

            sql = String.format( "SELECT * FROM items WHERE shop_id='%s'", c.getString( c.getColumnIndex( "shop_id" ) ) ) ;
        }

        Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
        int i = 0;
        items = new Item[ c.getCount() ];
        while( c.moveToNext() ){

            items[ i ] = new Item();
            items[ i ].setId( c.getString( c.getColumnIndex( "id" ) ) );
            items[ i ].setQuantityType( c.getString( c.getColumnIndex( "qty_type" ) ) );
            items[ i ].setItemName( c.getString( c.getColumnIndex( "item_name" ) ) );
            items[ i ].setItemCode( c.getString( c.getColumnIndex( "item_code" ) ) );
            items[ i ].setPrice( c.getString( c.getColumnIndex( "price" ) ) );
            items[ i ].setDiscount( c.getString( c.getColumnIndex( "discount" ) ) );
            items[ i ].setFinalPrice( c.getString( c.getColumnIndex( "final_price" ) ) );
            items[ i ].setShopId( c.getString( c.getColumnIndex( "shop_id" ) ) );
            i++;

        }


    }

    private void calculatePricesForAddItemDialog(){

        // Prices cannot be empty, so make them ZERO if they are empty
        String price_new = et_price_new.getText().toString().trim();
        if( price_new.equals( "" ) ) {
            price_new = "0";
            //et_price_new.setText( price_new );
        }
        float ft_price_new = Float.parseFloat( price_new.equals( "" )?"0.0":price_new );

        String discount_new = et_discount_new.getText().toString().trim();
        if( discount_new.equals( "" ) ) {
            discount_new = "0";
            //et_discount_new.setText( discount_new );
        }
        float ft_discount_new = Float.parseFloat( discount_new.equals( "" )?"0.0":discount_new );

        String final_price_new = et_final_price_new.getText().toString().trim();
        if( final_price_new.equals( "" ) ) {
            final_price_new = "0";
            //et_final_price_new.setText( final_price_new );
        }
        //float ft_final_price_new = Float.parseFloat( final_price_new.equals( "" )?"0.0":final_price_new );

        // Calculate the Total for every transaction, like, Final Price = Price - Discount
        float ft_final_price_new = ft_price_new - ft_discount_new;
        et_final_price_new.setText( String.valueOf( ft_final_price_new ) );

        String qty_type = "per_piece";
        if( rb_per_piece_new.isChecked() ) qty_type = "per_piece";
        else qty_type = "dozen";

        String quantity = et_item_qty_new.getText().toString().trim();
        if( quantity.equals( "0" ) || quantity.equals( "" ) ){
            //et_item_qty_new.setText( "1" );
            quantity = "1";
        }
        float ft_quantity = Float.parseFloat( quantity );
        if( qty_type.equals( "dozen" ) ){
            ft_quantity = ft_quantity/12;
        }


       // float ft_quantity = Float.parseFloat( quantity );

        float ft_price_total = ft_price_new * ((float)ft_quantity);
        float ft_discount_total = ft_discount_new * ((float)ft_quantity);
        float ft_final_price_total = ft_price_total - ft_discount_total;

        et_price_total_new.setText( String.valueOf( (int)Math.ceil( ft_price_total ) ) );
        et_discount_total_new.setText( String.valueOf( (int)Math.ceil( ft_discount_total ) ) );
        et_final_price_total_new.setText( String.valueOf( (int)Math.ceil( ft_final_price_total ) ) );

    }


    int INDEX_PRICE = 0;
    int INDEX_DISCOUNT = 1;
    int INDEX_FINAL_PRICE = 2;
    int INDEX_PRICE_TOTAL = 5;
    int INDEX_DISCOUNT_TOTAL = 6;
    int INDEX_FINAL_PRICE_TOTAL = 7;
    int INDEX_QTY_TYPE = 3;
    int INDEX_QTY = 4;

    private String[] calculatePricesForAddItemDialog( String params[] ){

        /*
        * price
        * discount
        * total
        * qty_type
        * qty
        *
        * */


        // Prices cannot be empty, so make them ZERO if they are empty
        String price_new = params[ INDEX_PRICE ];
        if( price_new.equals( "" ) ) {
            price_new = "0";
//            et_price_new.setText( price_new );
        }
        float ft_price_new = Float.parseFloat( price_new.equals( "" )?"0.0":price_new );

        String discount_new = params[ INDEX_DISCOUNT ];//et_discount_new.getText().toString().trim();
        if( discount_new.equals( "" ) ) {
            discount_new = "0";
            // et_discount_new.setText( discount_new );
        }
        float ft_discount_new = Float.parseFloat( discount_new.equals( "" )?"0.0":discount_new );

        String final_price_new = params[ INDEX_FINAL_PRICE ];//et_final_price_new.getText().toString().trim();
        if( final_price_new.equals( "" ) ) {
            final_price_new = "0";
            // et_final_price_new.setText( final_price_new );
        }
        //float ft_final_price_new = Float.parseFloat( final_price_new.equals( "" )?"0.0":final_price_new );

        // Calculate the Total for every transaction, like, Final Price = Price - Discount
        float ft_final_price_new = ft_price_new - ft_discount_new;
        // et_final_price_new.setText( String.valueOf( ft_final_price_new ) );

        String qty_type = params[ INDEX_QTY_TYPE ]; //"per_piece";
        //if( rb_per_piece_new.isChecked() ) qty_type = "per_piece";
        //else qty_type = "dozen";

        String quantity = params[ INDEX_QTY ];//et_item_qty_new.getText().toString().trim();
        if( quantity.equals( "0" ) || quantity.equals( "" ) ){
            // et_item_qty_new.setText( "1" );
            quantity = "1";
        }
        float ft_quantity = Float.parseFloat( quantity );
        if( qty_type.equals( "dozen" ) ){
            ft_quantity = ft_quantity/12;
        }


        // float ft_quantity = Float.parseFloat( quantity );

        float ft_price_total = ft_price_new * ((float)ft_quantity);
        float ft_discount_total = ft_discount_new * ((float)ft_quantity);
        float ft_final_price_total = ft_price_total - ft_discount_total;

        //et_price_total_new.setText( String.valueOf( (int)Math.ceil( ft_price_total ) ) );
        //et_discount_total_new.setText( String.valueOf( (int)Math.ceil( ft_discount_total ) ) );
        //et_final_price_total_new.setText( String.valueOf( (int)Math.ceil( ft_final_price_total ) ) );

        String data[] = new String[ 8 ];
        data[ INDEX_PRICE ] = String.valueOf( price_new );
        data[ INDEX_DISCOUNT ] = String.valueOf( discount_new );
        data[ INDEX_FINAL_PRICE ] = String.valueOf( final_price_new );
        data[ INDEX_PRICE_TOTAL ] = String.valueOf( (int)Math.ceil( ft_price_total ) );
        data[ INDEX_DISCOUNT_TOTAL ] = String.valueOf( (int)Math.ceil( ft_discount_total ) ) ;
        data[ INDEX_FINAL_PRICE_TOTAL ] = String.valueOf( (int)Math.ceil( ft_final_price_total ) );
        data[ INDEX_QTY_TYPE ] = String.valueOf( qty_type );
        data[ INDEX_QTY ] = String.valueOf( quantity );

        return data;
    }


    @Override
    protected void onNewIntent( Intent intent ) {
        super.onNewIntent( intent );

        Log.d( TAG, "onNewIntent()" );

        Log.d( TAG, "bill_id : "+id );

    }

/*    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent in = new Intent( context, MakeBill_Phase1Activity.class );
        startActivity( in );
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        id = getIntent().getStringExtra( "id" );

        loadBillInitials();

        loadBillData();
    }


    private void amendCartonInDatabase(){

        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
        cartonsMeta = new JSONArray();
        while( iterator.hasNext() ){
            BillCartonView bcv = iterator.next();
            try {
                cartonMetaObject = new JSONObject();
                cartonMetaObject.put("temp_carton_id", bcv.getTempID());
                cartonMetaObject.put("carton_name", bcv.getCartonName());
                cartonMetaObject.put("price", bcv.getCartonPrice());
                cartonMetaObject.put("is_despatched", "no" );
                cartonMetaObject.put("sr_no", bcv.getCartonSrNo() );
            }
            catch ( Exception e ){
                e.printStackTrace();
            }
            cartonsMeta.put( cartonMetaObject );
        }
        //Log.d( TAG, "Array length : "+cartonsMeta.toString() );

        String sql = String.format( "UPDATE bills SET cartons_meta='%s' WHERE id='%s'", cartonsMeta.toString(), id );
        Log.d( TAG, sql );
        UtilSQLite.executeQuery( sqldb, sql, true );

    }



    private void amendItemsInDatabase(){

        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
        itemsMeta = new JSONArray();
        while( iterator.hasNext() ){
            BillCartonView bcv = iterator.next();

            // Loop the Items for the carton bcv
            for( int i = 1 ; i < bcv.getTotalChildren() - 1 ; i++ ){ // first is carton name, last is total, so we ignore
                BillItem billItem = bcv.getItemDataAtPosition( i - 1 );
                try {
                    itemMetaObject = new JSONObject();
                    itemMetaObject.put( "temp_carton_id", bcv.getTempID() );
                    itemMetaObject.put( "temp_item_id", billItem.getBillItemID() );
                    itemMetaObject.put( "item_name", billItem.getItemName() );
                    itemMetaObject.put( "item_code", billItem.getItemCode() );
                    itemMetaObject.put( "price", billItem.getItemPrice() );
                    itemMetaObject.put( "discount", billItem.getItemDiscount() );
                    itemMetaObject.put( "final_price", billItem.getItemFinalPrice() );
                    itemMetaObject.put( "total_price", billItem.getItemPriceTotal() );
                    itemMetaObject.put( "total_discount", billItem.getItemDiscountTotal() );
                    itemMetaObject.put( "total_final_price", billItem.getItemFinalPriceTotal() );
                    itemMetaObject.put( "qty_type", billItem.getItemQtyType() );
                    itemMetaObject.put( "qty", billItem.getItemQuantity() );
                    itemMetaObject.put( "sr_no", billItem.getItemSrNo() );
                }
                catch ( Exception e ){
                    e.printStackTrace();
                }
                itemsMeta.put( itemMetaObject );
            }

        }
        //Log.d( TAG, "Array length : "+cartonsMeta.toString() );

        String sql = String.format( "UPDATE bills SET item_meta='%s' WHERE id='%s'", itemsMeta.toString(), id );
        Log.d( TAG, sql );
        UtilSQLite.executeQuery( sqldb, sql, true );

    }



    public float addToGrandTotal( float value ){
        grand_total += value;
        tv_total.setText( "Rs." + Math.round( Math.ceil( grand_total ) ) + "/-" );
        return grand_total;
    }

    public float subtractFromGrandTotal( float value ){
        grand_total -= value;
        tv_total.setText( "Rs." + Math.round( Math.ceil( grand_total ) ) + "/-" );
        return grand_total;
    }



    // MENU
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate( R.menu.make_bill_phase2_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        switch( item.getItemId() ){

            case R.id.menu_cancel_bill:
                cancelBill();
                return true;

            case R.id.menu_save_bill:
                saveBill();
                return true;

            case android.R.id.home:
                Intent in = new Intent( context, MakeBill_Phase1Activity.class );
                startActivity( in );
                finish();
                return true;

        }
        return( super.onOptionsItemSelected( item ) );
    }







    public String getCartonTempIDForItem( String temp_item_id ){
        // Iterate through the bcv vector to find out this item's carton and item info
        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
        while( iterator.hasNext() ){
            BillCartonView bcv = iterator.next();
            int total_items = bcv.getTotalChildren();
            for( int i = 1; i < total_items - 1; i++ ){
                //Log.d( TAG, "Looped "+i+" time" );
                //View temp_view = bcv.getItemAtPosition( i );
                //tv_bill_item_id = (TextView) temp_view.findViewById( R.id.tv_bill_item_id );
                //String bill_item_id = tv_bill_item_id.getText().toString().trim();
                tempBillItem = bcv.getItemDataAtPosition( i-1 );// i-1 because, we are skipping the first element
                if( temp_item_id.equals( tempBillItem.getBillItemID() ) ){
                    //Log.d( TAG, "Here is the item : "+tempBillItem.getBillItemID() );
                    return tempBillItem.getCartonTempID();
                }
            }

        }
        return "-1";
    }

    public BillItem getBillItemForItemID( String temp_carton_id, String temp_item_id ){
        Log.d( TAG, "aa : " + temp_carton_id + "," + temp_item_id );
        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
        int i = 0;
        BillItem billItem = null;
        if( temp_carton_id == null ){
            while (iterator.hasNext()) {
                BillCartonView billCartonView = iterator.next();
                for( int j = 1 ; j < billCartonView.getTotalChildren() - 1 ; j++ ){
                    Log.d( TAG, "looping inside carton "+j );
                    if( billCartonView.getItemDataAtPosition( j-1 ).getBillItemID().equals( temp_item_id ) ){
                        // Item found
                        return billCartonView.getItemDataAtPosition( j-1 );
                    }
                }
                i++;
            }
        }
        else {
            while (iterator.hasNext()) {
                BillCartonView billCartonView = iterator.next();
                if ( billCartonView.getTempID().equals( temp_carton_id ) ) { // Carton Found, iterate to find the item
                    Log.d( TAG, "carton found " );
                    for( int j = 1 ; j < billCartonView.getTotalChildren() - 1 ; j++ ){
                        Log.d( TAG, "looping inside carton "+j );
                        if( billCartonView.getItemDataAtPosition( j-1 ).getBillItemID().equals( temp_item_id ) ){
                            // Item found
                            return billCartonView.getItemDataAtPosition( j-1 );
                        }
                    }
                }
                i++;
            }
        }
        return null;
    }


    public BillCartonView getBillCartonViewForItemID( String temp_carton_id, String temp_item_id ){
        Log.d( TAG, temp_carton_id + "," + temp_item_id );
        Iterator<BillCartonView> iterator = all_cartons_vector.iterator();
        int i = 0;
        BillItem billItem = null;
        if( temp_carton_id == null ){
            while (iterator.hasNext()) {
                BillCartonView billCartonView = iterator.next();
                for( int j = 1 ; j < billCartonView.getTotalChildren() - 1 ; j++ ){
                    Log.d( TAG, "looping inside carton "+j );
                    if( billCartonView.getItemDataAtPosition( j-1 ).getBillItemID().equals( temp_item_id ) ){
                        // Item found
                        return billCartonView;
                    }
                }

                i++;
            }
        }
        else {
            while (iterator.hasNext()) {
                BillCartonView billCartonView = iterator.next();
                if ( billCartonView.getTempID().equals( temp_carton_id ) ) { // Carton Found, iterate to find the item
                    Log.d( TAG, "carton found " );
                    for( int j = 1 ; j < billCartonView.getTotalChildren() - 1 ; j++ ){
                        Log.d( TAG, "looping inside carton "+j );
                        if( billCartonView.getItemDataAtPosition( j-1 ).getBillItemID().equals( temp_item_id ) ){
                            // Item found
                            return billCartonView;
                        }
                    }
                }
                i++;
            }
        }
        return null;
    }




    public void viewBillButtonListener(){

        bt_view_bill.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                Intent in = new Intent( context, BillViewActivity.class );
                in.putExtra( "id", id );
                in.putExtra( "who_called", "makebill" );
                startActivity( in );

            }

        });

    }

    public void finishButtonListener(){

        bt_finish.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                final Dialog finish_dialog = new Dialog( context );
                finish_dialog.setContentView( R.layout.layout_finish_dialog );
                final EditText et_salesman = (EditText) finish_dialog.findViewById( R.id.et_salesman );
                final EditText et_invoice_by = (EditText) finish_dialog.findViewById( R.id.et_invoice_by );
                final EditText et_packed_by = (EditText) finish_dialog.findViewById( R.id.et_packed_by );

                et_salesman.setText( salesman );
                et_invoice_by.setText( invoice_by );
                et_packed_by.setText( packed_by );

                Button bt_finish_bill = (Button) finish_dialog.findViewById( R.id.bt_finish_bill );
                bt_finish_bill.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick( View view ) {

                        String str_salesman = et_salesman.getText().toString().trim();
                        String str_invoice_by = et_invoice_by.getText().toString().trim();
                        String str_packed_by = et_packed_by.getText().toString().trim();

                        if( str_salesman.equals( "" ) ||
                                str_invoice_by.equals( "" ) ||
                                str_packed_by.equals( "" ) ){
                            CustomToast.showCustomToast( context, "error", "Please input all the values before proceeding !", Toast.LENGTH_SHORT );
                            return;
                        }

                        String sql = String.format( "UPDATE bills SET salesman='%s', invoice_by='%s', packed_by='%s', bill_status='complete' WHERE id='%s'" ,
                                str_salesman, str_invoice_by, str_packed_by, id );
                        Log.d( TAG, sql );
                        UtilSQLite.executeQuery( sqldb, sql, true );

                        finish_dialog.dismiss();

                        // Navigate to View Bill page
                        bt_view_bill.performClick();

                    }

                });

                finish_dialog.show();

            }

        });

    }


}
