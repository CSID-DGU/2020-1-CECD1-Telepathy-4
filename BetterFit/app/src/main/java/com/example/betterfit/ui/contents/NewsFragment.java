package com.example.betterfit.ui.contents;

import android.content.Intent;
import android.net.Uri;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class NewsFragment extends Fragment {
    private Button videoBtn, productBtn;
    private TextView hashtag1;

    private static final String TAG = StatisticsFragment.class.getSimpleName();
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private StatisticsViewModel statisticsViewModel;
    Map<String,Integer> totalStep = new HashMap<String,Integer>();

    int[] intArr = new int[7];
    double dev = 0, devSqvSum = 0, avg, var, std1, std2;

    RecyclerView recyclerView;
    Vector<Articles> articles = new Vector<Articles>(); // 기사 사진-제목-저자-내용 벡터

    //private ContentsViewModel contentsViewModel;
    public NewsFragment() {

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
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_news, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView_news); // 뉴스용 recyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager( new LinearLayoutManager(getContext()));

        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        hashtag1 = (TextView) view.findViewById(R.id.hashtag1);

        videoBtn = (Button) view.findViewById(R.id.videoBtn);
        // newsBtn = (Button) view.findViewById(R.id.newsBtn); // Scroll up
        productBtn = (Button) view.findViewById(R.id.productBtn);

        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_news_to_navigation_contents);
            }
        });

        productBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_news_to_navigation_product);
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

        if(val1 != null)
            fin1 = val1; // this week step count
        if(val2 != null)
            fin2 = val2; // last week step count
        Log.d(TAG, "fin1: " + fin1 + ", fin2: " + fin2 + ", fin2/5: " + fin2/5);

        if(fin1 == 0) {
            std2 = std1;
        }

        if(fin1 != 0) {
            if (fin2 + std2 < fin1) { //more
                // show more contents
                Log.d(TAG, "more: " + Math.abs(fin1 - fin2));
                hashtag1.setText("#많이 걸은 날 #다리 붓기 #피로 회복");

                articles.add(new Articles(R.drawable.thumbnail1,
                "일상 속의 괴로움, 다리 붓기 빼기",
                        "문화뉴스, 박지민 기자",
                        "일상 속 서 있거나 앉아 있는 시간이 대부분인 현대인들에겐 다리가 붓는 것은 일상다반사일 것이다.…",
                        "http://www.mhns.co.kr/news/articleView.html?idxno=155978")); // 기사 1
                articles.add(new Articles(R.drawable.thumbnail2,
                        "다리 붓기 방치하면 안돼…하지정맥류 초기 증상·원인과 하지정맥 치료 방법을 알아보자",
                        "공감신문, 장희주 기자",
                        "하지정맥류 초기증상으로는 하지부종, 종아리부종 증상, 오른쪽이·왼쪽 종아리 저림 등이 발생한다.…",
                        "http://www.gokorea.kr/sub_read.html?uid=267061")); // 기사 2
                articles.add(new Articles(R.drawable.thumbnail3,
                        "피로 회복에 좋은 체조…간단한 동작으로 \"피로야 가라\"",
                        "조세일보, 임재윤 기자",
                        "피곤에 찌들어사는 현대인들이 많아 지면서 피로 회복에 좋은 체조에 대한 누리꾼들의 관심이 높아졌다.…",
                        "http://www.joseilbo.com/news/htmls/2014/11/20141111238979.html")); // 기사 3
                articles.add(new Articles(R.drawable.thumbnail4,
                        "운동 후 근육통 있다면 스트레칭 효과적…헬스장·수영장·홈트레이닝 등 운동 시 근육통 푸는법은?",
                        "공감신문, 유안나 기자\n",
                        "평소 운동을 잘 하지 않다가 과도하게 한 다음날 느낄 수 있는 근육통은 누구나 경험해봤을 것이다.…",
                        "http://www.gokorea.kr/sub_read.html?uid=56189")); // 기사 4
                NewsAdapter newsAdapter = new NewsAdapter(articles);
                recyclerView.setAdapter(newsAdapter);

            } else if (fin2 - std2 > fin1) { //less
                // show less contents
                Log.d(TAG, "less: " + Math.abs(fin1 - fin2));
                hashtag1.setText("#조금 걸은 날 #홈 트레이닝 #걷기의 좋은점");

                articles.add(new Articles(R.drawable.thumbnail21,
                        "따로 운동하기 힘든가요? 일상 속 걷기로 운동 효과 누려요",
                        "중앙일보, 정심교 기자",
                        "신종 코로나바이러스 감염증(코로나19)의 중증도를 높이는 고위험군 중 하나가 ‘비만’이다.…",
                        "https://news.joins.com/article/23903092")); // 기사 1
                articles.add(new Articles(R.drawable.thumbnail22,
                        "걷기예찬",
                        "아이뉴스24",
                        "주변에서 새해 계획으로 걸어서 출퇴근하기, 도보여행 등을 꼽는 사람들이 제법 된다.…",
                        "http://www.inews24.com/view/1153311")); // 기사 2
                articles.add(new Articles(R.drawable.thumbnail23,
                        "국민체육진흥공단, 면역력·건강 유지 위한 홈트레이닝 추천",
                        "MK 스포츠, 김성범 기자",
                        "국민체육진흥공단(이사장 조재기)은 신종 코로나바이러스 감염증(코로나19) 사태로 다중이용시설 등…",
                        "http://mksports.co.kr/view/2020/247205/")); // 기사 3
                articles.add(new Articles(R.drawable.thumbnail24,
                        "\'걷기 전도사\' 성기홍 박사",
                        "일요시사, 장지선 기자",
                        "걷기운동은 열풍을 넘어 일상으로 자리 잡았다. 걷기운동의 중요성과 효과는 이미 다양한 연구를 통해 입증됐다.…",
                        "http://www.ilyosisa.co.kr/news/articleView.html?idxno=208105")); // 기사 4
                NewsAdapter newsAdapter = new NewsAdapter(articles);
                recyclerView.setAdapter(newsAdapter);

            } else { //similar
                // show similar contents
                Log.d(TAG, "similar: " + Math.abs(fin1 - fin2));
                hashtag1.setText("#비슷하게 걸은 날 #바른 걸음걸이 #자세 교정");

                articles.add(new Articles(R.drawable.thumbnail31,
                        "엉덩이로 걸어라…재활의학과 의사의 바른 걷기법",
                        "중앙일보, 유재욱 기자",
                        "\"누구나 걷는다. 하지만 제대로 걷는 사람은 거의 없다." +"\n재활의학과 의사로서 25년간 잘못 걸어서 생긴 여러…",
                        "https://news.joins.com/article/23910703")); // 기사 1
                articles.add(new Articles(R.drawable.thumbnail32,
                        "신발과 걸음걸이, 그리고 나의 건강 상태",
                        "경기일보, 박태훈 기자",
                        "오래 신은 신발일수록 신발 바닥, 밑창이 많이 닳는다. 신발 밑창을 보고 \'신발을 바꿀 때가 됐구나\'라고 할 수도…",
                        "http://www.kyeonggi.com/news/articleView.html?idxno=2272435")); // 기사 2
                articles.add(new Articles(R.drawable.thumbnail33,
                        "바른 걸음걸이 알려주는 스마트 신발·깔창 \'주목\'",
                        "연합뉴스, 이주영 기자",
                        "힘이나 압력의 세기를 측정할 수 있는 촉각센서를 활용해 걸음걸이 자세를 확인하고 교정할 수 있게 도와주는…",
                        "https://www.yna.co.kr/view/AKR20150527120600063?input=1195m")); // 기사 3
                articles.add(new Articles(R.drawable.thumbnail34,
                        "바른 걸음걸이, 바르게 서기부터",
                        "메디파나뉴스, 박민욱 기자",
                        "걷기는 전신을 이용하는 움직임으로 우리 몸의 뼈와 근육, 관절 모든 부분에 자극을 준다. 유산소운동으로서 걷기가…",
                        "http://medipana.com/news/news_viewer.asp?NewsNum=180045&MainKind=A&NewsKind=5&vCount=12&vKind=1")); // 기사 4
                NewsAdapter newsAdapter = new NewsAdapter(articles);
                recyclerView.setAdapter(newsAdapter);
            }
        }
    }
}