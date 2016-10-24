package csi.testapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.R.attr.offset;


/**
 * Created by saurus on 2016. 10. 1..
 */

public class NextActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap; // 뷰 맵 객체 생성
    BitmapFactory.Options option;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity);
        //구글맵 뷰에 띄우기
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        option = new BitmapFactory.Options();


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
        mMap.addMarker(new MarkerOptions().position(Door).title("인하대 5호관 입구 도착")); //오호관 포인트 마커 추가
        mMap.addMarker(new MarkerOptions().position(INHA).title("인하대 5호관 건물")); //오호관 포인트 마커 추가
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(INHA,18)); //구글맵 화면 전환
        overlaySetBuilding(); //오버레이만드는 함수 제작

    }

    public void overlaySetBuilding() {


        LatLngBounds building = new LatLngBounds(       //오른쪽 위 왼쪽 아래 모서리 두개 잡고 이미지 오버레이 시키기
                new LatLng(37.450355, 126.651892),       // South west corner 남 서
                new LatLng(37.452054, 126.654249));      // North east corner 북 동
        GroundOverlayOptions fifthFirstMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.two)) //만든이미지 res/mipmap에 추가함
                .positionFromBounds(building);//오버레이 좌표 적용
        GroundOverlay imageOverlay = mMap.addGroundOverlay(fifthFirstMap);//화면에 오버레이 띄우기
    }
}