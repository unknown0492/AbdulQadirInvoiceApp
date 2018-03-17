package com.silentcoders.abdulqadir.invoiceapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;
import com.silentcoders.customitems.CustomToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;

public class BillViewActivity extends AppCompatActivity {

    Context context = this;
    SharedPreferences spfs;
    SQLiteDatabase sqldb;
    private static String TAG = "BillViewActivity";

    private RelativeLayout rl;

    LayoutInflater layoutInflater;
    LinearLayout ll_container;
    TextView tv_customer_name, tv_place, tv_date, tv_remarks, tv_grand_total;
    LinearLayout ll_extras_container; // Created in JAVA code and is the last View inside the ll_container

    String id = null;
    float grand_total = 0;
    float packaging_total = 0;
    float extras_total = 0;

    Button bt_add_extras, bt_subtract_extras;
    View.OnClickListener extrasButtonListener = null;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_bill_view );

        checkPermissions();

        init();

    }

    private void init(){

        initActionBar();

        initViews();

    }

    private void initActionBar(){
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle( "View Bill" );
    }

    private void initViews(){

        //id = "78Q8B";
        id = getIntent().getStringExtra( "id" );

        layoutInflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
        ll_container = findViewById( R.id.ll_container );

        sqldb = UtilSQLite.makeDatabase( Constants.DB_NAME, context );
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );

        bt_add_extras = (Button) findViewById( R.id.bt_add_extras );
        bt_subtract_extras = (Button) findViewById( R.id.bt_subtract_extras );
        tv_grand_total = (TextView) findViewById( R.id.tv_grand_total );
        ll_extras_container = new LinearLayout( context );
        ll_extras_container.setOrientation( LinearLayout.VERTICAL );

        getBillSummary();

        registerAddSubtractExtrasButtonListener();

        iterateBillItems();

        /*rl = (RelativeLayout) findViewById( R.id.rl );
        File file = saveBitMap(this, rl);    //which view you want to pass that view as parameter
        if (file != null) {
            Log.i("TAG", "Drawing saved to the gallery!");
        } else {
            Log.i("TAG", "Oops! Image could not be saved.");
        }*/


        /*rl.setDrawingCacheEnabled(true);
        rl.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        rl.layout(0, 0, rl.getMeasuredWidth(), rl.getMeasuredHeight());
        rl.buildDrawingCache(true);

        Bitmap b = Bitmap.createBitmap(rl.getDrawingCache());
        rl.setDrawingCacheEnabled(false);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "v2i.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    @Override
    protected void onResume() {
        super.onResume();

        id = getIntent().getStringExtra( "id" );

    }

    public void getBillSummary(){

        String sql = String.format( "SELECT * FROM bills WHERE id='%s'", id );
        Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
        c.moveToNext();

        tv_customer_name = (TextView) findViewById( R.id.tv_customer_name );
        tv_place = (TextView) findViewById( R.id.tv_place );
        tv_date = (TextView) findViewById( R.id.tv_date );
        tv_remarks = (TextView) findViewById( R.id.tv_remarks );

        tv_customer_name.setText( c.getString( c.getColumnIndex( "customer_name" ) ) + " - " );
        tv_place.setText( c.getString( c.getColumnIndex( "place" ) ) );
        tv_date.setText( LocalFunctions.convertMillisToDate( c.getLong( c.getColumnIndex( "timestamp" ) ) ) );
        tv_remarks.setText( "Remarks : " + c.getString( c.getColumnIndex( "remarks" ) ) );

    }

    public void iterateBillItems(){

        String sql = String.format( "SELECT cartons_meta, item_meta, bill_extras FROM bills WHERE id='%s'", id );
        Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
        c.moveToNext();

        JSONArray jsonArray = null;
        JSONArray jsonArray1 = null;
        JSONArray jsonArray2 = null;
        JSONObject jsonObject = null;
        JSONObject jsonObject1 = null;
        JSONObject jsonObject2 = null;

        try{
            jsonArray = new JSONArray( c.getString( c.getColumnIndex( "cartons_meta" ) ) );
            jsonArray1 = new JSONArray( c.getString( c.getColumnIndex( "item_meta" ) ) );
            jsonArray2 = new JSONArray( c.getString( c.getColumnIndex( "bill_extras" ) ) );
            int j = 0;
            float carton_total = 0;
            // This for loop is to iterate the Cartons
            for( int i = 0 ; i < jsonArray.length() ; i++ ){
                jsonObject = jsonArray.getJSONObject( i );
                addCartonView( jsonObject );
                carton_total = 0;

                // This for loop is to iterate the items inside the Current Carton
                // Iterate only till the Carton Temp ID matches with the Carton Temp id for the item
                try {
                    for (jsonObject1 = jsonArray1.getJSONObject(j); (j < jsonArray1.length()) && jsonObject.getString("temp_carton_id").equals(jsonObject1.getString("temp_carton_id")); ) {

                        addItemView(jsonObject1);
                        addItemPriceToGrandTotal(Float.parseFloat(jsonObject1.getString("total_final_price")));
                        carton_total += Float.parseFloat(jsonObject1.getString("total_final_price"));
                        //Log.d( TAG, jsonObject1.getString( "item_name" ) + ", "+jsonObject.getString( "carton_name" )  + ", " +j );
                        j++;
                        if ((j < jsonArray1.length()))
                            jsonObject1 = jsonArray1.getJSONObject(j);
                    }
                }
                catch ( Exception e ){
                    Log.e( TAG, "Exception : no items inside this carton" );
                }
                addCartonTotalView( carton_total );
                addPackingCharges( Float.parseFloat( jsonObject.getString( "price" ) ) );

            }
            addFinalTotalView( getGrandTotal() );
            addPackagingChargesView( getPackagingTotal() );

            ll_container.addView( ll_extras_container );
            // Run a for loop to input the bill extras
            for( int k = 0 ; k < jsonArray2.length() ; k++ ){
                jsonObject2 = jsonArray2.getJSONObject( k );
                addExtrasViewToBill( jsonObject2 );
            }

        }
        catch ( Exception e ){
            e.printStackTrace();
        }

        // Add all the totals to the Grand Total
        addToGrandTotal( getPackagingTotal() );
        //addToGrandTotal( getExtrasChargesTotal() );

        tv_grand_total.setText( "Grand Total : Rs. " +Math.round( getGrandTotal() )+ "/-" );
    }

    public void addCartonView( JSONObject jsonObject ){

        View carton_line = layoutInflater.inflate( R.layout.billview_carton_line, null );
        TextView carton_name = (TextView) carton_line.findViewById( R.id.tv_carton_name );
        TextView carton_price = (TextView) carton_line.findViewById( R.id.tv_carton_price );

        try {

            carton_name.setText( jsonObject.getString( "carton_name" ) + " " + jsonObject.getString( "sr_no" ) );
            carton_price.setText( "Packaging cost Rs. " + jsonObject.getString( "price" ) + "/-" );

        } catch ( JSONException e ) {
            e.printStackTrace();
        }

        ll_container.addView( carton_line );

    }

    public void addItemView( JSONObject jsonObject ){

        View item_line = layoutInflater.inflate( R.layout.billview_item_line, null );
        TextView tv_sr_no = (TextView) item_line.findViewById( R.id.tv_sr_no );
        TextView tv_qty = (TextView) item_line.findViewById( R.id.tv_qty );
        TextView tv_item_name = (TextView) item_line.findViewById( R.id.tv_item_name );
        TextView tv_item_price = (TextView) item_line.findViewById( R.id.tv_item_price );
        TextView tv_discount = (TextView) item_line.findViewById( R.id.tv_discount );
        TextView tv_final_price = (TextView) item_line.findViewById( R.id.tv_final_price );
        TextView tv_total_price = (TextView) item_line.findViewById( R.id.tv_total_price );

        try {

            tv_sr_no.setText( jsonObject.getString( "sr_no" ) );
            tv_qty.setText( jsonObject.getString( "qty" ) );
            tv_item_name.setText( jsonObject.getString( "item_name" ) + jsonObject.getString( "item_code" ) );
            tv_item_price.setText( jsonObject.getString( "price" ) );
            tv_discount.setText( jsonObject.getString( "discount" ) );
            tv_final_price.setText( jsonObject.getString( "final_price" ) );
            tv_total_price.setText( jsonObject.getString( "total_final_price" ) );




        } catch ( JSONException e ) {
            e.printStackTrace();
        }

        ll_container.addView( item_line );

    }

    public void addCartonTotalView( float carton_total ){

        int ct = Math.round( carton_total );

        View carton_total_line = layoutInflater.inflate( R.layout.billview_carton_total_line, null );
        TextView tv_carton_total = (TextView) carton_total_line.findViewById( R.id.tv_carton_total );

        tv_carton_total.setText( "Carton Total : Rs. " + String.valueOf( ct ) + "/-" );

        ll_container.addView( carton_total_line );

    }

    public void addFinalTotalView( float final_total ){

        int ft = Math.round( final_total );

        View final_total_line = layoutInflater.inflate( R.layout.billview_final_total_line, null );
        TextView tv_final_total = (TextView) final_total_line.findViewById( R.id.tv_final_total );

        tv_final_total.setText( "Total : Rs. " + String.valueOf( ft ) + "/-" );

        ll_container.addView( final_total_line );

    }

    public void addPackagingChargesView( float packaging_total ){

        int pt = Math.round( packaging_total );

        View packaging_total_line = layoutInflater.inflate( R.layout.billview_packaging_total_line, null );
        TextView tv_packaging_total = (TextView) packaging_total_line.findViewById( R.id.tv_packaging_total );

        tv_packaging_total.setText( "Packaging Charges : Rs. " + String.valueOf( pt ) + "/-" );

        ll_container.addView( packaging_total_line );

    }

    public void addExtrasViewToBill( String caption, String extras_charges, String add_or_subtract ){

        View view = layoutInflater.inflate( R.layout.billview_extras_captions_line, null );
        TextView tv_extras_caption = (TextView) view.findViewById( R.id.tv_extras_caption );
        TextView tv_extras_charges = (TextView) view.findViewById( R.id.tv_extras_charges );
        TextView tv_add_or_subtract = (TextView) view.findViewById( R.id.tv_add_or_subtract );

        //Log.d( TAG, caption + "," + extras_charges + "," +add_or_subtract );

        tv_extras_caption.setText( caption );
        tv_extras_charges.setText( "Rs." +extras_charges+ "/-" );
        tv_extras_charges.setTag( extras_charges );

        String str_add_or_subtract = "add +";
        if( add_or_subtract.equals( "subtract" ) ) {
            str_add_or_subtract = "less -";
            addToExtrasChargesTotal( -Float.parseFloat( extras_charges ) );
            addToGrandTotal( -Float.parseFloat( extras_charges ) );
        }
        else{
            addToExtrasChargesTotal( Float.parseFloat( extras_charges ) );
            addToGrandTotal( Float.parseFloat( extras_charges ) );
        }

        tv_add_or_subtract.setText( str_add_or_subtract );
        tv_add_or_subtract.setTag( add_or_subtract );

        view.setOnLongClickListener( new View.OnLongClickListener() {

            @Override
            public boolean onLongClick( final View view_extras_line ) {

                LinearLayout ll = new LinearLayout( context );
                ll.setOrientation( LinearLayout.VERTICAL );
                ll.setPadding( 5, 5, 5, 5 );
                // Button bt_edit_extras_item = new Button( context ); bt_edit_extras_item.setText( "Edit" );
                Button bt_delete_extras_item = new Button( context ); bt_delete_extras_item.setText( "Delete" );
                ll.addView( bt_delete_extras_item );

                final Dialog d = new Dialog( context );
                d.setContentView( ll );
                d.show();

                bt_delete_extras_item.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick( View view ) {

                        TextView tv_extras_charges = view_extras_line.findViewById( R.id.tv_extras_charges );
                        TextView tv_add_or_subtract = view_extras_line.findViewById( R.id.tv_add_or_subtract );

                        String extras_charges = tv_extras_charges.getTag().toString();
                        String add_or_subtract = tv_add_or_subtract.getTag().toString();

                        deleteExtrasView( view_extras_line, extras_charges, add_or_subtract );
                        d.hide();

                    }

                });

                return false;
            }

        });


        ll_extras_container.addView( view );

        amendBillExtrasInDatabase();

    }

    public void addExtrasViewToBill( JSONObject jsonObject ){
        try {
            addExtrasViewToBill( jsonObject.getString( "caption" ), jsonObject.getString( "charges" ), jsonObject.getString( "add_or_subtract" ) );
        }
        catch ( Exception e ){
            e.printStackTrace();
        }
    }

    public void deleteExtrasView( View view_extras_line, String extras_charges, String add_or_subtract ){

        if( add_or_subtract.equals( "subtract" ) ){ // Then add this amount to the grand total
            addToExtrasChargesTotal( Float.parseFloat( extras_charges ) );
            addToGrandTotal( Math.round( Float.parseFloat( extras_charges ) ) );
        }
        else{
            subtractFromExtrasChargesTotal( Float.parseFloat( extras_charges ) );
            subtractFromGrandTotal( Math.round( Float.parseFloat( extras_charges ) ) );
        }

        ll_extras_container.removeView( view_extras_line );

        amendBillExtrasInDatabase();

    }

    public void amendBillExtrasInDatabase(){

        JSONArray billExtrasArray = null;
        JSONObject billExtrasObject = null;

        int total_extras = ll_extras_container.getChildCount();
        //Log.d( TAG, "Total extras : "+total_extras );
        try {
            billExtrasArray = new JSONArray();

            for( int i = 0 ; i < total_extras ; i++ ){
                View view = ll_extras_container.getChildAt( i );
                TextView tv_extras_caption = view.findViewById( R.id.tv_extras_caption );
                TextView tv_extras_charges = view.findViewById( R.id.tv_extras_charges );
                TextView tv_add_or_subtract = view.findViewById( R.id.tv_add_or_subtract );

                String extras_charges = tv_extras_charges.getTag().toString();
                String add_or_subtract = tv_add_or_subtract.getTag().toString();

                billExtrasObject = new JSONObject();
                billExtrasObject.put( "caption", tv_extras_caption.getText().toString() );
                billExtrasObject.put( "add_or_subtract", add_or_subtract );
                billExtrasObject.put( "charges", extras_charges );

                billExtrasArray.put( billExtrasObject );

            }
        }
        catch ( Exception e ){
            e.printStackTrace();
        }

        //Log.d( TAG, "Array length : "+billExtrasArray.toString() );

        String sql = String.format( "UPDATE bills SET bill_extras='%s' WHERE id='%s'", billExtrasArray.toString(), id );
        Log.d( TAG, sql );
        UtilSQLite.executeQuery( sqldb, sql, true );

    }



    // Add/Less Item prices to Grand Total
    public float addItemPriceToGrandTotal( float value ){
        grand_total += value;
        return grand_total;
    }

    public float subtractItemPriceToGrandTotal( float value ){
        grand_total -= value;
        return grand_total;
    }

    public float getGrandTotal(){
        return grand_total;
    }
    // Add/Less Item prices to Grand Total

    // Add/Less Item prices to Packaging total
    public float addPackingCharges( float value ){
        packaging_total += value;
        return packaging_total;
    }

    public float subtractPackingCharges( float value ){
        packaging_total -= value;
        return packaging_total;
    }

    public float getPackagingTotal(){
        return packaging_total;
    }
    // Add/Less Item prices to Packaging total

    // Add/Less Extras charges to Extras total
    public float addToExtrasChargesTotal( float value ){
        extras_total += value;
        return extras_total;
    }

    public float subtractFromExtrasChargesTotal( float value ){
        extras_total -= value;
        return extras_total;
    }

    public float getExtrasChargesTotal(){
        return extras_total;
    }
    // Add/Less Extras charges to Extras total

    // Add/Less to Grand Total
    public float addToGrandTotal( float value ){
        grand_total += value;
        tv_grand_total.setText( "Grand Total : Rs. " + Math.round( grand_total ) + "/-" );
        return grand_total;
    }

    public float subtractFromGrandTotal( float value ){
        grand_total -= value;
        tv_grand_total.setText( "Grand Total : Rs. " + Math.round( grand_total ) + "/-" );
        return grand_total;
    }
    // Add/Less to Grand Total



    private void registerAddSubtractExtrasButtonListener(){

        extrasButtonListener = new View.OnClickListener() {

            @Override
            public void onClick( final View view ) {

                final Dialog add_extras_dialog = new Dialog( context );
                add_extras_dialog.setContentView( R.layout.billview_extras_dialog );
                Button bt_put_extras = (Button) add_extras_dialog.findViewById( R.id.bt_put_extras );

                bt_put_extras.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick( View v ) {

                        String add_or_subtract = "";
                        if( view.getId() == R.id.bt_add_extras ) add_or_subtract = "add";
                        else if( view.getId() == R.id.bt_subtract_extras ) add_or_subtract = "subtract";

                        EditText et_extras_caption = (EditText) add_extras_dialog.findViewById( R.id.et_extras_caption );
                        EditText et_extras_charges = (EditText) add_extras_dialog.findViewById( R.id.et_extras_charges );

                        // Validate
                        String str_extras_caption = et_extras_caption.getText().toString().trim();
                        String str_extras_charges = et_extras_charges.getText().toString().trim();
                        String str_add_or_subtract = add_or_subtract;
                        if( str_extras_caption.equals( "" ) ||
                                str_extras_charges.equals( "" ) ){
                            CustomToast.showCustomToast( context, "error", "Caption or cost cannot be empty !", Toast.LENGTH_SHORT );
                            return;
                        }

                        Log.d( TAG, str_extras_caption + "," + str_extras_charges + "," +str_add_or_subtract );

                        addExtrasViewToBill( str_extras_caption, str_extras_charges, str_add_or_subtract );
                        if( str_add_or_subtract.equals( "add" ) ) {
                            addToExtrasChargesTotal(Float.parseFloat(str_extras_charges));
                            // add to grand total
                        }
                        else {
                            subtractFromExtrasChargesTotal(Float.parseFloat(str_extras_charges));
                            // add to grand total
                        }
                        add_extras_dialog.dismiss();

                    }

                });
                add_extras_dialog.show();
            }

        };

        bt_add_extras.setOnClickListener( extrasButtonListener );
        bt_subtract_extras.setOnClickListener( extrasButtonListener );

    }

    private void printBill(){

        // Show dialog to prompt for Type1, Type2, Type3 bill
        // Navigate to the new WebView Activity which will generate an HTML file at runtime, and show the TypeX bill, with print button on top
        final Dialog print_dialog = new Dialog( context );
        final Button bt_full_bill = new Button( context );
        bt_full_bill.setText( "Full Bill" );
        final Button bt_agent_bill = new Button( context );
        bt_agent_bill.setText( "Agent Bill" );
        final Button bt_non_discounted = new Button( context );
        bt_non_discounted.setText( "Discounted" );

        LinearLayout ll_prints = new LinearLayout( context );
        ll_prints.setPadding( 5,5,5,5 );
        ll_prints.setOrientation( LinearLayout.VERTICAL );
        ll_prints.addView( bt_full_bill );
        ll_prints.addView( bt_agent_bill );
        ll_prints.addView( bt_non_discounted );

        print_dialog.setContentView( ll_prints );

        bt_agent_bill.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {
                Intent in = new Intent( context, BillWebViewActivity.class );
                in.putExtra( "bill_type", "agent_bill" );
                in.putExtra( "id", id );
                startActivity( in );

                print_dialog.hide();
            }
        });

        bt_full_bill.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {
                Intent in = new Intent( context, BillWebViewActivity.class );
                in.putExtra( "bill_type", "full_bill" );
                in.putExtra( "id", id );
                startActivity( in );

                print_dialog.hide();
            }
        });

        bt_non_discounted.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {
                Intent in = new Intent( context, BillWebViewActivity.class );
                in.putExtra( "bill_type", "discounted_bill" );
                in.putExtra( "id", id );
                startActivity( in );

                print_dialog.hide();
            }
        });



        print_dialog.show();

    }














    private File saveBitMap(Context context, View drawView){
        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Handcare");
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if(!isDirectoryCreated)
                Log.i("ATG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() +File.separator+ System.currentTimeMillis()+".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap =getBitmapFromView(drawView);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue saving the image.");
        }
        scanGallery( context,pictureFile.getAbsolutePath());
        return pictureFile;
    }
    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
    // used for scanning gallery
    private void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[] { path },null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    View bt_print = null;
    // MENU
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_view_bill_print, menu );

        /*bt_print = menu.findItem( R.id.menu_print_bill ).getActionView();
        bt_print.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick( View view ) {
                Toast.makeText( context, "print", Toast.LENGTH_SHORT ).show();
            }

        });*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        switch( item.getItemId() ){

            case android.R.id.home:
                Intent in = null;
                String who_called = getIntent().getStringExtra( "who_called" );
                if( who_called.equals( "makebill" ) )
                    in = new Intent( context, MakeBill_Phase2Activity.class );
                else
                    in = new Intent( context, ViewBillsActivity.class );
                in.putExtra( "id", id );
                startActivity( in );
                finish();
                return true;

            case R.id.menu_print_bill:
                printBill();
                return true;

        }
        return( super.onOptionsItemSelected( item ) );
    }





    /* Permission related content */

    String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    public void onRequestPermissionsResult( int requestCode, String permissions[], int[] grantResults ) {
        switch ( requestCode ) {
            case 10:
            {
                if( grantResults.length > 0 && grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED ){
                    // permissions granted.
                    Log.d( TAG, grantResults.length + " Permissions granted : " );
                } else {
                    String permission = "";
                    for ( String per : permissions ) {
                        permission += "\n" + per;
                    }
                    // permissions list of don't granted permission
                    Log.d( TAG, "Permissions not granted : "+permission );
                }
                return;
            }
        }
    }

    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for ( String p:permissions ) {
            result = ContextCompat.checkSelfPermission( this, p );
            if ( result != PackageManager.PERMISSION_GRANTED ) {
                listPermissionsNeeded.add( p );
            }
        }
        if ( !listPermissionsNeeded.isEmpty() ) {
            ActivityCompat.requestPermissions( this, listPermissionsNeeded.toArray( new String[ listPermissionsNeeded.size() ] ), 10 );
            return false;
        }
        return true;
    }

    /* Permission related content */
}
