package com.example.app_pro.duan.com.duan.google_map;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.app_pro.duan.R;
import com.example.app_pro.duan.com.duan.data.MyDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.katso.livebutton.LiveButton;

public class ThongTinMapActivity extends FragmentActivity implements OnMapReadyCallback,
        DirectionFinderListener {

    private GoogleMap mMap;
    LiveButton btnTenDuong, btnVeTinh, btnDiaHinh, btnFindPath, btnKm, btnTime;
    EditText edtDiemBatDau, edtDiemDen;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    //Address
    public static String START = "khoa hoc tu nhien";
    public static String END = "ben thanh";

    //Init databse
    final String DATABASE_NAME = "appnangcao.sqlite";
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_detail);
        init();
        //Lấy dữ liệu address
        START = getIntent().getStringExtra("START_ADDRESS");
        END = getIntent().getStringExtra("END_ADDRESS");

        setAddress();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        addEvent();
    }

    private void setAddress() {
        edtDiemBatDau.setText(START);
        edtDiemDen.setText(END);
    }

    private void addEvent() {
        btnTenDuong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });
        btnDiaHinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });
        btnVeTinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        });
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });

    }

    private void sendRequest() {
        String origin = edtDiemBatDau.getText().toString();
        String destination = edtDiemDen.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập điểm bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập điểm đến", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        btnTenDuong = findViewById(R.id.btnTenDuong);
        btnDiaHinh = findViewById(R.id.btnDiaHinh);
        btnVeTinh = findViewById(R.id.btnVeTinh);
        edtDiemBatDau = findViewById(R.id.edtdiemBatDau);
        edtDiemDen = findViewById(R.id.edtdiemDen);
        btnFindPath = findViewById(R.id.btnFindPath);
        btnKm = findViewById(R.id.btnSoKm);
        btnTime = findViewById(R.id.btnTime);

        sqLiteDatabase = MyDatabase.initDatabase(ThongTinMapActivity.this, DATABASE_NAME);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(21.0261872, 105.8099592);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Vui lòng chờ...",
                "Đang tìm đường...!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 14));
            //*******************************************
            btnTime.setText(route.duration.text);
            btnKm.setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_dau))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_dich))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));

            //Update database
            String startaddress=route.startAddress;
            double startlat=route.startLocation.latitude;
            double startlng=route.startLocation.longitude;

            String endaddress=route.endAddress;
            double endlat=route.endLocation.latitude;
            double endlng=route.endLocation.longitude;

            //execute

            Random random=new Random();
            int index=1+random.nextInt(560);
            String ID="000"+index;

            ContentValues contentValues = new ContentValues();
            contentValues.put("id", ID);
            contentValues.put("startaddress", startaddress);
            contentValues.put("startlat", startlat);
            contentValues.put("startlng", startlng);

            contentValues.put("endaddress",endaddress);
            contentValues.put("endlat", endlat);
            contentValues.put("endlng", endlng);

            long check=sqLiteDatabase.insert("Findpath", null, contentValues);
            if(check>0){
                Toast.makeText(this, "The information is saved to the database successfully!", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
