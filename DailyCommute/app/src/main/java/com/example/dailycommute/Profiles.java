package com.example.dailycommute;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Profiles extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextView usernametextview, userlocationtextview, emaildisplay, phonenumberdisplay,homeTextView, gymTextView, workTextView;;
    private ImageButton changeemail, changephonenumber,homeupdate,gymupdate,workupdate;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profiles);

        // Refrence to the Ui element
        usernametextview = findViewById(R.id.user_name);
        userlocationtextview = findViewById(R.id.user_location);
        emaildisplay = findViewById(R.id.email_display_profile);
        phonenumberdisplay = findViewById(R.id.phone_number_dis_profile);
        changeemail = findViewById(R.id.editemail_btn);
        changephonenumber = findViewById(R.id.editphone_btn);
        homeTextView = findViewById(R.id.home_loc_dis);
        gymTextView = findViewById(R.id.gym_loc_dis);
        workTextView = findViewById(R.id.work_loc_dis);
        homeupdate = findViewById(R.id.edithome_btn);
        gymupdate = findViewById(R.id.editgym_btn);
        workupdate = findViewById(R.id.editwork_btn);


        changeemail.setEnabled(false);

        // Initialize firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
     //   DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // **Fix: Initialize Firebase Database Reference only once**
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Intialize location client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();



        // Initialize the custom toolbar
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        Button button12 = findViewById(R.id.setting_btn_profile);

        // check if user is signed in
        if (currentUser != null) {
            // Extract the users display name part of email address
            String email = currentUser.getEmail();
            String UserName = email.split("@")[0];
            // Set the username to textview
            usernametextview.setText(UserName);
        } else {
            // Handle case where user is not signed in
            usernametextview.setText("User not signed in");
        }

        // Request location permissions and fetch loaction
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Enable the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true); // Disable default title
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
            getSupportActionBar().setTitle("Profile");
        }

        homeupdate.setOnClickListener(v -> {
            Intent intent = new Intent(Profiles.this, UpdateStoreLocationActivity.class);
            intent.putExtra("destination", "Home"); 
            startActivity(intent);
        });

        gymupdate.setOnClickListener(v -> {
            Intent intent = new Intent(Profiles.this, UpdateStoreLocationActivity.class);
            intent.putExtra("destination", "Gym");
            startActivity(intent);
        });

        workupdate.setOnClickListener(v -> {
            Intent intent = new Intent(Profiles.this, UpdateStoreLocationActivity.class);
            intent.putExtra("destination", "Work");
            startActivity(intent);
        });

        // Load the phone number from SharedPreferences and display it
        loadPhoneNumber();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            String email = user.getEmail();
            emaildisplay.setText(email);
        }

        button12.setOnClickListener(v -> startActivity(new Intent(Profiles.this, Setting.class)));
        changephonenumber.setOnClickListener(v -> showAddPhoneNumberDialog());
        changeemail.setOnClickListener(v -> Toast.makeText(Profiles.this, "You Can't Change Email", Toast.LENGTH_SHORT).show());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Get user current location
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                //if location is not null, display it
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // use Geocoder to get location name
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
                    if (addresses != null && addresses.size() >0){
                        String locationname = addresses.get(0).getAddressLine(0);
                        userlocationtextview.setText(locationname);
                    }else {
                        userlocationtextview.setText("Location not available");
                    }
                }catch (IOException e){
                    Log.e("Location","Error getting location name",e);
                    userlocationtextview.setText("Location not available");
                }
            } else {
                userlocationtextview.setText("Location not available");
            }
        });

        // Fetch and display locations
        fetchLocation("Home", homeTextView);
        fetchLocation("Gym", gymTextView);
        fetchLocation("Work", workTextView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Navigate to MainScreen activity
        Intent intent = new Intent(Profiles.this, MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        return true;
    }
    // Handle permission request result
    public void onRequestPermissionResult(int requestcode,String[] permissions,int [] grantResults) {
        super.onRequestPermissionsResult(requestcode, permissions, grantResults);
        if (requestcode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation(); // Retry getting location if permission is granted
            } else {
                userlocationtextview.setText("Permission Denied");
            }
        }
    }
    private void showAddPhoneNumberDialog() {
        // Inflate the popup layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_add_phone_number, null);

        // Initialize the EditText and Button in the popup
        EditText phoneNumberEditText = popupView.findViewById(R.id.phoneNumberEditText);
        Button savePhoneNumberButton = popupView.findViewById(R.id.savePhoneNumberButton);

        // Create an AlertDialog for the popup
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle save button click
        savePhoneNumberButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberEditText.getText().toString().trim();

            if (!phoneNumber.isEmpty()) {
                // Save phone number to the database
                savePhoneNumberToDatabase(phoneNumber);

                // Update the TextView on the profile page
                phonenumberdisplay.setText(phoneNumber);

                // Dismiss the dialog
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePhoneNumberToDatabase(String phoneNumber) {
        // Replace with your database logic
        // Example: Saving to SharedPreferences for simplicity
        getSharedPreferences("users", MODE_PRIVATE)
                .edit()
                .putString("PhoneNumber", phoneNumber)
                .apply();
    }

    private String getPhoneNumberFromDatabase() {
        // Replace with your database retrieval logic
        return getSharedPreferences("users", MODE_PRIVATE)
                .getString("PhoneNumber", "No phone number set");
    }

    private void savePhoneNumberToPreferences(String phoneNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("PhoneNumber", phoneNumber);
        editor.apply();
    }

    private void loadPhoneNumber() {
        SharedPreferences sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString("PhoneNumber", null);
        if (phoneNumber != null) {
            phonenumberdisplay.setText(phoneNumber);
        } else {
            phonenumberdisplay.setText("No phone number added");
        }
    }

    private void fetchLocation(String destinationName, TextView locationTextView) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
        DatabaseReference userDestinationRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("locations")
                .child(destinationName);

        userDestinationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("latitude").getValue() != null && snapshot.child("longitude").getValue() != null) {
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);
                    getAddressFromCoordinates(latitude, longitude, locationTextView);
                } else {
                    locationTextView.setText(destinationName + " location not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profiles.this, "Failed to load " + destinationName + " location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAddressFromCoordinates(double latitude, double longitude, TextView locationTextView) {
        try {
            List<Address> addresses = new Geocoder(this, Locale.getDefault()).getFromLocation(latitude, longitude, 1);
            locationTextView.setText(addresses != null && !addresses.isEmpty() ? addresses.get(0).getAddressLine(0) : "Unknown Address");
        } catch (IOException e) {
            locationTextView.setText("Error fetching address");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load phone number from the database and display it
        String phoneNumber = getPhoneNumberFromDatabase();
        phonenumberdisplay.setText(phoneNumber);
    }

}