package com.frost.firebasedb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.frost.firebasedb.databinding.ActivityDriverBinding;
import com.frost.firebasedb.models.Bus;
import com.frost.firebasedb.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private ActivityDriverBinding binding;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference busesReference;
    private DatabaseReference driverReference;
    private User user;
    private Bus bus;
    private Marker busMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpFireBaseDatabase();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver);
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);


        if (ContextCompat.checkSelfPermission(DriverActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            createLocationRequest();
        }
        setUp();
    }

    private void setUpFireBaseDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        busesReference = firebaseDatabase.getReference("buses");
        driverReference = firebaseDatabase.getReference("users");
    }


    private void setUp() {

        binding.imgProfile.setOnClickListener(v -> {
            startActivity(new Intent(DriverActivity.this, ProfileActivity.class));
        });

        binding.imgAction.setOnClickListener(v -> {
            if (bus != null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(DriverActivity.this);
                builder.setTitle(bus.isStatus() ? "Stop" : "Start");
                builder.setMessage("Do you want " + (bus.isStatus() ? "Stop" : "Start") + " the ride?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();

                    bus.setStatus(!bus.isStatus());
                    binding.imgAction.setImageResource(bus.isStatus() ? R.drawable.ic_stop_black_24dp : R.drawable.ic_record);

                    if (!bus.isStatus()) {
                        stopLocationUpdates();
                        updateBusStatus();
                    } else {
                        createLocationRequest();
                    }

                });

                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });


                builder.create().show();

            }
        });


        fetchDriverWithBusDetails();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ContextCompat.checkSelfPermission(DriverActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            this.googleMap.setMyLocationEnabled(true);


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


    private void createLocationRequest() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(DriverActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            googleApiClient.connect();
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .addLocationRequest(LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY));

        builder.setAlwaysShow(true); //this is the key ingredient

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                updateRequestLocation();
            } catch (ApiException exception) {
                exception.printStackTrace();
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(DriverActivity.this, 1439);
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

    public void updateRequestLocation() {
        fusedLocationProviderClient = null;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            zoomToPosition(new LatLng(location.getLatitude(), location.getLongitude()), 15);
                            if (bus == null) {
                                return;
                            }
                            if (!bus.isStatus()) { // NOT STARTED
                                return;
                            }
                            if (!Utility.isNetworkAvailable(DriverActivity.this)) {
                                Toast.makeText(DriverActivity.this, "Please check the internet", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            addBusMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                            updateBusLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                            return;
                        }
                    }
                }
            };
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }


    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private LocationCallback locationCallback;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1439) {
            if (ContextCompat.checkSelfPermission(DriverActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                googleMap.setMyLocationEnabled(true);

            createLocationRequest();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    updateRequestLocation();
                    break;
                case Activity.RESULT_CANCELED:
                    Utility.showSnackBar(DriverActivity.this, binding.getRoot(), "Cancelled location request", 2);
                    break;
                default:
                    updateRequestLocation();
                    break;
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopLocationUpdates();
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
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public void fetchDriverWithBusDetails() {
        showLoader(true);
        driverReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    showLoader(false);
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null)
                        getBusDetails(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utility.showSnackBar(DriverActivity.this,
                        binding.getRoot(), "Failed get profile: " + databaseError.getMessage(),
                        0);
                showLoader(false);

            }
        });

    }

    private void getBusDetails(User user) {
        this.user = user;
        if (user.getBusId() != 0) {
            showLoader(true);
            busesReference.child("" + user.getBusId()).getRef().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        showLoader(false);
                        Bus bus = dataSnapshot.getValue(Bus.class);
                        if (DriverActivity.this.bus != null)
                            return;

                        if (bus != null) {
                            DriverActivity.this.bus = bus;
                            binding.tvBusName.setText(bus.getName());
                            binding.tvNumber.setText(bus.getRegistrationNumber());
                            binding.tvNumber.setVisibility(View.VISIBLE);
                            binding.llBus.setVisibility(View.VISIBLE);
                            binding.imgAction.setVisibility(View.VISIBLE);
                            binding.imgAction.setImageResource(bus.isStatus() ? R.drawable.ic_stop_black_24dp : R.drawable.ic_record);
                            if (bus.isStatus()) {
                                Toast.makeText(DriverActivity.this, "Updating the bus location", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Utility.showSnackBar(DriverActivity.this,
                            binding.getRoot(), "Failed get bus details: " + databaseError.getMessage(),
                            0);
                    showLoader(false);

                }
            });
            return;
        }
        Utility.showSnackBar(DriverActivity.this, binding.getRoot(), "Bus details not found", 2);
    }

    private void showLoader(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateBusLocation(LatLng latLng) {
        showLoader(true);
        bus.setCurrent(new com.frost.firebasedb.models.Location(latLng.latitude, latLng.longitude,
                Utility.getUTCTime()));
        busesReference.child("" + user.getBusId()).setValue(bus).addOnCompleteListener(this, task -> {
            showLoader(false);
            if (task.isSuccessful()) {
            } else {
                task.getException().printStackTrace();
                Utility.showSnackBar(this, binding.getRoot(), "Failed to update: " + task.getException().getMessage(), 1);
            }

        });
    }

    private void updateBusStatus() {
        showLoader(true);
        busesReference.child("" + user.getBusId()).setValue(bus).addOnCompleteListener(this, task -> {
            showLoader(false);
            if (task.isSuccessful()) {
            } else {
                task.getException().printStackTrace();
                Utility.showSnackBar(this, binding.getRoot(), "Failed to update: " + task.getException().getMessage(), 1);
            }

        });
    }

}
