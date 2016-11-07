package csi.testapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.media.Image;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static csi.testapp.R.id.view;

public class MainActivity extends AppCompatActivity {

    //데이터베이스 생성관련한 주소 변수들
    //assets 폴더에 이미 SQLite로 만든 디비를 넣어놓고
    //안드로이드에 디비를 불러와 생성함
    public static final String PACKAGE_DIR = "/data/data/csi.testapp/";
    public static final String DATABASE_NAME = "Classes.db";
    public static final String COPY2DATABASE_NAME = "ClassesDB.db";
    public static SQLiteDatabase db;

    //사용자에게 입력받는 데이터들
    public static String roomnumber;


    //실내 네비용 도착지에 대한 경도 위도
    public static double desLangitute = 0;
    public static double desLongitute = 0;

    //티맵 관련 변수들
    public static String mApiKey = "8bdbb125-7d59-3684-84ff-ad4b5bb59e74";
    private TMapView mMapView = null;
    private RelativeLayout mMainRelativeLayout;
    private LocationManager locationManager;
    private ArrayList<TMapPoint> passPoints = new ArrayList();
    //티맵 포인터들
    Bitmap start;
    Bitmap end;
    Bitmap location;


    //외부 지도용 위도경도 새로 설정해야할때
    public static double n_Latitude = 0;
    public static double n_Longitude = 0;

    //외부 지도용 현재 위치 저장되는 위도 경도
    double Now_Latitude = 0;
    double Now_Longitude = 0;

    //AR용 건물 이름
    public static String building_n = null;

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
    ImageView bottom;

    //내부 들어갈때 설정되는 플래그
    int inner_F = 1;

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
        location = BitmapFactory.decodeResource(getResources(), R.drawable.location);
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
        bottom = (ImageView) findViewById(R.id.bottomBar);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            getWindow().setStatusBarColor(Color.parseColor("#0066cc"));
        }
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
            db = openOrCreateDatabase(COPY2DATABASE_NAME, Context.MODE_PRIVATE, null);
        } catch (Exception e) {
            Log.i("_)", "" + e.toString());
        }

    }


    //버튼들 최상단으로 새로고침 시켜주는 함수
    public void refresh() {
        bottom.bringToFront();
        menu.bringToFront();
        current.bringToFront();
        chbutton.bringToFront();
        arButton.bringToFront();
        setViewInvalidate(menu, current, chbutton, arButton, bottom);
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
                        n_Latitude = 37.451331;
                        n_Longitude = 126.654132;
                        building_n = "5호관";
                        inner_F = 0;
                        i_dialog();
                        DrawSurfaceView.props = new Point(MainActivity.n_Latitude, MainActivity.n_Longitude, MainActivity.building_n);
                        drawPedestrianPath(n_Latitude, n_Longitude);
                        passPointInfo(n_Latitude, n_Longitude);
                        break;

                    case R.id.center:
                        n_Latitude = 37.449476;
                        n_Longitude = 126.654388;
                        building_n = "본관";
                        inner_F = 0;
                        i_dialog();
                        DrawSurfaceView.props = new Point(MainActivity.n_Latitude, MainActivity.n_Longitude, MainActivity.building_n);
                        drawPedestrianPath(n_Latitude, n_Longitude);
                        passPointInfo(n_Latitude, n_Longitude);
                        break;

                    case R.id.tech:
                        n_Latitude = 37.450662;
                        n_Longitude = 126.656960;
                        building_n = "하이테크";
                        inner_F = 0;
                        i_dialog();
                        DrawSurfaceView.props = new Point(MainActivity.n_Latitude, MainActivity.n_Longitude, MainActivity.building_n);
                        drawPedestrianPath(n_Latitude, n_Longitude);
                        passPointInfo(n_Latitude, n_Longitude);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    //호관 누르면 호수 물어보는 팝업 창 뜨게 하는 함수
    //디비와 섞여있음
    public void i_dialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("강의실 번호를 입력해주세요");
        //alert.setMessage("Message");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                roomnumber = input.getText().toString();
                roomnumber.toString();
                // Do something with value!
                NextActivity.mainSearchClass();
            }
        });
        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

        alert.show();
    }

    //목적지를 선택하면 거기까지 가는 경로에서 꺾이는 지점들을 알림창으로 출력하는 함수
    public void passPointInfo(double n_Latitude, double n_Longitude){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        TMapPoint point1 = new TMapPoint(Now_Latitude, Now_Longitude);

        alert.setTitle("꺾이는 지점들");

        String message = "";

        for(int i = 0; i < passPoints.size(); i++) {
            message += "Lon : " + passPoints.get(i).getLongitude() + " Lat : " + passPoints.get(i).getLatitude() + "\n";
        }
        alert.setMessage(message);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do something with value!
            }
        });

        alert.show();
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
    public void drawPedestrianPath(final double n_Latitude, final double n_Longitude) {
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
                passPoints = tMapPolyLine.getLinePoint();
            }
        });

        check = 1;
    }

    //건물 입구 근처 도착 하면 띄워주는 함수
    public void alert(){
        double dist = 0;
        if((dist = calDistance()) <= 10 && inner_F == 0){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("건물에 입장하시면 확인을 눌러주세요");

            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    inner_F = 1;
                    DrawSurfaceView.props = new Point(MainActivity.desLangitute, MainActivity.desLongitute, MainActivity.roomnumber);
                    Intent intent = new Intent(MainActivity.this, Compass.class);
                    startActivity(intent);
                }
            });
            alert.setNegativeButton("취소",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

            alert.show();
        }

    };

    //위도 경도 거리계산
    public double calDistance(){
      double theta, dist;
        theta = n_Longitude - Now_Longitude;
        dist = Math.sin(deg2rad(n_Latitude)) * Math.sin(deg2rad(n_Latitude)) + Math.cos(deg2rad(n_Latitude))
                * Math.cos(deg2rad(n_Latitude)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    };

    // 주어진 도(degree) 값을 라디언으로 변환
    private double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
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

            alert();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.this, "GPS 사용을 체크해주세요 .", Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
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
