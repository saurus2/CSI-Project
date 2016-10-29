package csi.testapp;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.support.design.widget.Snackbar;
import android.widget.ToggleButton;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;
import com.perples.recosdk.RECOBeaconManager;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by saurus on 2016. 10. 1..
 */

public class NextActivity extends FragmentActivity implements OnMapReadyCallback, RECOServiceConnectListener, RECORangingListener {

    private GoogleMap mMap; // 뷰 맵 객체 생성
    //데이터베이스 생성관련한 주소 변수들
    //assets 폴더에 이미 SQLite로 만든 디비를 넣어놓고
    //안드로이드에 디비를 불러와 생성함
    public static final String PACKAGE_DIR = "/data/data/csi.testapp/";
    public static final String DATABASE_NAME = "Classes.db";
    public static final String COPY2DATABASE_NAME = "ClassesDB.db";
    LatLng Marker1;
    List<Marker> markers = new ArrayList<Marker>();


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


    @Override
    public void onServiceConnect(){
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




    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity);
        //구글맵 뷰에 띄우기
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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



    public void getView(int position, View convertView, ViewGroup parent) {
        RECOBeacon recoBeacon = mRangedBeacons.get(position);
        String proximityUuid = recoBeacon.getProximityUuid();

        Log.i("수행 레코 비콘 정보 가져오기",String.format("%s-%s-%s-%s-%s", proximityUuid.substring(0, 8), proximityUuid.substring(8, 12), proximityUuid.substring(12, 16), proximityUuid.substring(16, 20), proximityUuid.substring(20) ));
        Log.i("수행 레코 비콘 정보 가져오기",recoBeacon.getMajor()+"");
        Log.i("수행 레코 비콘 정보 가져오기",recoBeacon.getMinor()+"");
        Log.i("수행 레코 비콘 정보 가져오기",recoBeacon.getTxPower()+"");
        Log.i("수행 레코 비콘 정보 가져오기",recoBeacon.getRssi()+"");
        Log.i("수행 레코 비콘 정보 가져오기",recoBeacon.getBattery()+"");
        Log.i("수행 레코 비콘 정보 가져오기",recoBeacon.getProximity()+"");
        Log.i("수행 레코 비콘 정보 가져오기",String.format("%.2f", recoBeacon.getAccuracy())+"");
    }




    //원래 화면으로 돌아가는 버튼
    //버튼 만들고 액티비티랑 연결 -> 함수 미리 만들어 놓기 -> 버튼에서 onclick 기능 추가하면됨
    public void clicked3(View v){
        finish();
    }

    //구글맵 카메라 세팅
    @Override
    public void onMapReady(GoogleMap map) {
        //구글맵 카메라 위치, 마커 추가
        LatLng INHA = new LatLng(37.451179, 126.653162);
        LatLng Door = new LatLng(37.451298, 126.654124);
        mMap = map;//구글 맵 객체 추가
        Marker Do = mMap.addMarker(new MarkerOptions().position(Door).title("인하대 5호관 입구 도착")); //오호관 포인트 마커 추가
        Marker In = mMap.addMarker(new MarkerOptions().position(INHA).title("인하대 5호관 건물")); //오호관 포인트 마커 추가
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(INHA,18)); //구글맵 화면 전환
        overlaySetBuilding(); //오버레이만드는 함수 제작

    }

    public void overlaySetBuilding() {
        LatLngBounds building = new LatLngBounds(       //오른쪽 위 왼쪽 아래 모서리 두개 잡고 이미지 오버레이 시키기
                new LatLng(37.450355, 126.651892),       // South west corner 남 서
                new LatLng(37.452054, 126.654249));      // North east corner 북 동
        GroundOverlayOptions fifthFirstMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.fifth_building)) //만든이미지 res/mipmap에 추가함
                .positionFromBounds(building);//오버레이 좌표 적용
        GroundOverlay imageOverlay = mMap.addGroundOverlay(fifthFirstMap);//화면에 오버레이 띄우기
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

            SQLiteDatabase db = openOrCreateDatabase(COPY2DATABASE_NAME, Context.MODE_PRIVATE, null);


            Cursor cur = db.rawQuery("SELECT * From Classes", null);


            cur.moveToFirst();


            Log.i("move!!!", "" + cur.getString(0));
            //TextView tv = (TextView) findViewById(R.id.textView);
            String text1 = cur.getString(1);
            String text2 = cur.getString(2);
            double langitute = Double.parseDouble(text1);
            double longitute = Double.parseDouble(text2);
            Log.i("수행", "경도 :" + langitute);
            Log.i("수행", "위도 :" + longitute);
            Marker1 = new LatLng(langitute, longitute);


            //tv.setText(text);
        } catch (Exception e) {
            Log.i("_)", "" + e.toString());
        }

    }

    public void searchClass(View v) {
    //오른쪽에 있는 버튼을 클릭했을때 불리는 콜백함수

        try {
            EditText classNum = (EditText)findViewById(R.id.src_text);
            //텍스트에 입력한 문자를 가지고옮
            Log.i("수행", "" + classNum);
            int classNo = Integer.parseInt(classNum.getText().toString());
            //텍스트에서 가져온 문자를 정수로 변환 시켜줌


            SQLiteDatabase db = openOrCreateDatabase(COPY2DATABASE_NAME, Context.MODE_PRIVATE, null);
            //저장된 데이터베이스 포인터를 만들어줌
            if(classNo != 0) {
                String sql = "SELECT * From Classes Where room_no = " + classNo;
                //검색한 방의 번호와 같은 리스트만 sql로 처리함
                Cursor cur = db.rawQuery(sql, null);
                //데이터베이스에서 sql로 처리된 테이블에 cursor를 만듦

                cur.moveToFirst();
                //커서를 데이터베이스의 0,0 즉 맨 처음 부분에 가져감

                Log.i("move!!!", "" + cur.getString(0));
                //TextView tv = (TextView) findViewById(R.id.textView);
                String text1 = cur.getString(1);
                String text2 = cur.getString(2);
                double langitute = Double.parseDouble(text1);
                double longitute = Double.parseDouble(text2);
                //테이블의 1,2번째 칼럼 위도 경도를 실수로 저장함
                Log.i("수행", "방번호 :" + cur.getString(0));
                Log.i("수행", "경도 :" + langitute);
                Log.i("수행", "위도 :" + longitute);
                Marker1 = new LatLng(langitute, longitute);
                //포지션을 저장
                int flag = 0;
                int count = 0;
                for (Marker i : markers) {
                    //마커를 배열에 저장하여 배열에 검색한 마커가 있을 경우 추가하지 않음
                   String temp = "Class " + classNo;
                    count++;
                    Log.i("수행", "이름 : " + count);
                    Log.i("수행", "이름 : " + i.getTitle());
                    if (i.getTitle().equals("Class " + classNo)) {
                        flag = 1;
                        //같은 마커가 존재하면 플래그를 세움
                    }
                }
                if (flag != 1) {
                    Log.i("수행", "추가 :" + classNo);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(Marker1).title("Class " + classNo)); //새로운 마커 추가
                    //마커를 추가하고
                    markers.add(marker);
                    //리스트에 마커를 삽입함 .
                }

                //tv.setText(text);
            }else if(classNo == 0){
                //입력값이 0일 경우 데이터베이스의 모든 클래스를 마커로 저장 / 표시
                String sql = "SELECT * From Classes";
                Cursor cur = db.rawQuery(sql, null);
                cur.moveToFirst();
                int end = cur.getCount();
                //데이터 베이스의 row의 갯수를 구함
                for(int i=0; i < end; i++){
                    Log.i("move!!!", "" + cur.getString(0));
                    //TextView tv = (TextView) findViewById(R.id.textView);
                    String text1 = cur.getString(1);
                    String text2 = cur.getString(2);
                    double langitute = Double.parseDouble(text1);
                    double longitute = Double.parseDouble(text2);
                    Log.i("수행", "방번호 :" + cur.getString(0));
                    Log.i("수행", "경도 :" + langitute);
                    Log.i("수행", "위도 :" + longitute);
                    Marker1 = new LatLng(langitute, longitute);
                    int flag = 0;
                    int count = 0;
                    for (Marker j : markers) {
                        count++;
                        Log.i("수행", "이름 : " + count);
                        Log.i("수행", "이름 : " + j.getTitle());
                        if (j.getTitle().equals("Class " + cur.getString(0))) {
                            flag = 1;
                        }
                    }
                    if (flag != 1) {
                        Log.i("수행", "추가 :" + cur.getString(0));
                        Marker marker = mMap.addMarker(new MarkerOptions().position(Marker1).title("Class " + cur.getString(0))); //새로운 마커 추가
                        markers.add(marker);
                    }
                    cur.moveToNext();
                    //row를 계속 옮겨줌 1, 2, 3 .... 모든 row를 마커로 추가함
                }
            }
        } catch (Exception e) {
            Log.i("_)", "" + e.toString());
        }

    }

    public void deleteClass(View v) {


        try {
            EditText classNum = (EditText) findViewById(R.id.src_text);
            Log.i("수행", "" + classNum);
            int classNo = Integer.parseInt(classNum.getText().toString());
            SQLiteDatabase db = openOrCreateDatabase(COPY2DATABASE_NAME, Context.MODE_PRIVATE, null);

            if (classNo != 0) {
                String sql = "SELECT * From Classes Where room_no = " + classNo;
                Cursor cur = db.rawQuery(sql, null);


                cur.moveToFirst();


                Log.i("move!!!", "" + cur.getString(0));
                //TextView tv = (TextView) findViewById(R.id.textView);
                String text1 = cur.getString(1);
                String text2 = cur.getString(2);
                double langitute = Double.parseDouble(text1);
                double longitute = Double.parseDouble(text2);
                Log.i("수행", "방번호 :" + cur.getString(0));
                Log.i("수행", "경도 :" + langitute);
                Log.i("수행", "위도 :" + longitute);
                Marker1 = new LatLng(langitute, longitute);
                int flag = 0;
                int count = 0;
                for (Marker i : markers) {
                    String temp = "Class " + classNo;
                    count++;
                    Log.i("수행", "이름 : " + count);
                    Log.i("수행", "이름 : " + i.getTitle());
                    if (i.getTitle().equals("Class " + classNo)) {
                        Log.i("수행", "제거 :" + i.getTitle());
                        i.remove();
                        // 마커표시를 제거
                        markers.remove(i);
                        // 리스트에서 마커를 제거
                    }
                }

            }else if(classNo == 0){
                //입력값이 0일 경우 모든 마커 제거 리스트 초기화
                String sql = "SELECT * From Classes";
                Cursor cur = db.rawQuery(sql, null);
                cur.moveToFirst();
                int end = cur.getCount();
                //데이터 베이스의 row의 갯수를 구함
                for(int i=0; i < end; i++){
                    Log.i("move!!!", "" + cur.getString(0));
                    //TextView tv = (TextView) findViewById(R.id.textView);
                    String text1 = cur.getString(1);
                    String text2 = cur.getString(2);
                    double langitute = Double.parseDouble(text1);
                    double longitute = Double.parseDouble(text2);
                    Log.i("수행", "방번호 :" + cur.getString(0));
                    Log.i("수행", "경도 :" + langitute);
                    Log.i("수행", "위도 :" + longitute);
                    Marker1 = new LatLng(langitute, longitute);
                    int flag = 0;
                    int markEnd = markers.size();
                    int count = 0;
                    for (Iterator it = markers.iterator(); it.hasNext();) {
                        //이터레이터로 리스트를 for문 돌리기
                        count++;
                        Marker j = (Marker)it.next();
                        Log.i("수행", "이름 : " + count);
                        Log.i("수행", "이름 : " + j.getTitle());
                        if (j.getTitle().equals("Class " + cur.getString(0))) {
                            Log.i("수행", "제거 :" + j.getTitle());
                            j.remove();
                            //for문 돌면서 리스트에 있는 모든 마커표시를 제거
                            break;
                        }
                    }
                    cur.moveToNext();
                    //row를 계속 옮겨줌 1, 2, 3 .... 모든 row를 마커로 제거함
                }
                markers.clear();
                //마커를 다 제거한 후에 리스트를 초기화
            }
                //tv.setText(text);
            }catch(Exception e){
                Log.i("_)", "" + e.toString());
            }
        }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoRegion) {
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoRegion.getUniqueIdentifier() + ", number of beacons ranged: " + recoBeacons.size());
        mRangingListAdapter.updateAllBeacons(recoBeacons);
        mRangingListAdapter.notifyDataSetChanged();
        //Write the code when the beacons in the region is received
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion recoBeaconRegion, RECOErrorCode errorCode) {
        Log.i("RECORangingActivity", "error code = " + errorCode);
        //Write the code when the RECOBeaconService is failed to range beacons in the region.
        //See the RECOErrorCode in the documents.
        return;
    }
}
