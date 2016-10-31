package csi.testapp;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.skp.Tmap.TMapView;

/**
 * Created by lab1 on 10/26/16.
 */

public class Optimization extends AppCompatActivity implements LocationListener {
    //티맵 관련 변수들
    public static String mApiKey = "8bdbb125-7d59-3684-84ff-ad4b5bb59e74";
    private TMapView mMapView = null;
    private RelativeLayout mMainRelativeLayout;
    private LocationManager locationManager;
    //티맵 포인터들
    Bitmap start;
    Bitmap end;
    Bitmap location;
    BitmapFactory.Options options = new BitmapFactory.Options();

    //위도경도 새로 설정해야할때
    double n_Latitude = 0;
    double n_Longitude = 0;

    //현재 위치 저장되는 위도 경도
    double Now_Latitude = 0;
    double Now_Longitude = 0;

    //출발위치 변하지 않게 하기 위해
    int check = 0;

    //퍼미션 플래그
    final int READ_ROCATE_CODE = 0;

    //버튼 선언
    Button current;
    ImageButton menu;

    //지도와 버튼들 처음 초기화 시켜주는 함수
    void initView() {
        mMapView = new TMapView(this);
        //지도를 처음 띄우면 보이는 화면 설정
        //현재위치,지도 중심부도 처음엔 학교 중심부
        mMapView.setCenterPoint(126.65318, 37.449666);
        mMapView.setLocationPoint(126.65318, 37.449666);
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mMapView.setZoomLevel(17);

        //지도 아이콘들 셋팅
        options.inSampleSize = 60;
        location = BitmapFactory.decodeResource(getResources(), R.drawable.location, options);
        start = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        end = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mMapView.setIcon(location);
        mMapView.setIconVisibility(true);
        mMapView.setSightVisible(true);
        mMapView.setTMapPathIcon(start, end);

        //버튼 초기화
        current = (Button) findViewById(R.id.current);
        menu = (ImageButton) findViewById(R.id.popup);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainRelativeLayout = (RelativeLayout) findViewById(R.id.mainRelativeLayout);
        initView();
        mMainRelativeLayout.addView(mMapView);
        mMapView.setSKPMapApiKey(mApiKey);
        CheckPermission();

        current.setOnClickListener(turnon);
        menu.bringToFront();
        menu.invalidate();


    }

    //popupmenu 처리
    public void onPopupButtonClick(View button) {
        PopupMenu popup = new PopupMenu(this, button);

        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.fifth:
                        break;

                    case R.id.center:
                        break;

                    case R.id.tech:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    //현재 위치 버튼 눌렀을때 작동
    View.OnClickListener turnon = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FindmyLocation();
        }

    };


    //gps, network provider 설정
    void FindmyLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
    }

    //위치 관련 함수들
    @Override
    public void onLocationChanged(Location location) {

        Now_Latitude = location.getLatitude();
        Now_Longitude = location.getLongitude();

        /* 여기는 제대로 좌표 잡는지 테스트하려고 토스트 메세지 넣어둔거 */
        String msg = "New Latitude: " + Now_Latitude
                + "New Longitude: " + Now_Longitude;

        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

        ////////////////////////////////////////////////////////////////////

        //현재 위치에 따라서 맵위치도 다 바꿔줌
        mMapView.setCenterPoint(Now_Longitude, Now_Latitude);
        mMapView.setLocationPoint(Now_Longitude, Now_Latitude);

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

    //permission 설정
    void CheckPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FindmyLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission_group.LOCATION}, READ_ROCATE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case READ_ROCATE_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    FindmyLocation();
                }
        }
    }

}
