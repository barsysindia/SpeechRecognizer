package com.example.pratyush.speechrecognizer;

public class SpeechResult {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    private int distance;

    public SpeechResult(String name, int distance) {
        this.name = name;
        this.distance = distance;
    }


}
