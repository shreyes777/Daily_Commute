package com.example.dailycommute;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlanCommute extends AppCompatActivity {

    private SearchView startLocation, destinationLocation;
    private TextView timeTextView;
    private RecyclerView recyclerView;
    private List<CommuteRoute> routeList;
    private CommuteRouteAdapter routeAdapter;
    private int selectedHour, selectedMinute;
    private Button btnSetAlarm;

    RecyclerView trafficRecyclerView, transitRecyclerView;
    List<TrafficInfo> trafficList = new ArrayList<>();
    List<TransitDetail> transitList = new ArrayList<>();
    TrafficInfoAdapter trafficAdapter;
    TransitAdapter transitAdapter;

    private static final String API_KEY = "AIzaSyBn9Qfi7SqyfaIoRTFEXyEJLIuk2OfUx1M"; // Store in strings.xml for security

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_commute);

        startLocation = findViewById(R.id.editTextText4);
        destinationLocation = findViewById(R.id.editTextText5);
        timeTextView = findViewById(R.id.textClock2);
        recyclerView = findViewById(R.id.route_recycler_view);
        btnSetAlarm = findViewById(R.id.button14);

        routeList = new ArrayList<>();
        routeAdapter = new CommuteRouteAdapter(routeList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(routeAdapter);

        trafficRecyclerView = findViewById(R.id.traffic_recycler_view);
        trafficAdapter = new TrafficInfoAdapter(trafficList);
        trafficRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trafficRecyclerView.setAdapter(trafficAdapter);

        transitRecyclerView = findViewById(R.id.transit_recycler_view);
        transitAdapter = new TransitAdapter(transitList);
        transitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transitRecyclerView.setAdapter(transitAdapter);

        startLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchRoutes();
                fetchTrafficInfo();
                fetchTransitInfo();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        destinationLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchRoutes();
                fetchTrafficInfo();
                fetchTransitInfo();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Initialize TimePickerDialog for Time TextView
        timeTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(PlanCommute.this,
                    (view, hourOfDay, minute) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;

                        String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                        hourOfDay = (hourOfDay > 12) ? hourOfDay - 12 : hourOfDay;
                        hourOfDay = (hourOfDay == 0) ? 12 : hourOfDay;

                        timeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hourOfDay, minute, amPm));
                    }, currentHour, currentMinute, false);
            timePickerDialog.show();
        });

        btnSetAlarm.setOnClickListener(v -> setAlarm());

        Toolbar toolbar = findViewById(R.id.plancommute_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Plan Commute");

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(PlanCommute.this, MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        return true;
    }

    private void setAlarm() {
        String start = startLocation.getQuery().toString();
        String destination = destinationLocation.getQuery().toString();
        if (start.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "Please enter both locations", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeString = timeTextView.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        try {
            Date time = sdf.parse(timeString);
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTime(time);
            Calendar now = Calendar.getInstance();
            alarmTime.set(Calendar.YEAR, now.get(Calendar.YEAR));
            alarmTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
            alarmTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            if (alarmTime.before(now)) {
                alarmTime.add(Calendar.DATE, 1);
            }
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("startLocation", start);
            intent.putExtra("destination", destination);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    generateRequestCode(start, destination),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Android 12+
                    if (alarmManager.canScheduleExactAlarms()) {
                        scheduleAlarm(alarmManager, alarmTime, pendingIntent);
                    } else {
                        Toast.makeText(this, "Enable exact alarms in settings", Toast.LENGTH_LONG).show();
                        Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(settingsIntent);
                    }
                } else {
                    scheduleAlarm(alarmManager, alarmTime, pendingIntent);
                }
            }
            SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM d, yyyy h:mm a", Locale.getDefault());
            String displayTime = displayFormat.format(alarmTime.getTime());
            Toast.makeText(this, "Alarm set for your trip\nFrom: " + start + "\nTo: " + destination + "\nTime: " + displayTime, Toast.LENGTH_LONG).show();
            finish();
        } catch (ParseException e) {
            Toast.makeText(this, "Error parsing time", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void scheduleAlarm(AlarmManager alarmManager, Calendar alarmTime, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
        }
    }


    private int generateRequestCode(String start, String end) {
        return start.hashCode() + end.hashCode();
    }

    private void fetchRoutes() {
        String start = startLocation.getQuery().toString();
        String destination = destinationLocation.getQuery().toString();

        if (start.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "Please enter both start and destination", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            start = URLEncoder.encode(start, "UTF-8");
            destination = URLEncoder.encode(destination, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String[] modes = {"driving", "walking", "bicycling", "transit"};
        RequestQueue queue = Volley.newRequestQueue(this);

        routeList.clear(); // Clear the list once before starting requests

        for (String mode : modes) {
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                    start + "&destination=" + destination +
                    "&departure_time=now&mode=" + mode + "&key=" + API_KEY;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray routesArray = response.getJSONArray("routes");
                            for (int i = 0; i < routesArray.length(); i++) {
                                JSONObject routeObj = routesArray.getJSONObject(i);
                                JSONObject leg = routeObj.getJSONArray("legs").getJSONObject(0);

                                String distance = leg.getJSONObject("distance").getString("text");
                                String time = leg.has("duration_in_traffic") ?
                                        leg.getJSONObject("duration_in_traffic").getString("text") :
                                        leg.getJSONObject("duration").getString("text");
                                String destinationStreet = leg.getString("end_address");

                                JSONArray stepsArray = leg.getJSONArray("steps");
                                String modeText = "Unknown";

                                // Get travel mode from steps array
                                for (int j = 0; j < stepsArray.length(); j++) {
                                    JSONObject step = stepsArray.getJSONObject(j);
                                    String travelMode = step.getString("travel_mode");

                                    switch (travelMode) {
                                        case "WALKING":
                                            modeText = "Walk";
                                            break;
                                        case "BICYCLING":
                                            modeText = "Bike";
                                            break;
                                        case "DRIVING":
                                            modeText = "Car";
                                            break;
                                        case "TRANSIT":
                                            modeText = "Public Transport";
                                            break;
                                        default:
                                            modeText = "Unknown";
                                    }
                                }

                                // Add route to the list
                                routeList.add(new CommuteRoute(modeText, time, distance, destinationStreet, ""));
                            }

                            // Notify adapter after all modes are processed
                            routeAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                Toast.makeText(PlanCommute.this, "Error fetching routes", Toast.LENGTH_SHORT).show();
            });

            queue.add(request);
        }
    }

    private void fetchTrafficInfo() {
        String start = startLocation.getQuery().toString();
        String destination = destinationLocation.getQuery().toString();

        if (start.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "Please enter both start and destination", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            start = URLEncoder.encode(start, "UTF-8");
            destination = URLEncoder.encode(destination, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String[] modes = {"driving", "walking", "bicycling", "transit"};
        RequestQueue queue = Volley.newRequestQueue(this);

        trafficList.clear(); // Clear the list before starting new requests

        for (String mode : modes) {
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                    start + "&destination=" + destination +
                    "&departure_time=now&mode=" + mode + "&key=" + API_KEY;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray routesArray = response.getJSONArray("routes");
                            for (int i = 0; i < routesArray.length(); i++) {
                                JSONObject routeObj = routesArray.getJSONObject(i);
                                JSONObject leg = routeObj.getJSONArray("legs").getJSONObject(0);

                                String distance = leg.getJSONObject("distance").getString("text");
                                String time = leg.has("duration_in_traffic") ?
                                        leg.getJSONObject("duration_in_traffic").getString("text") :
                                        leg.getJSONObject("duration").getString("text");
                                String startAddress = leg.getString("start_address");
                                String endAddress = leg.getString("end_address");

                                String trafficStatus;
                                if (leg.has("duration_in_traffic")) {
                                    trafficStatus = "Heavy Traffic";
                                } else {
                                    trafficStatus = "Normal Traffic";
                                }

                                // Add traffic info to the list
                                trafficList.add(new TrafficInfo(mode, trafficStatus, time, distance, startAddress, endAddress));
                            }

                            // Notify adapter after all modes are processed
                            trafficAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                Toast.makeText(PlanCommute.this, "Error fetching traffic info", Toast.LENGTH_SHORT).show();
            });

            queue.add(request);
        }
    }

    private void fetchTransitInfo() {
        String start = startLocation.getQuery().toString();
        String destination = destinationLocation.getQuery().toString();

        if (start.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "Please enter both start and destination", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            start = URLEncoder.encode(start, "UTF-8");
            destination = URLEncoder.encode(destination, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                start + "&destination=" + destination +
                "&departure_time=now&mode=transit&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);

        transitList.clear(); // Clear the list before starting new requests

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routesArray = response.getJSONArray("routes");
                        for (int i = 0; i < routesArray.length(); i++) {
                            JSONObject routeObj = routesArray.getJSONObject(i);
                            JSONObject leg = routeObj.getJSONArray("legs").getJSONObject(0);
                            JSONArray stepsArray = leg.getJSONArray("steps");

                            for (int j = 0; j < stepsArray.length(); j++) {
                                JSONObject step = stepsArray.getJSONObject(j);

                                if (step.getString("travel_mode").equals("TRANSIT")) {
                                    JSONObject transitDetails = step.getJSONObject("transit_details");
                                    String departureTime = transitDetails.getJSONObject("departure_time").getString("text");
                                    String arrivalTime = transitDetails.getJSONObject("arrival_time").getString("text");
                                    String departureStop = transitDetails.getJSONObject("departure_stop").getString("name");
                                    String arrivalStop = transitDetails.getJSONObject("arrival_stop").getString("name");
                                    String lineName = transitDetails.getJSONObject("line").getString("short_name");
                                    String vehicleType = transitDetails.getJSONObject("line").getJSONObject("vehicle").getString("type");

                                    transitList.add(new TransitDetail(vehicleType, lineName, departureTime, arrivalTime, departureStop, arrivalStop));
                                }
                            }
                        }

                        // Notify adapter after processing
                        transitAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Toast.makeText(PlanCommute.this, "Error fetching transit info", Toast.LENGTH_SHORT).show();
        });

        queue.add(request);
    }
}