package csi.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;



/**
 * Created by lab1 on 11/30/16.
 */

public class AlertEnd extends Activity {
    private TextView mTitleView;
    private TextView mContentView;
    private Button mOkButton;
    private String mTitle;
    private String mContent;

    private View.OnClickListener mOkClickListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.alertend);
        setLayout();
        mOkButton.setOnClickListener(okClickListener);
    }

    public void setTitle(String title){
        mTitleView.setText(title);
    }

    public void setContent(String content){
        mContentView.setText(content);
    }

    View.OnClickListener okClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    public void setClickListener(View.OnClickListener ok){
        if(ok!=null){
            mOkButton.setOnClickListener(ok);
        }else {

        }
    }

    /*
     * Layout
     */
    private void setLayout(){
        mTitleView = (TextView) findViewById(R.id.tv_title);
        mContentView = (TextView) findViewById(R.id.tv_content);
        mOkButton = (Button) findViewById(R.id.bt_ok);
    }

}