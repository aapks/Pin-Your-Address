package com.zahran.pinyouraddress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;


import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.CameraPosition;
import com.huawei.hms.maps.model.CameraUpdateParam;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.util.LogM;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created By Mahmoud HUSSEIN 01-03-2020
 * Using HMS "Huawei Mobile Services" to add map and get location.
 *
 * using the following Tutorials
 * LocationKit: https://developer.huawei.com/consumer/en/codelab/HMSLocationKit/index.html#0
 * MapKit: https://developer.huawei.com/consumer/en/codelab/HMSMapKit/index.html#0
 *
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, HuaweiMap.OnCameraIdleListener,
        HuaweiMap.OnCameraMoveStartedListener, HuaweiMap.OnCameraMoveListener {

    private static final String TAG = "MapViewDemoActivity";
    //Huawei map
    private HuaweiMap hMap;
    CameraPosition build;
    CameraUpdate cameraUpdate;
    private static final int REQUEST_CODE = 100;
    private MapView mMapView;
    private Marker mMarker;
    private Marker myLocationMarker;

    Geocoder geocoder;
    List<Address> addresses = new ArrayList<>();
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingsClient;

   private static LatLng mLAT_LNG = null;


    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
    };

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LogM.d(TAG, "onCreate:hzj");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check permissions and get it if app don't have it.
        if (!hasPermissions(this, RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE);
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "sdk < 28 Q");
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, strings, 1);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED){
                String[] strings = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        "android.permission.ACCESS_BACKGROUND_LOCATION"};
                ActivityCompat.requestPermissions(this, strings, 2);
            }
        }

//       Create a location provider client and device setting client.
//       create a fusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //create a settingsClient
        mSettingsClient = LocationServices.getSettingsClient(this);
//        Create a location information request.
        mLocationRequest = new LocationRequest();
        // Sets the interval for location update (unit: Millisecond)
        mLocationRequest.setInterval(50000);
        // Sets the priority to define accuracy needed for location.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //get current location and put it with star mark on the map for first init for map!
        if (null == mLocationCallback) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        List<Location> locations = locationResult.getLocations();
                        if (!locations.isEmpty()) {
                            for (Location location : locations) {
//                                save current location LatLng
                                mLAT_LNG=new LatLng(location.getLatitude(), location.getLongitude());
                                removeLocationUpdatesWithCallback();
//                                set current location to the map
                                setLocationOnMap(hMap, mLAT_LNG, myLocationMarker,true);

                              Log.i(TAG,
                                        "onLocationResult location[Longitude,Latitude,Accuracy]:" + location.getLongitude()
                                                + "," + location.getLatitude() + "," + location.getAccuracy());
                            }
                        }
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (locationAvailability != null) {
                        boolean flag = locationAvailability.isLocationAvailable();
                        Log.i(TAG, "onLocationAvailability isLocationAvailable:" + flag);
                    }
                }
            };
        }

//        request location if it doesn't put in LatLng variable
        if (mLAT_LNG==null)
        requestLocationUpdatesWithCallback();




        //get mapview instance
        mMapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        //get map instance
        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(HuaweiMap map) {
        //get map instance in a callback method
        Log.d(TAG, "onMapReady: ");
        hMap = map;

//         Enable the my-location layer.
//         hMap.setMyLocationEnabled(true);
//        Enable the function of displaying the my-location icon.
//        hMap.getUiSettings().setMyLocationButtonEnabled(true);

//        Specify whether to enable the zoom controls.
        hMap.getUiSettings().setZoomControlsEnabled(true);
        // Specify whether to enable the compass.
        hMap.getUiSettings().setCompassEnabled(true);

//        use geocoder to get Address information using LatLng
        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());


//        request location if it still not saved to LatLng variable
        if (mLAT_LNG == null) {
            requestLocationUpdatesWithCallback();
        } else
        {
            setLocationOnMap(hMap, mLAT_LNG, myLocationMarker,true);
            removeLocationUpdatesWithCallback();
        }

//        Map click Listener
        hMap.setOnMapClickListener(new HuaweiMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
           //     Toast.makeText(getApplicationContext(), "onMapClick:" + latLng.toString(), Toast.LENGTH_SHORT).show();

                // mark can be add by HuaweiMap
                hMap.clear();
                if (mLAT_LNG == null) {
                    requestLocationUpdatesWithCallback();
                } else
                {

                    setLocationOnMap(hMap, mLAT_LNG, myLocationMarker,true);
                    removeLocationUpdatesWithCallback();
                }

                try {
                    setLocationOnMap(hMap, latLng, mMarker,false);
                }catch (Exception e){
                    Log.d(TAG, "onMapClick: Exception"+e.getMessage());
                    e.printStackTrace();
                }



            }
        });
        hMap.setOnCameraMoveStartedListener(this);
        hMap.setOnCameraIdleListener(this);
        hMap.setOnCameraMoveListener(this);
        hMap.setOnMapLoadedCallback(new HuaweiMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.i(TAG, "onMapLoaded:successful");
            }
        });

    }


    /**
     *   added by Mahmoud Hussein
     *   Set location on map custom function to add marker with address on map
     *
     * The Following params used in function:-
     *
     *      HuaweiMap huaweiMap: used to control Map.
     *      LatLng latLng: this is Location LatLng.
     *      Marker marker: this marker used to be added in Map.
     *      boolean myLocationFlag: this flag used to define is that current location or selected from Map.
     *
     */
    private void setLocationOnMap(HuaweiMap huaweiMap,LatLng latLng,Marker marker,boolean myLocationFlag){


        if(myLocationFlag){
            marker = huaweiMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_stars_black_24))
                    .clusterable(true));
        }else{
            marker = huaweiMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.round_person_pin_circle_black_24))
                    .clusterable(true));
        }
        //        move camera by CameraPosition param ,to my location latlag and zoom params can set here
            build = new CameraPosition.Builder().target(marker.getPosition()).zoom(12).build();
            cameraUpdate = CameraUpdateFactory.newCameraPosition(build);

            try {
                // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            if (!addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                marker.setTitle(address);
                marker.showInfoWindow();
            }
            // set camera update to change camera position to marker position
            cameraUpdate.getCameraUpdate().setNewLatLngZoom(new CameraUpdateParam.NewLatLngZoom(marker.getPosition(), 9));
            huaweiMap.animateCamera(cameraUpdate);
            // set zoom configuration
            huaweiMap.setMaxZoomPreference(13);
            huaweiMap.setMinZoomPreference(1);




    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: request running");
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocationUpdatesWithCallback();
        mMapView.onDestroy();

    }
    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /**
     * Requests a location update and calls back on the specified Looper thread.
     */
    private void requestLocationUpdatesWithCallback() {
        try {

            if (mLAT_LNG==null){
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();
            // Before requesting location update, invoke checkLocationSettings to check device settings.
            Task<LocationSettingsResponse> locationSettingsResponseTask = mSettingsClient.checkLocationSettings(locationSettingsRequest);
            locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    Log.i(TAG, "check location settings success");
                    mFusedLocationProviderClient
                            .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                   Log.i(TAG, "requestLocationUpdatesWithCallback onSuccess");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Log.e(TAG,
                                            "requestLocationUpdatesWithCallback onFailure:" + e.getMessage());
                                }
                            });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                           Log.e(TAG, "checkLocationSetting onFailure:" + e.getMessage());
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        //When the startResolutionForResult is invoked, a dialog box is displayed, asking you to open the corresponding permission.
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(MainActivity.this, 0);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.e(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                            }
                        }
                    });}else{
                removeLocationUpdatesWithCallback();
            }
        } catch (Exception e) {
            Log.e(TAG, "requestLocationUpdatesWithCallback exception:" + e.getMessage());
        }
    }
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // Callback when the camera starts moving
    @Override
    public void onCameraMoveStarted(int reason) {
        Log.i(TAG, "onCameraMoveStarted: successful");
    }
    // Callback when the camera stops moving
    @Override
    public void onCameraIdle() {
        Log.i(TAG, "onCameraIdle: successful");
    }
    // Callback during camera moving
    @Override
    public void onCameraMove() {

        Log.i(TAG, "onCameraMove: successful");
    }
    /**
     * Removed when the location update is no longer required.
     */
    private void removeLocationUpdatesWithCallback() {
        try {
            Task<Void> voidTask = mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            voidTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i(TAG, "removeLocationUpdatesWithCallback onSuccess");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "removeLocationUpdatesWithCallback onFailure:" + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "removeLocationUpdatesWithCallback exception:" + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSSION  failed");
            }
        }

        if (requestCode == 2) {
            if (grantResults.length > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION  failed");
            }
        }
    }

}
