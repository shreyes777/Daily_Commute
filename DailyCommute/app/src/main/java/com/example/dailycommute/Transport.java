package com.example.dailycommute;

public class Transport {

    private String name, type, vicinity, imageUrl;

    public Transport(String name, String type, String vicinity, String imageUrl) {
        this.name = name;
        this.type = type;
        this.vicinity = vicinity;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public String getVicinity() {
        return vicinity;
    }
    public String getImageUrl() {
        return imageUrl;
    }
}
