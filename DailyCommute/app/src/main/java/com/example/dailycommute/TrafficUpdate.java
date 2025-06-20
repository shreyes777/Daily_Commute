package com.example.dailycommute;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;

public class TrafficUpdate extends AppCompatActivity {
    private String location;
    private String status;

    public TrafficUpdate(String location, LatLng status)
    {
        this.location = location;
        this.status = String.valueOf(status);
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }
}
