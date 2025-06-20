package com.example.dailycommute;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UpdateStoreLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText destinationInput;
    private Button findLocationBtn, updateLocationBtn;
    private MapView mapView;
    private GoogleMap gMap;
    private Marker marker;
    private double selectedLatitude = 0, selectedLongitude = 0;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String destinationName;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "UpdateLocationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_store_location);

        // Get current user
        auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        // Reference to user's locations in Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid).child("locations");

        // Get destination name from Intent
        destinationName = getIntent().getStringExtra("destination");
        if (destinationName == null || destinationName.trim().isEmpty()) {
            destinationName = "Unknown";
        }

        // Initialize UI
        destinationInput = findViewById(R.id.updatesdestinationInput);
        findLocationBtn = findViewById(R.id.updatesfindLocationBtn);
        updateLocationBtn = findViewById(R.id.updateLocationBtn); // reuse button ID


        mapView = findViewById(R.id.updatemapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        findLocationBtn.setOnClickListener(v -> findLocation());
        updateLocationBtn.setOnClickListener(v -> updateDestination());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        if (gMap == null) {
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
            return;
        }

        gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        gMap.setOnMapClickListener(latLng -> {
            if (marker != null) marker.remove();

            marker = gMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            selectedLatitude = latLng.latitude;
            selectedLongitude = latLng.longitude;

            getAddressFromCoordinates(selectedLatitude, selectedLongitude);
        });

        if (!destinationName.equals("Unknown")) {
            destinationInput.setText(destinationName);
            findLocation();
        }
    }

    private void findLocation() {
        String locationName = destinationInput.getText().toString().trim();
        if (locationName.isEmpty()) {
            Toast.makeText(this, "Enter a destination", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedLatitude = address.getLatitude();
                selectedLongitude = address.getLongitude();

                LatLng newLocation = new LatLng(selectedLatitude, selectedLongitude);
                if (marker != null) marker.remove();
                marker = gMap.addMarker(new MarkerOptions().position(newLocation).title(locationName));
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 14));
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error fetching location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                destinationInput.setText(address.getAddressLine(0));
            } else {
                destinationInput.setText("Unknown Address");
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error fetching address", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void updateDestination() {
        if (destinationName.equals("Unknown")) {
            Toast.makeText(this, "Invalid destination name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLatitude == 0 && selectedLongitude == 0) {
            Toast.makeText(this, "Select a valid location first", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to update destination: " + destinationName + " Lat: " + selectedLatitude + " Lon: " + selectedLongitude);

        databaseReference.child(destinationName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    databaseReference.child(destinationName).child("latitude").setValue(selectedLatitude);
                    databaseReference.child(destinationName).child("longitude").setValue(selectedLongitude);
                    Toast.makeText(UpdateStoreLocationActivity.this, "Location updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UpdateStoreLocationActivity.this, "Destination does not exist to update", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(UpdateStoreLocationActivity.this, "Failed to check destination existence", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
