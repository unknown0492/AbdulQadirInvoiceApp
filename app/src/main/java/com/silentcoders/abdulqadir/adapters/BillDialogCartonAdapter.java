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
 * Created by Sohail on 11-01-2018.
 */

public class BillDialogCartonAdapter extends BaseAdapter {

    Context context;
    Carton[] cartons;

    public BillDialogCartonAdapter(){}

    public BillDialogCartonAdapter( Context context, Carton[] cartons ){
        this.context = context;
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
        LayoutInflater layoutInflater;
        TextView rb = (TextView) convertView;

        if( convertView == null ){
            layoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            rb = (TextView)layoutInflater.inflate( R.layout.radio_button_layout, null );
        }

        Carton carton = cartons[ position ];
        if ( carton != null ) {
            rb.setText( carton.getCartonName() + " - Rs. "+ carton.getCartonPrice() + "/-");
        }


        return rb;
    }

}
