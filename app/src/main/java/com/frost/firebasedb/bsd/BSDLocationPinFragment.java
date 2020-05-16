package com.frost.firebasedb.bsd;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.frost.firebasedb.R;
import com.frost.firebasedb.databinding.BsdLocationPinBinding;
import com.frost.firebasedb.models.PinPoint;
import com.google.android.gms.common.api.ApiException;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Created by Gokul Kalagara (Mr. Psycho) on 16-05-2020.
 * <p>
 * Frost
 */
public class BSDLocationPinFragment extends BottomSheetDialogFragment implements OnMapReadyCallback {


    private Marker pinMarker;
    private BsdLocationPinBinding binding;
    private GoogleMap googleMap;
    private PinPoint pinPoint;
    private boolean start;
    private IBSDLocationPinFragment ibsdLocationPinFragment;

    public BSDLocationPinFragment(PinPoint pinPoint, boolean start, IBSDLocationPinFragment ibsdLocationPinFragment) {
        this.pinPoint = pinPoint;
        this.start = start;
        this.ibsdLocationPinFragment = ibsdLocationPinFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        binding = DataBindingUtil.inflate(inflater, R.layout.bsd_location_pin, container, false);
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        setUp();


        return binding.getRoot();
    }

    private void setUp() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1439);
        } else {
            createLocationRequest();
        }

        if (pinPoint != null) {
            binding.etLocationName.setText(pinPoint.getLocationName());
        }


        binding.tvUpdate.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.etLocationName.getText())) {
                Toast.makeText(getActivity(), "Location name not be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pinPoint == null)
                pinPoint = new PinPoint();

            pinPoint.setLocationName(binding.etLocationName.getText().toString());
            pinPoint.setLatitude(googleMap.getCameraPosition().target.latitude);
            pinPoint.setLongitude(googleMap.getCameraPosition().target.longitude);
            ibsdLocationPinFragment.updatePinPoint(start, pinPoint);
            dismiss();

        });

        binding.imgClose.setOnClickListener(v -> {
            dismiss();
        });
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
                LocationServices.getSettingsClient(getActivity()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
            } catch (ApiException exception) {
                exception.printStackTrace();
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(getActivity(), 101);
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        if (dialog == null)
            return super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(bottomSheetDialog -> {
            BottomSheetDialog d = (BottomSheetDialog) bottomSheetDialog;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            // Right here!
            BottomSheetBehavior.from(bottomSheet)
                    .setState(BottomSheetBehavior.STATE_EXPANDED);
        });


        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    return true;
                }
            }
            return true;
        });


        return dialog;
    }


    @Override
    public void onStart() {
        super.onStart();
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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            this.googleMap.setMyLocationEnabled(true);


        if (pinPoint != null) {
            addPinMarker(new LatLng(pinPoint.getLatitude(), pinPoint.getLongitude()));
            zoomToPosition(new LatLng(pinPoint.getLatitude(), pinPoint.getLongitude()), 18);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1439) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    public interface IBSDLocationPinFragment {
        void updatePinPoint(boolean start, PinPoint pinPoint);
    }

    public void zoomToPosition(LatLng location, float value) {
        if (googleMap == null)
            return;

        CameraPosition.Builder builder = CameraPosition.builder();
        builder.target(location);
        builder.zoom(value);
        CameraPosition cameraPosition = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);

    }


    public void addPinMarker(LatLng latLng) {

        if (pinMarker != null) {
            pinMarker.setPosition(latLng);
            return;
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(getBitmapDescriptor(R.drawable.ic_flag_black, 80, 80));
        markerOptions.anchor(.5f, .5f);
        markerOptions.position(latLng);
        pinMarker = googleMap.addMarker(markerOptions);
    }

    private BitmapDescriptor getBitmapDescriptor(int id, int right, int bottom) {
        Drawable vectorDrawable = ContextCompat.getDrawable(getActivity(), id);
        vectorDrawable.setBounds(0, 0, right, bottom);
        Bitmap bm = Bitmap.createBitmap(right, bottom, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }


}
