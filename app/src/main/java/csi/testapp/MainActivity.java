package csi.testapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static csi.testapp.R.id.view;

public class MainActivity extends AppCompatActivity {

    //데이터베이스 생성관련한 주소 변수들
    //assets 폴더에 이미 SQLite로 만든 디비를 넣어놓고
    //안드로이드에 디비를 불러와 생성함
    public static final String PACKAGE_DIR = "/data/data/csi.testapp/";
    public static final String DATABASE_NAME = "Classes.db";
    public static final String COPY2DATABASE_NAME = "ClassesDB.db";

    //도착지에 대한 경도 위도
    public static double desLangitute = 0;
    public static double desLongitute = 0;

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

    //퍼미션 플래그
    final int READ_ROCATE_CODE = 0;

    //현재 위치와 나침반 효과 플래그
    int flag = 0;

    //바로 길찾기 눌렀을때 플래그
    int check = 0;

    //버튼 선언
    Button arButton;
    Button chbutton;
    ImageButton menu;
    ImageButton current;

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
        start = BitmapFactory.decodeResource(getResources(), R.drawable.start);
        end = BitmapFactory.decodeResource(getResources(), R.drawable.end);
        mMapView.setIcon(location);
        mMapView.setIconVisibility(true);
        mMapView.setSightVisible(true);
        mMapView.setTMapPathIcon(start, end);

        //버튼 초기화
        arButton = (Button) findViewById(R.id.arButton);
        chbutton = (Button) findViewById(R.id.chButton);
        menu = (ImageButton) findViewById(R.id.popup);
        current = (ImageButton) findViewById(R.id.current);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //티맵 화면에 출력해주는 함수들
        mMainRelativeLayout = (RelativeLayout) findViewById(R.id.mainRelativeLayout);
        initView();
        mMainRelativeLayout.addView(mMapView);
        mMapView.setSKPMapApiKey(mApiKey);
        CheckPermission();

        //버튼 리스너들 등록 버튼 위치 조정
        current.setOnClickListener(turnOn);
        chbutton.setOnClickListener(inner);
        arButton.setOnClickListener(arMode);
        refresh();

        //데이터 베이스 초기화
        Log.i("수행","메세지");
        databaseInitialize(getApplicationContext());
        Log.i("수행","이건 " + getApplicationContext());
        //데이터 베이스 생성
        makeDatabase();
        Log.i("수행","데이터베이스생성");
    }

    public static void databaseInitialize(Context ctx) {
        // check
        File folder = new File(PACKAGE_DIR + "databases");
        folder.mkdirs();
        File outfile = new File(PACKAGE_DIR + "databases/" + COPY2DATABASE_NAME);

        if (outfile.length() >= 0) {
            AssetManager assetManager = ctx.getResources().getAssets();
            try {
                InputStream is = assetManager.open(DATABASE_NAME, AssetManager.ACCESS_BUFFER);
                long filesize = is.available();
                byte [] tempdata = new byte[(int)filesize];
                is.read(tempdata);
                is.close();
                outfile.createNewFile();
                FileOutputStream fo = new FileOutputStream(outfile);
                fo.write(tempdata);
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void makeDatabase() {

        try {
            //database 생성
            SQLiteDatabase db = openOrCreateDatabase(COPY2DATABASE_NAME, Context.MODE_PRIVATE, null);
        } catch (Exception e) {
            Log.i("_)", "" + e.toString());
        }

    }


    //버튼들 최상단으로 새로고침 시켜주는 함수
    public void refresh() {
        menu.bringToFront();
        current.bringToFront();
        chbutton.bringToFront();
        arButton.bringToFront();
        setViewInvalidate(menu, current, chbutton, arButton);
    }

    private void setViewInvalidate(View... views) {
        for (View v : views) {
            v.invalidate();
        }
    }
    /////////////////////////////////////////////////////

    //실내 지도로 화면 변환
    View.OnClickListener inner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, NextActivity.class);
            startActivity(intent);
        }
    };

    //AR 모드로 전환
    View.OnClickListener arMode = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, Compass.class);
            startActivity(intent);
        }
    };

    //popupmenu 처리
    public void onPopupButtonClick(View button) {
        PopupMenu popup = new PopupMenu(this, button);

        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.fifth:
                        n_Latitude = 37.4508;
                        n_Longitude = 126.6525;
                        drawPedestrianPath(n_Latitude, n_Longitude);
                        break;

                    case R.id.center:
                        n_Latitude = 37.449476;
                        n_Longitude = 126.654388;
                        drawPedestrianPath(n_Latitude, n_Longitude);
                        break;

                    case R.id.tech:
                        n_Latitude = 37.450662;
                        n_Longitude = 126.656960;
                        drawPedestrianPath(n_Latitude, n_Longitude);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    //현재 위치 버튼 눌렀을때 작동
    //1번 누르면 현재 위치 찾기
    //2번 누르면 나침반 모드
    //3번 누르면 나침반 모드 종료
    View.OnClickListener turnOn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (flag == 0) {
                FindmyLocation();
                flag = 1;
            } else if (flag == 1) {
                mMapView.setCompassMode(true);
                flag = 2;
            } else if (flag == 2) {
                mMapView.setCompassMode(false);
                flag = 3;
            } else if (flag == 3) {
                locationManager.removeUpdates(locationfunction);
                flag = 0;
            }

        }

    };


    //gps, network provider 설정
    void FindmyLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationfunction);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationfunction);
    }

    //맵에 길 그려주는 부분
    public void drawPedestrianPath(double n_Latitude, double n_Longitude) {
        if (check == 0) {
            FindmyLocation();
        }

        TMapPoint point1 = new TMapPoint(Now_Latitude, Now_Longitude);
        TMapPoint point2 = new TMapPoint(n_Latitude, n_Longitude);

        TMapData tmapdata = new TMapData();

        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, point1, point2, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapPolyLine.setLineColor(Color.BLUE);
                mMapView.addTMapPath(tMapPolyLine);
            }
        });

        check = 1;
    }


    //위치 관련 함수들
    long updatedOn = Long.MAX_VALUE;
    final Object locationSync = new Object();
    LocationListener locationfunction = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Now_Latitude = location.getLatitude();
            Now_Longitude = location.getLongitude();

        /* 여기는 제대로 좌표 잡는지 테스트하려고 토스트 메세지 넣어둔거
            String msg = "New Latitude: " + Now_Latitude
                    + "New Longitude: " + Now_Longitude;

            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

            ////////////////////////////////////////////////////////////////////*/

            //현재 위치에 따라서 맵위치도 다 바꿔줌
            mMapView.setCenterPoint(Now_Longitude, Now_Latitude);
            mMapView.setLocationPoint(Now_Longitude, Now_Latitude);
            check = 1;

            synchronized (locationSync) {
                if (updatedOn + 1 > TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) {
                    Log.i(getString(R.string.app_name), "onLocationChanged() called in less than 10 seconds, ignoring...");
                    return;
                }
            }

            try {
                Compass.mDrawView.setMyLocation(location.getLatitude(), location.getLongitude());
                Compass.mDrawView.invalidate();

                synchronized (locationSync) {
                    updatedOn = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                }
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "", e);
            }


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


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
