package com.example.betterfit.ui.contents;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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

import com.example.betterfit.MainActivity;
import com.example.betterfit.R;
import com.example.betterfit.ui.statistics.StatisticsViewModel;


import java.util.Calendar;

public class ProductFragment extends Fragment {
    private TextView hashtag1, hashtag2, hashtag3, title1, title2, title3;
    private Button videoBtn, newsBtn, productBtn;
    private View videoBar, newsBar, productBar;

    //private ContentsViewModel contentsViewModel;
    public ProductFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //contentsViewModel = new ViewModelProvider(this).get(ContentsViewModel.class);
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_product, container, false);
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
}