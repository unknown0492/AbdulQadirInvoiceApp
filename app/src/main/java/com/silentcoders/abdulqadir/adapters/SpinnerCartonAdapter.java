package com.silentcoders.abdulqadir.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.silentcoders.abdulqadir.invoiceapp.R;

/**
 * Created by Sohail on 17-01-2018.
 */

public class SpinnerCartonAdapter extends BaseAdapter {

    String carton_names[];
    String carton_temp_ids[];
    Context context;

    public SpinnerCartonAdapter( Context context, String carton_names[], String carton_temp_ids[] ){
        this.carton_names = carton_names;
        this.carton_temp_ids = carton_temp_ids;
        this.context = context;
    }

    @Override
    public int getCount() {
        return carton_names.length;
    }

    @Override
    public Object getItem( int i ) {
        return carton_names[ i ];
    }

    @Override
    public long getItemId( int i ) {
        return i;
    }

    public int getItemPositionByTag( String tempCartonID ){
        for( int i = 0 ; i <carton_temp_ids.length ; i++ ){
            if( tempCartonID.equals( carton_temp_ids[ i ] ) )
                return i;
        }
        return -1;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup viewGroup ) {
        View v = convertView;
        if ( v == null ) {
            LayoutInflater lif = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            v = lif.inflate( R.layout.spinner_carton_element, null );
        }
        TextView tv_carton_name = (TextView) v.findViewById( R.id.tv_carton_name );
        if ( tv_carton_name != null ) {
            tv_carton_name.setText( carton_names[ position ] );
            tv_carton_name.setTag( carton_temp_ids[ position ] );
        }

        return v;
    }
}
