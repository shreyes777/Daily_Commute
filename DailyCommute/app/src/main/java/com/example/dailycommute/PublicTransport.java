package com.example.dailycommute;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PublicTransport extends AppCompatActivity {

    // UI elements
    private SearchView locationInput;
    private Button searchButton;
    private RecyclerView recyclerView;

    private List<Transport> transportList;
    private TransportAdapter adapter;
    private static final String API_KEY = "AIzaSyBn9Qfi7SqyfaIoRTFEXyEJLIuk2OfUx1M";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Set the content view to the activity's layout
        setContentView(R.layout.activity_public_transport);

        locationInput = findViewById(R.id.location_Inputfield_transport);
        recyclerView = findViewById(R.id.stops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transportList = new ArrayList<>();
        adapter = new TransportAdapter(transportList);
        recyclerView.setAdapter(adapter);

        Button searchButton = findViewById(R.id.search_station_btn);
        searchButton.setOnClickListener(view -> getCoordinatesFromLocation(locationInput.getQuery().toString()));

        Toolbar toolbar = findViewById(R.id.publictransport_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Public Transport");

        }

        // Handle edge-to-edge display insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getCoordinatesFromLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                fetchTransportData(latitude, longitude);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(PublicTransport.this, MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        return true;
    }

private void fetchTransportData(double latitude, double longitude) {
    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
            "location=" + latitude + "," + longitude + "&radius=500&type=transit_station&key=" + API_KEY;

    RequestQueue queue = Volley.newRequestQueue(this);
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    JSONArray results = response.getJSONArray("results");
                    transportList.clear();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject place = results.getJSONObject(i);
                        String name = place.getString("name");
                        String vicinity = place.getString("vicinity");
                        String type = "Unknown";
                        JSONArray types = place.getJSONArray("types");
                        for (int j = 0; j < types.length(); j++) {
                            String typeStr = types.getString(j);
                            if (typeStr.contains("bus")) type = "Bus Station";
                            if (typeStr.contains("subway")) type = "Metro Station";
                            if (typeStr.contains("train")) type = "Train Station";
                        }
                        String imageUrl = "https://maps.gstatic.com/mapfiles/place_api/icons/bus-71.png";

                        transportList.add(new Transport(name, type, vicinity, imageUrl));
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, Throwable::printStackTrace);
    queue.add(request);
}
}
