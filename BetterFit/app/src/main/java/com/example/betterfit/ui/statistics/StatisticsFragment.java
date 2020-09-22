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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StatisticsFragment extends Fragment {

    private static final String TAG = StatisticsFragment.class.getSimpleName();
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    private TextView date, daystep;
    private Button prevBtn, nextBtn;
    private BarChart daychart;
    private StatisticsViewModel statisticsViewModel;

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

    @Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                // Google Fit access granted. Could remove this code, but it's nice to have when checking if
                // the permissions were granted.
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override public void onResume() {
        super.onResume();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                Objects.requireNonNull(getActivity()),
                mFitnessOptions);

        /*final DateFormat mFormat = new SimpleDateFormat("yyyy.M.d.EE");
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        String formatDate = mFormat.format(cal.getTime());

        date = (TextView) findViewById(R.id.date);
        date.setText(formatDate);
        prevBtn = (Button) findViewById(R.id.prev);
        nextBtn = (Button) findViewById(R.id.next);*/

        // Check Google Signin Permission
        if (!GoogleSignIn.hasPermissions(account, mFitnessOptions)) {
            GoogleSignIn.requestPermissions(this, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, account,
                    mFitnessOptions);
        } else {
            daychart.removeAllViews();
            getDailyStepCountsFromGoogleFit(mFitnessOptions);
            buildChart();

            /*prevBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cal.add(Calendar.DATE, -1);
                    date.setText(mFormat.format(cal.getTime()));
                    getDailyStepCountsFromGoogleFit(mFitnessOptions);
                    buildChart();
                }
            });*/

            /*nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cal.add(Calendar.DATE, +1);
                    date.setText(mFormat.format(cal.getTime()));
                    getDailyStepCountsFromGoogleFit(mFitnessOptions);
                    buildChart();
                }
            });*/

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getDailyStepCountsFromGoogleFit(FitnessOptions fitnessOptions) {
        // Create the start and end times for the date range
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.get(Calendar.SECOND));
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1); // cal.add(Calendar.MONTH, -12);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Log.d(TAG, "Date Start: " + dateFormat.format(startTime));
        Log.d(TAG, "Date End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.HOURS) // .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                Objects.requireNonNull(getActivity()), fitnessOptions);

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
                                    Log.d(TAG, "dataset: " + set.getDataType().getName());
                                    for (DataPoint dp : dataPoints) {
                                        Log.d(TAG, "datapoint: " + dp.getDataType().getName());
                                        for (Field field : dp.getDataType().getFields()) {
                                            stepCount = dp.getValue(field).toString();
                                            Log.d(TAG, "Field: " + field.getName() + " Value: " + dp.getValue(field));
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

                            buildChart();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    /***
     * Method to read current days total step count.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void readDailyTotalSteps() {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                Objects.requireNonNull(getActivity()),
                mFitnessOptions);

        Fitness.getHistoryClient(getActivity(), account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override public void onSuccess(DataSet dataSet) {

                        List<DataPoint> dataPoints = dataSet.getDataPoints();
                        String dailyTotalStepCount = "0";
                        for (DataPoint dp : dataPoints) {
                            List<Field> fields = dp.getDataType().getFields();
                            for (Field field : fields) {
                                Log.d(TAG, "Field2: " + field.getName() + " Value2: " + dp.getValue(field));
                                dailyTotalStepCount = dp.getValue(field).toString();
                            }
                        }

                        //statisticsViewModel.addDailyStepCount(new Date(System.currentTimeMillis()), dailyTotalStepCount);
                        // Add the data
                        if (statisticsViewModel != null) {
                            statisticsViewModel.addDailyStepCount(new Date(System.currentTimeMillis()), dailyTotalStepCount);
                        }

                        //buildChart();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    /***
     * Method to get the data from the viewmodel and then build the TableLayout.
     */
    private void buildChart() {

        final DateFormat dateFormat = DateFormat.getDateInstance();

        float hour = 0;
        daychart.removeAllViews();
        List<BarEntry> values = new ArrayList<BarEntry>();
        SimpleDateFormat fm = new SimpleDateFormat("HH");
        for (Map.Entry<Date, String> entry : statisticsViewModel.getFitnessData().entrySet()) {
            String x = fm.format(entry.getKey());
            String y = entry.getValue();
            values.add(new BarEntry(Integer.parseInt(x), Integer.parseInt(y)));
        }

        BarDataSet set1 = new BarDataSet(values, "steps");
        set1.setColor(Color.parseColor("#FE4901"));
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);

        int stepsum = 0;
        for (Map.Entry<Date, String> entry : statisticsViewModel.getFitnessData().entrySet()) {
            stepsum += Integer.parseInt(entry.getValue());
        }
        daystep.setText("총 "+Integer.toString(stepsum)+"걸음");

        daychart.setScaleEnabled(false);
        daychart.animateY(1000);
        daychart.setData(data);
        daychart.invalidate();

    }
}