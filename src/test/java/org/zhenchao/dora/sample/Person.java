package org.zhenchao.dora.sample;

import java.util.ArrayList;

public class Person {

    private int age;
    private ArrayList<HeartRate> heartRates;

    public Person(int age) {
        this.age = age;
        heartRates = new ArrayList<>();
    }

    public void addHeartRate(int rate, long timestamp) {
        heartRates.add(new HeartRate(timestamp, rate));
    }

    public ArrayList<HeartRate> getHeartRates() {
        return heartRates;
    }
}
