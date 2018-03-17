package com.silentcoders.abdulqadir.invoiceapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silentcoders.classlibrary.UtilSQLite;
import com.silentcoders.classlibrary.UtilSharedPreferences;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_IS_LOGGED_IN;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_ROLE_ID;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.KEY_USERNAME;
import static com.silentcoders.abdulqadir.invoiceapp.Constants.SPFS_INFO;

/**
 * Created by Sohail on 29-12-2017.
 */

public class LocalFunctions {

    static SharedPreferences spfs;
    final static String TAG = "LocalFunctions";

    public static boolean isLoggedIn( Context context ){
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );
        String is_logged_in = String.valueOf( UtilSharedPreferences.getSharedPreference( spfs, KEY_IS_LOGGED_IN, "0" ) );
        return is_logged_in.equals( "1" )?true:false;
    }

    public static void logout( Context context ){
        spfs = UtilSharedPreferences.createSharedPreference( context, SPFS_INFO );
        UtilSharedPreferences.editSharedPreference( spfs, KEY_IS_LOGGED_IN, "0" );
        UtilSharedPreferences.editSharedPreference( spfs, KEY_ROLE_ID, "-1" );
        UtilSharedPreferences.editSharedPreference( spfs, KEY_USERNAME, "-1" );
    }

    public static boolean havePrivilege( SQLiteDatabase sqldb, SharedPreferences spfs, String function_name ){
        String role_id = String.valueOf( UtilSharedPreferences.getSharedPreference( spfs, KEY_ROLE_ID, "-1" ) );

        String sql = String.format( "SELECT * FROM roles_privileges WHERE (role_id='%s') AND (functionality_name='%s')", role_id, function_name );
        // Log.d( TAG, sql );
        Cursor c = UtilSQLite.executeQuery( sqldb, sql, false );
        if( c.getCount() != 0 )
            return true;

        return false;
    }

    public static String generateRandomPrimaryKey( int length ){
        String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Set<String> identifiers = new HashSet<String>();
        java.util.Random rand = new java.util.Random();
        StringBuilder builder = new StringBuilder();
        while( builder.toString().length() == 0 ) {
            //int len = rand.nextInt(length)+length;
            for( int i = 0; i < length; i++ ) {
                builder.append( lexicon.charAt( rand.nextInt( lexicon.length() ) ) );
            }
            if( identifiers.contains( builder.toString() ) ) {
                builder = new StringBuilder();
            }
        }
        return builder.toString();
    }

    public static long convertDateToMillis( String date ){

        String[] d = date.split( "-" );
        int day = Integer.parseInt( d[ 0 ] );
        int month = Integer.parseInt( d[ 1 ] );
        int year = Integer.parseInt( d[ 2 ] );

        Calendar cal = Calendar.getInstance();
        cal.set( year, month-1, day );

        return cal.getTimeInMillis();
    }

    public static String convertMillisToDate( long millis ){

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( millis );

        int day = cal.get( Calendar.DATE );
        int month = cal.get( Calendar.MONTH ) + 1;
        int year = cal.get( Calendar.YEAR );

        String date = String.format( "%d-%d-%d", day, month, year );

        return date;
    }

}
