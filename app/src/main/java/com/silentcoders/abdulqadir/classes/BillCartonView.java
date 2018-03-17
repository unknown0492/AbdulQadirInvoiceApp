package com.silentcoders.abdulqadir.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.silentcoders.abdulqadir.invoiceapp.R;

import java.util.Vector;

/**
 * Created by Sohail on 11-01-2018.
 */

public class BillCartonView extends LinearLayout implements Cloneable {

    Context context;
    LayoutInflater layoutInflater;

    View bill_carton_placement, bill_item_placement, bill_carton_total_placement;
    LinearLayout ll_carton;

    TextView tv_carton_sr_no, tv_carton_name, tv_carton_total, tv_total_pieces, tv_is_despatched;

    BillCarton carton;
    float cartonTotal = 0;
    int piecesTotal = 0;
    String tempID;

    Vector<BillItem> billItemVector;

    public BillCartonView( Context context ) {
        super( context );
        this.context = context;

        layoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        bill_carton_placement = layoutInflater.inflate( R.layout.bill_carton_placement, null );
        bill_item_placement = layoutInflater.inflate( R.layout.bill_item_placement, null );
        bill_carton_total_placement = layoutInflater.inflate( R.layout.bill_carton_total_placement, null );

        carton = new BillCarton();

        ll_carton = new LinearLayout( context );
        ll_carton.setOrientation( LinearLayout.VERTICAL );
        ll_carton.addView( bill_carton_placement );
        tv_carton_name = (TextView) ll_carton.findViewById( R.id.tv_carton_name );
        tv_carton_sr_no = (TextView) ll_carton.findViewById( R.id.tv_carton_sr_no );
        //tv_is_despatched = (TextView) ll_carton.findViewById( R.id.tv_is_despatched );

        ll_carton.addView( bill_carton_total_placement );
        tv_carton_total = (TextView) ll_carton.findViewById( R.id.tv_carton_total );
        tv_carton_total.setText( "Rs. 0" );

        tv_total_pieces = (TextView) ll_carton.findViewById( R.id.tv_total_pieces );
        tv_total_pieces.setText( "0 pieces" );

        //tv_is_despatched.setText( "no" );

        billItemVector = new Vector<BillItem>();

    }

    public void addItemAtPosition( View view, BillItem billItem ){
        ll_carton.removeView( bill_carton_total_placement );
        ll_carton.addView( view );
        ll_carton.addView( bill_carton_total_placement );
        billItemVector.add( billItem );
    }


    public void setItemAtPosition( View view, int position ){
        ll_carton.removeViewAt( position );
        ll_carton.addView( view, position );
    }

    public View getItemAtPosition( int position ){
        return ll_carton.getChildAt( position );
    }

    public void removeItemAtPosition( int position ){
        ll_carton.removeViewAt( position );
        billItemVector.removeElementAt( position - 1 );
    }


    public void setItemDataAtPosition( BillItem billItem, int position ){
        billItemVector.set( position, billItem );
    }

    public BillItem getItemDataAtPosition( int position ){
        return billItemVector.elementAt( position );
    }



    public BillCarton getCarton(){
        return carton;
    }

    public void setCartonName( String cartonName ){
        carton.setCartonName( cartonName );
        tv_carton_name.setText( cartonName );
    }

    public String getCartonName(){
        return carton.getCartonName();
    }

    public void setCartonSrNo( String sr_no ){
        carton.setCartonSrNo( sr_no );
        tv_carton_sr_no.setText( sr_no );
    }

    public String getCartonSrNo(){
        return carton.getCartonSrNo();
    }





    // Listeners on Cartons and Items
    public void setCartonLongClickListener( OnLongClickListener olcl ){
        bill_carton_placement.setOnLongClickListener( olcl );
    }



    public void setCartonPrice( String cartonPrice ){
        carton.setCartonPrice( cartonPrice );
    }

    public String getCartonPrice(){
        return carton.getCartonPrice();
    }

    public void setIsDespatched( String isDespatched ){
        carton.setIsDespatched( isDespatched );
    }

    public String getIsDespatched(){
        return carton.getIsDespatched();
    }



    public float addToCartonTotal( float value ){
        cartonTotal += value;
        tv_carton_total.setText( "Rs." + Math.round( Math.ceil( cartonTotal ) ) + "/-" );
        carton.setCartonTotal( String.valueOf( Math.round( Math.ceil( cartonTotal ) ) ) );
        return cartonTotal;
    }

    public float subtractFromCartonTotal( float value ){
        cartonTotal -= value;
        tv_carton_total.setText( "Rs." + Math.round( Math.ceil( cartonTotal ) ) + "/-" );
        carton.setCartonTotal( String.valueOf( Math.round( Math.ceil( cartonTotal ) ) ) );
        return cartonTotal;
    }




    public void addPiecesToCarton( int pieces ){
        piecesTotal += pieces;
        tv_total_pieces.setText( piecesTotal + " pieces" );
        // tv_total_pieces.setText( piecesTotal + "pc" );
    }

    public void subtractPiecesFromCarton( int pieces ){
        piecesTotal -= pieces;
        tv_total_pieces.setText( piecesTotal + " pieces" );
        //tv_total_pieces.setText( piecesTotal + "pc" );
    }

    public int getPiecesTotal(){
        return piecesTotal;
    }






    public void setTempID( String tempID ){
        this.tempID = tempID;
    }

    public String getTempID(){
        return tempID;
    }



    public LinearLayout getView(){
        return ll_carton;
    }
    public void setView( LinearLayout ll_carton ){
        this.ll_carton = ll_carton;
    }

    public int getTotalChildren(){
        return ll_carton.getChildCount();
    }

    /*public View getItemViewFromCarton( BillItem billItem, BillCartonView bcv ){
        LinearLayout ll = bcv.getView();    // Parent view i.e. the carton
        // Finding the item from the list of items
        *//*for(){

        }*//*


    }*/

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Object cloneMe() throws CloneNotSupportedException {
        return clone();
    }

}
