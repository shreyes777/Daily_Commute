package com.example.dailycommute;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.w3c.dom.Document;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DirectionsFetcher {

    public static void fetchDirections(Context context, double startLat, double startLng, double endLat, double endLng, final DirectionsCallback callback) {
        String url = "https://maps.googleapis.com/maps/api/directions/xml?" +
                "origin=" + startLat + "," + startLng +
                "&destination=" + endLat + "," + endLng +
                "&sensor=false&units=metric&mode=driving";

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Convert string response to InputStream
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getBytes());

                            // Parse XML response
                            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                            Document doc = builder.parse(inputStream);

                            // Return the parsed XML document
                            callback.onSuccess(doc);
                        } catch (Exception e) {
                            Log.e("VolleyError", "XML Parsing Error: " + e.getMessage());
                            callback.onError(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", "Network Error: " + error.getMessage());
                callback.onError(error);
            }
        });

        // Add the request to the queue
        requestQueue.add(stringRequest);
    }

    // Callback interface for handling success and errors
    public interface DirectionsCallback {
        void onSuccess(Document doc);
        void onError(Exception e);
    }
}
