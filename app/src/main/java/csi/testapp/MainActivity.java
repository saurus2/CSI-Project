package csi.testapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;
import com.skp.Tmap.TMapGpsManager.onLocationChangedCallback;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapData.BizCategoryListenerCallback;
import com.skp.Tmap.TMapData.ConvertGPSToAddressListenerCallback;
import com.skp.Tmap.TMapData.FindAllPOIListenerCallback;
import com.skp.Tmap.TMapData.FindAroundNamePOIListenerCallback;
import com.skp.Tmap.TMapData.FindPathDataAllListenerCallback;
import com.skp.Tmap.TMapData.FindPathDataListenerCallback;
import com.skp.Tmap.TMapData.TMapPathType;

import static csi.testapp.R.id.view;

public class MainActivity extends AppCompatActivity implements LocationListener{

    public static String mApiKey = "8bdbb125-7d59-3684-84ff-ad4b5bb59e74";
    private TMapView mMapView = null;
    private TMapPoint n_Locate = null;
    private RelativeLayout mMainRelativeLayout=null;
    TMapGpsManager gps;
    final int READ_ROCATE_CODE = 0;
    Bitmap bitmap;
    Bitmap end;
    private LocationManager locationManager;
    Button btn_compas;
    Button second;
    Button back;
    Button high;
    Button center;
    double n_Latitude = 0;
    double n_Longitude = 0;
    int check = 0;

    private void configureMapView(){
        mMapView.setSKPMapApiKey(mApiKey);
    }

    void initView(){
        mMapView = new TMapView(this);
        mMapView.setCenterPoint(126.65318, 37.449666);
        mMapView.setLocationPoint(126.65318, 37.449666);
        bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        end = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mMapView.setIcon(bitmap);
        mMapView.setIconVisibility(true);
        mMapView.setSightVisible(true);
        mMapView.setZoomLevel(17);
        gps=new TMapGpsManager(this);
        mMapView.setTMapPathIcon(bitmap,end);
        mMapView.setCompassMode(true);
        btn_compas = (Button)findViewById(R.id.compas);
        second = (Button)findViewById(R.id.second);
        back = (Button)findViewById(R.id.back);
        high = (Button)findViewById(R.id.high);
        center = (Button)findViewById(R.id.center);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainRelativeLayout = (RelativeLayout)findViewById(R.id.mainRelativeLayout);
        initView();
        mMainRelativeLayout.addView(mMapView);
        configureMapView();
        tryCheckPermission();
        findViewById(R.id.button).setOnClickListener(handler);

        //컴파스 버튼 테스트 부분

        btn_compas.setOnClickListener(compas);
        second.setOnClickListener(Des);
        back.setOnClickListener(Des);
        high.setOnClickListener(Des);
        center.setOnClickListener(Des);
    }

    View.OnClickListener compas = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            mMapView.setCompassMode(true);
        }
    };

    View.OnClickListener Des = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.back:
                    n_Latitude = 37.451037;
                    n_Longitude = 126.656499;
                    break;
                case R.id.center:
                    n_Latitude = 37.449476;
                    n_Longitude = 126.654388;
                    break;
                case R.id.high:
                    n_Latitude = 37.450662;
                    n_Longitude = 126.656960;
                    break;
                case R.id.second:
                    n_Latitude = 37.450337;
                    n_Longitude = 126.655693;
                    break;
            }
            check = 0;

        }
    };

    View.OnClickListener handler = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            doAction();
        }

    };

    //    화면변환하는 버튼 onclick 활성화 xml 의 디자인에서 수정함
    //    버튼 이벤트 자바에 미리 만들어 놓기 -> 엑티비티 연결된 xml에서 버튼 생성 -> 버튼 프로퍼티에서 Onclick 옵션에 추가

    public void clicked2(View v) {
        Intent intent = new Intent(getApplicationContext(), NextActivity.class);
        startActivity(intent);
    }


    void settingGPS(){
        gps.setMinTime(1000);
        gps.setMinDistance(5);
        gps.setProvider(gps.GPS_PROVIDER);
        gps.OpenGps();
    };



    void doAction(){
        n_Latitude = 37.4508;
        n_Longitude = 126.6525;
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,2000,10,this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,10,this);

    };

    public void drawPedestrianPath(double s_lat, double s_long,double n_Latitude, double n_Longitude) {
        TMapPoint point1 = new TMapPoint(s_lat,s_long);
        TMapPoint point2 = new TMapPoint(n_Latitude, n_Longitude);

        TMapData tmapdata = new TMapData();

        tmapdata.findPathDataWithType(TMapPathType.PEDESTRIAN_PATH, point1, point2, new FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setLineColor(Color.BLUE);
                mMapView.addTMapPath(polyLine);
            }
        });
    }


    void tryCheckPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION) == PackageManager.PERMISSION_GRANTED){
            settingGPS();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission_group.LOCATION},READ_ROCATE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        switch(requestCode){
            case READ_ROCATE_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    settingGPS();
                }
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        double Latitude = location.getLatitude();
        double Longitude = location.getLongitude();
        String msg = "New Latitude: " + Latitude
                + "New Longitude: " + Longitude;

        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
        mMapView.setCenterPoint(Longitude, Latitude);
        mMapView.setLocationPoint(Longitude, Latitude);
        if(check == 0) {
            drawPedestrianPath(Latitude, Longitude,n_Latitude,n_Longitude);
            check++;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(getBaseContext(), "Gps is turned on!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();
    }
}
