package com.example.dailycommute;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Don't delete, created by Shreyas
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get origin, destination, and requestCode from Intent
        String origin = intent.getStringExtra("origin");
        String destination = intent.getStringExtra("destination");
        int requestCode = intent.getIntExtra("requestCode", (int) System.currentTimeMillis());

        if (origin == null || destination == null) {
            Log.e(TAG, "Missing origin or destination!");
            return;
        }

        // Fetch traffic data asynchronously
        new TrafficTask(context, origin, destination, requestCode).execute();
    }

    // AsyncTask to fetch traffic data from Google Maps API
    private static class TrafficTask extends AsyncTask<Void, Void, String> {
        private final Context context;
        private final String origin, destination;
        private final int requestCode;
        private final String apiKey;

        TrafficTask(Context context, String origin, String destination, int requestCode) {
            this.context = context;
            this.origin = origin;
            this.destination = destination;
            this.requestCode = requestCode;
            this.apiKey = context.getString(R.string.google_maps_api_key); // Fetch API key from secrets.xml
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Construct API URL
                String urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + origin +
                        "&destination=" + destination +
                        "&departure_time=now" +  // Fetch real-time traffic
                        "&traffic_model=best_guess" +
                        "&key=" + apiKey;

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Read API response
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray routes = jsonResponse.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONArray legs = route.getJSONArray("legs");
                    if (legs.length() > 0) {
                        JSONObject leg = legs.getJSONObject(0);
                        String distance = leg.getJSONObject("distance").getString("text");  // Distance
                        int duration = leg.getJSONObject("duration").getInt("value");  // Normal time in seconds
                        int durationInTraffic = leg.getJSONObject("duration_in_traffic").getInt("value");  // Traffic time in seconds

                        // Calculate traffic congestion level
                        double increasePercent = ((double) (durationInTraffic - duration) / duration) * 100;
                        String trafficStatus;

                        if (increasePercent < 10) {
                            trafficStatus = "Light Traffic 🚀";
                        } else if (increasePercent < 30) {
                            trafficStatus = "Moderate Traffic 🚗";
                        } else if (increasePercent < 60) {
                            trafficStatus = "Heavy Traffic 🚦";
                        } else {
                            trafficStatus = "Severe Traffic 🔴";
                        }

                        return origin + " → " + destination + "\n" +
                                "Traffic: " + trafficStatus + "\n" +
                                "Time: " + (durationInTraffic / 60) + " min" +
                                " (" + distance + ")";
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching traffic data", e);
            }
            return "Traffic data unavailable ❌";
        }

        @Override
        protected void onPostExecute(String trafficInfo) {
            showNotification(context, trafficInfo, requestCode);
        }
    }

    private static void showNotification(Context context, String trafficInfo, int requestCode) {
        String channelId = "traffic_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Traffic Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Traffic Update 🚦")
                .setContentText(trafficInfo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(trafficInfo)) // Allow multiline text
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(requestCode, builder.build()); // Unique ID for each notification
        }
    }
}
