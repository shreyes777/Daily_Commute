package com.example.dailycommute;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.content.pm.PackageManager;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;


/** @noinspection ALL*/
public class MainScreen extends AppCompatActivity {


    public String Work_Source = "", Work_Destination = "", Home_Source = "", Home_Destination = "", Gym_Source = "", Gym_Destination = "", Work_Going_Hour = "", Work_Going_Minute = "", Work_Coming_Hour = "", Work_Coming_Minute = "", Home_Going_Hour = "", Home_Going_Minute = "", Home_Coming_Hour = "", Home_Coming_Minute = "", Gym_Going_Hour = "", Gym_Going_Minute = "", Gym_Coming_Hour = "", Gym_Coming_Minute = "";
    public int subtract_minute = 15, work_going_hour, work_going_minute, work_coming_hour, work_coming_minute, home_going_hour, home_going_minute, home_coming_hour, home_coming_minute, gym_going_hour, gym_going_minute, gym_coming_hour, gym_coming_minute;
    private EditText minuteField;
    private Button tutorial, preprogram, chatbot;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String userId;

    private ImageButton profile, notification;
    private Button map, destlocation, work, home, gym;
    private SearchView searchView;
    private static final String CHANNEL_ID = "TRAFFIC_CHANNEL";
    private static final int NAVIGATION_NOTIFICATION_ID = 1;
    public Intent toMapTool;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String FIRST_LAUNCH_KEY = "first_launch";
    private SharedPreferences sharedPreferences;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.POST_NOTIFICATIONS,
    };
    private FusedLocationProviderClient fusedLocationClient;
    private NotificationManagerCompat notificationManager;
    private RequestQueue requestQueue;
    static String Notification = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_screen); // Ensure this layout file exists
        profile = findViewById(R.id.profile_btn);
        searchView = findViewById(R.id.search_locinput_main);
        map = findViewById(R.id.mapy);
        toMapTool = new Intent(MainScreen.this, map_tool.class);
        work = findViewById(R.id.work_place_btn);
        home = findViewById(R.id.home_place_btn);
        gym = findViewById(R.id.gym_place_btn);
        chatbot=findViewById(R.id.chatbot);
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        destlocation = findViewById(R.id.set_destination_loc_btn);


        // Initialize Volley
        requestQueue = Volley.newRequestQueue(this);


        // Initialize FirebaseAuth and get the current user's UID
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid(); // Get the current user's ID from FirebaseAuth

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("userss");

        // Set up Firebase reference to the destinations of the current user
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("destinations");


        int theme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(theme);

        // Set up click listener for the button
        destlocation.setOnClickListener(v -> showNotification());

        // Handle WindowInsets for Edge-to-Edge

        profile.setOnClickListener(v -> startActivity(new Intent(MainScreen.this, Profiles.class)));

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(toMapTool);
            }
        });
        chatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainScreen.this, Chatbox.class));
            }
        });


        // Initialize the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav); // Check if 'bottom_nav' is correct in your layout

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.signup_btn) {
                startActivity(new Intent(MainScreen.this, MainScreen.class));
                return true;
            } else if (itemId == R.id.commute_planner) {
                startActivity(new Intent(MainScreen.this, PlanCommute.class));
                return true;
            } else if (itemId == R.id.traffic) {
                startActivity(new Intent(MainScreen.this, map_tool.class));
                return true;
            } else if (itemId == R.id.public_transport) {
                startActivity(new Intent(MainScreen.this, PublicTransport.class));
                return true;
            } else if (itemId == R.id.setting) {
                startActivity(new Intent(MainScreen.this, Setting.class));
                return true;
            }

            return false;
        });

        if (isFirstLaunch()) {
            requestpermissions();
        }
        work.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDestinationExists("Work");
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDestinationExists("Home");
            }
        });
        gym.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDestinationExists("Gym");
            }
        });

        loadInfo();
    }


    private boolean isFirstLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(FIRST_LAUNCH_KEY, true);
    }

    private void setFirstLaunch(boolean firstLaunch) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(FIRST_LAUNCH_KEY, firstLaunch).apply();
    }

    private void requestpermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allPermissionGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allPermissionGranted = false;
                    Toast.makeText(this, "Permissions denied:" + permissions[i], Toast.LENGTH_SHORT).show();
                }
            }
            if (allPermissionGranted) {
                Toast.makeText(this, "All Permissions granted", Toast.LENGTH_SHORT).show();
            }
            setFirstLaunch(false);
        }
    }

    private void showNotification() {
        // Create Notification Channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sample Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        String s = "Unknown", d = "Unknown";
        s = readFromFile("Sou.txt", MainScreen.this);
        d = readFromFile("Des.txt", MainScreen.this);
        Toast.makeText(this, "Notification Written", Toast.LENGTH_SHORT).show();
        Notification = "Traffic between " + s + " to " + d;
        // Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Traffic Update")
                .setContentText(Notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Display the Notification
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }


    private void ReminderScheduler()    //This Method is who will inform Alarm Manager when to start
    {
        try {
            work_going_minute = work_going_minute - subtract_minute;
            if (work_going_minute < 0) {
                work_going_hour = work_going_hour - 1;
                work_going_minute = work_going_minute + 60;
            }
            work_coming_minute = work_coming_minute - subtract_minute;
            if (work_coming_minute < 0) {
                work_coming_hour = work_coming_hour - 1;
                work_coming_minute = work_coming_minute + 60;
            }

            home_going_minute = home_going_minute - subtract_minute;
            if (home_going_minute < 0) {
                home_going_hour = home_going_hour - 1;
                home_going_minute = home_going_minute + 60;
            }
            home_coming_minute = home_coming_minute - subtract_minute;
            if (home_coming_minute < 0) {
                home_coming_hour = home_coming_hour - 1;
                home_coming_minute = home_coming_minute + 60;
            }

            gym_going_minute = gym_going_minute - subtract_minute;
            if (gym_going_minute < 0) {
                gym_going_hour = gym_going_hour - 1;
                gym_going_minute = gym_going_minute + 60;
            }
            gym_coming_minute = gym_coming_minute - subtract_minute;
            if (gym_coming_minute < 0) {
                gym_coming_hour = gym_coming_hour - 1;
                gym_coming_minute = gym_coming_minute + 60;
            }
        } catch (Exception e) {
            Toast.makeText(MainScreen.this, "Cannot Set Timer", Toast.LENGTH_LONG).show();
        }
    }

    private void Stringtoint() {
        try {
            Work_Going_Hour.trim();
            Work_Going_Minute.trim();
            Work_Coming_Hour.trim();
            Work_Coming_Minute.trim();
            work_going_hour = Integer.parseInt(Work_Going_Hour);
            work_going_minute = Integer.parseInt(Work_Going_Minute);
            work_coming_hour = Integer.parseInt(Work_Coming_Hour);
            work_coming_minute = Integer.parseInt(Work_Coming_Minute);
            Work_Going_Hour=null;
            Work_Going_Minute=null;
            Work_Coming_Hour=null;
            Work_Coming_Minute=null;
            System.gc();

            Home_Going_Hour.trim();
            Home_Going_Minute.trim();
            Home_Coming_Hour.trim();
            Home_Coming_Minute.trim();
            home_going_hour = Integer.parseInt(Home_Going_Hour);
            home_going_minute = Integer.parseInt(Home_Going_Minute);
            home_coming_hour = Integer.parseInt(Home_Coming_Hour);
            home_coming_minute = Integer.parseInt(Home_Coming_Minute);
            Home_Going_Hour=null;
            Home_Going_Minute=null;
            Home_Coming_Hour=null;
            Home_Coming_Minute=null;
            System.gc();

            Gym_Going_Hour.trim();
            Gym_Going_Minute.trim();
            Gym_Coming_Hour.trim();
            Gym_Coming_Minute.trim();
            gym_going_hour = Integer.parseInt(Gym_Going_Hour);
            gym_going_minute = Integer.parseInt(Gym_Coming_Minute);
            gym_coming_hour = Integer.parseInt(Gym_Coming_Hour);
            gym_coming_minute = Integer.parseInt(Gym_Coming_Minute);
            Gym_Going_Hour=null;
            Gym_Going_Minute=null;
            Gym_Coming_Hour=null;
            Gym_Coming_Minute=null;
            System.gc();

            ReminderScheduler();
        } catch (Exception e) {
            //Toast.makeText(MainScreen.this, "Cannot Convert Data-Types", Toast.LENGTH_LONG).show();
        }
    }

    private void loadInfo() {
        try {
            Work_Source = readFromFile("work_s.txt", MainScreen.this);
            Work_Destination = readFromFile("work_d.txt", MainScreen.this);
            Work_Going_Hour = readFromFile("work_g_h.txt", MainScreen.this);
            Work_Coming_Minute = readFromFile("work_g_m.txt", MainScreen.this);
            Work_Coming_Hour = readFromFile("work_c_h.txt", MainScreen.this);
            Work_Coming_Minute = readFromFile("work_c_m.txt", MainScreen.this);

            Home_Source = readFromFile("home_s.txt", MainScreen.this);
            Home_Destination = readFromFile("home_d.txt", MainScreen.this);
            Home_Going_Hour = readFromFile("home_g_h.txt", MainScreen.this);
            Home_Going_Minute = readFromFile("home_g_m.txt", MainScreen.this);
            Home_Coming_Hour = readFromFile("home_c_h.txt", MainScreen.this);
            Home_Coming_Minute = readFromFile("home_c_m.txt", MainScreen.this);

            Gym_Source = readFromFile("gym_s.txt", MainScreen.this);
            Gym_Destination = readFromFile("gym_d.txt", MainScreen.this);
            Gym_Going_Hour = readFromFile("gym_g_h.txt", MainScreen.this);
            Gym_Going_Hour = readFromFile("gym_g_m.txt", MainScreen.this);
            Gym_Coming_Hour = readFromFile("gym_c_h.txt", MainScreen.this);
            Gym_Coming_Minute = readFromFile("gym_c_m.txt", MainScreen.this);

            Stringtoint();
        } catch (Exception e) {
            //Toast.makeText(MainScreen.this, "Cannot Load Files", Toast.LENGTH_LONG).show();
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

    private void checkDestinationExists(String destination) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
        DatabaseReference destinationRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("locations")
                .child(destination);

        destinationRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                double latitude = task.getResult().child("latitude").getValue(Double.class);
                double longitude = task.getResult().child("longitude").getValue(Double.class);

                Intent intent = new Intent(MainScreen.this, NavigationActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainScreen.this, StoreLocationActivity.class);
                intent.putExtra("destination", destination);
                startActivity(intent);
            }
        });
    }

}
