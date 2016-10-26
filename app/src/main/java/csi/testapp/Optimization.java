package csi.testapp;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;


import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapView;

/**
 * Created by lab1 on 10/26/16.
 */

public class Optimization extends AppCompatActivity{
    //티맵 관련 변수들
    public static String mApiKey = "8bdbb125-7d59-3684-84ff-ad4b5bb59e74";
    private TMapView mMapView = null;
    private RelativeLayout mMainRelativeLayout=null;
    private LocationManager locationManager;
    TMapGpsManager gps;
    //티맵 포인터들
    Bitmap start;
    Bitmap end;
    Bitmap location;
    //위도경도 새로 설정해야할때
    double n_Latitude = 0;
    double n_Longitude = 0;

    //출발위치 변하지 않게 하기 위해
    int check = 0;

    //지도와 버튼들 처음 초기화 시켜주는 함수
    void initView(){
        mMapView.setSKPMapApiKey(mApiKey);
        gps=new TMapGpsManager(this);
        //지도를 처음 띄우면 보이는 화면 설정
        //현재위치,지도 중심부도 처음엔 학교 중심부
        mMapView.setCenterPoint(126.65318, 37.449666);
        mMapView.setLocationPoint(126.65318, 37.449666);
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mMapView.setZoomLevel(17);

        //지도 아이콘들 셋팅
        location = BitmapFactory.decodeResource(getResources(), R.drawable.location);
        start = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        end = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        mMapView.setIcon(location);
        mMapView.setIconVisibility(true);
        mMapView.setSightVisible(true);
        mMapView.setTMapPathIcon(start ,end);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }



}
