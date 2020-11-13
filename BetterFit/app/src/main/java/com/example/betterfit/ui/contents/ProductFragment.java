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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Vector;
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
    private Button videoBtn, newsBtn, productBtn;
    private TextView hashtag1;

    private static final String TAG = StatisticsFragment.class.getSimpleName();
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private StatisticsViewModel statisticsViewModel;
    Map<String,Integer> totalStep = new HashMap<String,Integer>();

    int[] intArr = new int[7];
    double dev = 0, devSqvSum = 0, avg, var, std1, std2;

    RecyclerView recyclerView;
    Vector<Products> products = new Vector<Products>(); // 제품 사진-물품명-설명 벡터
    
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

        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView_products); // 제품용 recyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager( new LinearLayoutManager(getContext()));

        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        hashtag1 = (TextView) view.findViewById(R.id.hashtag1);

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

                products.add(new Products(R.drawable.product11, // 제품 1
                        "발 마사지기",
                        "강력한 공기압 마사지와 파워 모터가 장착된 에어 웰 밸런스 발 마사지기가 하루 종일 당신을 위해 지친 발에 피로를 풀어드립니다.",
                        "https://smartstore.naver.com/zespa/products/409303517?NaPm=ct%3Dkhfvmnaw%7Cci%3D00243af34a84e0e936aa2349d8ab8fd88d80a062%7Ctr%3Dslsl%7Csn%3D187924%7Chk%3Daec5af90ae461c5db1299e6bfc166139e5021734")); // 기사 1
                products.add(new Products(R.drawable.product14, // 제품 2
                        "종아리 마사지기",
                        "종아리 전체를 감싸주는 공기압 안마기,\n" + "지친 나를 위한 편안한 홈케어!",
                        "https://smartstore.naver.com/zespa/products/458421834?NaPm=ct%3Dkhfvo300%7Cci%3D7dbe4f0b9ca55ddbe7b182cea9f1f5dd365e7a1b%7Ctr%3Dslsl%7Csn%3D187924%7Chk%3D8d095a5df5b6b03e56b9138bd5a0f81c9f2cf6f0")); // 제품 3
                products.add(new Products(R.drawable.product13,
                        "건식 좌훈 족욕기",
                        "건강에 좋은 족욕 + 좌훈을 간편하게!\n" + "매일 한시간씩 집에서 혈액순환 관리",
                        "https://everjoy.co.kr/product/detail.html?product_no=129&cate_no=70&display_group=1&cafe_mkt=naver_ks&mkt_in=Y&ghost_mall_id=naver&ref=naver_open&NaPm=ct%3Dkhfvqiyo%7Cci%3D84ad5aaddcae4fc1531912c7e4edddba434ee124%7Ctr%3Dslsl%7Csn%3D455817%7Chk%3Dee9862186742ae0cd5ce1fc21b2d8f3c98ac4f29")); // 제품 4
                products.add(new Products(R.drawable.product12,
                        "발바닥 힐링패치",
                        "쉽게 혹사 당하는 피곤한 발을 시원하게 풀어주고 몸에 활력과 생기를 더해주는 간편한 힐링 아이템!",
                        "https://meditherapy.co.kr/product/detail.html?product_no=33&cate_no=24&display_group=1&cafe_mkt=naver_ks&mkt_in=Y&ghost_mall_id=naver&ref=naver_open&NaPm=ct%3Dkhfvr6vs%7Cci%3D78f91f3e83257cd494b9bca0ae4c2ac6aeb99eba%7Ctr%3Dslsl%7Csn%3D635810%7Chk%3D77ddaba13eae2f2ba47a018c92a5919bfc359908"));

                ProductAdapter productAdapter = new ProductAdapter(products);
                recyclerView.setAdapter(productAdapter);
            } else if (fin2 - std2 > fin1) { //less
                // show less contents
                Log.d(TAG, "less: " + Math.abs(fin1 - fin2));
                hashtag1.setText("#조금 걸은 날 #홈 트레이닝 #걷기의 좋은점");

                products.add(new Products(R.drawable.product31, // 제품 1
                        "발가락 운동 기구",
                        "몸의 건강은 발 끝에서 시작한다!\n" + "홈트족을 위한 발가락 운동기구",
                        "https://smartstore.naver.com/welpia/products/4742006066?NaPm=ct%3Dkhfvzcv4%7Cci%3D0ut7000a%2DoTtmYjqxeXc%7Ctr%3Dpla%7Chk%3Ddfc891ad0295f5571b4cbd20c7cd84081d10a8bb")); // 기사 1
                products.add(new Products(R.drawable.product32, // 제품 2
                        "홈 트레이닝 탄력 밴드",
                        "코어 및 근력 운동에 효과적인 신축성 좋은 탄력 밴드!\n" + "탄력 밴드로 홈트레이닝을 쉽고 간편하게!",
                        "https://smartstore.naver.com/bikesports/products/3641492933?NaPm=ct%3Dkhfvy7yw%7Cci%3Dda838a74b390387ffcefef7d215a2549f0ce8d96%7Ctr%3Dslsl%7Csn%3D217890%7Chk%3Dbf3b3d91431243e1221809cf232ab6570be7e90f")); // 제품 3
                products.add(new Products(R.drawable.product33,
                        "운동보조제",
                        "기운 뿜뿜! 운동 효과를 높여주는 단백질 보조제로 근육 피로도 회복하고 운동 강도를 늘리세요!",
                        "https://smartstore.naver.com/kingkongshop/products/3530090944?n_media=11068&n_query=%EC%9A%B4%EB%8F%99%EB%B3%B4%EC%A1%B0%EC%A0%9C&n_rank=1&n_ad_group=grp-a001-02-000000008112045&n_ad=nad-a001-02-000000042204783&n_campaign_type=2&n_mall_pid=3530090944&n_ad_group_type=2&NaPm=ct%3Dkhfw3pi0%7Cci%3D0z40003l%2DoTtADR7WvnS%7Ctr%3Dpla%7Chk%3Dcc95921c859e7f9f2c9743d4e583e4f88a932ab3")); // 제품 4
                products.add(new Products(R.drawable.product34,
                        "저칼로리 곤약 현미밥",
                        "식이섬유가 풍부한 저혈당지수 식품 곤약쌀을 사용하여 칼로리는 낮지만 포만감은 오래!",
                        "http://dshop.dietshin.com/goods/view.asp?g=12140&t=9537&jp=B&utm_source=naver&utm_medium=shopping&utm_campaign=naver_shopping&NaPm=ct%3Dkhfw59ts%7Cci%3Dd74a25771660c960803362d2b8a80cace31367e9%7Ctr%3Dslsl%7Csn%3D313373%7Chk%3Dcd6a0d41af1475e15b820495fe6f2eaea94344ec"));
                ProductAdapter productAdapter = new ProductAdapter(products);
                recyclerView.setAdapter(productAdapter);
            } else { //similar
                // show similar contents
                Log.d(TAG, "similar: " + Math.abs(fin1 - fin2));
                hashtag1.setText("#비슷하게 걸은 날 #바른 걸음걸이 #자세 교정");

                products.add(new Products(R.drawable.product24, // 제품 1
                        "자세 교정 밴드",
                        "당신을 바른 자세로 이끌어줄 리얼핏 밴드!\n" + "가벼운 무게로 신체에 부담 없어 꾸준히 착용 가능!",
                        "https://brand.naver.com/melkin/products/4645319961?NaPm=ct%3Dkhfvi26g%7Cci%3D7150f5cd788e6e84cd7caeef7ac7089b274fe33a%7Ctr%3Dslsl%7Csn%3D447572%7Chk%3D10464646e0a37a8f97a4b9bf95ce3c141bcd7c3b")); // 기사 1
                products.add(new Products(R.drawable.product22, // 제품 2
                        "기능성 깔창",
                        "발을 더 편안하게, 천연라텍스를 사용하여 차별화된 편안함!",
                        "http://dagoodtem121.godomall.com/goods/goods_view.php?goodsNo=1000000001&inflow=naver&NaPm=ct%3Dkhfvhqls%7Cci%3D0ee95b66f9b8fa1541f4d5f7162a19332bb59f02%7Ctr%3Dslsl%7Csn%3D1213667%7Chk%3Da17e90d477735c862ec5bc62af13251a9fc36745")); // 제품 3
                products.add(new Products(R.drawable.product23,
                        "발가락 링",
                        "발가락을 고르게 펴 주어서 발가락의 균형감을 향상시켜 착용 상태에서 편안하게 걸을 수 있도록 설계되었습니다.",
                        "https://smartstore.naver.com/tsgreen/products/3769288166?NaPm=ct%3Dkhfvj7ug%7Cci%3Dfcaf69bc9c78016c4860bbda4a73433b551f85ed%7Ctr%3Dslsl%7Csn%3D647100%7Chk%3D8503ff3b182b2b80a9458d9469f916f6c78db1f2")); // 제품 4
                products.add(new Products(R.drawable.product21,
                        "걸음걸이 교정 깔창",
                        "깔창으로 걸음 걸이를 교정하세요!",
                        "https://smartstore.naver.com/whyshop/products/4543895193?NaPm=ct%3Dkhfvk3hc%7Cci%3D8169140282fbd5001cc7e6f29a086f9fe1df8320%7Ctr%3Dslsl%7Csn%3D955149%7Chk%3D3ff3c95a3580d008edde4abb75e464c9ff74af66"));

                ProductAdapter productAdapter = new ProductAdapter(products);
                recyclerView.setAdapter(productAdapter);
            }
        }
    }
}