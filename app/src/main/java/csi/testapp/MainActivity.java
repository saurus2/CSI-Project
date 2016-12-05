package csi.testapp;


import android.app.ActionBar;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.WindowManager;

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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;
import com.skp.Tmap.MapUtils;
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
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static csi.testapp.R.id.view;

public class MainActivity extends AppCompatActivity implements RECOServiceConnectListener, RECORangingListener {

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
    private static TMapView mMapView = null;
    private static RelativeLayout mMainRelativeLayout;
    private LocationManager locationManager;
    private static ArrayList<TMapPoint> passPoints = new ArrayList();
    private static ArrayList<TMapPoint> passIndoor = new ArrayList();

    //티맵 포인터들
    Bitmap start;
    Bitmap end;
    Bitmap location;



    //외부 지도용 위도경도 새로 설정해야할때
    public static double n_Latitude = 0;
    public static double n_Longitude = 0;

    //비콘 으로 위도 경도 받아올 값 저장
    public static double beacon_Latitude = 0;
    public static double beacon_Longitude = 0;


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

    //길찾기 했을 때 point arraylist를 순서대로 체크하기 위한 변수
    static int pathIndex = 1;

    //길찾기 할 때 목적지까지 남은 거리
    double remainDistance = 0;

    //버튼 선언
    static Button arButton;
//    static Button chbutton;
    static ImageButton menu;
    static ImageButton current;
    static ImageView bottom;
    static TextView distance;
    static ImageView guide;

    //내부 들어갈때 설정되는 플래그
    static int inner_F = 1;

    //테스트용 메시지 변수
    public static String msg = "";
    public static String remainDistanceMsg = "";

    //비콘 관련 변수들
    //RECOBeaconManager.getInstance(Context, boolean, boolean)의 경우,
    //Context, RECO 비콘만을 대상으로 동작 여부를 설정하는 값, 그리고 백그라운드 monitoring 중 ranging 시 timeout을 설정하는 값을 매개변수로 받습니다.
    //This is a default proximity uuid of the RECO
    public static final String RECO_UUID = "24DDF411-8CF1-440C-87CD-E368DAF9C93E";
    public static final boolean mScanRecoOnly = true;
    public static final boolean mEnableBackgroundTimeout = true;
    public static final boolean DISCONTINUOUS_SCAN = false;
    protected RECOBeaconManager mRecoManager;
    protected ArrayList<RECOBeaconRegion> mRegions;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 10;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;


    private RecoRangingListAdapter mRangingListAdapter;
    private ListView mRegionListView;

    //현재 층수 나타내는 변수
    public static String flo = "1";
    //건물에 들어갔는지를 표시하는 플래그
    public static String entrance = "0";

    //빌딩 경고창 한번만 뜨게 하는 플래그
    public static int checkbuilding = 0;

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
//        chbutton = (Button) findViewById(R.id.chButton);
        menu = (ImageButton) findViewById(R.id.popup);
        current = (ImageButton) findViewById(R.id.current);
        bottom = (ImageView) findViewById(R.id.bottomBar);
        distance = (TextView) findViewById(R.id.distance);
        guide = (ImageView) findViewById(R.id.guide);

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
//        chbutton.setOnClickListener(inner);
        arButton.setOnClickListener(arMode);
        refresh();

        //데이터 베이스 초기화
        Log.i("수행","메세지");
        databaseInitialize(getApplicationContext());
        Log.i("수행","이건 " + getApplicationContext());
        //데이터 베이스 생성
        makeDatabase();
        Log.i("수행","데이터베이스생성");

        //비콘 소스 코드 추가



        /* 안드로이드 API 23 (마시멜로우)이상 버전부터, 정상적으로 RECO SDK를 사용하기 위해서는
         * 위치 권한 (ACCESS_COARSE_LOCATION 혹은 ACCESS_FINE_LOCATION)을 요청해야 합니다.
         * 권한 요청의 경우, 구글에서 제공하는 가이드를 참고하시기 바랍니다.
         *
         * http://www.google.com/design/spec/patterns/permissions.html
         * https://github.com/googlesamples/android-RuntimePermissions
         */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is not granted.");
                this.requestLocationPermission();
            } else {
                Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is already granted.");
            }
        }


//        //RECOServiceConnectListener 인터페이스를 설정하고, RECOBeaconManager의 인스턴스를 RECOBeaconService와 연결합니다.
//        mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), NextActivity.mScanRecoOnly, NextActivity.mEnableBackgroundTimeout);
//
//        mRegions = this.generateBeaconRegion();
//        //mRecoManager will be created here. (Refer to the RECOActivity.onCreate())
//        //mRecoManager 인스턴스는 여기서 생성됩니다. RECOActivity.onCreate() 메소들르 참고하세요.
//        //Set RECORangingListener (Required)
//        //RECORangingListener 를 설정합니다. (필수)
//        mRecoManager.setRangingListener(this);
//        //Activity에서 생성되고 리스너를 셋하지 않으면 정보를 가져올 수 없다
//        mRecoManager.bind(this);



    }

    private void requestLocationPermission() {
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mRangingListAdapter = new RecoRangingListAdapter(this);
        Log.i("문제다 ",""+mRangingListAdapter);
        mRegionListView = (ListView)findViewById(R.id.list_ranging);
        mRegionListView.setAdapter(mRangingListAdapter);
        //리스트뷰가 다른 xml에 있어서 에러가 났던 거임.
        //리스트뷰에만 저장하고 할수 있는지 알아봐야함
        // 만약 리스트뷰에서만 가능하다면 리스트 뷰를 감추고 정보만 사용하도록 하자


    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion() {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<RECOBeaconRegion>();

        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(NextActivity.RECO_UUID, "RECO Sample Region");
        regions.add(recoRegion);

        return regions;
    }

    void start(ArrayList<RECOBeaconRegion> regions) {
        /**
         * There is a known android bug that some android devices scan BLE devices only once. (link: http://code.google.com/p/android/issues/detail?id=65863)
         * To resolve the bug in our SDK, you can use setDiscontinuousScan() method of the RECOBeaconManager.
         * This method is to set whether the device scans BLE devices continuously or discontinuously.
         * The default is set as FALSE. Please set TRUE only for specific devices.
         *
         * mRecoManager.setDiscontinuousScan(true);
         */

        for(RECOBeaconRegion region : regions) {
            try {

                mRecoManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    protected void onDestory(){
        this.stop(mRegions);
        this.unbind();

    }

    private void unbind() {
        try {
            mRecoManager.unbind();
        } catch (RemoteException e) {
            Log.i("RECORangingActivity", "Remote Exception");
            e.printStackTrace();
        }
    }

    void stop(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    private ArrayList<RECOBeacon> mRangedBeacons;
    private LayoutInflater mLayoutInflater;
    private ArrayList<RECOBeacon> ranged;




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
    public static void refresh() {
        bottom.bringToFront();
        menu.bringToFront();
        current.bringToFront();
//        chbutton.bringToFront();
        guide.bringToFront();
        arButton.bringToFront();
        distance.bringToFront();
        setViewInvalidate(menu, current, arButton, bottom,distance,guide);
        guide.setVisibility(View.VISIBLE);
    }

    private static void setViewInvalidate(View... views) {
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

//            ViewGroup.LayoutParams params = mMapView.getLayoutParams();
//            params.width = ActionBar.LayoutParams.MATCH_PARENT;
//            params.height = 700;
//            mMapView.setLayoutParams(params);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 2.0f);
            mMainRelativeLayout.setLayoutParams(params);

            mMapView.bringToFront();
            setViewInvalidate(mMapView);
        }
    };

    public static void returnARmode()
    {
//        ViewGroup.LayoutParams params = mMapView.getLayoutParams();
//        params.height = ActionBar.LayoutParams.MATCH_PARENT;
//        mMapView.setLayoutParams(params);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        mMainRelativeLayout.setLayoutParams(params);
        refresh();
    }

    //popupmenu 처리
    public void onPopupButtonClick(View button) {
        PopupMenu popup = new PopupMenu(this, button);

        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                guide.setVisibility(View.INVISIBLE);
                switch (item.getItemId()) {
                    case R.id.fifth:
                        n_Latitude = 37.451331;
                        n_Longitude = 126.654132;
                        building_n = "5호관";
                        pathIndex = 1;
                        inner_F = 0;
                        i_dialog();
                        DrawSurfaceView.props = new Point(MainActivity.n_Latitude, MainActivity.n_Longitude, MainActivity.building_n);
                        drawPedestrianPath(n_Latitude, n_Longitude);
                        break;

                    case R.id.center:
                        n_Latitude = 37.449476;
                        n_Longitude = 126.654388;
                        building_n = "본관";
                        pathIndex = 1;
                        inner_F = 0;
                        i_dialog();
                        DrawSurfaceView.props = new Point(MainActivity.n_Latitude, MainActivity.n_Longitude, MainActivity.building_n);
                        drawPedestrianPath(n_Latitude, n_Longitude);
                        break;

                    case R.id.tech:
                        n_Latitude = 37.450662;
                        n_Longitude = 126.656960;
                        building_n = "하이테크";
                        pathIndex = 1;
                        inner_F = 0;
                        i_dialog();
                        DrawSurfaceView.props = new Point(MainActivity.n_Latitude, MainActivity.n_Longitude, MainActivity.building_n);
                        drawPedestrianPath(n_Latitude, n_Longitude);
                        break;
                }
                checkbuilding = 0;
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
        indoorPassInit();
        //강의실 안내할 패스를 생성함
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                roomnumber = input.getText().toString();
                roomnumber.toString();
                // Do something with value!
                mainSearchClass();
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

    public void indoorPassInit(){
        TMapPoint point1 = new TMapPoint(37.451348, 126.653993);
        TMapPoint point1_2 = new TMapPoint(37.451348, 126.653993);
        TMapPoint point2 = new TMapPoint(37.450939, 126.653733);
        TMapPoint point2_2 = new TMapPoint(37.450939, 126.653733);
        TMapPoint point3 = new TMapPoint(37.450939, 126.653733);
        TMapPoint point3_2 = new TMapPoint(37.450939, 126.653733);
        TMapPoint point4 = new TMapPoint(37.451348, 126.653993);
        TMapPoint point4_2 = new TMapPoint(37.451348, 126.653993);
        TMapPoint point5 = new TMapPoint(37.451569, 126.653521);
        TMapPoint point5_2= new TMapPoint(37.451569, 126.653521);

        passIndoor.clear();
        passIndoor.add(point1);
        passIndoor.add(point1_2);
        passIndoor.add(point2);
        passIndoor.add(point2_2);
        passIndoor.add(point3);
        passIndoor.add(point3_2);
        passIndoor.add(point4);
        passIndoor.add(point4_2);
        passIndoor.add(point5);
        passIndoor.add(point5_2);
    }

    public static void mainSearchClass() {
        //오른쪽에 있는 버튼을 클릭했을때 불리는 콜백함수

        try {

            //텍스트에 입력한 문자를 가지고옮

            int classNo = Integer.parseInt(MainActivity.roomnumber.toString());
            //텍스트에서 가져온 문자를 정수로 변환 시켜줌


            //MainActivity.db = openOrCreateDatabase(COPY2DATABASE_NAME, Context.MODE_PRIVATE, null);
            //저장된 데이터베이스 포인터를 만들어줌
            if(classNo != 0) {
                String sql = "SELECT * From Classes Where room_no = " + classNo;
                //검색한 방의 번호와 같은 리스트만 sql로 처리함d
                Cursor cur = MainActivity.db.rawQuery(sql, null);
                //데이터베이스에서 sql로 처리된 테이블에 cursor를 만듦

                cur.moveToFirst();
                //커서를 데이터베이스의 0,0 즉 맨 처음 부분에 가져감

                Log.i("move!!!", "" + cur.getString(0));
                //TextView tv = (TextView) findViewById(R.id.textView);
                String text1 = cur.getString(1);
                String text2 = cur.getString(2);
                //Mainactivity 의 목적지 위도 경도를 저장시킴
                MainActivity.desLangitute = Double.parseDouble(text1);
                MainActivity.desLongitute = Double.parseDouble(text2);
                //테이블의 1,2번째 칼럼 위도 경도를 실수로 저장함
                Log.i("수행", "방번호 :" + cur.getString(0));
                Log.i("수행", "경도 :" + MainActivity.desLangitute);
                Log.i("수행", "위도 :" + MainActivity.desLongitute);
            }
        } catch (Exception e) {
            Log.i("_)", "" + e.toString());
        }

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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationfunction);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationfunction);
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


    //건물 입구에서 첫 입구 비콘까지 도달할때까지 로딩창 띄워주기
    public void showProgressDialog(){
            ProgressDialog asyncDialog = new ProgressDialog(this);
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        asyncDialog.setMessage("로딩중입니다..");

        asyncDialog.show();
    }






    //건물 입구 근처에 도착하면 안내문구를 띄워주는 함수
    public void alertBuilding(final double n_Latitude, final double n_Longitude){
        double dist = 0;
        if((dist = calDistance(n_Latitude, n_Longitude)) <= 1000 && inner_F == 0 && checkbuilding == 0){
            checkbuilding = 1;
            //RECOServiceConnectListener 인터페이스를 설정하고, RECOBeaconManager의 인스턴스를 RECOBeaconService와 연결합니다.
            mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), NextActivity.mScanRecoOnly, NextActivity.mEnableBackgroundTimeout);

            mRegions = this.generateBeaconRegion();
            //mRecoManager will be created here. (Refer to the RECOActivity.onCreate())
            //mRecoManager 인스턴스는 여기서 생성됩니다. RECOActivity.onCreate() 메소들르 참고하세요.
            //Set RECORangingListener (Required)
            //RECORangingListener 를 설정합니다. (필수)
            mRecoManager.setRangingListener(this);
            //Activity에서 생성되고 리스너를 셋하지 않으면 정보를 가져올 수 없다
            mRecoManager.bind(this);

            Intent intent = new Intent(MainActivity.this, AlertBuilding.class);
            startActivity(intent);
        }
    };

    //미리 찾아두었던 실내 비콘 간 경로를 기존 길찾기 함수에 적용시키기 위해 passPoints 변수로 적용시킨다
    public static void setIndoorPass(){
        passPoints = passIndoor;
    }




    //목적지의 위도와 경도를 전달받아서 현재 위치와의 거리를 계산하는 함수
    public double calDistance(final double desLat, final double desLon){
      double theta, dist;
        theta = Now_Longitude - desLon;
        dist = Math.sin(deg2rad(Now_Latitude)) * Math.sin(deg2rad(desLat))
                + Math.cos(deg2rad(Now_Latitude)) * Math.cos(deg2rad(desLat)) * Math.cos(deg2rad(theta));
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

        // 제대로 좌표 잡는지 테스트를 위한 메세지
            msg = "New Latitude: " + Now_Latitude
                    + "\nNew Longitude: " + Now_Longitude
                    + "\nPass Point: " + pathIndex + "/" + passPoints.size();


            ////////////////////////////////////////////////////////////////////*/

            //현재 위치에 따라서 맵위치도 다 바꿔줌
            mMapView.setCenterPoint(Now_Longitude, Now_Latitude);
            mMapView.setLocationPoint(Now_Longitude, Now_Latitude);
            check = 1;

            synchronized (locationSync) {
                if (updatedOn + 1 > TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) {
                    Log.i(getString(R.string.app_name), "onLocationChanged() called in less than 1 seconds, ignoring...");
                    return;
                }
            }

            try {
                if(entrance.equals("1")) {
                    //처음 건물안에 들어갔을때
                    Compass.mDrawView.setMyLocation(beacon_Latitude, beacon_Longitude);
                    mMapView.setZoomLevel(20);

                }else{
                    Compass.mDrawView.setMyLocation(location.getLatitude(), location.getLongitude());
                }
                Compass.mDrawView.invalidate();

                //경로가 찾아져있다면 중간지점들을 저장하고있는 passPoint 변수의 사이즈가 0이 아닐 것이다
                if(passPoints.size() != 0 && passPoints.size() > pathIndex) {

                    if(pathIndex == 1)
                    {
                        double distance = 0.0D;
                        if(passPoints.size() > 1) {
                            for(int i = 1; i < passPoints.size(); ++i) {
                                distance += MapUtils.getDistance((TMapPoint)passPoints.get(i), (TMapPoint)passPoints.get(i + 1));
                                if(i + 1 == passPoints.size() - 1) {
                                    break;
                                }
                            }
                        }

                        remainDistance = distance;
                        remainDistanceMsg = "\nRemainDistance: " + ((int)remainDistance) + "m";
                    }


                    double nextLat = passPoints.get(pathIndex).getLatitude();
                    double nextLon = passPoints.get(pathIndex).getLongitude();
                    double nextPointDistance = calDistance(nextLat, nextLon);

                    //현재 위치와 다음 지점까지의 거리가 10미터 미만이라면 너무 가까우므로, array의 index를 2 증가시킨다
                    //다음 지점에서 목적지까지의 거리를 갱신해서 변수에 저장한다
                    if(nextPointDistance < 10) {

                        //실외일경우에만 gps정보로 array를 조정한다
                        if(entrance.equals("0")) {
                            pathIndex += 2;
                        }

                        double distance = 0.0D;
                        if(passPoints.size() > 1) {
                            for(int i = pathIndex; i < passPoints.size(); ++i) {
                                distance += MapUtils.getDistance((TMapPoint)passPoints.get(i), (TMapPoint)passPoints.get(i + 1));
                                if(i + 1 == passPoints.size() - 1) {
                                    break;
                                }
                            }
                        }
                        remainDistance = distance;
                    }
                    //다음 목적지와의 거리가 10미터 이상이라면 그 지점을 다음 중간목적지로 설정한다
                    else {
                        //실외일경우에만 gps정보로 array를 조정한다
                        if(entrance.equals("0")) {
                            DrawSurfaceView.props = new Point(nextLat, nextLon, MainActivity.building_n);
                        }
                        remainDistanceMsg = "\nRemainDistance: " + ((int)remainDistance + (int)nextPointDistance) + "m";
                    }
                }
                else
                    pathIndex = 1;


                synchronized (locationSync) {
                    updatedOn = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                }
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "", e);
            }

            alertBuilding(n_Latitude, n_Longitude);
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

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoRegion) {
        TextView test = (TextView) findViewById(R.id.distance);
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoRegion.getUniqueIdentifier() + ", number of beacons ranged: " + recoBeacons.size());
        ranged = new ArrayList<RECOBeacon>(recoBeacons);
        int a = ranged.size();
        String numStr2 = String.valueOf(a);
        Log.v("RECOArrayList size ", numStr2);
        try {
            RECOBeacon reco = ranged.get(a - 1);
            numStr2 = String.valueOf(reco.getMinor());
            Log.v("FUCK", numStr2);
            //비콘을 하나씩 불러오는 함수
            if (a != 0 && entrance.equals("0")) { //아직 입장 안했을때
                for (int b = 0; b < a; b++) {
                    reco = ranged.get(b);
                    int beaconMinor = reco.getMinor();
                    double dis = reco.getAccuracy();
                    if (beaconMinor == 8846 && dis < 0.07) {
                        String msg1 = "Entered : " + reco.getMinor() + "\n" + String.format("%.2f", reco.getAccuracy());
                        test.setText(msg1);
                        entrance = "1";
                        detectBeacon(8846);
                    }

                }
            } else if (a != 0 && entrance.equals("1")) {
                if (a != 0) { // 입장하고 난뒤
                    //비콘을 하나씩 불러오는 함수
                    for (int b = 0; b < a; b++) {
                        reco = ranged.get(b);
                        int beaconMinor = reco.getMinor();
                        String msg1 = "Entered : " + reco.getMinor() + "\n" + String.format("%.2f", reco.getAccuracy());
//                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                        test.setText(msg1);
                        detectBeacon(beaconMinor);
                    }
                }

//        mRangingListAdapter.updateAllBeacons(recoBeacons);
//        mRangingListAdapter.notifyDataSetChanged();
                //Write the code when the beacons in the region is received
            }
        }catch(Exception e){
            Log.v("no","beacon");
        }
    }

    public static int onetime = 0;

    public void detectBeacon(int minor) {
        //오른쪽에 있는 버튼을 클릭했을때 불리는 콜백함수

        try {

            //텍스트에 입력한 문자를 가지고옮

            int classNo = minor;
            //텍스트에서 가져온 문자를 정수로 변환 시켜줌


            //MainActivity.db = openOrCreateDatabase(COPY2DATABASE_NAME, Context.MODE_PRIVATE, null);
            //저장된 데이터베이스 포인터를 만들어줌
            if(classNo != 0) {
                String sql = "SELECT * From Classes Where room_no = " + classNo + " and floor = " + flo;
                //minor와 층수를 비교하여 위도 경도를 찾는다
                //검색한 방의 번호와 같은 리스트만 sql로 처리함d
                Cursor cur = MainActivity.db.rawQuery(sql, null);
                //데이터베이스에서 sql로 처리된 테이블에 cursor를 만듦

                cur.moveToFirst();
                //커서를 데이터베이스의 0,0 즉 맨 처음 부분에 가져감

                Log.i("move!!!", "" + cur.getString(0));
                //TextView tv = (TextView) findViewById(R.id.textView);
                String text1 = cur.getString(1);
                String text2 = cur.getString(2);
                //Mainactivity 의 목적지 위도 경도를 저장시킴
                beacon_Latitude = Double.parseDouble(text1);
                beacon_Longitude = Double.parseDouble(text2);

                double nextLat = 0;
                double nextLon = 0;

                if(passPoints.size() > pathIndex+2) {
                    nextLat = passPoints.get(pathIndex + 2).getLatitude();
                    nextLon = passPoints.get(pathIndex + 2).getLongitude();

                    //현재 잡히는 비콘이 다음 지점이라면 해당 지점에 도착한것이므로 그 다음 지점을 찍는다
                    if(nextLat == beacon_Latitude && nextLon == beacon_Longitude){
                        pathIndex += 2;
                        DrawSurfaceView.props = new Point(nextLat, nextLon, MainActivity.building_n);
                    }
                }
                else {
                    nextLat = passPoints.get(passPoints.size() - 1).getLatitude();
                    nextLon = passPoints.get(passPoints.size() - 1).getLongitude();

                    //현재 잡히는 비콘이 다음 지점이라면 해당 지점에 도착한것이므로 그 다음 지점을 찍는다
                    if(nextLat == beacon_Latitude && nextLon == beacon_Longitude){
                        pathIndex = passPoints.size() - 1;
                        DrawSurfaceView.props = new Point(nextLat, nextLon, MainActivity.building_n);
                    }
                }


                Log.i("stair!!!", "" + classNo);

                //계단일 때
                if(classNo == 8847 && onetime == 0) {
                    onetime = 1;
                    Log.i("enterance!!!", "" + entrance);
                    Intent intent = new Intent(MainActivity.this, AlertStair.class);
                    startActivity(intent);
                }

                //도착했을 때
                if(classNo == 8845) {
                    Intent intent = new Intent(MainActivity.this, AlertStair.class);
                    startActivity(intent);
                }

                //테이블의 1,2번째 칼럼 위도 경도를 실수로 저장함
                Log.i("수행", "방번호 :" + cur.getString(0));
                Log.i("수행", "경도 :" + beacon_Latitude);
                Log.i("수행", "위도 :" + beacon_Longitude);
            }
        } catch (Exception e) {
            Log.i("_)", "" + e.toString());
        }

    }



    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion recoBeaconRegion, RECOErrorCode recoErrorCode) {

    }

    @Override
    public void onServiceConnect() {
        //RECOBeaconService와 연결 시 코드 작성
        Log.i("RECORangingActivity", "onServiceConnect()");

        mRecoManager.setDiscontinuousScan(DISCONTINUOUS_SCAN);
        this.start(mRegions);
        //Write the code when RECOBeaconManager is bound to RECOBeaconService
    }

    @Override
    public void onServiceFail(RECOErrorCode recoErrorCode) {
//RECOBeaconService와 연결 되지 않았을 시 코드 작성
        return;
    }
}
