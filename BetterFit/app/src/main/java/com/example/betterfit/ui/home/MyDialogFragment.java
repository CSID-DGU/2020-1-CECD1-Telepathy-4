package com.example.betterfit.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.betterfit.R;

public class MyDialogFragment extends DialogFragment {


    public interface OnInputListener {
        void sendInput(String input);
    }

    public OnInputListener onInputListener;

    private static final String TAG = "MyCustomDialog";

    private EditText mInput;
    private TextView mActionOk;
    private TextView mActionCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog, container, false);

        mInput = view.findViewById(R.id.new_goal);
        mActionOk = view.findViewById(R.id.set_goal_btn);
        //mActionCancel = view.findViewById(R.id.action_cancel);
        /*
        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();;
            }
        });
*/
        mActionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mInput.getText().toString();
                if (!input.equals("")) {
                    onInputListener.sendInput(input);
                }
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            /* this line is main difference for fragment to fragment communication & fragment to activity communication
            fragment to fragment: onInputListener = (OnInputListener) getTargetFragment();
            fragment to activity: onInputListener = (OnInputListener) getActivity();
             */
            onInputListener = (OnInputListener) getTargetFragment();
            Log.d(TAG, "onAttach: " + onInputListener );
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException : " + e.getMessage());
        }
    }
}