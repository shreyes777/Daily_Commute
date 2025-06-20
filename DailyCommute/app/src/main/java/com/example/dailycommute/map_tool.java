package com.example.dailycommute;

import static com.google.firebase.auth.AuthKt.getAuth;

import static java.util.Collections.replaceAll;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import org.checkerframework.checker.units.qual.Current;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telecom.Call;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.textclassifier.ConversationActions;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.security.auth.callback.Callback;

//"not Tested" is commented for features currently only tested for Positive Instance not negative Instance
//"not Implemented" is commented for feature that have not given desired output so are kept there but not used
//Huge gap between lines represent currently testing code

// OnMapReadyCallback is necessary to track User it requires some redundant Methods be Overridden
public class map_tool extends AppCompatActivity implements OnMapReadyCallback {
    //This Variables are for "UI Components"
    private Button stop,start,set_origin,set_dest,save,load,clear; //Buttons to be shown
    private EditText SavedOrigin,SavedDestination;  //Used to View User's Route
    private MapView mapView; //Shows Map in separate window
    private ScrollView scrollView;
    private CardView savecardView;

    private boolean isZooming = false;
    private SearchView searchView; //Use for Searching Addresses no submit Button required

    //This Variables are for "App Navigation"
    private Intent toMain; //Sends to "activity_main_screen"

    //This Variables are for "Modifying Map Instance"
    private GoogleMap googleMap; //Instance of Map from Google requires savedInstance state through onMapReady
    public static String PStart=null,PEnd=null; //Reads Text from Search Bar must be static so only one Instance is available and must be null initially so that app does not automatically puts them on Search Bar
    //"P"  is reference to Point of
    public static LatLng LLStart,LLEnd; //Required to store Co-ordinate values as they are negative and decimal both
    //"LL" is short for Latitude-Longitude

    //This Variables are for "Map Visuals"
    public static List<Marker> RTMarkers = new ArrayList<Marker>();    //Used to store and delete Runtime Markers drawn by AddMarker and deleted by removeAllMArkers
    public static List<Polyline> RTLines=new ArrayList<Polyline>();    //Used to store and delete Runtime Polylines
    public int colourShifter=1;     //Variable to change Colour of Polyline/Markers when using "load" Button
    public static List<Marker> DBMarkers = new ArrayList<Marker>();    //Used to store and delete Database Markers drawn by AddMarker and deleted by removeAllMArkers
    public static List<Polyline> DBLines=new ArrayList<Polyline>();    //Used to store and delete Database Polylines

    //This Variables are for "Tracking User"
    private static Location Cloc;  //Stores the value for Current Location, here Location is complex String like URL with numbers and special characters in specific pattern
    //"C" is short for Current
    private FusedLocationProviderClient fusedLocationProvider;  //Client used identify this App Instance requires API
    private FusedLocationProviderClient TrafficClient;   //Client for Tarffic Updates
    public LocationRequest locationRequest; //It is used to call API to know current Location used in Callback
    public LocationCallback locationCallback; //Looped to continuously send requests without re-taps
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;  //Must have this value
    private static final int MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66; //Must have this value
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    //This Variables are for "Traffic Updates"
    public RecyclerView trafficRecyclerView;   //Improvement over ListView
    public List<TrafficInfo> trafficList = new ArrayList<>();  //Stores Traffic Data
    public TrafficInfoAdapter trafficAdapter;      //Requests Traffic Data through Class Object
    public List<TransitDetail> transitList = new ArrayList<>();    //Stores Transit Data
    public TransitAdapter transitAdapter;       //Requests Transit Data through Class Object

    //This Variables are for "Database/Storage"
    public FirebaseDatabase FD; //Creates an Instance of Firebase Object
    public DatabaseReference ref; //Identifier for recognizing this User on that Database
    public static String name; //Passes Identifier between Internal Storage and Database Reference
    public String nameCOPY; //Empty variable to call readdata() as it will override "name"'s value which may create Loopback for null value and is not Tested

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_tool);

        // Initialize the toolbar
        Toolbar toolbar = findViewById(R.id.trafficupdate_toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Traffic Update");
        }

        //Initiating all Click able Components
        searchView = findViewById(R.id.searchView);
        stop=findViewById(R.id.stop_track);
        start=findViewById(R.id.start_track);
        set_origin=findViewById(R.id.origin);
        set_dest=findViewById(R.id.dest);
        save=findViewById(R.id.save);
        save.setVisibility(View.INVISIBLE); //Must be Invisible if their is nothing to Save
        savecardView=findViewById(R.id.cv_save);
        savecardView.setVisibility(View.INVISIBLE);
        load=findViewById(R.id.load);
        load.setVisibility(View.VISIBLE);
        //traffick=findViewById(R.id.traffick);
        clear=findViewById(R.id.clear);
        //Intent for Main Menu
        toMain=new Intent(map_tool.this,MainScreen.class);

        // Initialize Map View
        mapView = findViewById(R.id.mapView);
        // Initialize MapView
        mapView.onCreate(savedInstanceState);  //Works with combination with getMapAsync()


        // Prevent ScrollView from intercepting touch when zooming

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });


        name=readFromFile("EM.txt",map_tool.this);   //It is used to retrieve previously saved route from Database
        nameCOPY=name;
        startTrack();   //It is used to start MapView at Current Location

        //Listeners and their Click Events
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    String location = searchView.getQuery().toString();
                    if (location.isEmpty())
                    {
                        Toast.makeText(map_tool.this, "Enter In Search Bar", Toast.LENGTH_SHORT).show();
                    } else if (location != null)
                    {
                        //Use onQueryTextChange instead as no other Button is required to initiate search
                        /**
                         LatLng l = getLocationFromAddress(map_tool.this, location);
                         googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 10));
                         Marker mLocationMarker = googleMap.addMarker(new MarkerOptions().position(l).title("Here")); // add the marker to Map
                         AllMarkers.add(mLocationMarker); // Adding Markers to Array
                         **/
                    }}
                catch(Exception e)
                {
                    //Toast.makeText(map_tool.this, "Incorrect Spelling", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                try
                {
                    String location = searchView.getQuery().toString();
                    if(location.isEmpty())
                    {
                        Toast.makeText(map_tool.this, "Enter In Search Bar", Toast.LENGTH_SHORT).show();
                    }
                    else if(location!=null)
                    {
                        LatLng l = getLocationFromAddress(map_tool.this, location);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 10));
                    }
                }
                catch (Exception e)
                {
                    //Toast.makeText(map_tool.this, "Incorrect Spelling", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        set_origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String location = searchView.getQuery().toString();
                PStart=location;
                try
                {
                    if(PStart.isEmpty())
                    {
                        Toast.makeText(map_tool.this, "Enter In Search Bar", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        LLStart=getLocationFromAddress(map_tool.this,PStart);
                        Marker mLocationMarker = googleMap.addMarker(new MarkerOptions().position(LLStart).title("Here")); // add the marker to Map
                        RTMarkers.add(mLocationMarker); // Adding Markers to Array
                        Toast.makeText(map_tool.this, "Start:"+PStart+" LatLang="+getLocationFromAddress(map_tool.this,PEnd), Toast.LENGTH_SHORT).show();
                        if(PEnd!=null && PStart!=null)
                        {
                            save.setVisibility(View.VISIBLE);
                            savecardView.setVisibility(View.VISIBLE);
                            getRoute(LLStart,LLEnd);
                        }
                    }
                }
                catch (Exception e)
                {
                    //Toast.makeText(map_tool.this, "Incorrect Spelling", Toast.LENGTH_SHORT).show();
                }

            }
        });
        set_dest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = searchView.getQuery().toString();
                PEnd=location;
                try
                {
                    if(PEnd.isEmpty())
                    {
                        Toast.makeText(map_tool.this, "Enter In Search Bar", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        LLEnd=getLocationFromAddress(map_tool.this,PEnd);
                        Marker mLocationMarker = googleMap.addMarker(new MarkerOptions().position(LLEnd).title("Saved Origin")); // add the marker to Map
                        RTMarkers.add(mLocationMarker); // Adding Markers to Array
                        Toast.makeText(map_tool.this, "End:"+PEnd+" LatLang="+getLocationFromAddress(map_tool.this,PEnd), Toast.LENGTH_SHORT).show();
                        if(PStart!=null && PEnd!=null)
                        {
                            save.setVisibility(View.VISIBLE);
                            getRoute(LLStart,LLEnd);
                        }
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(map_tool.this, "Incorrect Spelling", Toast.LENGTH_SHORT).show();
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener()
        {
            //Available only if Destination and Start are valid
            @Override
            public void onClick(View v) {
                if (PStart == null || PEnd == null)
                {
                    Toast.makeText(map_tool.this, "Source & Destination Both Required", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(!PStart.isEmpty()&&!PEnd.isEmpty())
                    {
                        try
                        {
                            removeDBMarkers();



                            if(name!=null)
                            {
                                users user = new users(name, PStart, PEnd);
                                FD = FirebaseDatabase.getInstance();
                                ref = FD.getReference("users");
                                ref.child(name).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(map_tool.this, "Data Sent for "+nameCOPY, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(map_tool.this, "name is null", Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e)
                        {
                            String s=String.valueOf(e);
                            Toast.makeText(map_tool.this, s, Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(map_tool.this, "From " + PStart + " To " + PEnd, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        load.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    colourShifter=0;
                    readFromDB(nameCOPY);
                } catch (Exception e)
                {
                    Toast.makeText(map_tool.this, "Insufficient Network Speed", Toast.LENGTH_LONG).show();
                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                removeRTMarkers();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(map_tool.this, MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        return true;
    }

    //This Methods are for "Internal Storage"
    public void writeToFile(String fileName, String fileContents, Context context)      //Necessary to store Notification Paramteter
    {
        FileOutputStream spos = null;
        try {
            spos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            spos.write(fileContents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (spos != null) {
                try {
                    spos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        }
    public String readFromFile(String fileName, Context context) //Firebase requires "name" as Identifier which is stored internally through this
    {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = context.openFileInput(fileName);
            int ch;
            while ((ch = fis.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
    private void readFromDB(String nameCOPY)     //Necessary to Store and Read Routes from Firebase
    {
        name=nameCOPY;
        DatabaseReference pass = FirebaseDatabase.getInstance().getReference("users");
        pass.child(name).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    if (task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        PStart=null;
                        PStart=(Objects.requireNonNull(snapshot.child(
                                "source").getValue()).toString());
                        PEnd=null;
                        PEnd=(Objects.requireNonNull(snapshot.child(
                                "destination").getValue()).toString());
                        LatLng ll1=getLocationFromAddress(map_tool.this,PStart);
                        LatLng ll2=getLocationFromAddress(map_tool.this,PEnd);
                        getRoute(ll1,ll2);
                        Marker StartMarker = googleMap.addMarker(new MarkerOptions().position(ll1).title("Loaded Source"));
                        Marker EndMarker = googleMap.addMarker(new MarkerOptions().position(ll2).title("Loaded Destination"));
                        DBMarkers.add(StartMarker);
                        DBMarkers.add(EndMarker);
                        if (StartMarker != null && EndMarker != null)
                        {
                            StartMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            EndMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        }
                        Toast.makeText(map_tool.this, "From " + PStart + " To " + PEnd, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(map_tool.this, "No User",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(map_tool.this, "Failure while Retrieval",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //This Methods are for "Listener Modifications"
    private void removeRTMarkers()     //Provide feature to remove Runtime Markers and Polylines at tap of Button
    {
        for (Marker mLocationMarker: RTMarkers)
        {
            mLocationMarker.remove();
        }
        for(Polyline mLocationPolyline: RTLines)
        {
            mLocationPolyline.remove();
        }
        RTMarkers.clear();
        RTLines.clear();
    }
    private void removeDBMarkers()     //Provide feature to remove Database Markers and Polylines at tap of Button
    {
        for (Marker mLocationMarker: DBMarkers)
        {
            mLocationMarker.remove();
        }
        for(Polyline mLocationPolyline: DBLines)
        {
            mLocationPolyline.remove();
        }
        DBMarkers.clear();
        DBLines.clear();
    }
    public LatLng getLocationFromAddress(Context context, String strAddress)    //Converts Address(Text) to LatLng(Co-ordinates) and also reverse this allows to read Text from Search Bar
    {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng LatLan= null;
        try
        {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null)
            {
                return null;
            }

            Address location = address.get(0);
            LatLan= new LatLng(location.getLatitude(), location.getLongitude() );
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return LatLan;
    }
    private String getAddressfromLocation(Location current)     //Reversed Geocoder to get STring instead
    {
        Location location= Cloc;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0); // Get full address
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(map_tool.this, "Cannot Convert Address", Toast.LENGTH_SHORT).show();
        }
        return "Unknown Location"; // Return fallback string
    }

    //This Methods are for "Tracking User"
    private void startTrack()   //Use to retrieve Current Location of User periodically
    {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(10000)
                .build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLocations().size() > 0) {
                    Location l = locationResult.getLastLocation();
                    Cloc = l;
                    mapView.getMapAsync(map_tool.this); //This method is necessary to correlate View and Map
                    //Toast.makeText(map_tool.this, "Got Location: " + l, Toast.LENGTH_LONG).show();
                }
            }
        };
        checkLocationPermission();
        //Do not remove this Listeners from here as locationCallback is required by them which cannot be created without Permissions leading to Activity not opening at all
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                fusedLocationProvider.removeLocationUpdates(locationCallback);
                Toast.makeText(map_tool.this, "Stopped", Toast.LENGTH_SHORT).show();
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                checkLocationPermission();
                fusedLocationProvider.requestLocationUpdates(locationRequest,locationCallback,null);
                Toast.makeText(map_tool.this, "Started", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkLocationPermission()    //Used to Check Location Permission not Tested for rejection
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", (dialog, which) -> requestLocationPermission())
                        .create()
                        .show();
            } else
            {
                requestLocationPermission();
            }
        } else
        {
            checkBackgroundLocation();
        }
    }
    private void checkBackgroundLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestBackgroundLocationPermission();
        }
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
    }
    private void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    //This Methods are for "Navigating User"
    private void getRoute(LatLng source, LatLng destination)    //Used to request Parameters from Server
    {
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + source.latitude + "," + source.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&mode=driving"
                + "&key=" + "AIzaSyBn9Qfi7SqyfaIoRTFEXyEJLIuk2OfUx1M";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(map_tool.this,"Got Route",Toast.LENGTH_LONG).show();
                        drawRoute(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(map_tool.this,"Error Fetching Route",Toast.LENGTH_LONG).show();
                        Log.e("MAPS", "Error fetching route: " + error.getMessage());
                    }
                });

        queue.add(stringRequest);
    }
    private void drawRoute(String response)     //Used to actually draw Polyline on MapView
    {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);

                //Supposed to read "status" directly through JSON Object for Polyline's instance currently not Tested
                try
                {
                    String status = jsonObject.getString("status");
                    if (status.equals("ZERO_RESULTS")) {
                        Log.i("ROAD_CHECK", "Not Available");
                        Toast.makeText(map_tool.this, "Not Available", Toast.LENGTH_LONG).show();
                    } else {
                        Log.i("ROAD_CHECK", "Available");
                        Toast.makeText(map_tool.this, "Available", Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception e)
                {
                    Toast.makeText(map_tool.this, "Missed Step", Toast.LENGTH_LONG).show();
                }

                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");
                List<LatLng> polylinePoints = decodePolyline(encodedPolyline);
                if(colourShifter == 1)
                {
                Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                        .addAll(polylinePoints)
                        .width(10)
                        .color(Color.BLUE)
                        .geodesic(true));
                RTLines.add(polyline);
                }
                if(colourShifter == 0)
                {
                    Polyline branches = googleMap.addPolyline(new PolylineOptions()
                            .addAll(polylinePoints)
                            .width(10)
                            .color(Color.GRAY)
                            .geodesic(true));
                    DBLines.add(branches);
                    colourShifter=1;
                }

            }
        } catch (Exception e) {
            Log.e("MAPS", "Error parsing route: " + e.getMessage());
        }
    }
    private List<LatLng> decodePolyline(String encoded)     //Used to read "Steps" length and direction from JSON
    {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dLat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dLng;

            polyline.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return polyline;
    }
    private void checkRoadClosure(String origin, String destination)    //Suppose to Check Road Status but is not Implemented directly instead its code replicated in drawRoute()
    {
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + origin
                + "&destination=" + destination
                + "&key=AIzaSyBn9Qfi7SqyfaIoRTFEXyEJLIuk2OfUx1M" ;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("ZERO_RESULTS")) {
                                Log.i("ROAD_CHECK", "Not Available");
                                Toast.makeText(map_tool.this,"Not Available",Toast.LENGTH_LONG).show();
                            } else {
                                Log.i("ROAD_CHECK", "Available");
                                Toast.makeText(map_tool.this,"Available",Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("ROAD_CHECK", "No response " + e.getMessage());
                            Toast.makeText(map_tool.this,"Bad Response",Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ROAD_CHECK", "API Error: " + error.getMessage());
                        Toast.makeText(map_tool.this,"API Error",Toast.LENGTH_LONG).show();
                    }
                });
        queue.add(request);
    }

    //This is a Default Method
    @Override
    public void onMapReady(GoogleMap map)   //Defines actual Map in MapView,is called by Track Me and Search Bar
    {

        googleMap = map;
        googleMap.setTrafficEnabled(true);
        LatLng latLng = new LatLng(Cloc.getLatitude(),Cloc.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

        Marker mLocationMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Here")); // add the marker to Map
        RTMarkers.add(mLocationMarker); // Adding Markers to Array
    }

    //This Methods are for but MapView are currently useless so not Implemented
    public void showDirections(LatLng l1,LatLng l2)
    {
        LatLng destination = l1;
        LatLng origin = l2;
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(origin)
                .add(destination)
                .color(Color.BLACK)
                .width(10);
        googleMap.addPolyline(polylineOptions);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
}