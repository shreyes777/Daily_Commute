package com.example.dailycommute;

import java.util.List;

public class Route {

    private String summary;
    private List<Leg>legs;

    public String getSummary(){
        return summary;
    }
    public List<Leg>getLegs(){
        return legs;
    }
}
