package com.example.betterfit.ui.statistics;

import androidx.lifecycle.ViewModelProvider;

import com.example.betterfit.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StatisticsFragment extends Fragment {

    private static final String TAG = StatisticsFragment.class.getSimpleName();
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    private TextView date, step_count, review1, review2, review3;
    private Button dayBtn, weekBtn, monthBtn, yearBtn, prevBtn, nextBtn;
    private BarChart chart;
    private StatisticsViewModel statisticsViewModel;
    Map<String,Integer> totalStep1 = new HashMap<String,Integer>();
    Map<String,Integer> totalStep2 = new HashMap<String,Integer>();
    Map<String,Integer> totalStep3 = new HashMap<String,Integer>();
    Map<String,Integer> totalStep4 = new HashMap<String,Integer>();

    private final FitnessOptions mFitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build();

    private final DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .setAppPackageName("com.google.android.gms")
            .build();

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        chart = (BarChart)view.findViewById(R.id.chart2);
        step_count = (TextView)view.findViewById(R.id.step_count);

        return view;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override public void onResume() {
        super.onResume();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(getActivity(), mFitnessOptions);

        final DateFormat mFormat = new SimpleDateFormat("yyyy.M.d.EE");
        final DateFormat mFormat2 = new SimpleDateFormat("yyyy.M");
        final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        final Calendar cal = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();
        final Calendar check = Calendar.getInstance();
        cal.setTime(new Date());
        cal2.setTime(new Date());
        final String formatDate = mFormat.format(cal.getTime());
        int flag = 0; // 0:today, 1:prev, 2:next
        final int colorBlack = getResources().getColor(R.color.colorBlack);
        final int colorGray = getResources().getColor(R.color.colorDarkGray);

        date = (TextView) getView().findViewById(R.id.date);
        date.setText(formatDate);
        prevBtn = (Button) getView().findViewById(R.id.prev);
        nextBtn = (Button) getView().findViewById(R.id.next);
        review1 = (TextView) getView().findViewById(R.id.review1);
        review2 = (TextView) getView().findViewById(R.id.review2);
        review3 = (TextView) getView().findViewById(R.id.review3);
        final ProgressBar progressBar1 = (ProgressBar) getView().findViewById(R.id.progressBar1);
        final ProgressBar progressBar2 = (ProgressBar) getView().findViewById(R.id.progressBar2);

        dayBtn = (Button) getView().findViewById(R.id.day);
        weekBtn = (Button) getView().findViewById(R.id.week);
        monthBtn = (Button) getView().findViewById(R.id.month);
        yearBtn = (Button) getView().findViewById(R.id.year);

        // Check Google Fit Permission
        if (GoogleSignIn.hasPermissions(account, mFitnessOptions)) {
            chart.removeAllViews();

            cal.add(Calendar.DATE, -1);
            getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 1);
            cal.add(Calendar.DATE, +1);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 0);
            prevBtn.setEnabled(true);
            nextBtn.setEnabled(true);
            date.setTextColor(colorBlack);
            step_count.setVisibility(View.VISIBLE);

            prevBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cal.add(Calendar.DATE, -2);
                    getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 1);

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    cal.add(Calendar.DATE, +1);
                    date.setText(mFormat.format(cal.getTime()));
                    getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 1);
                }

            });

            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(cal.get(Calendar.YEAR) == check.get(Calendar.YEAR)
                            && cal.get(Calendar.MONTH) == check.get(Calendar.MONTH)
                            && cal.get(Calendar.DATE) == check.get(Calendar.DATE)) {
                    }
                    else {
                        cal.add(Calendar.DATE, +1);
                        date.setText(mFormat.format(cal.getTime()));
                        getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 2);
                    }
                }
            });

            dayBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    dayBtn.setBackgroundResource(R.drawable.greenbtn);
                    weekBtn.setBackgroundResource(R.drawable.graybtn);
                    monthBtn.setBackgroundResource(R.drawable.graybtn);
                    yearBtn.setBackgroundResource(R.drawable.graybtn);

                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);
                    date.setTextColor(colorBlack);
                    step_count.setVisibility(View.VISIBLE);

                    chart.removeAllViews();
                    cal.setTime(new Date());
                    date.setText(mFormat.format(cal.getTime()));

                    cal.add(Calendar.DATE, -1);
                    getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 1);
                    cal.add(Calendar.DATE, +1);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 0);

                    prevBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cal.add(Calendar.DATE, -2);
                            getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 1);

                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            cal.add(Calendar.DATE, +1);
                            date.setText(mFormat.format(cal.getTime()));
                            getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 1);
                        }
                    });

                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(cal.get(Calendar.YEAR) == check.get(Calendar.YEAR)
                                    && cal.get(Calendar.MONTH) == check.get(Calendar.MONTH)
                                    && cal.get(Calendar.DATE) == check.get(Calendar.DATE)) {
                            }
                            else {
                                cal.add(Calendar.DATE, +1);
                                date.setText(mFormat.format(cal.getTime()));
                                getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal, 2);
                            }
                        }
                    });

                    return false;
                }
            });

            weekBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    dayBtn.setBackgroundResource(R.drawable.graybtn);
                    weekBtn.setBackgroundResource(R.drawable.greenbtn);
                    monthBtn.setBackgroundResource(R.drawable.graybtn);
                    yearBtn.setBackgroundResource(R.drawable.graybtn);

                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);
                    date.setTextColor(colorBlack);
                    step_count.setVisibility(View.VISIBLE);

                    chart.removeAllViews();
                    cal.setTime(new Date());
                    cal2.setTime(cal.getTime());
                    cal2.add(Calendar.DATE, -6);
                    //Log.d(TAG, "1. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                    date.setText(mFormat.format(cal2.getTime())+"~"+mFormat.format(cal.getTime()));

                    cal.add(Calendar.DATE, -6);
                    //Log.d(TAG, "2. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                    getDailyStepCountsFromGoogleFit2(mFitnessOptions, cal, 1); // last week
                    // cal.add(Calendar.DATE, +6);
                    //Log.d(TAG, "3-1. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getDailyStepCountsFromGoogleFit2(mFitnessOptions, cal, 0); // this week
                    cal.add(Calendar.DATE, +6);
                    //Log.d(TAG, "3-2. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());

                    prevBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //cal2.setTime(cal.getTime());
                            cal2.add(Calendar.DATE, -7);
                            //Log.d(TAG, "4. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());

                            cal.add(Calendar.DATE, -13);
                            //Log.d(TAG, "5-1. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime()); // last x 2 week
                            getDailyStepCountsFromGoogleFit2(mFitnessOptions, cal, 1);
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //Log.d(TAG, "5-2. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                            cal.add(Calendar.DATE, +14);
                            //Log.d(TAG, "6. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                            cal.add(Calendar.DATE, -1);
                            date.setText(mFormat.format(cal2.getTime())+"~"+mFormat.format(cal.getTime()));
                            cal.add(Calendar.DATE, +1);
                            getDailyStepCountsFromGoogleFit2(mFitnessOptions, cal, 1); // last week
                            //Log.d(TAG, "7. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                            cal.add(Calendar.DATE, +6);
                            //Log.d(TAG, "8. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                        }
                    });

                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //cal2.add(Calendar.DATE, +1);
                            //Log.d(TAG, "9. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());

                            if(cal.get(Calendar.YEAR) == check.get(Calendar.YEAR)
                                    && cal.get(Calendar.MONTH) == check.get(Calendar.MONTH)
                                    && cal.get(Calendar.DATE) == check.get(Calendar.DATE)) {
                            }
                            else {
                                //cal2.setTime(cal.getTime());
                                cal2.add(Calendar.DATE, +7);
                                cal.add(Calendar.DATE, +8);
                                //Log.d(TAG, "10. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                                getDailyStepCountsFromGoogleFit2(mFitnessOptions, cal, 2);
                                cal.add(Calendar.DATE, +6);
                                date.setText(mFormat.format(cal2.getTime())+"~"+mFormat.format(cal.getTime()));
                            }
                        }
                    });

                    return false;
                }
            });

            monthBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    dayBtn.setBackgroundResource(R.drawable.graybtn);
                    weekBtn.setBackgroundResource(R.drawable.graybtn);
                    monthBtn.setBackgroundResource(R.drawable.greenbtn);
                    yearBtn.setBackgroundResource(R.drawable.graybtn);

                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);
                    date.setTextColor(colorBlack);
                    step_count.setVisibility(View.VISIBLE);

                    chart.removeAllViews();
                    cal.setTime(new Date());
                    cal2.setTime(cal.getTime());
                    cal2.add(Calendar.DATE, -29);
                    //Log.d(TAG, "1. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                    date.setText(mFormat.format(cal2.getTime())+"~"+mFormat.format(cal.getTime()));

                    cal.add(Calendar.DATE, -29);
                    //Log.d(TAG, "2. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                    getDailyStepCountsFromGoogleFit3(mFitnessOptions, cal, 1); // last week
                    // cal.add(Calendar.DATE, +6);
                    //Log.d(TAG, "3-1. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getDailyStepCountsFromGoogleFit3(mFitnessOptions, cal, 0); // this week
                    cal.add(Calendar.DATE, +29);
                    //Log.d(TAG, "3-2. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());

                    prevBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //cal2.setTime(cal.getTime());
                            cal2.add(Calendar.DATE, -30);
                            //Log.d(TAG, "4. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());

                            cal.add(Calendar.DATE, -59);
                            //Log.d(TAG, "5-1. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime()); // last x 2 week
                            getDailyStepCountsFromGoogleFit3(mFitnessOptions, cal, 1);
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //Log.d(TAG, "5-2. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                            cal.add(Calendar.DATE, +60);
                            //Log.d(TAG, "6. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                            cal.add(Calendar.DATE, -1);
                            date.setText(mFormat.format(cal2.getTime())+"~"+mFormat.format(cal.getTime()));
                            cal.add(Calendar.DATE, +1);
                            getDailyStepCountsFromGoogleFit3(mFitnessOptions, cal, 1); // last week
                            //Log.d(TAG, "7. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                            cal.add(Calendar.DATE, +29);
                            //Log.d(TAG, "8. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                        }
                    });

                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //cal2.add(Calendar.DATE, +1);
                            //Log.d(TAG, "9. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());

                            if(cal.get(Calendar.YEAR) == check.get(Calendar.YEAR)
                                    && cal.get(Calendar.MONTH) == check.get(Calendar.MONTH)
                                    && cal.get(Calendar.DATE) == check.get(Calendar.DATE)) {
                            }
                            else {
                                //cal2.setTime(cal.getTime());
                                cal2.add(Calendar.DATE, +30);
                                cal.add(Calendar.DATE, +31);
                                //Log.d(TAG, "10. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                                getDailyStepCountsFromGoogleFit3(mFitnessOptions, cal, 2);
                                cal.add(Calendar.DATE, +29);
                                date.setText(mFormat.format(cal2.getTime())+"~"+mFormat.format(cal.getTime()));
                                //Log.d(TAG, "11. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                            }
                        }
                    });

                    return false;
                }
            });

            yearBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    dayBtn.setBackgroundResource(R.drawable.graybtn);
                    weekBtn.setBackgroundResource(R.drawable.graybtn);
                    monthBtn.setBackgroundResource(R.drawable.graybtn);
                    yearBtn.setBackgroundResource(R.drawable.greenbtn);

                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);
                    date.setTextColor(colorBlack);
                    step_count.setVisibility(View.VISIBLE);

                    chart.removeAllViews();
                    cal.setTime(new Date());
                    cal2.setTime(cal.getTime());
                    cal2.add(Calendar.YEAR, -1);
                    cal2.add(Calendar.MONTH, +1);
                    //Log.d(TAG, "1. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                    date.setText(mFormat.format(cal2.getTime())+"~"+mFormat.format(cal.getTime()));

                    cal.add(Calendar.MONTH, -7);
                    cal.add(Calendar.DATE, +1);
                    //Log.d(TAG, "2. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                    //getDailyStepCountsFromGoogleFit4(mFitnessOptions, cal, 1); // last week
                    //cal.add(Calendar.DATE, +6);
                    //Log.d(TAG, "3-1. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getDailyStepCountsFromGoogleFit4(mFitnessOptions, cal, 0); // this week
                    cal.add(Calendar.MONTH, +7);
                    cal.add(Calendar.DATE, -1);
                    //Log.d(TAG, "3-2. cal1: " + cal.getTime() + ", cal2: " + cal2.getTime());

                    prevBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /*cal2.add(Calendar.YEAR, -1);
                            cal.add(Calendar.YEAR, -2);
                            cal.add(Calendar.DATE, +1);
                            //getDailyStepCountsFromGoogleFit4(mFitnessOptions, cal, 1);
                            cal.add(Calendar.YEAR, +2);
                            cal.add(Calendar.DATE, -1);
                            date.setText(mFormat.format(cal2.getTime())+"~"+mFormat.format(cal.getTime()));
                            cal.add(Calendar.DATE, +1);
                            getDailyStepCountsFromGoogleFit4(mFitnessOptions, cal, 1); // last week
                            cal.add(Calendar.YEAR, +1);
                            cal.add(Calendar.DATE, -1);*/
                        }
                    });

                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(cal.get(Calendar.YEAR) == check.get(Calendar.YEAR)
                                    && cal.get(Calendar.MONTH) == check.get(Calendar.MONTH)
                                    && cal.get(Calendar.DATE) == check.get(Calendar.DATE)) {
                            }
                            else {
                                /*cal2.add(Calendar.YEAR, +1);
                                cal.add(Calendar.YEAR, +1);
                                cal.add(Calendar.DATE, +1);
                                getDailyStepCountsFromGoogleFit4(mFitnessOptions, cal, 2);
                                cal.add(Calendar.YEAR, +1);
                                cal.add(Calendar.DATE, -1);
                                date.setText(mFormat.format(cal2.getTime())+"~"+mFormat.format(cal.getTime()));*/
                            }
                        }
                    });
//
                    return false;
                }
            });
        }
        else {
            Log.w(TAG, "Google Fit Permission failed");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getDailyStepCountsFromGoogleFit1(FitnessOptions fitnessOptions, final Calendar cal, final int flag) {
        // Create the start and end times for the date range
        long endTime, startTime;

        if(flag == 0){
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, +1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            endTime = cal.getTimeInMillis();
            /*cal.setTime(new Date());
            cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal.get(Calendar.SECOND));
            endTime = cal.getTimeInMillis();*/

            cal.add(Calendar.DAY_OF_YEAR, -1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();
        }
        else { // (flag == 1 || flag == 2)
            cal.add(Calendar.DAY_OF_YEAR, +1); // cal.add(Calendar.MONTH, -12);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            endTime = cal.getTimeInMillis();

            cal.add(Calendar.DAY_OF_YEAR, -1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();
        }

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //Log.d(TAG, "Date Start1: " + dateFormat.format(startTime));
        //Log.d(TAG, "Date End1: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.HOURS) // .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                getActivity(), fitnessOptions);

        Fitness.getHistoryClient(getActivity(), account)
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override public void onSuccess(DataReadResponse response) {
                        statisticsViewModel.clearData();

                        if (!response.getBuckets().isEmpty()) {
                            for (Bucket bucket : response.getBuckets()) {
                                String stepCount = "0";
                                Date bucketStart = new Date(bucket.getStartTime(TimeUnit.MILLISECONDS));
                                Date bucketEnd = new Date(bucket.getEndTime(TimeUnit.MILLISECONDS));
                                Log.d(TAG, "Bucket start / end times: " +  dateFormat.format(bucketStart)
                                        + " - " + dateFormat.format(bucketEnd));

                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet set : dataSets) {
                                    List<DataPoint> dataPoints = set.getDataPoints();
                                    //Log.d(TAG, "dataset: " + set.getDataType().getName());
                                    for (DataPoint dp : dataPoints) {
                                        //Log.d(TAG, "datapoint: " + dp.getDataType().getName());
                                        for (Field field : dp.getDataType().getFields()) {
                                            stepCount = dp.getValue(field).toString();
                                            //Log.d(TAG, "Field: " + field.getName() + " Value: " + dp.getValue(field));
                                        }
                                    }
                                }

                                //statisticsViewModel.addDailyStepCount(bucketStart, stepCount);
                                // Add the data
                                if (statisticsViewModel != null) {
                                    statisticsViewModel.addDailyStepCount(bucketStart, stepCount);
                                }
                            }

                            // Update current day step count
                            //readDailyTotalSteps();
                            buildChart1(cal);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        //Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    /***
     * Method to get the data from the viewmodel and then build the TableLayout.
     */
    private void buildChart1(Calendar cal) {

        int check = 0;
        int stepsum = 0;
        int stepmax = 0;
        final DateFormat dateFormat = DateFormat.getDateInstance();
        chart.removeAllViews();
        List<BarEntry> values = new ArrayList<BarEntry>();
        SimpleDateFormat hf = new SimpleDateFormat("HH");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        for (Map.Entry<Date, String> entry : statisticsViewModel.getFitnessData().entrySet()) {
            String x = hf.format(entry.getKey());
            String y = entry.getValue();
            values.add(new BarEntry(Integer.parseInt(x), Integer.parseInt(y)));

            stepsum += Integer.parseInt(entry.getValue());
            check++;
            if(check == 24) {
                totalStep1.put(df.format(entry.getKey()), stepsum);
                //totalStep1.put(Integer.parseInt(df.format(entry.getKey())), stepsum);
                check = 0;
                stepsum = 0;
            }
            if(stepmax < Integer.parseInt(entry.getValue())) {
                stepmax = Integer.parseInt(entry.getValue());
            }
        }

        BarDataSet set1 = new BarDataSet(values, "걸음 수");
        set1.setColor(Color.parseColor("#FE4901"));
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        data.setHighlightEnabled(false);
        data.setValueTextSize(8f);

        /*for (Map.Entry<String, Integer> entry : totalStep1.entrySet()) {
            Log.d(TAG, "key1: " + entry.getKey() + ", value1: " + entry.getValue());
        }*/

        //Log.d(TAG, "총 "+stepsum+"걸음");
        set1.setValueFormatter(new StepValueFormatter());
        //set1.setDrawValues(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new DayValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        YAxis yLAxis = chart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = chart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
        yLAxis.setAxisMinimum(0);

        ProgressBar progressBar1 = (ProgressBar) getView().findViewById(R.id.progressBar1);
        ProgressBar progressBar2 = (ProgressBar) getView().findViewById(R.id.progressBar2);

        for(int i = 1; i <= 60; i++) {
            if(stepmax <= 500*i) {
                yLAxis.setAxisMaximum(500*(i+1));
                break;
            }
        }
        //Log.d(TAG, Integer.toString(progressBar1.getMax()));

        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        //chart.animateY(800);
        chart.setData(data);
        chart.invalidate();
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);

        //TreeMap<Integer,Integer> tm = new TreeMap<Integer,Integer>(totalStep1);
        //Iterator<Integer> iterator = tm.keySet( ).iterator( );
        /*while(iterator.hasNext()) {
            int key = iterator.next();
            Log.d(TAG, "key = " + key);
            Log.d(TAG, "value = " + totalStep1.get(key));
        }*/

        //progressBar1.setMax(5000);
        //progressBar2.setMax(5000);

        int fin1 = 0, fin2 = 0;
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(cal.getTime());
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        String fd = df.format(cal2.getTime());
        Integer val1 = totalStep1.get(fd);
        cal2.add(Calendar.DATE, -1);
        fd = df.format(cal2.getTime());
        Integer val2 = totalStep1.get(fd);

        if(val1 != null)
            fin1 = val1;
        if(val2 != null)
            fin2 = val2;

        int max_fin = Math.max(fin1, fin2);
        progressBar1.setMax(max_fin);
        progressBar2.setMax(max_fin);
        progressBar1.setProgress(fin1);
        progressBar2.setProgress(fin2);

        step_count.setText("총 "+fin1+"걸음");
        if(fin1 - fin2 > 0) {
            int more = fin1 - fin2;
            review1.setText("어제보다 "+more+"걸음 더 걸었습니다.");
        }
        else if(fin2 - fin1 > 0) {
            int less = fin2 - fin1;
            review1.setText("어제보다 "+less+"걸음 덜 걸었습니다.");
        }
        else {
            review1.setText("어제와 오늘의 걸음 수가 동일합니다.");
        }
        review2.setText("오늘 "+fin1+" 걸음");
        review3.setText("어제 "+fin2+" 걸음");
    }

    class StepValueFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public StepValueFormatter() {
            mFormat = new DecimalFormat("#");
        }

        @Override
        public String getFormattedValue(float value) {
            if(value > 0)
                return mFormat.format(value);
            else
                return "";
        }
    }

    class DayValueFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public DayValueFormatter() {
            mFormat = new DecimalFormat("##시");
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value);
        }
    }

    private void getDailyStepCountsFromGoogleFit2(FitnessOptions fitnessOptions, final Calendar cal, final int flag) {
        // Create the start and end times for the date range
        long endTime, startTime;
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        if(flag == 0){
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, +1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            endTime = cal.getTimeInMillis();
            /*cal.setTime(new Date());
            cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal.get(Calendar.SECOND));
            endTime = cal.getTimeInMillis();*/

            cal.add(Calendar.DAY_OF_YEAR, -7);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();
        }
        else { // flag == 1 or 2
            //cal.add(Calendar.DAY_OF_YEAR, +6); // cal.add(Calendar.MONTH, -12);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            endTime = cal.getTimeInMillis();

            cal.add(Calendar.DAY_OF_YEAR, -7);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();
        }


        //Log.d(TAG, "Date Start2: " + dateFormat.format(startTime));
        //Log.d(TAG, "Date End2: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.HOURS) // .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                requireActivity(), fitnessOptions);

        Fitness.getHistoryClient(getActivity(), account)
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override public void onSuccess(DataReadResponse response) {
                        statisticsViewModel.clearData();

                        if (!response.getBuckets().isEmpty()) {
                            for (Bucket bucket : response.getBuckets()) {
                                String stepCount = "0";
                                Date bucketStart = new Date(bucket.getStartTime(TimeUnit.MILLISECONDS));
                                Date bucketEnd = new Date(bucket.getEndTime(TimeUnit.MILLISECONDS));
                                /*Log.d(TAG, "Bucket start2 / end times2: " +  dateFormat.format(bucketStart)
                                        + " - " + dateFormat.format(bucketEnd));*/

                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet set : dataSets) {
                                    List<DataPoint> dataPoints = set.getDataPoints();
                                    //Log.d(TAG, "dataset2: " + set.getDataType().getName());
                                    for (DataPoint dp : dataPoints) {
                                        //Log.d(TAG, "datapoint2: " + dp.getDataType().getName());
                                        for (Field field : dp.getDataType().getFields()) {
                                            stepCount = dp.getValue(field).toString();
                                            //Log.d(TAG, "Field2: " + field.getName() + " Value2: " + dp.getValue(field));
                                        }
                                    }
                                }

                                //statisticsViewModel.addDailyStepCount(bucketStart, stepCount);
                                // Add the data
                                if (statisticsViewModel != null) {
                                    statisticsViewModel.addDailyStepCount(bucketStart, stepCount);
                                }
                            }

                            // Update current day step count
                            //readDailyTotalSteps();
                            buildChart2(cal);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        //Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    private void buildChart2(Calendar cal) {

        int check1 = 0, check2 = 0, check3 = 0;
        int stepsum = 0, weeksum = 0;
        int stepmax = 0;
        int x = 0;
        final DateFormat dateFormat = DateFormat.getDateInstance();
        chart.removeAllViews();
        List<BarEntry> values = new ArrayList<BarEntry>();
        SimpleDateFormat wf = new SimpleDateFormat("EE");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        for (Map.Entry<Date, String> entry : statisticsViewModel.getFitnessData().entrySet()) {
            stepsum += Integer.parseInt(entry.getValue());
            check1++;
            check2++;
            check3++;
            if(check3 == 24) {
                switch(wf.format(entry.getKey())){
                    case "일":
                        x = 0;
                        values.add(new BarEntry(x, stepsum));
                        break;
                    case "월":
                        x = 10;
                        values.add(new BarEntry(x, stepsum));
                        break;
                    case "화":
                        x = 20;
                        values.add(new BarEntry(x, stepsum));
                        break;
                    case "수":
                        x = 30;
                        values.add(new BarEntry(x, stepsum));
                        break;
                    case "목":
                        x = 40;
                        values.add(new BarEntry(x, stepsum));
                        break;
                    case "금":
                        x = 50;
                        values.add(new BarEntry(x, stepsum));
                        break;
                    case "토":
                        x = 60;
                        values.add(new BarEntry(x, stepsum));
                        break;
                }
                x++;
            }
            if(check1 == 24) {
                //values.add(new BarEntry(Integer.parseInt(wf.format(entry.getKey())), stepsum));
                //Log.d(TAG, "BarEntry2: " + df.format(entry.getKey()) + " stepsum2: " + stepsum);
                //Log.d(TAG, "BarEntry2: " + values.toString());
                if(check3 > 24) {
                    values.add(new BarEntry(x, stepsum));
                    x++;
                    //Log.d(TAG, "BarEntry1: " + values.toString());
                }
                if(stepmax < stepsum) {
                    stepmax = stepsum;
                }
                weeksum += stepsum;
                stepsum = 0;
                check1 = 0;
            }
            if(check2 == 168) {
                totalStep2.put(df.format(entry.getKey()), weeksum / 7);
                //Log.d(TAG, "key2: " + df.format(entry.getKey()) + ", value2: " + weeksum/7);
            }
        }

        /*for (Map.Entry<Integer, Integer> entry : totalStep2.entrySet()) {
            Log.d(TAG, "key2: " + entry.getKey() + "value2: " + entry.getValue());
        }*/

        /*for (Map.Entry<Integer, Integer> entry : totalStep2.entrySet()) {
            String x = df.format(entry.getKey());
            values.add(new BarEntry(Integer.parseInt(x), entry.getValue()));
        }*/

        BarDataSet set1 = new BarDataSet(values, "걸음 수");
        set1.setColor(Color.parseColor("#FE4901"));
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        //data.setValueFormatter(new WeekValueFormatter());
        data.setHighlightEnabled(false);
        data.setValueTextSize(8f);
        data.setBarWidth(0.7f);

        //Log.d(TAG, "총 "+stepsum+"걸음");
        set1.setValueFormatter(new StepValueFormatter());
        //Log.d(TAG, "set1: " + set1);
        //set1.setDrawValues(false);

        //Log.d(TAG, "chart1: " + chart.getX() + ", " + chart.getXAxis());
        //chart.setX(2);
        XAxis xAxis = chart.getXAxis();
        //Log.d(TAG, "chart2: " + chart.getX() + ", " + chart.getXAxis());
        //Log.d(TAG, "chart3: " + xAxis.getLongestLabel() + ", " + xAxis.getLabelCount());
        xAxis.setValueFormatter(new WeekValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        YAxis yLAxis = chart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = chart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
        yLAxis.setAxisMinimum(0);

        ProgressBar progressBar1 = (ProgressBar) getView().findViewById(R.id.progressBar1);
        ProgressBar progressBar2 = (ProgressBar) getView().findViewById(R.id.progressBar2);

        for(int i = 1; i <= 60; i++) {
            if(stepmax <= 500*i) {
                yLAxis.setAxisMaximum(500*(i+1));
                break;
            }
        }
        //Log.d(TAG, Integer.toString(progressBar1.getMax()));

        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        //chart.animateY(800);
        chart.setData(data);
        chart.invalidate();
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        //chart.getAxisLeft().setLabelCount(7, true);
        //chart.setVisibleXRangeMaximum(7);

        /*TreeMap<String,Integer> tm = new TreeMap<String,Integer>(totalStep2);
        Iterator<String> iterator = tm.keySet( ).iterator( );
        while(iterator.hasNext()) {
            String key = iterator.next();
            Log.d(TAG, "key = " + key);
            Log.d(TAG, "value = " + totalStep2.get(key));
        }*/

        //progressBar1.setMax(5000);
        //progressBar2.setMax(5000);

        int fin1 = 0, fin2 = 0;
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(cal.getTime());
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        String fd = df.format(cal2.getTime());
        Integer val1 = totalStep2.get(fd);
        cal2.add(Calendar.DATE, -7);
        fd = df.format(cal2.getTime());
        Integer val2 = totalStep2.get(fd);

        /*String fd = df.format(cal.getTime());
        Integer val1 = totalStep2.get(Integer.parseInt(fd));
        Integer val2 = totalStep2.get(Integer.parseInt(fd)-7);*/

        if(val1 != null)
            fin1 = val1;
        if(val2 != null)
            fin2 = val2;

        int max_fin = Math.max(fin1, fin2);
        progressBar1.setMax(max_fin);
        progressBar2.setMax(max_fin);
        progressBar1.setProgress(fin1);
        progressBar2.setProgress(fin2);

        step_count.setText("평균 "+fin1+"걸음");
        if(fin1 - fin2 > 0) {
            int more = fin1 - fin2;
            review1.setText("저번주보다 하루 평균 "+more+"걸음 더 걸었습니다.");
        }
        else if(fin2 - fin1 > 0) {
            int less = fin2 - fin1;
            review1.setText("저번주보다 하루 평균 "+less+"걸음 덜 걸었습니다.");
        }
        else {
            review1.setText("이번주와 저번주의 평균 걸음 수가 동일합니다.");
        }
        review2.setText("이번주 평균 "+fin1+" 걸음/일");
        review3.setText("저번주 평균 "+fin2+" 걸음/일");
    }

    class WeekValueFormatter extends ValueFormatter {

        public WeekValueFormatter() {
            //f1 = new DecimalFormat("월");
        }

        @Override
        public String getFormattedValue(float value) {
            //Log.d(TAG, "value: " + value);
            if(value == 0 || value == 16 || value == 25 || value == 34 || value == 43 || value == 52 || value == 61)
                return "일";
            else if(value == 1 || value == 10 || value == 26 || value == 35 || value == 44 || value == 53 || value == 62)
                return "월";
            else if(value == 2 || value == 11 || value == 20 || value == 36 || value == 45 || value == 54 || value == 63)
                return "화";
            else if(value == 3 || value == 12 || value == 21 || value == 30 || value == 46 || value == 55 || value == 64)
                return "수";
            else if(value == 4 || value == 13 || value == 22 || value == 31 || value == 40 || value == 56 || value == 65)
                return "목";
            else if(value == 5 || value == 14 || value == 23 || value == 32 || value == 41 || value == 50 || value == 66)
                return "금";
            else // (value == 6 || value == 15 || value == 24 || value == 33 || value == 42 || value == 51 || value == 60)
                return "토";
        }
    }

    private void getDailyStepCountsFromGoogleFit3(FitnessOptions fitnessOptions, final Calendar cal, final int flag) {
        // Create the start and end times for the date range
        long endTime, startTime;
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        if(flag == 0){
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, +1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            endTime = cal.getTimeInMillis();
            /*cal.setTime(new Date());
            cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal.get(Calendar.SECOND));
            endTime = cal.getTimeInMillis();*/

            cal.add(Calendar.DAY_OF_YEAR, -30);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();

            Log.d(TAG, "Date Start3-1: " + dateFormat.format(startTime));
            Log.d(TAG, "Date End3-1: " + dateFormat.format(endTime));
        }
        else { // flag == 1 or 2
            //cal.add(Calendar.DAY_OF_YEAR, +6); // cal.add(Calendar.MONTH, -12);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            endTime = cal.getTimeInMillis();

            cal.add(Calendar.DAY_OF_YEAR, -30);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();

            Log.d(TAG, "Date Start3-2: " + dateFormat.format(startTime));
            Log.d(TAG, "Date End3-2: " + dateFormat.format(endTime));
        }

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.HOURS) // .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                requireActivity(), fitnessOptions);

        Fitness.getHistoryClient(getActivity(), account)
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override public void onSuccess(DataReadResponse response) {
                        statisticsViewModel.clearData();

                        if (!response.getBuckets().isEmpty()) {
                            for (Bucket bucket : response.getBuckets()) {
                                String stepCount = "0";
                                Date bucketStart = new Date(bucket.getStartTime(TimeUnit.MILLISECONDS));
                                Date bucketEnd = new Date(bucket.getEndTime(TimeUnit.MILLISECONDS));
                                /*Log.d(TAG, "Bucket start2 / end times2: " +  dateFormat.format(bucketStart)
                                        + " - " + dateFormat.format(bucketEnd));*/

                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet set : dataSets) {
                                    List<DataPoint> dataPoints = set.getDataPoints();
                                    //Log.d(TAG, "dataset2: " + set.getDataType().getName());
                                    for (DataPoint dp : dataPoints) {
                                        //Log.d(TAG, "datapoint2: " + dp.getDataType().getName());
                                        for (Field field : dp.getDataType().getFields()) {
                                            stepCount = dp.getValue(field).toString();
                                            //Log.d(TAG, "Field2: " + field.getName() + " Value2: " + dp.getValue(field));
                                        }
                                    }
                                }

                                //statisticsViewModel.addDailyStepCount(bucketStart, stepCount);
                                // Add the data
                                if (statisticsViewModel != null) {
                                    statisticsViewModel.addDailyStepCount(bucketStart, stepCount);
                                }
                            }

                            // Update current day step count
                            //readDailyTotalSteps();
                            buildChart3(cal);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        //Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    private void buildChart3(Calendar cal) {

        int check1 = 0, check2 = 0, check3 = 0;
        int stepsum = 0, monthsum = 0;
        int stepmax = 0;
        String str = null;
        //int x = 1000;
        final DateFormat dateFormat = DateFormat.getDateInstance();
        chart.removeAllViews();
        List<BarEntry> values = new ArrayList<BarEntry>();
        SimpleDateFormat wf = new SimpleDateFormat("dd");
        SimpleDateFormat mf = new SimpleDateFormat("MM");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        for (Map.Entry<Date, String> entry : statisticsViewModel.getFitnessData().entrySet()) {
            stepsum += Integer.parseInt(entry.getValue());
            check1++;
            check2++;
            check3++;
            if(check3 == 24) {
                str = mf.format(entry.getKey());
            }
            if(check1 == 24) {
                //x += Integer.parseInt(wf.format(entry.getKey()));
                if(mf.format(entry.getKey()).equals(str)) {
                    values.add(new BarEntry(Integer.parseInt(wf.format(entry.getKey())), stepsum));
                }
                else {
                    values.add(new BarEntry(31+Integer.parseInt(wf.format(entry.getKey())), stepsum));
                }
                //x -= Integer.parseInt(wf.format(entry.getKey()));
                //x += 100;
                //Log.d(TAG, "BarEntry2: " + df.format(entry.getKey()) + " stepsum2: " + stepsum);
                //Log.d(TAG, "BarEntry2: " + values.toString());
                if(stepmax < stepsum) {
                    stepmax = stepsum;
                }
                monthsum += stepsum;
                stepsum = 0;
                check1 = 0;
            }
            if(check2 == 720) {
                totalStep3.put(df.format(entry.getKey()), monthsum / 30);
                //Log.d(TAG, "key2: " + df.format(entry.getKey()) + ", value2: " + weeksum/7);
            }
        }

        /*for (Map.Entry<Integer, Integer> entry : totalStep2.entrySet()) {
            Log.d(TAG, "key2: " + entry.getKey() + "value2: " + entry.getValue());
        }*/

        /*for (Map.Entry<Integer, Integer> entry : totalStep2.entrySet()) {
            String x = df.format(entry.getKey());
            values.add(new BarEntry(Integer.parseInt(x), entry.getValue()));
        }*/

        BarDataSet set1 = new BarDataSet(values, "걸음 수");
        set1.setColor(Color.parseColor("#FE4901"));
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        //data.setValueFormatter(new WeekValueFormatter());
        data.setHighlightEnabled(false);
        data.setValueTextSize(5f);
        data.setBarWidth(0.7f);

        //Log.d(TAG, "총 "+stepsum+"걸음");
        set1.setValueFormatter(new StepValueFormatter());
        //Log.d(TAG, "set1: " + set1);
        //set1.setDrawValues(false);

        //Log.d(TAG, "chart1: " + chart.getX() + ", " + chart.getXAxis());
        //chart.setX(2);
        XAxis xAxis = chart.getXAxis();
        //Log.d(TAG, "chart2: " + chart.getX() + ", " + chart.getXAxis());
        //Log.d(TAG, "chart3: " + xAxis.getLongestLabel() + ", " + xAxis.getLabelCount());
        xAxis.setValueFormatter(new MonthValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        YAxis yLAxis = chart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = chart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
        yLAxis.setAxisMinimum(0);

        ProgressBar progressBar1 = (ProgressBar) getView().findViewById(R.id.progressBar1);
        ProgressBar progressBar2 = (ProgressBar) getView().findViewById(R.id.progressBar2);

        for(int i = 1; i <= 60; i++) {
            if(stepmax <= 500*i) {
                yLAxis.setAxisMaximum(500*(i+1));
                break;
            }
        }
        //Log.d(TAG, Integer.toString(progressBar1.getMax()));

        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        //chart.animateY(800);
        chart.setData(data);
        chart.invalidate();
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        //chart.getAxisLeft().setLabelCount(7, true);
        //chart.setVisibleXRangeMaximum(7);

        /*TreeMap<String,Integer> tm = new TreeMap<String,Integer>(totalStep2);
        Iterator<String> iterator = tm.keySet( ).iterator( );
        while(iterator.hasNext()) {
            String key = iterator.next();
            Log.d(TAG, "key = " + key);
            Log.d(TAG, "value = " + totalStep2.get(key));
        }*/

        //progressBar1.setMax(5000);
        //progressBar2.setMax(5000);

        int fin1 = 0, fin2 = 0;
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(cal.getTime());
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        String fd = df.format(cal2.getTime());
        Integer val1 = totalStep3.get(fd);
        cal2.add(Calendar.DATE, -30);
        fd = df.format(cal2.getTime());
        Integer val2 = totalStep3.get(fd);

        /*String fd = df.format(cal.getTime());
        Integer val1 = totalStep2.get(Integer.parseInt(fd));
        Integer val2 = totalStep2.get(Integer.parseInt(fd)-7);*/

        if(val1 != null)
            fin1 = val1;
        if(val2 != null)
            fin2 = val2;

        int max_fin = Math.max(fin1, fin2);
        progressBar1.setMax(max_fin);
        progressBar2.setMax(max_fin);
        progressBar1.setProgress(fin1);
        progressBar2.setProgress(fin2);

        step_count.setText("평균 "+fin1+"걸음");
        if(fin1 - fin2 > 0) {
            int more = fin1 - fin2;
            review1.setText("저번달보다 하루 평균 "+more+"걸음 더 걸었습니다.");
        }
        else if(fin2 - fin1 > 0) {
            int less = fin2 - fin1;
            review1.setText("저번달보다 하루 평균 "+less+"걸음 덜 걸었습니다.");
        }
        else {
            review1.setText("이번달과 저번달의 평균 걸음 수가 동일합니다.");
        }
        review2.setText("이번달 평균 "+fin1+" 걸음/일");
        review3.setText("저번달 평균 "+fin2+" 걸음/일");
    }

    class MonthValueFormatter extends ValueFormatter {

        private DecimalFormat mFormat;
        private String str;
        int idx;

        public MonthValueFormatter() {
            mFormat = new DecimalFormat("##일");
        }

        @Override
        public String getFormattedValue(float value) {
            if(value >= 32) {
                value -= 31;
            }
            //Log.d(TAG, "value: " + value);
            str = Float.toString(value);
            idx = str.indexOf(".");
            return mFormat.format(Float.parseFloat(str.substring(0,idx)));
        }
    }

    private void getDailyStepCountsFromGoogleFit4(FitnessOptions fitnessOptions, final Calendar cal, final int flag) {
        // Create the start and end times for the date range
        long endTime, startTime;
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        if(flag == 0){
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, +1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            endTime = cal.getTimeInMillis();

            cal.add(Calendar.MONTH, -6);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();

            //Log.d(TAG, "Date Start3-1: " + dateFormat.format(startTime));
            //Log.d(TAG, "Date End3-1: " + dateFormat.format(endTime));
        }
        else { // flag == 1 or 2
            //cal.add(Calendar.DAY_OF_YEAR, +6); // cal.add(Calendar.MONTH, -12);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            endTime = cal.getTimeInMillis();

            cal.add(Calendar.MONTH, -6);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();

            //Log.d(TAG, "Date Start3-2: " + dateFormat.format(startTime));
            //Log.d(TAG, "Date End3-2: " + dateFormat.format(endTime));
        }

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.HOURS) // .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                requireActivity(), fitnessOptions);

        Fitness.getHistoryClient(getActivity(), account)
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse response) {
                        statisticsViewModel.clearData();

                        if (!response.getBuckets().isEmpty()) {
                            for (Bucket bucket : response.getBuckets()) {
                                String stepCount = "0";
                                Date bucketStart = new Date(bucket.getStartTime(TimeUnit.MILLISECONDS));
                                Date bucketEnd = new Date(bucket.getEndTime(TimeUnit.MILLISECONDS));
                                /*Log.d(TAG, "Bucket start / end times: " +  dateFormat.format(bucketStart)
                                        + " - " + dateFormat.format(bucketEnd));*/

                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet set : dataSets) {
                                    List<DataPoint> dataPoints = set.getDataPoints();
                                    //Log.d(TAG, "dataset2: " + set.getDataType().getName());
                                    for (DataPoint dp : dataPoints) {
                                        //Log.d(TAG, "datapoint2: " + dp.getDataType().getName());
                                        for (Field field : dp.getDataType().getFields()) {
                                            stepCount = dp.getValue(field).toString();
                                            //Log.d(TAG, "Field2: " + field.getName() + " Value2: " + dp.getValue(field));
                                        }
                                    }
                                }

                                //statisticsViewModel.addDailyStepCount(bucketStart, stepCount);
                                // Add the data
                                if (statisticsViewModel != null) {
                                    statisticsViewModel.addDailyStepCount(bucketStart, stepCount);
                                }
                            }

                            // Update current day step count
                            //readDailyTotalSteps();
                            buildChart4(cal);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    private void buildChart4(Calendar cal) {

        int month_tot = 0, month_count = 0, year_tot = 0;
        int check1 = 0, check2 = 0;
        int stepmax = 0;
        String yea, mon;

        chart.removeAllViews();
        List<BarEntry> values = new ArrayList<BarEntry>();
        SimpleDateFormat yf = new SimpleDateFormat("yy");
        SimpleDateFormat mf = new SimpleDateFormat("M");
        yea = yf.format(cal.getTime());
        mon = "5"; // mf.format(cal.getTime());

        values.add(new BarEntry(Integer.parseInt("0"), 0)); // 12
        values.add(new BarEntry(Integer.parseInt("1"), 0));
        values.add(new BarEntry(Integer.parseInt("2"), 0));
        values.add(new BarEntry(Integer.parseInt("3"), 0));
        values.add(new BarEntry(Integer.parseInt("4"), 0));
        for (Map.Entry<Date, String> entry : statisticsViewModel.getFitnessData().entrySet()) {
            ++check2;
            if (yea.equals(yf.format(entry.getKey())) && mon.equals(mf.format(entry.getKey()))) {
                month_tot += Integer.parseInt(entry.getValue());
                ++check1;
            }
            if(!mon.equals(mf.format(entry.getKey())) || check2 == 4416) {
                if (stepmax < month_tot / (check1 / 24)) {
                    stepmax = month_tot / (check1 / 24);
                }
                values.add(new BarEntry(Integer.parseInt(mon), month_tot / (check1 / 24)));
                Log.d(TAG, "x: " + mon + ", y: " + month_tot / (check1 / 24));
                year_tot += month_tot / (check1 / 24);
                ++month_count;

                mon = Integer.toString(Integer.parseInt(mon) + 1);

                /*if (yea != yf.format(entry.getKey())) { // year changes
                    yea = Integer.toString(Integer.parseInt(yea) + 1);
                    mon = Integer.toString(Integer.parseInt(mon) - 11);
                } else { // month changes
                    mon = Integer.toString(Integer.parseInt(mon) + 1);
                }*/
                month_tot = 0;
                check1 = 0;
            }
        }
        totalStep4.put(yea, year_tot / month_count);

        BarDataSet set1 = new BarDataSet(values, "걸음 수");
        set1.setColor(Color.parseColor("#FE4901"));
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        //data.setValueFormatter(new WeekValueFormatter());
        data.setHighlightEnabled(false);
        data.setValueTextSize(5f);
        data.setBarWidth(0.7f);

        //Log.d(TAG, "총 "+stepsum+"걸음");
        set1.setValueFormatter(new StepValueFormatter());
        //Log.d(TAG, "set1: " + set1);
        //set1.setDrawValues(false);

        //Log.d(TAG, "chart1: " + chart.getX() + ", " + chart.getXAxis());
        //chart.setX(2);
        XAxis xAxis = chart.getXAxis();
        //Log.d(TAG, "chart2: " + chart.getX() + ", " + chart.getXAxis());
        //Log.d(TAG, "chart3: " + xAxis.getLongestLabel() + ", " + xAxis.getLabelCount());
        xAxis.setValueFormatter(new YearValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        YAxis yLAxis = chart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = chart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
        yLAxis.setAxisMinimum(0);

        ProgressBar progressBar1 = (ProgressBar) getView().findViewById(R.id.progressBar1);
        ProgressBar progressBar2 = (ProgressBar) getView().findViewById(R.id.progressBar2);

        for(int i = 1; i <= 60; i++) {
            if(stepmax <= 500*i) {
                yLAxis.setAxisMaximum(500*(i+1));
                break;
            }
        }
        //Log.d(TAG, Integer.toString(progressBar1.getMax()));

        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        //chart.animateY(800);
        chart.setData(data);
        chart.invalidate();
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        //chart.getAxisLeft().setLabelCount(7, true);
        //chart.setVisibleXRangeMaximum(7);

        //progressBar1.setMax(5000);
        //progressBar2.setMax(5000);

        int fin1 = 0, fin2 = 0;
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(cal.getTime());
        String fd = yf.format(cal2.getTime());
        Integer val1 = totalStep4.get(fd);
        Integer val2 = 0;
        /*cal.add(Calendar.YEAR, -1);
        fd = yf.format(cal2.getTime());
        Integer val2 = totalStep4.get(fd);*/

        if(val1 != null)
            fin1 = val1;
        if(val2 != null)
            fin2 = val2;

        int max_fin = Math.max(fin1, fin2);
        progressBar1.setMax(max_fin);
        progressBar2.setMax(max_fin);
        progressBar1.setProgress(fin1);
        progressBar2.setProgress(fin2);

        step_count.setText("평균 "+fin1+"걸음");
        if(fin1 - fin2 > 0) {
            int more = fin1 - fin2;
            review1.setText("작년보다 하루 평균 "+more+"걸음 더 걸었습니다.");
        }
        else if(fin2 - fin1 > 0) {
            int less = fin2 - fin1;
            review1.setText("작년보다 하루 평균 "+less+"걸음 덜 걸었습니다.");
        }
        else {
            review1.setText("작년과 올해의 평균 걸음 수가 동일합니다.");
        }
        review2.setText("올해 평균 "+fin1+" 걸음/일");
        review3.setText("작년 평균 "+fin2+" 걸음/일");
    }

    class YearValueFormatter extends ValueFormatter {

        private DecimalFormat mFormat;
        private String str;
        int idx;

        public YearValueFormatter() {
            mFormat = new DecimalFormat("##월");
        }

        @Override
        public String getFormattedValue(float value) {
            if(value == -1)
                return mFormat.format(11);
            else if (value == 0)
                return mFormat.format(12);
            else
                return mFormat.format(value);
            //Log.d(TAG, "value: " + value);
            /*str = Float.toString(value);
            idx = str.indexOf(".");
            return mFormat.format(Float.parseFloat(str.substring(0,idx)));*/
        }
    }
}