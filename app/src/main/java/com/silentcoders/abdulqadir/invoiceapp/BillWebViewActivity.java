package com.silentcoders.abdulqadir.invoiceapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.silentcoders.classlibrary.UtilFile;
import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;

public class BillWebViewActivity extends AppCompatActivity {

    Context context = this;
    SharedPreferences spfs;
    SQLiteDatabase sqldb;
    final static String TAG = "BillWebViewActivity";

    String id = "";
    String bill_type = "";
    Cursor c;
    String party_name_html = "no_name.html";
    String party_name_pdf  = "no_name.pdf";

    float grand_total = 0;
    float packaging_total = 0;
    float extras_total = 0;

    WebView wv_open_page;

    String htmlData = "";
    File full_bill, agent_bill, discounted_bill, current_bill;
    File full_bill_pdf, agent_bill_pdf, discounted_bill_pdf, current_bill_pdf;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        //setContentView( R.layout.activity_bill_web_view );

        init();


    }

    private void init(){

        initViews();

    }

    private void initViews(){

        sqldb = UtilSQLite.makeDatabase( Constants.DB_NAME, context );
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );

        wv_open_page = new WebView( this );
        wv_open_page.getSettings().setJavaScriptEnabled( true );
        wv_open_page.getSettings().setAppCacheEnabled( false );
        //wv_open_page.set

        wv_open_page.setWebViewClient( new WebViewClient() {

            ProgressDialog p;

            @Override
            public void onPageStarted( WebView view, String url, Bitmap favicon ) {
                super.onPageStarted( view, url, favicon );
                //p.show();
            }

            @Override
            public void onPageFinished( WebView view, String url ) {
                super.onPageFinished( view, url );
                //p.hide();
            }

        });

        generateBillHTML();

    }

    public void generateBillHTML(){

        // First fetch, which type of bill is being expected
        Intent in = getIntent();
        bill_type = in.getStringExtra( "bill_type" );
        id = in.getStringExtra( "id" );


        String sql = String.format( "SELECT * FROM bills WHERE id='%s'", id );
        c = UtilSQLite.executeQuery( sqldb, sql, false );
        c.moveToNext();
        party_name_html = c.getString( c.getColumnIndex( "customer_name" ) ) + ".html";
        party_name_pdf  = c.getString( c.getColumnIndex( "customer_name" ) ) + ".pdf";

        if( bill_type.equals( "agent_bill" ) ){
            generateAgentBill();
        }
        else if( bill_type.equals( "full_bill" ) ){
            generateFullBill();
        }
        else if( bill_type.equals( "discounted_bill" ) ){
            generateDiscountedBill();
        }
    }

    public void generateAgentBill(){

        //agent_bill = UtilFile.createFileIfNotExist( "Calculator", "agent_bill.html" );
        //agent_bill_pdf = UtilFile.createFileIfNotExist( "Calculator", "agent_bill.pdf" );
        agent_bill = UtilFile.createFileIfNotExist( "Calculator", party_name_html );
        agent_bill_pdf = UtilFile.createFileIfNotExist( "Calculator", party_name_pdf );

        htmlData = "<html>" +
                "<head><title>Agent Bill</title>" +
                "<style>\n" +
                "        td, p, body{\n" +
                "            font-size: 8px;\n" +
                "        }\n" +
                "        </style>" +
                "</head>\n" +
                "<body>\n" +
                "        <table align=\"center\" style=\"border-collapse: collapse; font-family: Calibri; width: 100%; border: 1px solid grey; margin-top: 10px;\" border=\"1\">\n" +
                "            \n" +
                "            <tr style=\"font-size: 19px;\">\n" +
                "                <td style=\"padding:5px;\">\n" +
                "                    <p style=\"margin:3px\">"+LocalFunctions.convertMillisToDate( Long.parseLong( c.getString( c.getColumnIndex( "timestamp" ) ) ) )+"</p>\n" +
                "                    <p style=\"margin:3px\"><span style=\"font-weight: bold;\">"+ c.getString( c.getColumnIndex( "customer_name" ) ) + "</span> - " + c.getString( c.getColumnIndex( "place" ) ) + "</p>\n" +
                "                    <p style=\"margin:3px\"><span style=\"font-weight: bold;\">Remarks : </span>"+c.getString( c.getColumnIndex( "remarks" ) )+"</p>\n" +
                "                </td>                \n" +
                "            </tr>\n" +
                "            \n" +
                "        </table>\n" +
                "        " +
                "        <table align=\"center\" style=\"\tborder-collapse: collapse; font-family: Calibri; width: 100%; border: 1px solid grey; margin-top: 20px;\" border=\"1\">\n" +
                "            <thead>\n" +
                "                <tr style=\"font-size: 10px; background: #eee;\">\n" +
                "                    <th style=\"border: 1px solid grey; text-align: center ; padding: 3px;\">Sr. No.</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Quantity</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Item Name</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Price</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Total Price</th>\n" +
                "                </tr>\n" +
                "            </thead>\n" +
                "            <tbody>\n";

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
            int carton_pieces = 0;
            // This for loop is to iterate the Cartons
            for( int i = 0 ; i < jsonArray.length() ; i++ ){
                jsonObject = jsonArray.getJSONObject( i );
                //addCartonView( jsonObject );
                htmlData += "<tr style=\"font-size: 19px; \">\n" +
                        "                    <td colspan=\"5\" style=\"border: 1px solid grey; text-align: left; padding: 3px;\"><span style=\"font-weight: bold;\">"+ jsonObject.getString( "carton_name" ) + " " + jsonObject.getString( "sr_no" ) +" - </span><span style=\"font-size: 8px;\">Packaging cost Rs. "+ jsonObject.getString( "price" ) +"/-</span></td>\n" +
                        "                </tr>";
                carton_total = 0;

                // This for loop is to iterate the items inside the Current Carton
                // Iterate only till the Carton Temp ID matches with the Carton Temp id for the item
                for( jsonObject1 = jsonArray1.getJSONObject( j ); (j < jsonArray1.length()) && jsonObject.getString( "temp_carton_id" ).equals( jsonObject1.getString( "temp_carton_id" ) ) ;  ){

                    htmlData += "<tr style=\"font-size: 19px; \">\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "sr_no" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "qty" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "item_name" ) + jsonObject1.getString( "item_code" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "price" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "total_price" ) +"</td>\n" +
                            "                </tr>";
                    addItemPriceToGrandTotal( Float.parseFloat( jsonObject1.getString( "total_price" ) ) );
                    carton_total += Float.parseFloat( jsonObject1.getString( "total_price" ) );
                    carton_pieces += Integer.parseInt( jsonObject1.getString( "qty" ) );
                    //Log.d( TAG, jsonObject1.getString( "item_name" ) + ", "+jsonObject.getString( "carton_name" )  + ", " +j );
                    j++;
                    if( (j < jsonArray1.length()) )
                        jsonObject1 = jsonArray1.getJSONObject( j );
                }
                addPackingCharges( Float.parseFloat( jsonObject.getString( "price" ) ) );
                htmlData += "<tr style=\"font-size: 19px; \">\n" +
                        "                    <td colspan=\"2\" style=\"border: 1px solid grey; padding: 3px;\"><span style=\"font-weight: bold;\">"+ carton_pieces +"pcs</span></td>\n" +
                        "                    <td colspan=\"3\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\"><span style=\"font-weight: bold;\">Carton Total : Rs. "+ Math.round( carton_total ) +"/-</span></td>\n" +
                        "                </tr>";
                carton_pieces = 0;
            }

            htmlData += "                <tr style=\"font-size: 19px; \">\n" +
                    "                    <td colspan=\"5\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\"><span style=\"font-weight: bold;\">Total : Rs. "+ Math.round( getGrandTotal() ) +"/-</span></td>\n" +
                    "                </tr>\n" +

                    "                <tr style=\"font-size: 19px; \">\n" +
                    "                    <td colspan=\"5\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\"><span style=\"font-weight: bold;\">Packaging Charges : Rs. "+ Math.round( getPackagingTotal() ) +"/-</span></td>\n" +
                    "                </tr>\n";



            // Run a for loop to input the bill extras
            htmlData += "                <tr style=\"font-size: 19px; \">\n" +
                    "<td colspan=\"2\" style=\"padding-left:10px; border-right: 0px;\">\n" +
                    "                        <p style=\"margin: 5px 0 5px 0; \">\n" +
                    "                            <span style=\"padding-right: 5px; font-weight: bold;\">Salesman : </span>\n" +
                    "                            <span style=\"padding-right: 0px;\">"+ c.getString( c.getColumnIndex( "salesman" ) ) +"</span>\n" +
                    "                        </p>\n" +
                    "                        <p style=\"margin: 5px 0 5px 0; \">\n" +
                    "                            <span style=\"padding-right: 5px; font-weight: bold;\">Quotation By : </span>\n" +
                    "                            <span style=\"padding-right: 0px;\">"+ c.getString( c.getColumnIndex( "invoice_by" ) ) +"</span>\n" +
                    "                        </p>\n" +
                    "                        <p style=\"margin: 5px 0 5px 0; \">\n" +
                    "                            <span style=\"padding-right: 5px; font-weight: bold;\">Packed By : </span>\n" +
                    "                            <span style=\"padding-right: 0px;\">"+ c.getString( c.getColumnIndex( "packed_by" ) ) +"</span>\n" +
                    "                        </p>\n" +
                    "                    </td>" +
                    "                    <td colspan=\"3\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\">";
            for( int k = 0 ; k < jsonArray2.length() ; k++ ){
                jsonObject2 = jsonArray2.getJSONObject( k );

                String add_or_subtract = jsonObject2.getString( "add_or_subtract" );
                String str_add_or_subtract = "add +";
                String extras_charges = jsonObject2.getString( "charges" );

                if( add_or_subtract.equals( "subtract" ) ) {
                    str_add_or_subtract = "less -";
                    addToExtrasChargesTotal( -Float.parseFloat( extras_charges ) );
                    addToGrandTotal( -Float.parseFloat( extras_charges ) );
                }
                else{
                    addToExtrasChargesTotal( Float.parseFloat( extras_charges ) );
                    addToGrandTotal( Float.parseFloat( extras_charges ) );
                }

                htmlData += "<p style=\"margin: 5px 0 5px 0;\">\n" +
                        "                            <span style=\"padding-right: 40px;\">"+ jsonObject2.getString( "caption" ) +"</span>\n" +
                        "                            <span style=\"padding-right: 40px;\">"+ str_add_or_subtract +"</span>\n" +
                        "                            <span style=\"\">Rs. "+ jsonObject2.getString( "charges" ) +"/-</span>\n" +
                        "                        </p>";

                //Log.d( TAG, "hii" );

            }

            htmlData += "    </td>\n" +
                    "                </tr>";



        }
        catch ( Exception e ){
            e.printStackTrace();
        }

        // Add all the totals to the Grand Total
        addToGrandTotal( getPackagingTotal() );

        htmlData += "<tr style=\"font-size: 23px; \">\n" +
                "                    <td colspan=\"5\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\"><span style=\"font-weight: bold;\">Grand Total : Rs. "+ Math.round( getGrandTotal() ) +"/-</span></td>\n" +
                "                </tr>";

        htmlData += "</tbody>\n" +
                "        </table>\n" +
                "        \n" +
                "        \n" +
                "    </body>\n" +
                "</html>";

        UtilFile.saveDataToFile( agent_bill, htmlData );
        createPDF( agent_bill_pdf, htmlData );

        Log.d( TAG, "file://" + agent_bill.getAbsolutePath().toString() );

        wv_open_page.loadUrl( "file://" + agent_bill.getAbsolutePath().toString() );

        setContentView( wv_open_page );

        current_bill = agent_bill;
        current_bill_pdf = agent_bill_pdf;
    }

    public void generateFullBill(){

        // full_bill = UtilFile.createFileIfNotExist( "Calculator", "full_bill.html" );
        //full_bill_pdf = UtilFile.createFileIfNotExist( "Calculator", "full_bill.pdf" );
        full_bill = UtilFile.createFileIfNotExist( "Calculator", party_name_html );
        full_bill_pdf = UtilFile.createFileIfNotExist( "Calculator", party_name_pdf );

        htmlData = "<html>" +
                "<head><title>Full Bill</title>" +
                "<style>\n" +
                "        td, th, p, body{\n" +
                "            font-size: 8px;\n" +
                "        }\n" +
                "        </style>" +
                "</head>\n" +
                "<body>\n" +
                "<h2 align=\"center\">Quotation</h2>" +
                "        <table align=\"center\" style=\"border-collapse: collapse; font-family: Calibri; width: 100%; border: 1px solid grey; margin-top: 10px;\" border=\"1\">\n" +
                "            \n" +
                "            <tr >\n" +
                "                <td style=\"padding:15px;\" >\n" +
                "                    <p style=\"margin:0px\" style=\"font-size: 12px;\">"+LocalFunctions.convertMillisToDate( Long.parseLong( c.getString( c.getColumnIndex( "timestamp" ) ) ) )+"</p>\n" +
                "                    <p style=\"margin:0px\" style=\"font-size: 12px;\"><span style=\"font-weight: bold;\">"+ c.getString( c.getColumnIndex( "customer_name" ) ) + "</span> - " + c.getString( c.getColumnIndex( "place" ) ) + "</p>\n" +
                "                    <p style=\"margin:0px\" style=\"font-size: 12px;\"><span style=\"font-weight: bold;\">Remarks : </span>"+c.getString( c.getColumnIndex( "remarks" ) )+"</p>\n" +
                "                </td>                \n" +
                "            </tr>\n" +
                "            \n" +
                "        </table>\n" +
                "        " +
                "        <table align=\"center\" style=\"\tborder-collapse: collapse; font-family: Calibri; width: 100%; border: 1px solid grey; margin-top: 20px;\" border=\"1\">\n" +
                "            <thead>\n" +
                "                <tr style=\"font-size: 10px; background: #eee;\">\n" +
                "                    <th style=\"border: 1px solid grey; text-align: center ; padding: 3px;\">Sr. No.</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Quantity</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Item Name</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Price</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Discount</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Final Price</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Total Price</th>\n" +
                "                </tr>\n" +
                "            </thead>\n" +
                "            <tbody>\n";

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
            int carton_pieces = 0;
            // This for loop is to iterate the Cartons
            for( int i = 0 ; i < jsonArray.length() ; i++ ){
                jsonObject = jsonArray.getJSONObject( i );
                //addCartonView( jsonObject );
                htmlData += "<tr style=\"font-size: 19px; \">\n" +
                        "                    <td colspan=\"7\" style=\"border: 1px solid grey; text-align: left; padding: 3px;\"><span style=\"font-weight: bold;\">"+ jsonObject.getString( "carton_name" ) + " " + jsonObject.getString( "sr_no" ) +" - </span><span style=\"font-size: 8px;\">Packaging cost Rs. "+ jsonObject.getString( "price" ) +"/-</span></td>\n" +
                        "                </tr>";
                carton_total = 0;
                carton_pieces = 0;

                // This for loop is to iterate the items inside the Current Carton
                // Iterate only till the Carton Temp ID matches with the Carton Temp id for the item
                for( jsonObject1 = jsonArray1.getJSONObject( j ); (j < jsonArray1.length()) && jsonObject.getString( "temp_carton_id" ).equals( jsonObject1.getString( "temp_carton_id" ) ) ;  ){

                    htmlData += "<tr style=\"font-size: 19px; \">\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "sr_no" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "qty" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "item_name" ) + jsonObject1.getString( "item_code" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "price" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "discount" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "final_price" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "total_final_price" ) +"</td>\n" +
                            "                </tr>";
                    addItemPriceToGrandTotal( Float.parseFloat( jsonObject1.getString( "total_final_price" ) ) );
                    carton_total += Float.parseFloat( jsonObject1.getString( "total_final_price" ) );
                    carton_pieces += Integer.parseInt( jsonObject1.getString( "qty" ) );

                    //Log.d( TAG, jsonObject1.getString( "item_name" ) + ", "+jsonObject.getString( "carton_name" )  + ", " +j );
                    j++;
                    if( (j < jsonArray1.length()) )
                        jsonObject1 = jsonArray1.getJSONObject( j );
                }
                addPackingCharges( Float.parseFloat( jsonObject.getString( "price" ) ) );
                htmlData += "<tr style=\"font-size: 19px; \">\n" +
                        "                    <td colspan=\"3\" style=\"border: 1px solid grey; padding: 3px;\"><span style=\"font-weight: bold;\">"+ carton_pieces +"pcs</span></td>\n" +
                        "                    <td colspan=\"4\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\"><span style=\"font-weight: bold;\">Carton Total : Rs. "+ Math.round( carton_total ) +"/-</span></td>\n" +
                        "                </tr>";

            }

            htmlData += "                <tr style=\"font-size: 19px; \">\n" +
                    "                    <td colspan=\"7\" style=\"border: 1px solid grey; text-align: right; padding: 3px; font-size: 12px;\"><span style=\"font-weight: bold;\">Total : Rs. "+ Math.round( getGrandTotal() ) +"/-</span></td>\n" +
                    "                </tr>\n" +

                    "                <tr style=\"font-size: 19px; \">\n" +
                    "                    <td colspan=\"7\" style=\"border: 1px solid grey; text-align: right; padding: 3px; font-size: 12px;\"><span style=\"font-weight: bold;\">Packaging Charges : Rs. "+ Math.round( getPackagingTotal() ) +"/-</span></td>\n" +
                    "                </tr>\n";



            // Run a for loop to input the bill extras
            htmlData += "                <tr style=\"font-size: 19px; \">\n" +
                    "<td colspan=\"3\" style=\"padding-left:10px; padding-top: 10px border-right: 0px;\">\n" +
                    "                        <p style=\"margin: 5px 0 5px 0; font-size: 12px;\">\n" +
                    "                            <span style=\"padding-right: 5px; font-weight: bold;\">Salesman : </span>\n" +
                    "                            <span style=\"padding-right: 0px;\">"+ c.getString( c.getColumnIndex( "salesman" ) ) +"</span>\n" +
                    "                        </p>\n" +
                    "                        <p style=\"margin: 5px 0 5px 0; font-size: 12px \">\n" +
                    "                            <span style=\"padding-right: 5px; font-weight: bold;\">Quotation By : </span>\n" +
                    "                            <span style=\"padding-right: 0px;\">"+ c.getString( c.getColumnIndex( "invoice_by" ) ) +"</span>\n" +
                    "                        </p>\n" +
                    "                        <p style=\"margin: 5px 0 5px 0; font-size: 12px \">\n" +
                    "                            <span style=\"padding-right: 5px; font-weight: bold;\">Packed By : </span>\n" +
                    "                            <span style=\"padding-right: 0px;\">"+ c.getString( c.getColumnIndex( "packed_by" ) ) +"</span>\n" +
                    "                        </p>\n" +
                    "                    </td>" +
                    "                    <td colspan=\"4\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\">";
            for( int k = 0 ; k < jsonArray2.length() ; k++ ){
                jsonObject2 = jsonArray2.getJSONObject( k );

                String add_or_subtract = jsonObject2.getString( "add_or_subtract" );
                String str_add_or_subtract = "add +";
                String extras_charges = jsonObject2.getString( "charges" );

                if( add_or_subtract.equals( "subtract" ) ) {
                    str_add_or_subtract = "less -";
                    addToExtrasChargesTotal( -Float.parseFloat( extras_charges ) );
                    addToGrandTotal( -Float.parseFloat( extras_charges ) );
                }
                else{
                    addToExtrasChargesTotal( Float.parseFloat( extras_charges ) );
                    addToGrandTotal( Float.parseFloat( extras_charges ) );
                }

                htmlData += "<p style=\"margin: 5px 0 5px 0; font-size: 12px\">\n" +
                        "                            <span style=\"\">"+ jsonObject2.getString( "caption" ) +" : </span>\n" +
                        "                            <span style=\"\">"+ str_add_or_subtract +"</span>\n" +
                        "                            <span style=\"\">Rs. "+ jsonObject2.getString( "charges" ) +"/-</span>\n" +
                        "                        </p>";

                //Log.d( TAG, "hii" );

            }

            htmlData += "    </td>\n" +
                    "                </tr>";



        }
        catch ( Exception e ){
            e.printStackTrace();
        }

        // Add all the totals to the Grand Total
        addToGrandTotal( getPackagingTotal() );

        htmlData += "<tr style=\"font-size: 23px; \">\n" +
                "                    <td colspan=\"7\" style=\"border: 1px solid grey; text-align: right; padding: 3px; font-size: 12px;\"><span style=\"font-weight: bold;\">Grand Total : Rs. "+ Math.round( getGrandTotal() ) +"/-</span></td>\n" +
                "                </tr>";

        htmlData += "</tbody>\n" +
                "        </table>\n" +
                "        \n" +
                "        \n" +
                "    </body>\n" +
                "</html>";

        UtilFile.saveDataToFile( full_bill, htmlData );
        createPDF( full_bill_pdf, htmlData );

        Log.d( TAG, "file://" + full_bill.getAbsolutePath().toString() );

        wv_open_page.loadUrl( "file://" + full_bill.getAbsolutePath().toString() );

        setContentView( wv_open_page );

        current_bill = full_bill;
        current_bill_pdf = full_bill_pdf;

    }

    public void generateDiscountedBill(){

        /*discounted_bill = UtilFile.createFileIfNotExist( "Calculator", "discounted_bill.html" );
        discounted_bill_pdf = UtilFile.createFileIfNotExist( "Calculator", "discounted_bill.pdf" );*/
        discounted_bill = UtilFile.createFileIfNotExist( "Calculator", party_name_html );
        discounted_bill_pdf = UtilFile.createFileIfNotExist( "Calculator", party_name_pdf );

        htmlData = "<html>" +
                "<head><title>Discounted Bill</title>" +
                "<style>\n" +
                "        td, p, body{\n" +
                "            font-size: 8px;\n" +
                "        }\n" +
                "        </style>" +
                "</head>\n" +
                "<body>\n" +
                "        <table align=\"center\" style=\"border-collapse: collapse; font-family: Calibri; width: 100%; border: 1px solid grey; margin-top: 10px;\" border=\"1\">\n" +
                "            \n" +
                "            <tr style=\"font-size: 19px;\">\n" +
                "                <td style=\"padding:5px;\">\n" +
                "                    <p style=\"margin:3px\">"+LocalFunctions.convertMillisToDate( Long.parseLong( c.getString( c.getColumnIndex( "timestamp" ) ) ) )+"</p>\n" +
                "                    <p style=\"margin:3px\"><span style=\"font-weight: bold;\">"+ c.getString( c.getColumnIndex( "customer_name" ) ) + "</span> - " + c.getString( c.getColumnIndex( "place" ) ) + "</p>\n" +
                "                    <p style=\"margin:3px\"><span style=\"font-weight: bold;\">Remarks : </span>"+c.getString( c.getColumnIndex( "remarks" ) )+"</p>\n" +
                "                </td>                \n" +
                "            </tr>\n" +
                "            \n" +
                "        </table>\n" +
                "        " +
                "        <table align=\"center\" style=\"\tborder-collapse: collapse; font-family: Calibri; width: 100%; border: 1px solid grey; margin-top: 20px;\" border=\"1\">\n" +
                "            <thead>\n" +
                "                <tr style=\"font-size: 10px; background: #eee;\">\n" +
                "                    <th style=\"border: 1px solid grey; text-align: center ; padding: 3px;\">Sr. No.</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Quantity</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Item Name</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Final Price</th>\n" +
                "                    <th style=\"border: 1px solid grey;text-align: center; padding: 3px;\">Total Price</th>\n" +
                "                </tr>\n" +
                "            </thead>\n" +
                "            <tbody>\n";

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
            int carton_pieces = 0;
            // This for loop is to iterate the Cartons
            for( int i = 0 ; i < jsonArray.length() ; i++ ){
                carton_pieces = 0;
                jsonObject = jsonArray.getJSONObject( i );
                //addCartonView( jsonObject );
                htmlData += "<tr style=\"font-size: 19px; \">\n" +
                        "                    <td colspan=\"5\" style=\"border: 1px solid grey; text-align: left; padding: 3px;\"><span style=\"font-weight: bold;\">"+ jsonObject.getString( "carton_name" ) + " " + jsonObject.getString( "sr_no" ) +" - </span><span style=\"font-size: 8px;\">Packaging cost Rs. "+ jsonObject.getString( "price" ) +"/-</span></td>\n" +
                        "                </tr>";
                carton_total = 0;

                // This for loop is to iterate the items inside the Current Carton
                // Iterate only till the Carton Temp ID matches with the Carton Temp id for the item
                for( jsonObject1 = jsonArray1.getJSONObject( j ); (j < jsonArray1.length()) && jsonObject.getString( "temp_carton_id" ).equals( jsonObject1.getString( "temp_carton_id" ) ) ;  ){

                    htmlData += "<tr style=\"font-size: 19px; \">\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "sr_no" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "qty" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "item_name" ) + jsonObject1.getString( "item_code" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "final_price" ) +"</td>\n" +
                            "                    <td style=\"border: 1px solid grey; text-align: center; padding: 3px;\">"+ jsonObject1.getString( "total_final_price" ) +"</td>\n" +
                            "                </tr>";
                    addItemPriceToGrandTotal( Float.parseFloat( jsonObject1.getString( "total_final_price" ) ) );
                    carton_total += Float.parseFloat( jsonObject1.getString( "total_final_price" ) );
                    carton_pieces += Integer.parseInt( jsonObject1.getString( "qty" ) );
                    //Log.d( TAG, jsonObject1.getString( "item_name" ) + ", "+jsonObject.getString( "carton_name" )  + ", " +j );
                    j++;
                    if( (j < jsonArray1.length()) )
                        jsonObject1 = jsonArray1.getJSONObject( j );
                }
                addPackingCharges( Float.parseFloat( jsonObject.getString( "price" ) ) );
                htmlData += "<tr style=\"font-size: 19px; \">\n" +
                        "                    <td colspan=\"2\" style=\"border: 1px solid grey; padding: 3px;\"><span style=\"font-weight: bold;\">"+ carton_pieces +"pcs</span></td>\n" +
                        "                    <td colspan=\"3\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\"><span style=\"font-weight: bold;\">Carton Total : Rs. "+ Math.round( carton_total ) +"/-</span></td>\n" +
                        "                </tr>";

            }

            htmlData += "                <tr style=\"font-size: 19px; \">\n" +
                    "                    <td colspan=\"5\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\"><span style=\"font-weight: bold;\">Total : Rs. "+ Math.round( getGrandTotal() ) +"/-</span></td>\n" +
                    "                </tr>\n" +

                    "                <tr style=\"font-size: 19px; \">\n" +
                    "                    <td colspan=\"5\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\"><span style=\"font-weight: bold;\">Packaging Charges : Rs. "+ Math.round( getPackagingTotal() ) +"/-</span></td>\n" +
                    "                </tr>\n";



            // Run a for loop to input the bill extras
            htmlData += "                <tr style=\"font-size: 19px; \">\n" +
                    "<td colspan=\"2\" style=\"padding-left:10px; border-right: 0px;\">\n" +
                    "                        <p style=\"margin: 5px 0 5px 0; \">\n" +
                    "                            <span style=\"padding-right: 5px; font-weight: bold;\">Salesman : </span>\n" +
                    "                            <span style=\"padding-right: 0px;\">"+ c.getString( c.getColumnIndex( "salesman" ) ) +"</span>\n" +
                    "                        </p>\n" +
                    "                        <p style=\"margin: 5px 0 5px 0; \">\n" +
                    "                            <span style=\"padding-right: 5px; font-weight: bold;\">Quotation By : </span>\n" +
                    "                            <span style=\"padding-right: 0px;\">"+ c.getString( c.getColumnIndex( "invoice_by" ) ) +"</span>\n" +
                    "                        </p>\n" +
                    "                        <p style=\"margin: 5px 0 5px 0; \">\n" +
                    "                            <span style=\"padding-right: 5px; font-weight: bold;\">Packed By : </span>\n" +
                    "                            <span style=\"padding-right: 0px;\">"+ c.getString( c.getColumnIndex( "packed_by" ) ) +"</span>\n" +
                    "                        </p>\n" +
                    "                    </td>" +
                    "                    <td colspan=\"3\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\">";
            for( int k = 0 ; k < jsonArray2.length() ; k++ ){
                jsonObject2 = jsonArray2.getJSONObject( k );

                String add_or_subtract = jsonObject2.getString( "add_or_subtract" );
                String str_add_or_subtract = "add +";
                String extras_charges = jsonObject2.getString( "charges" );

                if( add_or_subtract.equals( "subtract" ) ) {
                    str_add_or_subtract = "less -";
                    addToExtrasChargesTotal( -Float.parseFloat( extras_charges ) );
                    addToGrandTotal( -Float.parseFloat( extras_charges ) );
                }
                else{
                    addToExtrasChargesTotal( Float.parseFloat( extras_charges ) );
                    addToGrandTotal( Float.parseFloat( extras_charges ) );
                }

                htmlData += "<p style=\"margin: 5px 0 5px 0;\">\n" +
                        "                            <span style=\"padding-right: 40px;\">"+ jsonObject2.getString( "caption" ) +"</span>\n" +
                        "                            <span style=\"padding-right: 40px;\">"+ str_add_or_subtract +"</span>\n" +
                        "                            <span style=\"\">Rs. "+ jsonObject2.getString( "charges" ) +"/-</span>\n" +
                        "                        </p>";

                //Log.d( TAG, "hii" );

            }

            htmlData += "    </td>\n" +
                    "                </tr>";



        }
        catch ( Exception e ){
            e.printStackTrace();
        }

        // Add all the totals to the Grand Total
        addToGrandTotal( getPackagingTotal() );

        htmlData += "<tr style=\"font-size: 23px; \">\n" +
                "                    <td colspan=\"5\" style=\"border: 1px solid grey; text-align: right; padding: 3px;\"><span style=\"font-weight: bold;\">Grand Total : Rs. "+ Math.round( getGrandTotal() ) +"/-</span></td>\n" +
                "                </tr>";

        htmlData += "</tbody>\n" +
                "        </table>\n" +
                "        \n" +
                "        \n" +
                "    </body>\n" +
                "</html>";

        UtilFile.saveDataToFile( discounted_bill, htmlData );
        createPDF( discounted_bill_pdf, htmlData );

        Log.d( TAG, "file://" + discounted_bill.getAbsolutePath().toString() );

        wv_open_page.loadUrl( "file://" + discounted_bill.getAbsolutePath().toString() );

        setContentView( wv_open_page );

        current_bill = discounted_bill;
        current_bill_pdf = discounted_bill_pdf;
    }

    public void createPDF( File file, String htmlText ){
        try {
            Document document = new Document( PageSize.LETTER );
            document.setMargins( 30f, 30f, 30f, 30f );
            PdfWriter pdfWriter = PdfWriter.getInstance( document, new FileOutputStream( file.getAbsolutePath() ) );
            document.open();

            InputStream inputStream = new ByteArrayInputStream( htmlText.getBytes() );
            XMLWorkerHelper.getInstance().parseXHtml( pdfWriter, document, inputStream, null, Charset.forName( "UTF-8" ) );

            document.close();

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        wv_open_page.clearCache( true );

        context.deleteDatabase( "webview.db" );
        context.deleteDatabase( "webviewCache.db" );

        File dir = getCacheDir();

        if ( dir != null && dir.isDirectory() ){
            try{
                File[] children = dir.listFiles();
                if ( children.length > 0 ){
                    for ( int i = 0; i < children.length; i++ ){
                        File[] temp = children[ i ].listFiles();
                        for ( int x = 0; x < temp.length; x++ ){
                            temp[ x ].delete();
                        }
                    }
                }
            }
            catch ( Exception e ){
                Log.e( TAG, "failed cache clean" );
            }
        }
        Log.i( TAG, "onDestroy()" );
    }

    public float addItemPriceToGrandTotal( float value ){
        grand_total += value;
        return grand_total;
    }

    public float addPackingCharges( float value ){
        packaging_total += value;
        return packaging_total;
    }

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
        return grand_total;
    }

    public float subtractFromGrandTotal( float value ){
        grand_total -= value;
        return grand_total;
    }

    public float getGrandTotal(){
        return grand_total;
    }
    // Add/Less to Grand Total

    public float getPackagingTotal(){
        return packaging_total;
    }



    @Override
    protected void onResume() {
        super.onResume();

        Intent in = getIntent();
        id = in.getStringExtra( "id" );
        bill_type = in.getStringExtra( "bill_type" );

    }

    @SuppressLint("NewApi")
    public void print(){

        Intent intentShareFile = new Intent( Intent.ACTION_SEND );
        //intentShareFile.setType( "text/html" );
        intentShareFile.setType( "application/pdf" );
        intentShareFile.putExtra( Intent.EXTRA_STREAM, Uri.parse( "file://" + current_bill_pdf.getAbsolutePath().toString() ) );

        // intentShareFile.putExtra( Intent.EXTRA_SUBJECT, "Sharing File..." );
        // intentShareFile.putExtra( Intent.EXTRA_TEXT, "Sharing File..." );

        startActivity( Intent.createChooser( intentShareFile, "Quotation" ) );

    }

    // menu
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_view_bill_print, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        switch( item.getItemId() ){

            case android.R.id.home:
                Intent in = new Intent( context, BillViewActivity.class );
                in.putExtra( "id", id );
                startActivity( in );
                finish();
                return true;

            case R.id.menu_print_bill:
                print();
                return true;

        }
        return( super.onOptionsItemSelected( item ) );
    }
}
