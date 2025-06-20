package com.example.dailycommute;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class Setting extends AppCompatActivity {

    // SharedPreferences keys for storing user preferences
    private static final String PREFS_NAME = "user_preferences";
    private static final String THEME_KEY = "theme";
    private static final String CURRENCY_KEY = "currency";
    private static final String UNIT_KEY = "distance_unit";
    private static final String NOTIFICATIONS_KEY = "notifications_enabled";
    private static final String LANGUAGE_KEY = "language";

    // UI elements
    private TextView themeSwitch, currencyChangeTextView, distanceUnitChangeTextView,
            notificationStatusTextView, languageChangeTextView, emailVerificationStatusTextView,
            phoneNumberStatusTextView;

    // Constants for options used in the dialogs
    private final String[] themes = {"Light", "Dark"};
    private final String[] currencies = {"USD", "EUR", "GBP", "JPY", "INR"};
    private final String[] distanceUnits = {"KM", "Miles"};
    private final String[] notifications = {"ON", "OFF"};
    private final String[] languages = {"English", "Spanish", "French", "German", "Chinese"};

    // Permission request launcher for notifications
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private Button about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize the toolbar
        Toolbar toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Setting");
        }

        // Initialize UI elements
        themeSwitch = findViewById(R.id.switch_theme);
        currencyChangeTextView = findViewById(R.id.Switch_currency);
        distanceUnitChangeTextView = findViewById(R.id.switch_unit);
        notificationStatusTextView = findViewById(R.id.alert_notification_switch);
        languageChangeTextView = findViewById(R.id.switch_language);
        emailVerificationStatusTextView = findViewById(R.id.email_verification);
        phoneNumberStatusTextView = findViewById(R.id.phone_verification);
        about = findViewById(R.id.btn_about);

        // Initialize permission launcher for notifications
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> updateNotificationStatus(isGranted)
        );

        // Get the currently logged-in user from Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            // Check if the email is verified
            if (email != null && !email.isEmpty()) {
                emailVerificationStatusTextView.setText("Verified");
            } else {
                emailVerificationStatusTextView.setText("Not Verified");
            }
        }

        // Set click listeners for various settings
        themeSwitch.setOnClickListener(v -> showPopupDialog("theme"));
        currencyChangeTextView.setOnClickListener(v -> showPopupDialog("currency"));
        distanceUnitChangeTextView.setOnClickListener(v -> showPopupDialog("distance_unit"));
        notificationStatusTextView.setOnClickListener(v -> toggleNotificationPermission());
        languageChangeTextView.setOnClickListener(v -> showPopupDialog("language"));

        // Load the saved phone number from SharedPreferences
        loadPhoneNumber();

        // Apply the user's saved settings to the UI and app
        applySavedSettings();
        
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Setting.this, About.class));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Request notification permission for Android Tiramisu and above
    private void toggleNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            updateNotificationStatus(true);
        }
    }

    // Update notification status in UI and SharedPreferences
    private void updateNotificationStatus(boolean isGranted) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putBoolean(NOTIFICATIONS_KEY, isGranted).apply();
        notificationStatusTextView.setText(isGranted ? "ON" : "OFF");
    }

    // Show a dialog for selecting settings
    private void showPopupDialog(String settingType) {
        String[] options;
        TextView targetTextView;

        // Determine the options and target TextView based on the setting type
        switch (settingType) {
            case "theme":
                options = themes;
                targetTextView = themeSwitch;
                break;
            case "currency":
                options = currencies;
                targetTextView = currencyChangeTextView;
                break;
            case "distance_unit":
                options = distanceUnits;
                targetTextView = distanceUnitChangeTextView;
                break;
            case "language":
                options = languages;
                targetTextView = languageChangeTextView;
                break;
            default:
                return;
        }

        // Create and display the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select " + settingType.replace("_", " ").toUpperCase());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
        builder.setAdapter(adapter, (dialog, which) -> {
            String selectedOption = options[which];
            saveSetting(settingType, selectedOption);
            targetTextView.setText(selectedOption);
            dialog.dismiss();
        });

        builder.show();
    }

    // Save the selected setting to SharedPreferences
    private void saveSetting(String settingType, String value) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        switch (settingType) {
            case "theme":
                int themeMode = value.equals("Light") ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
                editor.putInt(THEME_KEY, themeMode);
                AppCompatDelegate.setDefaultNightMode(themeMode);
                recreate(); // Apply theme change
                break;
            case "currency":
                editor.putString(CURRENCY_KEY, value);
                break;
            case "distance_unit":
                editor.putString(UNIT_KEY, value.equals("Kilometers") ? "km" : "mi");
                break;
            case "language":
                editor.putString(LANGUAGE_KEY, value);
                applyLanguage(value);
                break;
        }
        editor.apply();
    }

    // Apply the selected language
    private void applyLanguage(String language) {
        Locale locale;
        switch (language) {
            case "Spanish":
                locale = new Locale("es");
                break;
            case "French":
                locale = new Locale("fr");
                break;
            case "German":
                locale = new Locale("de");
                break;
            case "Chinese":
                locale = new Locale("zh");
                break;
            default:
                locale = Locale.ENGLISH;
        }

        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Refresh the activity to apply the language change
        Intent refresh = new Intent(this, Setting.class);
        finish();
        startActivity(refresh);
    }

    // Apply saved settings to the app and UI
    private void applySavedSettings() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        int theme = preferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(theme);
        themeSwitch.setText(theme == AppCompatDelegate.MODE_NIGHT_NO ? "Light" : "Dark");

        currencyChangeTextView.setText(preferences.getString(CURRENCY_KEY, "USD"));
        distanceUnitChangeTextView.setText(preferences.getString(UNIT_KEY, "km").equals("km") ? "KM" : "Miles");
        notificationStatusTextView.setText(preferences.getBoolean(NOTIFICATIONS_KEY, false) ? "ON" : "OFF");
        languageChangeTextView.setText(preferences.getString(LANGUAGE_KEY, "English"));
    }

    // Load the saved phone number and update the UI
    private void loadPhoneNumber() {
        SharedPreferences sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString("PhoneNumber", null);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            phoneNumberStatusTextView.setText("Added");
        } else {
            phoneNumberStatusTextView.setText("Not Added");
        }
    }
}
