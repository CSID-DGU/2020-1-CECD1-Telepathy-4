package com.example.betterfit.ui.home;

import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class HomeViewModel extends ViewModel {

    private Map<Date, String> mDailyStepCountMap = new TreeMap<>();

    /***
     * Method to add new key-value into the fitness data map.
     * @param date Date for steps.
     * @param steps String representing the number of steps.
     */
    public void addDailyStepCount(Date date, String steps) {
        if (date == null) {
            return;
        }

        mDailyStepCountMap.put(date, steps.isEmpty() ? "0" : steps);
    }

    /***
     * Method to clear the data in the map.
     */
    public void clearData() {
        mDailyStepCountMap.clear();
    }

    /***
     * Method to get the fitness data.
     * @return Map of data
     */
    public Map<Date, String> getFitnessData() {
        return mDailyStepCountMap;
    }

}