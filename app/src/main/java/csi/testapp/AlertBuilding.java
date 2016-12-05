package csi.testapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import static csi.testapp.MainActivity.inner_F;



/**
 * Created by lab1 on 11/30/16.
 */

public class AlertBuilding extends Activity {
    private TextView mTitleView;
    private TextView mContentView;
    private Button mLeftButton;
    private Button mRightButton;
    private String mTitle;
    private String mContent;

    private View.OnClickListener mLeftClickListener;
    private View.OnClickListener mRightClickListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.alertbuilding);
        setLayout();
        mLeftButton.setOnClickListener(leftClickListener);
        mRightButton.setOnClickListener(rightClickListener);
        //setLayout();

    }
//     public AlertBuilding(Context context , String title , String content ,
//                        View.OnClickListener leftListenern , View.OnClickListener rightListenern) {
//
//        //super(context , android.R.style.Theme_Translucent_NoTitleBar);
//
//        setLayout();
//        this.mTitle = title;
//        this.mContent = content;
//        this.mLeftButton.setOnClickListener(rightClickListener);
//        this.mRightButton.setOnClickListener(leftClickListener);
//    }

    public void setTitle(String title){
        mTitleView.setText(title);
    }

    public void setContent(String content){
        mContentView.setText(content);
    }

    View.OnClickListener rightClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            inner_F = 1;
            Intent i = new Intent(AlertBuilding.this, Loading.class);
            startActivity(i);
            finish();
        }
    };

    View.OnClickListener leftClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            inner_F = 1;
        }
    };

    public void setClickListener(View.OnClickListener left , View.OnClickListener right){
        if(left!=null && right!=null){
            mLeftButton.setOnClickListener(left);
            mRightButton.setOnClickListener(right);
        }else if(left!=null && right==null){
            mLeftButton.setOnClickListener(left);
        }else {

        }
    }

    /*
     * Layout
     */
    private void setLayout(){
        mTitleView = (TextView) findViewById(R.id.tv_title);
        mContentView = (TextView) findViewById(R.id.tv_content);
        mLeftButton = (Button) findViewById(R.id.bt_left);
        mRightButton = (Button) findViewById(R.id.bt_right);
    }

}