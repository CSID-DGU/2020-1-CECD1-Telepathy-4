package com.example.betterfit.ui.contents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.betterfit.R;

public class ContentsFragment extends Fragment {

    private ContentsViewModel contentsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        contentsViewModel =
                ViewModelProviders.of(this).get(ContentsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contents, container, false);
        return root;
    }
}