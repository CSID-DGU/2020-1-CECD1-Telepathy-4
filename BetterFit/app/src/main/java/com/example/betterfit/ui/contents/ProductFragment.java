package com.example.betterfit.ui.contents;

import android.content.Context;
import android.graphics.Color;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.content.Context;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.betterfit.MainActivity;
import com.example.betterfit.R;
import com.example.betterfit.ui.statistics.StatisticsFragment;
import com.example.betterfit.ui.statistics.StatisticsViewModel;
import com.github.mikephil.charting.data.BarEntry;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.example.betterfit.MainActivity;
import com.example.betterfit.R;
import com.example.betterfit.ui.statistics.StatisticsFragment;
import com.example.betterfit.ui.statistics.StatisticsViewModel;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProductFragment extends Fragment {
    private TextView hashtag1, hashtag2, hashtag3, title1, title2, title3;
    private Button videoBtn, newsBtn, productBtn;
    private View videoBar, newsBar, productBar;

    private static final String TAG = StatisticsFragment.class.getSimpleName();
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private StatisticsViewModel statisticsViewModel;
    Map<String,Integer> totalStep = new HashMap<String,Integer>();

    //private ContentsViewModel contentsViewModel;
    public ProductFragment() {

    }

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //contentsViewModel = new ViewModelProvider(this).get(ContentsViewModel.class);
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_product, container, false);

        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        videoBtn = (Button) view.findViewById(R.id.videoBtn);
        newsBtn = (Button) view.findViewById(R.id.newsBtn);
        //productBtn = (Button) view.findViewById(R.id.productBtn); // Scroll up

        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_product_to_navigation_contents);
            }
        });

        newsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_product_to_navigation_news);
            }
        });

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
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        final String formatDate = mFormat.format(cal.getTime());
        int flag = 0; // 0:today, 1:prev, 2:next
        final int colorBlack = getResources().getColor(R.color.colorBlack);
        final int colorGray = getResources().getColor(R.color.colorDarkGray);

        // Check Google Fit Permission
        if (GoogleSignIn.hasPermissions(account, mFitnessOptions)) {

            cal.setTime(new Date());
            cal.add(Calendar.DATE, -6);
            getDailyStepCountsFromGoogleFit(mFitnessOptions, cal, 1); // last week
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getDailyStepCountsFromGoogleFit(mFitnessOptions, cal, 0); // this week
            cal.add(Calendar.DATE, +6);

        }
        else {
            Log.w(TAG, "Google Fit Permission failed");
        }
    }

    private void getDailyStepCountsFromGoogleFit(FitnessOptions fitnessOptions, final Calendar cal, final int flag) {
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
                            compare(cal);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        //Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    private void compare(Calendar cal) {

        int check1 = 0, check2 = 0, check3 = 0;
        int stepsum = 0, weeksum = 0;
        int stepmax = 0;
        int x = 0;

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
                totalStep.put(df.format(entry.getKey()), weeksum / 7);
                //Log.d(TAG, "key2: " + df.format(entry.getKey()) + ", value2: " + weeksum/7);
            }
        }

        int fin1 = 0, fin2 = 0;
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(cal.getTime());
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        String fd = df.format(cal2.getTime());
        Integer val1 = totalStep.get(fd);
        cal2.add(Calendar.DATE, -7);
        fd = df.format(cal2.getTime());
        Integer val2 = totalStep.get(fd);

        /*String fd = df.format(cal.getTime());
        Integer val1 = totalStep2.get(Integer.parseInt(fd));
        Integer val2 = totalStep2.get(Integer.parseInt(fd)-7);*/

        if(val1 != null)
            fin1 = val1; // this week step count
        if(val2 != null)
            fin2 = val2; // last week step count
        Log.d(TAG, "fin1: " + fin1 + ", fin2: " + fin2 + ", fin2/5: " + fin2/5);

        if(fin1 != 0) {
            if (fin2 + (fin2 / 5) < fin1) { //more
                // show more contents
                Log.d(TAG, "more: " + Math.abs(fin1 - fin2));

            } else if (fin2 - (fin2 / 5) > fin1) { //less
                // show less contents
                Log.d(TAG, "less: " + Math.abs(fin1 - fin2));
            } else { //similar
                // show similar contents
                Log.d(TAG, "similar: " + Math.abs(fin1 - fin2));
            }
        }
    }
}