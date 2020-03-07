package com.frost.firebasedb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.frost.firebasedb.databinding.ActivityLocationBinding;
import com.frost.firebasedb.models.Bus;
import com.frost.firebasedb.models.Location;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityLocationBinding binding;
    private GoogleMap googleMap;
    private Bus bus;
    private Marker busMarker;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference busesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpFirebase();
        if (getIntent().getParcelableExtra("Bus") != null) {
            bus = getIntent().getParcelableExtra("Bus");
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location);
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1439);
        } else {
            createLocationRequest();
        }

        setUp();
    }

    private void setUpFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        busesReference = firebaseDatabase.getReference("buses");
    }

    private void setUp() {
        binding.tvAddNewLocation.setOnClickListener(v -> {
            binding.llNewLocation.setVisibility(View.VISIBLE);
            binding.rlLoader.setVisibility(View.VISIBLE);
            if (googleMap != null) {
                googleMap.clear();
            }
        });


        binding.tvUpdate.setOnClickListener(v -> {

            if (!Utility.isNetworkAvailable(LocationActivity.this)) {
                Utility.showSnackBar(this, binding.getRoot(), "Please check internet connection", 2);
                return;
            }
            showLoader(true);
            bus.setCurrent(new Location(googleMap.getCameraPosition().target.latitude,
                    googleMap.getCameraPosition().target.longitude,
                    Utility.getUTCTime()));
            busesReference.child(bus.getBusId()).setValue(bus).addOnCompleteListener(this, task -> {
                showLoader(false);
                if (task.isSuccessful()) {
                    Utility.showSnackBar(this, binding.getRoot(), "Location updated successfully", 1);
                } else {
                    task.getException().printStackTrace();
                    Utility.showSnackBar(this, binding.getRoot(), "Failed to update: " + task.getException().getMessage(), 1);
                }

            });


        });


        binding.imgClose.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;


        if (ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            this.googleMap.setMyLocationEnabled(true);


        if (bus != null && bus.getCurrent() != null) {
            binding.tvTitle.setText(bus.getName() + " Location");
            addBusMarker(new LatLng(bus.getCurrent().getLatitude(), bus.getCurrent().getLongitude()));
            zoomToPosition(new LatLng(bus.getCurrent().getLatitude(), bus.getCurrent().getLongitude()), 15);
        } else {
            Toast.makeText(LocationActivity.this, "Location is not available", Toast.LENGTH_SHORT).show();
        }


    }


    public void addBusMarker(LatLng latLng) {

        if (busMarker != null) {
            busMarker.setPosition(latLng);
            return;
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(getBitmapDescriptor(R.drawable.ic_bus, 80, 80));
        markerOptions.anchor(.5f, .5f);
        markerOptions.position(latLng);
        busMarker = googleMap.addMarker(markerOptions);
    }

    private BitmapDescriptor getBitmapDescriptor(int id, int right, int bottom) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, id);
        vectorDrawable.setBounds(0, 0, right, bottom);
        Bitmap bm = Bitmap.createBitmap(right, bottom, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();

        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }


    public void zoomToPosition(LatLng location, float value) {
        CameraPosition.Builder builder = CameraPosition.builder();
        builder.target(location);
        builder.zoom(value);
        CameraPosition cameraPosition = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);

    }


    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1439) {
            if (ContextCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                googleMap.setMyLocationEnabled(true);
        }
    }


    public void showLoader(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.tvUpdate.setVisibility(!show ? View.VISIBLE : View.GONE);
    }


    public void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(3 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .addLocationRequest(LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY));

        builder.setAlwaysShow(true); //this is the key ingredient

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
            } catch (ApiException exception) {
                exception.printStackTrace();
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(LocationActivity.this, 101);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
