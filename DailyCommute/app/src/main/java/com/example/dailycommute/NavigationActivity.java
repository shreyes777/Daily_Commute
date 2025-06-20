package com.example.dailycommute;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import org.json.*;

import java.util.*;

public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double destinationLat, destinationLng;
    private Polyline currentPolyline;
    private Marker userMarker;
    private Circle userCircle;
    private TextView instructionTextView;
    private List<String> instructionsList;
    private int instructionIndex = 0;

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String API_KEY = "AIzaSyBn9Qfi7SqyfaIoRTFEXyEJLIuk2OfUx1M"; // Replace with your actual API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mapView = findViewById(R.id.mapView);
        instructionTextView = findViewById(R.id.instructionTextView);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Bundle mapViewBundle = savedInstanceState != null ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        destinationLat = getIntent().getDoubleExtra("latitude", 0);
        destinationLng = getIntent().getDoubleExtra("longitude", 0);
        instructionsList = new ArrayList<>();

        requestLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.addMarker(new MarkerOptions().position(new LatLng(destinationLat, destinationLng)).title("Destination"));
    }

    private void requestLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000); // Update every 3 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    updateLocationOnMap(location);
                    fetchRoute(location.getLatitude(), location.getLongitude());
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateLocationOnMap(Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Update marker
        if (userMarker != null) {
            userMarker.setPosition(userLatLng);
        } else {
            userMarker = googleMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
        }

        // Update blue circle
        if (userCircle != null) {
            userCircle.setCenter(userLatLng);
        } else {
            userCircle = googleMap.addCircle(new CircleOptions()
                    .center(userLatLng)
                    .radius(10) // 10 meters
                    .strokeColor(0x550000FF) // Transparent blue
                    .fillColor(0x220000FF)); // Light blue fill
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));
    }

    private void fetchRoute(double userLat, double userLng) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + userLat + "," + userLng +
                "&destination=" + destinationLat + "," + destinationLng + "&mode=driving&key=" + API_KEY;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> drawRoute(response),
                error -> Toast.makeText(NavigationActivity.this, "Error fetching route", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void drawRoute(JSONObject response) {
        try {
            JSONArray routes = response.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");

                List<LatLng> decodedPath = decodePolyline(encodedPolyline);

                if (currentPolyline != null) {
                    currentPolyline.remove();
                }
                currentPolyline = googleMap.addPolyline(new PolylineOptions()
                        .addAll(decodedPath)
                        .width(12)
                        .color(0xFF0000FF)); // Blue color

                // Extract turn-by-turn instructions
                extractInstructions(route);
                if (!instructionsList.isEmpty()) {
                    instructionTextView.setText(instructionsList.get(0));
                    instructionIndex = 0;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            polyline.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return polyline;
    }

    private void extractInstructions(JSONObject route) throws JSONException {
        instructionsList.clear();
        JSONArray legs = route.getJSONArray("legs");
        JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");

        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.getJSONObject(i);
            String instruction = step.getString("html_instructions").replaceAll("<.*?>", ""); // Remove HTML tags
            instructionsList.add(instruction);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
