package com.example.betterfit.ui.statistics;

import androidx.lifecycle.ViewModelProvider;

import com.example.betterfit.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class StatisticsFragment extends Fragment {

    private static final String TAG = StatisticsFragment.class.getSimpleName();
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    private TextView date, daystep, day_review1, day_review2, day_review3;
    private Button day, week, month, year, prevBtn, nextBtn;
    private BarChart daychart;
    private StatisticsViewModel statisticsViewModel;
    HashMap<Integer,Integer> totalStep = new HashMap<Integer,Integer>();

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

        daychart = (BarChart)view.findViewById(R.id.daychart);
        daystep = (TextView)view.findViewById(R.id.daystep);

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
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        final Calendar cal = Calendar.getInstance();
        final Calendar check = Calendar.getInstance();
        cal.setTime(new Date());
        String formatDate = mFormat.format(cal.getTime());
        String fd;
        int more, less;

        date = (TextView) getView().findViewById(R.id.date);
        date.setText(formatDate);
        prevBtn = (Button) getView().findViewById(R.id.prev);
        nextBtn = (Button) getView().findViewById(R.id.next);
        day_review1 = (TextView) getView().findViewById(R.id.day_review1);
        day_review2 = (TextView) getView().findViewById(R.id.day_review2);
        day_review3 = (TextView) getView().findViewById(R.id.day_review3);

        int flag = 0; // 0:today, 1:prev, 2:next
        // Check Google Fit Permission
        if (GoogleSignIn.hasPermissions(account, mFitnessOptions)) {
            daychart.removeAllViews();

            cal.add(Calendar.DATE, -1);
            getDailyStepCountsFromGoogleFit(mFitnessOptions, cal, 1);
            Log.d(TAG, "one");
            cal.add(Calendar.DATE, +1);
            getDailyStepCountsFromGoogleFit(mFitnessOptions, cal, 0);
            Log.d(TAG, "two");

            prevBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cal.add(Calendar.DATE, -2);
                    getDailyStepCountsFromGoogleFit(mFitnessOptions, cal, 1);

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    cal.add(Calendar.DATE, +1);
                    date.setText(mFormat.format(cal.getTime()));
                    getDailyStepCountsFromGoogleFit(mFitnessOptions, cal, 1);
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
                        getDailyStepCountsFromGoogleFit(mFitnessOptions, cal, 2);
                    }
                }
            });

        }
        else {
            Log.w(TAG, "Google Fit Permission failed");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getDailyStepCountsFromGoogleFit(FitnessOptions fitnessOptions, final Calendar cal, final int flag) {
        // Create the start and end times for the date range
        long endTime, startTime;

        if(flag == 0){
            cal.setTime(new Date());
            cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal.get(Calendar.SECOND));
            endTime = cal.getTimeInMillis();

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
        Log.d(TAG, "Date Start: " + dateFormat.format(startTime));
        Log.d(TAG, "Date End: " + dateFormat.format(endTime));

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
                                /*Log.d(TAG, "Bucket start / end times: " +  dateFormat.format(bucketStart)
                                        + " - " + dateFormat.format(bucketEnd));*/

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
                            buildChart(cal);

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
    private void buildChart(Calendar cal) {

        final DateFormat dateFormat = DateFormat.getDateInstance();
        daychart.removeAllViews();
        List<BarEntry> values = new ArrayList<BarEntry>();
        SimpleDateFormat fm = new SimpleDateFormat("HH");
        for (Map.Entry<Date, String> entry : statisticsViewModel.getFitnessData().entrySet()) {
            String x = fm.format(entry.getKey());
            String y = entry.getValue();
            values.add(new BarEntry(Integer.parseInt(x), Integer.parseInt(y)));
        }

        BarDataSet set1 = new BarDataSet(values, "걸음 수");
        set1.setColor(Color.parseColor("#FE4901"));
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        data.setValueTextSize(8f);

        int stepsum = 0;
        int stepmax = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        for (Map.Entry<Date, String> entry : statisticsViewModel.getFitnessData().entrySet()) {
            //Log.d(TAG, "date: "+df.format(entry.getKey()));
            if(stepmax < Integer.parseInt(entry.getValue())) {
                stepmax = Integer.parseInt(entry.getValue());
            }
            stepsum += Integer.parseInt(entry.getValue());
            totalStep.put(Integer.parseInt(df.format(entry.getKey())), stepsum);
        }
        //Log.d(TAG, "총 "+stepsum+"걸음");
        set1.setValueFormatter(new StepValueFormatter());
        //set1.setDrawValues(false);

        XAxis xAxis = daychart.getXAxis();
        xAxis.setValueFormatter(new TimeValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        YAxis yLAxis = daychart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = daychart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
        yLAxis.setAxisMinimum(0);

        ProgressBar progressBar1 = (ProgressBar) getView().findViewById(R.id.progressBar1);
        ProgressBar progressBar2 = (ProgressBar) getView().findViewById(R.id.progressBar2);

        for(int i = 1; i <= 60; i++) {
            if(stepmax <= 500*i) {
                yLAxis.setAxisMaximum(500*i);
                break;
            }
        }
        //Log.d(TAG, Integer.toString(progressBar1.getMax()));

        daychart.setScaleEnabled(false);
        daychart.setPinchZoom(false);
        daychart.setDrawGridBackground(false);
        daychart.animateY(800);
        daychart.setData(data);
        daychart.invalidate();
        daychart.setDescription(null);
        daychart.getLegend().setEnabled(false);

        //TreeMap<Integer,Integer> tm = new TreeMap<Integer,Integer>(totalStep);
        //Iterator<Integer> iterator = tm.keySet( ).iterator( );
        /*while(iterator.hasNext()) {
            int key = iterator.next();
            Log.d(TAG, "key = " + key);
            Log.d(TAG, "value = " + totalStep.get(key));
        }*/

        progressBar1.setMax(5000);
        progressBar2.setMax(5000);

        int fin1 = 0, fin2 = 0;
        String fd = df.format(cal.getTime());
        Integer val1 = totalStep.get(Integer.parseInt(fd));
        Integer val2 = totalStep.get(Integer.parseInt(fd)-1);

        if(val1 != null)
            fin1 = val1;
        if(val2 != null)
            fin2 = val2;
        progressBar1.setProgress(fin1);
        progressBar2.setProgress(fin2);

        daystep.setText("총 "+fin1+"걸음");
        if(fin1 - fin2 > 0) {
            int more = fin1 - fin2;
            day_review1.setText("어제보다 "+more+"걸음 더 걸었습니다.");
        }
        else if(fin2 - fin1 > 0) {
            int less = fin2 - fin1;
            day_review1.setText("어제보다 "+less+"걸음 덜 걸었습니다.");
        }
        else {
            day_review1.setText("어제와 오늘의 걸음 수가 동일합니다.");
        }
        day_review2.setText("오늘 "+fin1+" 걸음");
        day_review3.setText("어제 "+fin2+" 걸음");
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

    class TimeValueFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public TimeValueFormatter() {
            mFormat = new DecimalFormat("##시");
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value);
        }
    }
}