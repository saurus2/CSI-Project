package csi.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

/**
 * Created by saurus on 2016. 11. 2..
 */

public class  IntroActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //인트로화면이므로 타이틀바를 없앤다
        setContentView(R.layout.main_logo);

        //화면 전환 부분
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                startActivity(new Intent(IntroActivity.this,MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        };
        //3초 딜레이
        handler.sendEmptyMessageDelayed(0, 3000);
    }
}
