package com.example.betterfit.ui.contents;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.betterfit.MainActivity;
import com.example.betterfit.R;
import com.example.betterfit.ui.statistics.StatisticsViewModel;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;

public class ContentsFragment extends Fragment {
    private TextView hashtag1, hashtag2, hashtag3, title1, title2, title3;
    private Button videoBtn, newsBtn, productBtn;
    private View videoBar, newsBar, productBar;

    private static final String API_KEY = "AIzaSyBiEWqzkWLAVs0RJsw7_WhZOUP3W5pn1BY";
    private static String VIDEO1_ID = "KHOhjvYPZfg";
    private static String VIDEO2_ID = "WSEtdciBPLM";
    private static String VIDEO3_ID = "FVfIdxyRTHw";

    //private ContentsViewModel contentsViewModel;
    public ContentsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //contentsViewModel = new ViewModelProvider(this).get(ContentsViewModel.class);
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_contents, container, false);

        YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtubeView1, youTubePlayerFragment).commit();

        // videoBtn = (Button) view.findViewById(R.id.videoBtn); // Scroll up
        newsBtn = (Button) view.findViewById(R.id.newsBtn);
        productBtn = (Button) view.findViewById(R.id.productBtn);

        //Log.d("파싱한 아이디id 값", VIDEO1_ID);
        //String url1 ="https://img.youtube.com/vi/"+ VIDEO1_ID + "/" + "default.jpg"; //유튜브 썸네일 불러오는 방법

        //Glide.with(this).load(url1).into(R.id.youtubeView1);

        youTubePlayerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                if (!wasRestored) {
                    player.cueVideo(VIDEO1_ID);
                    //player.play();
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                // YouTube error
                String errorMessage = error.toString();
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                Log.d("errorMessage: ", errorMessage);
            }
        });

        newsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_contents_to_navigation_news);
            }
        });

        productBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_contents_to_navigation_product);
            }
        });

        return view;
    }
}