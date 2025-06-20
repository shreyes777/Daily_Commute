package com.example.dailycommute;

public class CommuteRoute {

    private String mode;
    private String time;
    private String distance;
    private String imageUrl;
    private String destinationStreet;

    // Constructor with all fields
    public CommuteRoute(String mode, String time, String distance, String imageUrl, String destinationStreet) {
        this.mode = mode;
        this.time = time;
        this.distance = distance;
        this.imageUrl = imageUrl;
        this.destinationStreet = destinationStreet;
    }

    // Getters
    public String getMode() {
        return mode;
    }

    public String getTime() {
        return time;
    }

    public String getDistance() {
        return distance;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDestinationStreet() {
        return destinationStreet;
    }

}
