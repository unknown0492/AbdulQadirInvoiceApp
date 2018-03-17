package com.silentcoders.abdulqadir.invoiceapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.silentcoders.classlibrary.MD5;
import com.silentcoders.classlibrary.UtilFile;
import com.silentcoders.classlibrary.UtilNetwork;
import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;
import com.silentcoders.classlibrary.UtilURL;
import com.silentcoders.customitems.CustomToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.silentcoders.abdulqadir.invoiceapp.Constants.DB_NAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_IS_LOGGED_IN;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_ROLE_ID;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_USERNAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.isLoggedIn;
import static com.silentcoders.abdulqadir.invoiceapp.LocalFunctions.logout;


public class LoginActivity extends AppCompatActivity {

    Context context = this;
    final static String TAG = "LoginActivity";

    TextView tv_username, tv_password;
    EditText et_username, et_password;
    Button bt_login, bt_sync_users, bt_continue, bt_logout;
    LinearLayout ll_login_form, ll_logout_form;

    SQLiteDatabase sqldb;
    SharedPreferences spfs;

    String SQL_CREATE_USERS = "CREATE TABLE IF NOT EXISTS users ( user_id text PRIMARY KEY,  password text,  role_id text );";
    String SQL_CREATE_ROLES_PRIVILEGES = "CREATE TABLE IF NOT EXISTS roles_privileges ( role_id text, privilege_id text, privilege_name text, functionality_name text, plugin_id text );";
    String SQL_CREATE_SHOP = "CREATE TABLE IF NOT EXISTS shop ( shop_id text PRIMARY KEY, shop_name text );";
    String SQL_CREATE_USER_SHOPS = "CREATE TABLE IF NOT EXISTS user_shops ( user_id text, shop_id text, PRIMARY KEY( user_id, shop_id ) );";
    String SQL_CREATE_CARTON = "CREATE TABLE IF NOT EXISTS cartons ( ai_id INTEGER PRIMARY KEY AUTOINCREMENT, id text, carton_name text, carton_price text );";
    String SQL_CREATE_ITEM = "CREATE TABLE IF NOT EXISTS items ( id text, item_name text, item_code text, price text, discount text, final_price text, qty_type text, shop_id text, PRIMARY KEY( item_name, item_code ) );";
    String SQL_CREATE_BILLS = "CREATE TABLE IF NOT EXISTS bills ( id text PRIMARY KEY, user_id text, timestamp text, customer_name text, place text, remarks text, salesman text, invoice_by text, packed_by text, cartons_meta text, item_meta text, bill_status text, bill_extras text );";

    String UPDATE_TABLE = "ALTER table bills ADD user_id text";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        init();
        
    }

    private void init(){
        initViews();

        initDatabase();

        addEventsOnViews();

        //pdftest();

    }



    @SuppressLint("NewApi")
    public void pdftest(){
        File file =  UtilFile.createFileIfNotExist( "Calculator", "test2.pdf" );

        try {
            Document document = new Document(PageSize.LETTER);
            PdfWriter pdfWriter = PdfWriter.getInstance
                    (document, new FileOutputStream(file.getAbsolutePath()));
            document.open();

            String htmlText = "<html><body>hiii</body></html>";
            // Fixing xhtml tag
            /*Tidy tidy = new Tidy(); // obtain a new Tidy instance
            tidy.setXHTML(true); // set desired config options using tidy setters
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            tidy.setCharEncoding(Configuration.UTF8);

            tidy.parse(new ByteArrayInputStream( htmlText.getBytes(), output );*/
            String preparedText = htmlText;//output.toString("UTF-8");

            Log.i("CHECKING", "JTidy Out: " + preparedText);

            InputStream inputStream = new ByteArrayInputStream(preparedText.getBytes());
            XMLWorkerHelper.getInstance().parseXHtml( pdfWriter, document,
                    inputStream, null, Charset.forName("UTF-8") );

            document.close();
            //return true;
        } catch (Exception e) {
            //File file = new File(absoluteFilePath);
            if(file.exists()) {
                boolean isDeleted = file.delete();
                Log.i("CHECKING", "PDF isDeleted: " + isDeleted);
            }
            //LOGGER.error("Exception: " + e.getMessage());
            e.printStackTrace();
            //return false;
        }

        /*try{

            //PDFWriter pdfWriter = new PDFWriter();
            //pdfWriter.addText( "<html><body>hiii</body></html>" );

            //String pdfcontent = pdfWriter.asString();
            //FileOutputStream pdfFile = new FileOutputStream( file );
            //pdfFile.write( pdfcontent.getBytes( "ISO-8859-1" ) );
            //pdfFile.close();
            // outputToFile( "helloworld.pdf", pdfcontent, "ISO-8859-1" );


        } catch (Exception e) {
            e.printStackTrace();
         //   return false;
        }*/
    }




    private void initViews(){
        tv_username = (TextView) findViewById( R.id.tv_username );
        tv_password = (TextView) findViewById( R.id.tv_password );
        et_username = (EditText) findViewById( R.id.et_username );
        et_password = (EditText) findViewById( R.id.et_password );
        bt_login = (Button) findViewById( R.id.bt_login );
        bt_sync_users = (Button) findViewById( R.id.bt_sync_users );
        bt_continue = (Button) findViewById( R.id.bt_continue );
        bt_logout = (Button) findViewById( R.id.bt_logout );
        ll_login_form = (LinearLayout) findViewById( R.id.ll_login_form );
        ll_logout_form = (LinearLayout) findViewById( R.id.ll_logout_form );
    }

    private void initDatabase(){
        sqldb = UtilSQLite.makeDatabase( DB_NAME, context );
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );

        UtilSQLite.executeQuery( sqldb, SQL_CREATE_USERS, true );
        UtilSQLite.executeQuery( sqldb, SQL_CREATE_ROLES_PRIVILEGES, true );
        UtilSQLite.executeQuery( sqldb, SQL_CREATE_SHOP, true );
        UtilSQLite.executeQuery( sqldb, SQL_CREATE_USER_SHOPS, true );
        UtilSQLite.executeQuery( sqldb, SQL_CREATE_CARTON, true );
        UtilSQLite.executeQuery( sqldb, SQL_CREATE_ITEM, true );
        UtilSQLite.executeQuery( sqldb, SQL_CREATE_BILLS, true );
        UtilSQLite.executeQuery( sqldb, UPDATE_TABLE, true );

        // String sql = "INSERT INTO users( `user_id`, `password`, `role_id` ) VALUES( '1', '2', '3' );";
        // UtilSQLite.executeQuery( sqldb, sql, true );

    }

    private void addEventsOnViews(){
        syncUsersDataEvent();

        loginButtonEvent();

        logoutButtonEvent();

        continueButtonEvent();
    }

    private void syncUsersDataEvent(){

        bt_sync_users.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View view ) {

                if( ! UtilNetwork.isConnectedToInternet( context ) ){
                    CustomToast.showCustomToast( context, "error", "You are not connected to the Internet ! Please connect to WiFi or Mobile data and try again !", 5000 );
                    return;
                }

                new AsyncTask<Void, String, String>() {

                        ProgressDialog progressDialog;

                        @Override
                        protected String doInBackground( Void... voids ) {
                            String webservice_url = UtilURL.getWebserviceURL( context );
                            Log.i( TAG, "Webservice path : "+ webservice_url );
                            String response = UtilNetwork.makeRequestForData( webservice_url, "POST",
                                    UtilURL.getURLParamsFromPairs( new String[][]{ { "what_do_you_want", "get_users_info" } } ), context );



                            return response;
                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();

                            progressDialog = new ProgressDialog( context );
                            progressDialog.setTitle( "Please wait..." );
                            progressDialog.setMessage( "Synchronizing" );
                            progressDialog.setCancelable( false );
                            progressDialog.show();

                        }

                        @Override
                        protected void onPostExecute( String s ) {
                            super.onPostExecute( s );

                            if( s == null ){
                                Log.d( TAG, "Null Received");
                                progressDialog.dismiss();
                                return;
                            }

                            processInitDataResponse( s );

                            progressDialog.dismiss();

                        }
                    }.execute();

                }

        });
    }

    private void loginButtonEvent(){

        bt_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Query for Login
                String username = et_username.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                if( username.equals( "" ) || password.equals( "" ) ){
                    CustomToast.showCustomToast( context, "error", "Username/Password cannot be empty", 5000 );
                    return;
                }

                password = MD5.getMD5String( password );
                Log.i( TAG, password );

                String sql = String.format( "SELECT * FROM users WHERE ( user_id='%s' ) AND ( password='%s' )", username, password );
                Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
                if( c.getCount() == 0 ){
                    CustomToast.showCustomToast( context, "error", "Username/Password incorrect. Connect to WiFi/Mobile-Data and click on Sync Users and try again !", 5000 );
                    return;
                }

                c.moveToNext();

                // This section will be reached when login is successful, so Save the Login state, and the Shop id, name
                UtilSharedPreferences.editSharedPreference( spfs, KEY_IS_LOGGED_IN, "1" );
                UtilSharedPreferences.editSharedPreference( spfs, KEY_ROLE_ID, c.getString( c.getColumnIndex( "role_id" ) ) );
                UtilSharedPreferences.editSharedPreference( spfs, KEY_USERNAME, c.getString( c.getColumnIndex( "user_id" ) ) );

                Intent in = new Intent( context, Decision.class );
                startActivity( in );
                finish();

            }

        });

    }

    private void logoutButtonEvent(){

        bt_logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                logout( context );
                ll_login_form.setVisibility( View.VISIBLE );
                ll_logout_form.setVisibility( View.GONE );
                CustomToast.showCustomToast( context, "success", "You have been logged out successfully !", 5000 );

            }

        });

    }

    private void continueButtonEvent(){

        bt_continue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent in = new Intent( context, Decision.class );
                startActivity( in );
                finish();

            }

        });

    }


    private void processInitDataResponse( String response ){

        JSONObject jsonObject = null;
        JSONArray jsonArray = null;

        try{
            jsonObject = new JSONObject( response );

            // 1. Fetch Users from the JSON
            jsonArray = jsonObject.getJSONArray( "users" );

            if( jsonArray.length() != 0 ){
                // Clear the existing users because we are over writing the users
                String sql = "DELETE FROM users";
                UtilSQLite.executeQuery( sqldb, sql, true );
            }

            for( int i = 0 ; i < jsonArray.length() ; i++ ){
                String sql = String.format( "INSERT INTO users( 'user_id', 'password', 'role_id' ) VALUES( '%s', '%s', '%s' )",
                        jsonArray.getJSONObject( i ).getString( "user_id" ),
                        jsonArray.getJSONObject( i ).getString( "password" ),
                        jsonArray.getJSONObject( i ).getString( "role_id" ) );
                UtilSQLite.executeQuery( sqldb, sql, true );
            }

            // 2. Fetch the roles_privileges
            jsonArray = jsonObject.getJSONArray( "roles_privileges" );

            if( jsonArray.length() != 0 ){
                // Clear the existing users because we are over writing the users
                String sql = "DELETE FROM roles_privileges";
                UtilSQLite.executeQuery( sqldb, sql, true );
            }

            for( int i = 0 ; i < jsonArray.length() ; i++ ){
                String sql = String.format( "INSERT INTO roles_privileges( 'role_id', 'privilege_id', 'privilege_name', 'functionality_name', 'plugin_id' ) VALUES( '%s', '%s', '%s', '%s', '%s' )",
                        jsonArray.getJSONObject( i ).getString( "role_id" ),
                        jsonArray.getJSONObject( i ).getString( "privilege_id" ),
                        jsonArray.getJSONObject( i ).getString( "privilege_name" ),
                        jsonArray.getJSONObject( i ).getString( "functionality_name" ),
                        jsonArray.getJSONObject( i ).getString( "plugin_id" ) );
                UtilSQLite.executeQuery( sqldb, sql, true );
            }

            // 3. Fetch the shop
            jsonArray = jsonObject.getJSONArray( "shop" );

            if( jsonArray.length() != 0 ){
                // Clear the existing users because we are over writing the users
                String sql = "DELETE FROM shop";
                UtilSQLite.executeQuery( sqldb, sql, true );
            }

            for( int i = 0 ; i < jsonArray.length() ; i++ ){
                String sql = String.format( "INSERT INTO shop( 'shop_id', 'shop_name' ) VALUES( '%s', '%s' )",
                        jsonArray.getJSONObject( i ).getString( "shop_id" ),
                        jsonArray.getJSONObject( i ).getString( "shop_name" ) );
                UtilSQLite.executeQuery( sqldb, sql, true );
            }

            // 4. Fetch the user_shops
            jsonArray = jsonObject.getJSONArray( "user_shops" );

            if( jsonArray.length() != 0 ){
                // Clear the existing users because we are over writing the users
                String sql = "DELETE FROM user_shops";
                UtilSQLite.executeQuery( sqldb, sql, true );
            }

            for( int i = 0 ; i < jsonArray.length() ; i++ ){
                String sql = String.format( "INSERT INTO user_shops( 'user_id', 'shop_id' ) VALUES( '%s', '%s' )",
                        jsonArray.getJSONObject( i ).getString( "user_id" ),
                        jsonArray.getJSONObject( i ).getString( "shop_id" ) );
                UtilSQLite.executeQuery( sqldb, sql, true );
            }

        }
        catch( Exception e ){
            e.printStackTrace();
        }

    }

    private void checkIfLoggedIn(){
        if( isLoggedIn( context ) ){
            ll_login_form.setVisibility( View.GONE );
            ll_logout_form.setVisibility( View.VISIBLE );
            Log.d( TAG, "Logged In !" );
        }
        else{
            ll_login_form.setVisibility( View.VISIBLE );
            ll_logout_form.setVisibility( View.GONE );
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        checkIfLoggedIn();
    }
}
