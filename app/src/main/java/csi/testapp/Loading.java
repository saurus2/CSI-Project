package csi.testapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by lab1 on 11/18/16.
 */


public class Loading extends Activity{
    int check = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.custom_dialog);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        CheckTypesTask task = new CheckTypesTask();
        task.execute();
    }

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(
                Loading.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중입니다..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            check = NextActivity.ranged.size();
            while(check == 0){
                check = NextActivity.ranged.size();
            }

//            try {
//                for (int i = 0; i < 5; i++) {
//                    //asyncDialog.setProgress(i * 30);
//                    Thread.sleep(500);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            DrawSurfaceView.props = new Point(MainActivity.desLangitute, MainActivity.desLongitute, MainActivity.roomnumber+" 강의실");
            Intent intent = new Intent(Loading.this, Compass.class);
            startActivity(intent);
            finish();
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }
    }
}