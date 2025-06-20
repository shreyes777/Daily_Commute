package com.example.dailycommute;

public class TrafficInfo {
    private String mode;
    private String trafficStatus;
    private String time;
    private String distance;
    private String startAddress;
    private String endAddress;

    public TrafficInfo(String mode, String trafficStatus, String time, String distance, String startAddress, String endAddress) {
        this.mode = mode;
        this.trafficStatus = trafficStatus;
        this.time = time;
        this.distance = distance;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    public String getMode() {
        return mode;
    }

    public String getTrafficStatus() {
        return trafficStatus;
    }

    public String getTime() {
        return time;
    }

    public String getDistance() {
        return distance;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }
}
