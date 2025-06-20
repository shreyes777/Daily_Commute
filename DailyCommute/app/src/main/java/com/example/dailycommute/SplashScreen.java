package com.example.dailycommute;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.FOREGROUND_SERVICE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECEIVE_BOOT_COMPLETED;
import static android.Manifest.permission.SET_ALARM;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_SCREEN_TIMEOUT = 3000; // 3 seconds
    private FirebaseAuth mAuth;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String FIRST_LAUNCH_KEY = "first_launch";
    private final List<String> permissions = new ArrayList<>();
    private int currentPermissionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(SplashScreen.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Add permissions based on API version
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        permissions.add(Manifest.permission.FOREGROUND_SERVICE);
        permissions.add(Manifest.permission.VIBRATE);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);  // Added permission
        permissions.add(Manifest.permission.SET_ALARM);  // Added permission

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (isFirstLaunch()) {
            requestNextPermission();
        } else {
            screenout();
        }
    }

    private void screenout() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        new android.os.Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is logged in, go to MainActivity
                startActivity(new Intent(SplashScreen.this, MainScreen.class));
            } else {
                // User not logged in, go to LoginActivity
                startActivity(new Intent(SplashScreen.this, Login.class));
            }
            finish();
        }, SPLASH_SCREEN_TIMEOUT);
    }

    private boolean isFirstLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(FIRST_LAUNCH_KEY, true);
    }

    private void setFirstLaunch(boolean firstLaunch) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(FIRST_LAUNCH_KEY, firstLaunch).apply();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    showPermissionDeniedDialog();
                }
                requestNextPermission();  // Move to the next permission
            });

    private void requestNextPermission() {
        if (currentPermissionIndex < permissions.size()) {
            String permission = permissions.get(currentPermissionIndex);
            currentPermissionIndex++;

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog(permission);
            } else {
                requestNextPermission();  // Skip if already granted
            }
        } else {
            // If all permissions are handled, proceed to screenout
            screenout();
        }
    }

    private void showPermissionDialog(String permission) {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app requires " + permission + " to function properly. Please grant this permission.")
                .setCancelable(false)
                .setPositiveButton("Allow", (dialog, which) -> requestPermissionLauncher.launch(permission))
                .setNegativeButton("Deny", (dialog, which) -> requestNextPermission())
                .show();
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage("Some features may not work without the required permissions.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> requestNextPermission())
                .show();
    }
}
