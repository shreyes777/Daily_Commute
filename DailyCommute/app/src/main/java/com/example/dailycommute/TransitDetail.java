package com.example.dailycommute;

public class TransitDetail {
    private String vehicleType;
    private String lineName;
    private String departureTime;
    private String arrivalTime;
    private String departureStop;
    private String arrivalStop;

    public TransitDetail(String vehicleType, String lineName, String departureTime, String arrivalTime, String departureStop, String arrivalStop) {
        this.vehicleType = vehicleType;
        this.lineName = lineName;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.departureStop = departureStop;
        this.arrivalStop = arrivalStop;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getLineName() {
        return lineName;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureStop() {
        return departureStop;
    }

    public String getArrivalStop() {
        return arrivalStop;
    }
}
