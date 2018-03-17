package com.silentcoders.abdulqadir.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.silentcoders.abdulqadir.classes.Bill;
import com.silentcoders.abdulqadir.invoiceapp.R;

/**
 * Created by Sohail on 06-01-2018.
 */

public class BillAdapter extends BaseAdapter {

    int resId;
    Context context;
    Bill[] bills;

    public BillAdapter(){}

    public BillAdapter( Context context, int resId, Bill bills[] ){
        this.context = context;
        this.resId = resId;
        this.bills = bills;
    }

    @Override
    public int getCount() {
        return bills.length;
    }

    @Override
    public Object getItem(int i) {
        return bills[ i ];
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
            v = lif.inflate( R.layout.layout_bill_display_line, null );
        }
        Bill bill = bills[ position ];
        int sr_no = position + 1;
        if ( bill != null ) {
            TextView tv_sr_no = (TextView) v.findViewById( R.id.tv_sr_no );
            TextView tv_customer_name = (TextView) v.findViewById( R.id.tv_customer_name );
            TextView tv_place = (TextView) v.findViewById( R.id.tv_place );
            TextView tv_date = (TextView) v.findViewById( R.id.tv_date );
            TextView tv_bill_status = (TextView) v.findViewById( R.id.tv_bill_status );
            TextView tv_remarks = (TextView) v.findViewById( R.id.tv_remarks );
            TextView tv_total_cartons = (TextView) v.findViewById( R.id.tv_total_cartons );

            if ( tv_sr_no != null ) {
                tv_sr_no.setText( sr_no + ". " );
            }
            if( tv_customer_name != null ){
                tv_customer_name.setText( bill.getCustomerName() );
            }
            if( tv_place != null ){
                tv_place.setText( bill.getPlace() );
            }
            if( tv_date != null ){
                tv_date.setText( bill.getDate() );
            }
            if( tv_remarks != null ){
                tv_remarks.setText( bill.getRemarks() );
            }
            if( tv_bill_status != null ){
                tv_bill_status.setText( bill.getBillStatus() );
                if( bill.getBillStatus().equals( "complete" ) ) tv_bill_status.setTextColor( context.getResources().getColor( R.color.colorSuccess ) );
                else tv_bill_status.setTextColor( context.getResources().getColor( R.color.colorError ) );
            }
            if( tv_total_cartons != null ){
                tv_total_cartons.setText( bill.getTotalCartons() + " cartons" );
            }
        }
        return v;
    }
}
