package com.example.betterfit.ui.home;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.betterfit.R;
import com.example.betterfit.ui.contents.VideoAdapter;
import com.example.betterfit.ui.contents.YouTubeVideos;
import com.example.betterfit.ui.statistics.StatisticsFragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.betterfit.ui.statistics.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

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
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements MyDialogFragment.OnInputListener {

    private static final String TAG = StatisticsFragment.class.getSimpleName();
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private StatisticsViewModel statisticsViewModel;
    Map<String,Integer> totalStep = new HashMap<String,Integer>();

    private TextView date, step_count, goal_step, yester_comment, yesterday, yesterday_review, contents_title, andmore;
    private TextView walking_cal, walking_km;
    private TextView home_tag1, home_tag2, home_tag3;
    private String max_count;  // modified
    private int goal_count = 10000;  // Max(goal_count)값은 10000가 default - modified
    private HomeViewModel homeViewModel;
    Map<String, Integer> totalStep1 = new HashMap<String, Integer>();
    private TextView pattern;
    private BarChart barchart;
    private ProgressBar circle_progressBar;  // modified

    int[] intArr = new int[7];
    double dev = 0, devSqvSum = 0, avg, var, std1, std2;

    RecyclerView recyclerView;
    Vector<YouTubeVideos> youtubeVideos = new Vector<YouTubeVideos>();

    /*private static final String API_KEY = "AIzaSyBiEWqzkWLAVs0RJsw7_WhZOUP3W5pn1BY";
    private static String VIDEO1_ID = "KHOhjvYPZfg";
    private static String VIDEO2_ID = "WSEtdciBPLM";
    private static String VIDEO3_ID = "FVfIdxyRTHw";*/

    private final FitnessOptions mFitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .build();

    private final DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .setAppPackageName("com.google.android.gms")
            .build();

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        final ScrollView scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        step_count = (TextView) view.findViewById(R.id.step_count);
        barchart = (BarChart)view.findViewById(R.id.chart1);
        //linechart = (LineChart)view.findViewById(R.id.chart1);
        pattern = (TextView) view.findViewById(R.id.pattern);

        home_tag1 = (TextView) view.findViewById(R.id.home_tag1);
        home_tag2 = (TextView) view.findViewById(R.id.home_tag2);
        home_tag3 = (TextView) view.findViewById(R.id.home_tag3);

        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager( new LinearLayoutManager(getContext()));

        // modified
        TextView goalText = (TextView) view.findViewById(R.id.goal_step);  // 이거 누르면 Dialog창 뜸 : 목표 걸음 수 재설정 창
        goal_step = (TextView) view.findViewById(R.id.goal_step);
        goalText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: opening the dialog");
                MyDialogFragment dialog = new MyDialogFragment();
                dialog.setTargetFragment(HomeFragment.this, 22); // in case of fragment to activity communication we do not need this line. But must write this i case of fragment to fragment communication
                dialog.show(getFragmentManager(), "MyCustomDialog");
            }
        });
        // 여기까지

        yester_comment = (TextView) view.findViewById(R.id.yester_comment);
        yester_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_home_to_navigation_statistics);
            }
        });

        contents_title = (TextView) view.findViewById(R.id.contents_title);
        contents_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_home_to_navigation_contents);
            }
        });

        andmore = (TextView) view.findViewById(R.id.andmore);
        andmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_home_to_navigation_contents);
            }
        });

        return view;
    }

    // modified
    public void sendInput(String input){  // 바꾼 목표 걸음 수를 input 데이터로 받아오는 함수
        goal_step.setText("/ "+input);
        max_count = input;
        //onResume();
        int count_goal = Integer.parseInt(max_count);
        //System.out.println("-------------------------------------> 1) max count string : "+max_count);
        setGoalCount(count_goal);
        //circle_progressBar.setMax(count_goal);   // progressBar setMax
        circle_progressBar.setMax(getGoalCount());   // progressBar setMax
        //setMax(count_goal);
    }
    public void setGoalCount(int num){
        goal_count = num;
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> set goal count : "+goal_count);
    }
    public int getGoalCount(){
        return goal_count;
    }
    // 여기까지

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        super.onResume();

        int max_step = getGoalCount();  // modified
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1) onResume : "+getGoalCount());  // modified

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(getActivity(), mFitnessOptions);

        final DateFormat mFormat1 = new SimpleDateFormat("yyyy.M.d.EE");
        final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        final Calendar cal1 = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();
        final Calendar cal3 = Calendar.getInstance();
        cal1.setTime(new Date());  // 오늘 날짜 받아옴
        cal2.setTime(new Date());  // 오늘 날짜 받아온 다음
        cal2.add(Calendar.DATE, -1);  // -1 하면 어제
        cal3.setTime(new Date());

        final String formatDate = mFormat1.format(cal1.getTime());  // 오늘
        final String formatDate2 = mFormat1.format(cal2.getTime());  // 어제
        final int colorBlack = getResources().getColor(R.color.colorBlack);
        final int colorGray = getResources().getColor(R.color.colorDarkGray);

        date = (TextView) getView().findViewById(R.id.today);
        date.setText(formatDate);
        yesterday = (TextView) getView().findViewById(R.id.yesterday);
        yesterday.setText(formatDate2);

        goal_step = (TextView) getView().findViewById(R.id.goal_step);
        goal_step.setText("/ " + max_step);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 2) onResume : "+max_step);
        // 3줄 modified
        yesterday_review = (TextView) getView().findViewById(R.id.yester_review);

        final Calendar cal = Calendar.getInstance();
        final DateFormat mFormat = new SimpleDateFormat("EE");
        final String date = mFormat.format(cal.getTime());

        // Check Google Fit Permission
        if (GoogleSignIn.hasPermissions(account, mFitnessOptions)) {
            barchart.removeAllViews();
            //linechart.removeAllViews();

            cal1.add(Calendar.DATE, -1);
            getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal1, 1);
            cal1.add(Calendar.DATE, +1);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getDailyStepCountsFromGoogleFit1(mFitnessOptions, cal1, 0);
            getCalorieFromGoogleFit1(mFitnessOptions, cal1);
            getDistanceFromGoogleFit1(mFitnessOptions, cal1);
            step_count.setVisibility(View.VISIBLE);

            cal.setTime(new Date());
            if (date.equals("월")) {
                cal.add(Calendar.DATE, -28);
                getStepGraph(mFitnessOptions, cal);
            } else if (date.equals("화")) {
                cal.add(Calendar.DATE, -29);
                getStepGraph(mFitnessOptions, cal);
            } else if (date.equals("수")) {
                cal.add(Calendar.DATE, -30);
                getStepGraph(mFitnessOptions, cal);
            } else if (date.equals("목")) {
                cal.add(Calendar.DATE, -31);
                getStepGraph(mFitnessOptions, cal);
            } else if (date.equals("금")) {
                cal.add(Calendar.DATE, -32);
                getStepGraph(mFitnessOptions, cal);
            } else if (date.equals("토")) {
                cal.add(Calendar.DATE, -33);
                getStepGraph(mFitnessOptions, cal);
            } else if (date.equals("일")) {
                cal.add(Calendar.DATE, -34);
                getStepGraph(mFitnessOptions, cal);
            }

            cal3.setTime(new Date());
            cal3.add(Calendar.DATE, -6);
            getDailyStepCountsFromGoogleFit2(mFitnessOptions, cal3, 1); // last week
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getDailyStepCountsFromGoogleFit2(mFitnessOptions, cal3, 0); // this week
            cal3.add(Calendar.DATE, +6);


        } else {
            Log.w(TAG, "Google Fit Permission failed");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getDailyStepCountsFromGoogleFit1(FitnessOptions fitnessOptions, final Calendar cal, final int flag) {
        // Create the start and end times for the date range
        long endTime, startTime;

        if (flag == 0) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, +1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            endTime = cal.getTimeInMillis();

            cal.add(Calendar.DAY_OF_YEAR, -1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTimeInMillis();
        } else { // (flag == 1 || flag == 2)
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
                    @Override
                    public void onSuccess(DataReadResponse response) {
                        homeViewModel.clearData();

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
                                // Add the data
                                if (homeViewModel != null) {
                                    homeViewModel.addDailyStepCount(bucketStart, stepCount);
                                }
                            }

                            // Update current day step count
                            //readDailyTotalSteps();
                            printStepInfo(cal);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getCalorieFromGoogleFit1(FitnessOptions fitnessOptions, final Calendar cal) {
        // Create the start and end times for the date range
        final long endTime, startTime;

        cal.add(Calendar.DAY_OF_YEAR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        startTime = cal.getTimeInMillis();  // 오늘의 00시 = startTime

        cal.setTime(new Date());
        endTime = cal.getTimeInMillis();  // 현재 시간 = endTime

        final DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.HOURS) // .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                getActivity(), fitnessOptions);


        Fitness.getHistoryClient(getActivity(), account)
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse response) {
                        homeViewModel.clearData();
                        if (!response.getBuckets().isEmpty()) {
                            int total_cal = 0;
                            for (Bucket bucket : response.getBuckets()) {
                                String calorie = "0";

                                Date bucketStart = new Date(bucket.getStartTime(TimeUnit.MILLISECONDS));
                                Date bucketEnd = new Date(bucket.getEndTime(TimeUnit.MILLISECONDS));
                                /*Log.d(TAG, "Bucket start / end times: " +  dateFormat2.format(bucketStart)
                                        + " - " + dateFormat2.format(bucketEnd));*/

                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet set : dataSets) {
                                    List<DataPoint> dataPoints = set.getDataPoints();
                                    //Log.d(TAG, "dataset: " + set.getDataType().getName());
                                    for (DataPoint dp : dataPoints) {
                                        //Log.d(TAG, "datapoint: " + dp.getDataType().getName());
                                        for (Field field : dp.getDataType().getFields()) {
                                            String str = dp.getValue(field).toString();  // 문자열로 바꾸고
                                            int idx = str.indexOf(".");  // 소수점 아래는 버림
                                            calorie = str.substring(0, idx);  // 소수점 위만 살려서 문자열로
                                            //System.out.println("calorie : "+calorie);
                                            int tmp = Integer.parseInt(calorie);  // 문자열을 int형으로
                                            total_cal += tmp;  // 현재까지의 칼로리를 합함
                                            //Log.d(TAG, "Field: " + field.getName() + " Value: " + dp.getValue(field));
                                        }
                                    }
                                }
                                // Add the data
                                //System.out.println("total calorie : " + total_cal);
                            }
                            printCalorie(total_cal);  //화면에 칼로리 출력
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getDistanceFromGoogleFit1(FitnessOptions fitnessOptions, final Calendar cal) {
        // Create the start and end times for the date range
        final long endTime, startTime;

        cal.add(Calendar.DAY_OF_YEAR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        startTime = cal.getTimeInMillis();  // 오늘의 00시 = startTime

        cal.setTime(new Date());
        endTime = cal.getTimeInMillis();  // 현재 시간 = endTime

        final DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.HOURS) // .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(
                getActivity(), fitnessOptions);

        //System.out.println("getDistance");
        Fitness.getHistoryClient(getActivity(), account)
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse response) {
                        homeViewModel.clearData();
                        if (!response.getBuckets().isEmpty()) {
                            double total_distance = 0;
                            for (Bucket bucket : response.getBuckets()) {
                                String distance = "0";

                                Date bucketStart = new Date(bucket.getStartTime(TimeUnit.MILLISECONDS));
                                Date bucketEnd = new Date(bucket.getEndTime(TimeUnit.MILLISECONDS));
                                /*Log.d(TAG, "Bucket start / end times: " +  dateFormat2.format(bucketStart)
                                        + " - " + dateFormat2.format(bucketEnd));*/

                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet set : dataSets) {
                                    List<DataPoint> dataPoints = set.getDataPoints();
                                    //Log.d(TAG, "dataset: " + set.getDataType().getName());
                                    for (DataPoint dp : dataPoints) {
                                        //Log.d(TAG, "datapoint: " + dp.getDataType().getName());
                                        for (Field field : dp.getDataType().getFields()) {
                                            distance = dp.getValue(field).toString();  // 문자열로 바꾸고
                                            //System.out.println("km : "+distance);
                                            double tmp = Double.parseDouble(distance);  // 문자열을 int형으로
                                            total_distance += tmp*0.001;  // 현재까지의 거리를 합함 (m->km)
                                            //Log.d(TAG, "Field: " + field.getName() + " Value: " + dp.getValue(field));
                                        }
                                    }
                                }
                            }
                            printDistance(Math.round(total_distance*100)/100.0);  //화면에 칼로리 출력
                            // 소수점 두번째자리까지 반올림해서
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.d(TAG, "OnFailure()", e);
                    }
                });
    }

    private void printStepInfo(Calendar cal) {
        int check = 0;
        int stepsum = 0;
        int stepmax = 0;
        List<BarEntry> values = new ArrayList<BarEntry>();
        final DateFormat dateFormat = DateFormat.getDateInstance();
        SimpleDateFormat hf = new SimpleDateFormat("HH");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        for (Map.Entry<Date, String> entry : homeViewModel.getFitnessData().entrySet()) {
            String x = hf.format(entry.getKey());
            String y = entry.getValue();
            values.add(new BarEntry(Integer.parseInt(x), Integer.parseInt(y)));

            stepsum += Integer.parseInt(entry.getValue());
            check++;
            if (check == 24) {
                totalStep1.put(df.format(entry.getKey()), stepsum);
                //totalStep1.put(Integer.parseInt(df.format(entry.getKey())), stepsum);
                check = 0;
                stepsum = 0;
            }
            if (stepmax < Integer.parseInt(entry.getValue())) {
                stepmax = Integer.parseInt(entry.getValue());
            }
        }

        int fin1 = 0, fin2 = 0;
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(cal.getTime());
        String fd1 = df.format(cal2.getTime());
        Integer val1 = totalStep1.get(fd1);

        cal2.add(Calendar.DATE, -1);
        String fd2 = df.format(cal2.getTime());
        Integer val2 = totalStep1.get(fd2);

        if (val1 != null)
            fin1 = val1;
        if (val2 != null)
            fin2 = val2;

        step_count.setText("" + fin1 + "");
        yesterday_review = (TextView) getView().findViewById(R.id.yester_review);
        yesterday_review.setText("- 총 " + fin2 + "걸음 걸었습니다.");

        // modified
        int max_step = getGoalCount();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> printStepInfo - max step : "+getGoalCount());
        circle_progressBar = (ProgressBar) getView().findViewById(R.id.progressBar_circle);
        //circle_progressBar.setMax(goal_count);  // progressBar setMax
        circle_progressBar.setMax(max_step);   // progressBar setMax
        circle_progressBar.setProgress(fin1);
        // 여기까지
    }

    private void printCalorie(int calorie) { // 칼로리 TextView 설정
        walking_cal = (TextView) getView().findViewById(R.id.walking_cal);
        walking_cal.setText("" + calorie + " cal");
    }
    private void printDistance(double distance){
        walking_km = (TextView) getView().findViewById(R.id.walking_km);
        walking_km.setText(("" + distance + " km"));
    }
    private int setGoalCount() {  // goal_step TextView가 클릭되면 해당 함수로 goal_count 다시 설정
        int new_goal = 0;
        return new_goal;
    }

    static class StepValueFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public StepValueFormatter() {
            mFormat = new DecimalFormat("#");
        }

        @Override
        public String getFormattedValue(float value) {
            if (value > 0)
                return mFormat.format(value);
            else
                return "";
        }
    }

    private void getStepGraph(FitnessOptions fitnessOptions, final Calendar cal) {
        // Create the start and end times for the date range
        long startTime, endTime;
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        startTime = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, +28);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        endTime = cal.getTimeInMillis();

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
                        homeViewModel.clearData();

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
                                    for (DataPoint dp : dataPoints) {
                                        for (Field field : dp.getDataType().getFields()) {
                                            stepCount = dp.getValue(field).toString();
                                        }
                                    }
                                }

                                //statisticsViewModel.addDailyStepCount(bucketStart, stepCount);
                                // Add the data
                                if (homeViewModel != null) {
                                    homeViewModel.addDailyStepCount(bucketStart, stepCount);
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

    private void buildChart(Calendar cal) {

        barchart.removeAllViews();
        //linechart.removeAllViews();

        int check = 1, track = 1, stepmax = 0;
        int monstep = 0, tuestep = 0, wedstep = 0, thustep = 0, fristep = 0, satstep = 0, sunstep = 0;
        int monmean = 0, tuemean = 0, wedmean = 0, thumean = 0, frimean = 0, satmean = 0, sunmean = 0;
        List<BarEntry> values = new ArrayList<BarEntry>();
        //List<Entry> entries = new ArrayList<>();

        for (Map.Entry<Date, String> entry : homeViewModel.getFitnessData().entrySet()) {
            if(check >= 1 && check <= 24) {
                monstep += Integer.parseInt(entry.getValue());
                ++check;
            } else if(check >= 25 && check <= 48) {
                tuestep += Integer.parseInt(entry.getValue());
                ++check;
            } else if(check >= 49 && check <= 72) {
                wedstep += Integer.parseInt(entry.getValue());
                ++check;
            } else if(check >= 73 && check <= 96) {
                thustep += Integer.parseInt(entry.getValue());
                ++check;
            } else if(check >= 97 && check <= 120) {
                fristep += Integer.parseInt(entry.getValue());
                ++check;
            } else if(check >= 121 && check <= 144) {
                satstep += Integer.parseInt(entry.getValue());
                ++check;
            } else if(check >= 145 && check <= 168) {
                sunstep += Integer.parseInt(entry.getValue());
                ++check;
                if(check == 169) {
                    ++track;
                    check = 1;
                }
            }

            if(track == 4) {
                monmean = monstep / 4;
                tuemean = tuestep / 4;
                wedmean = wedstep / 4;
                thumean = thustep / 4;
                frimean = fristep / 4;
                satmean = satstep / 4;
                sunmean = sunstep / 4;

                values.add(new BarEntry(1, monmean));
                values.add(new BarEntry(2, tuemean));
                values.add(new BarEntry(3, wedmean));
                values.add(new BarEntry(4, thumean));
                values.add(new BarEntry(5, frimean));
                values.add(new BarEntry(6, satmean));
                values.add(new BarEntry(7, sunmean));

                /*entries.add(new Entry(1, monstep / 4));
                entries.add(new Entry(2, tuestep / 4));
                entries.add(new Entry(3, wedstep / 4));
                entries.add(new Entry(4, thustep / 4));
                entries.add(new Entry(5, fristep / 4));
                entries.add(new Entry(6, satstep / 4));
                entries.add(new Entry(7, sunstep / 4));*/

                if(stepmax < (monmean)) {
                    stepmax = monmean;
                }
                if(stepmax < (tuemean)) {
                    stepmax = tuemean;
                }
                if(stepmax < (wedmean)) {
                    stepmax = wedmean;
                }
                if(stepmax < (thumean)) {
                    stepmax = thumean;
                }
                if(stepmax < (frimean)) {
                    stepmax = frimean;
                }
                if(stepmax < (satmean)) {
                    stepmax = satmean;
                }
                if(stepmax < (sunmean)) {
                    stepmax = sunmean;
                }

                // match pattern
                double sum = 0, weeksum = 0, avg = 0, weekavg = 0, dev = 0, devSqvSum = 0, var = 0;
                double max, secondmax, min, secondmin, max_rate, min_rate;
                int[] arr  = {monmean, tuemean, wedmean, thumean, frimean, satmean, sunmean};
                for (int i = 0; i < arr.length; i++) {
                    sum += arr[i];
                    if(i < 5)
                        weeksum += arr[i];
                }
                avg = sum / arr.length; //average
                weekavg = weeksum / arr.length; // weekday average
                for (int i = 0; i < arr.length; i++) {
                    dev = (arr[i] - avg);
                    devSqvSum += Math.pow(dev, 2);
                }
                var = devSqvSum / arr.length; //variance

                max = secondmax = arr[0];
                min = secondmin = arr[0];
                for(int i = 0; i < arr.length; i++)
                {
                    if(arr[i] > max)
                    {
                        secondmax = max;
                        max = arr[i];
                    }
                    else if( (arr[i] > secondmax && arr[i] < max) || max == secondmax)
                        secondmax = arr[i];

                    if(arr[i] < min)
                    {
                        secondmin = min;
                        min = arr[i];
                    }
                    else if( ( min < arr[i] && arr[i] < secondmin ) || min ==  secondmin)
                    {
                        secondmin = arr[i];
                    }
                }
                max_rate = ((max - secondmax) / avg) * 100;
                min_rate = ((secondmin - min) / avg) * 100;

                pattern.setPaintFlags(pattern.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
                if(var < 0.35)
                    pattern.setText("\"밸런스형\"");
                else if((max == satmean || max == sunmean) && (weekavg < satmean) && (weekavg < sunmean))
                    pattern.setText("\"주말증가형\"");
                else if((min == satmean || min == sunmean) && (weekavg > satmean) && (weekavg > sunmean))
                    pattern.setText("\"주말감소형\"");
                else if(max_rate >= 50 && min_rate < 25)
                    pattern.setText("\"특정요일증가형\"");
                else if(max_rate < 50 && min_rate >= 25)
                    pattern.setText("\"특정요일감소형\"");
                else
                    pattern.setText("\"짧은증감주기형\"");
                break;
            }
        }

        BarDataSet set1 = new BarDataSet(values, "걸음 수");
        set1.setColor(Color.parseColor("#FE4901"));
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        data.setHighlightEnabled(false);
        data.setValueTextSize(8f);
        data.setBarWidth(0.7f);

        XAxis xAxis = barchart.getXAxis();
        xAxis.setValueFormatter(new HomeFragment.MeanValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        YAxis yLAxis = barchart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = barchart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
        yLAxis.setAxisMinimum(0);

        for(int i = 1; i <= 60; i++) {
            if(stepmax <= 500*i) {
                yLAxis.setAxisMaximum(500*(i+1));
                break;
            }
        }

        barchart.setScaleEnabled(false);
        barchart.setPinchZoom(false);
        barchart.setDrawGridBackground(false);
        //barchart.animateY(800);
        barchart.setData(data);
        barchart.invalidate();
        barchart.setDescription(null);
        barchart.getLegend().setEnabled(false);
        //barchart.setVisibleXRangeMaximum(7);

    }

    class MeanValueFormatter extends ValueFormatter {

        public MeanValueFormatter() { }

        @Override
        public String getFormattedValue(float value) {
            if (value == 1)
                return "월";
            else if (value == 2)
                return "화";
            else if (value == 3)
                return "수";
            else if (value == 4)
                return "목";
            else if (value == 5)
                return "금";
            else if (value == 6)
                return "토";
            else // (value == 7)
                return "일";
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

                avg = weeksum / 7;
                for (int j = 0; j < 7; j++) {
                    dev = (intArr[j] - avg);
                    devSqvSum += Math.pow(dev, 2);
                }
                var = devSqvSum / 7;
                std1 = Math.sqrt(var);
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

        if (val1 != null)
            fin1 = val1; // this week step count
        if (val2 != null)
            fin2 = val2; // last week step count
        Log.d(TAG, "fin1: " + fin1 + ", fin2: " + fin2 + ", fin2/5: " + fin2 / 5);

        if(fin1 == 0) {
            std2 = std1;
        }

        if(fin1 != 0) {
            if (fin2 + std2 < fin1) { //more
                // show more contents
                home_tag1.setText("#많이 걸은 날 #다리 붓기 #피로 회복");

                youtubeVideos.add(new YouTubeVideos("<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/KHOhjvYPZfg\" frameborder=\"0\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>", ""));
                VideoAdapter videoAdapter = new VideoAdapter(youtubeVideos);
                recyclerView.setAdapter(videoAdapter);
            } else if (fin2 - std2 > fin1) { //less
                // show less contents
                home_tag1.setText("#조금 걸은 날 #홈 트레이닝 #걷기의 좋은점");

                youtubeVideos.add(new YouTubeVideos("<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/WSEtdciBPLM\" frameborder=\"0\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>", ""));
                VideoAdapter videoAdapter = new VideoAdapter(youtubeVideos);
                recyclerView.setAdapter(videoAdapter);
            } else { //similar
                // show similar contents
                home_tag1.setText("#비슷하게 걸은 날 #바른 걸음걸이 #자세 교정");

                youtubeVideos.add(new YouTubeVideos("<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/FVfIdxyRTHw\" frameborder=\"0\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>", ""));
                VideoAdapter videoAdapter = new VideoAdapter(youtubeVideos);
                recyclerView.setAdapter(videoAdapter);
            }
        }
    }

}