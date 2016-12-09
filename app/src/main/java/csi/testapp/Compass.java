package csi.testapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


/*
 * Portions (c) 2009 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Coby Plain coby.plain@gmail.com, Ali Muzaffar ali@muzaffar.me
 */

public class Compass extends Activity {

    private static final String TAG = "Compass";
    private static boolean DEBUG = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    public static DrawSurfaceView mDrawView;


    // 이미지 움직이는거 테스트
    private AnimationDrawable frameAnimation;
    private ImageView view;


    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (DEBUG)
                Log.d(TAG, "sensorChanged (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
            if (mDrawView != null) {
                mDrawView.setOffset(event.values[0]);
                mDrawView.invalidate();

                if(DrawSurfaceView.directionFlag == 1) {
                    frameAnimation.start();
                    view.setVisibility(View.VISIBLE);
                }
                else {
                    frameAnimation.stop();
                    view.setVisibility(View.INVISIBLE);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //액티비티를 팝업 형태로 띄우는 변수들, 현재는 안 쓰므로 주석처리
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        getWindow().setGravity(Gravity.BOTTOM);
//
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        Window window = getWindow();
//        lp.copyFrom(window.getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        window.setAttributes(lp);


        //센서들 설정
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        setContentView(R.layout.activity_ar);
        mDrawView = (DrawSurfaceView) findViewById(R.id.drawSurfaceView);

        // 컨트롤 ImageView 객체를 가져온다
        view = (ImageView) findViewById(R.id.imgView);

        // animation_list.xml 를 ImageView 백그라운드에 셋팅한다
        view.setBackgroundResource(R.drawable.animation);

        // 이미지를 동작시키기위해  AnimationDrawable 객체를 가져온다.
        frameAnimation = (AnimationDrawable) view.getBackground();
    }


    public void endAR(View button) {
        finish();
    }

    @Override
    protected void onResume() {
        if (DEBUG)
            Log.d(TAG, "onResume");
        super.onResume();

        mSensorManager.registerListener(mListener, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        if (DEBUG)
            Log.d(TAG, "onStop");
        mSensorManager.unregisterListener(mListener);
        super.onStop();
    }

    @Override
    public void finish() {
        MainActivity.returnARmode();

        super.finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // 어플에 포커스가 갈때 시작된다
            frameAnimation.start();
        } else {
            // 어플에 포커스를 떠나면 종료한다
            frameAnimation.stop();
        }
    }
}

