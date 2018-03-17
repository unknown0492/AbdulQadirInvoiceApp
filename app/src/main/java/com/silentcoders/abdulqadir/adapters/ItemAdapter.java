package com.silentcoders.abdulqadir.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.silentcoders.abdulqadir.classes.Item;
import com.silentcoders.abdulqadir.invoiceapp.R;

/**
 * Created by Sohail on 06-01-2018.
 */

public class ItemAdapter extends BaseAdapter {

    int resId;
    Context context;
    Item[] items;

    public ItemAdapter(){}

    public ItemAdapter( Context context, int resId, Item items[] ){
        this.context = context;
        this.resId = resId;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int i) {
        return items[ i ];
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
            v = lif.inflate( R.layout.layout_item_element, null );
        }
        Item item = items[ position ];
        int sr_no = position + 1;
        if ( item != null ) {
            TextView tv_sr_no = (TextView) v.findViewById( R.id.tv_sr_no );
            TextView tv_item_name = (TextView) v.findViewById( R.id.tv_item_name );
            TextView tv_item_code = (TextView) v.findViewById( R.id.tv_item_code );
            TextView tv_item_price = (TextView) v.findViewById( R.id.tv_item_price );
            TextView tv_discount = (TextView) v.findViewById( R.id.tv_discount );
            TextView tv_final_price = (TextView) v.findViewById( R.id.tv_final_price );
            TextView tv_qty_type = (TextView) v.findViewById( R.id.tv_qty_type );

            if ( tv_sr_no != null ) {
                tv_sr_no.setText( sr_no + ". " );
            }
            if( tv_item_name != null ){
                tv_item_name.setText( item.getItemName() );
            }
            if( tv_item_code != null ){
                tv_item_code.setText( item.getItemCode() );
            }
            if( tv_item_price != null ){
                tv_item_price.setText( "Price : " + item.getPrice() );
            }
            if( tv_discount != null ){
                tv_discount.setText( "Discount : " + item.getDiscount() );
            }
            if( tv_final_price != null ){
                tv_final_price.setText( "Final Price : " + item.getFinalPrice() );
            }
            if( tv_qty_type != null ){
                tv_qty_type.setText( item.getQuantityType() );
            }
        }
        return v;
    }
}
