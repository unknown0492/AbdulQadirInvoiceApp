package com.silentcoders.abdulqadir.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.silentcoders.abdulqadir.classes.Carton;
import com.silentcoders.abdulqadir.invoiceapp.R;

/**
 * Created by Sohail on 06-01-2018.
 */

public class CartonAdapter extends BaseAdapter {

    int resId;
    Context context;
    Carton[] cartons;

    public CartonAdapter(){}

    public CartonAdapter( Context context, int resId, Carton cartons[] ){
        this.context = context;
        this.resId = resId;
        this.cartons = cartons;
    }

    @Override
    public int getCount() {
        return cartons.length;
    }

    @Override
    public Object getItem(int i) {
        return cartons[ i ];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup viewGroup ) {
        View v = convertView;
        if ( v == null ) {
            LayoutInflater lif = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            v = lif.inflate( R.layout.layout_carton_element, null );
        }
        Carton carton = cartons[ position ];
        if ( carton != null ) {
            TextView tv_carton_name = (TextView) v.findViewById( R.id.tv_carton_name );
            TextView tv_carton_price = (TextView) v.findViewById( R.id.tv_carton_price );
            if ( tv_carton_name != null ) {
                tv_carton_name.setText( carton.getCartonName() );                            }
            if( tv_carton_price != null ){
                tv_carton_price.setText( "Rs. "+ carton.getCartonPrice() + "/-");
            }
        }
        return v;
    }
}
