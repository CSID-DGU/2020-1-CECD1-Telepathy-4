package com.example.betterfit.ui.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.betterfit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Calendar;
import java.util.Date;

public class ProfileFragment extends Fragment {
    private static final String TAG = "Profile";

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        final ScrollView scrollView = (ScrollView) root.findViewById(R.id.scrollView2);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });

        /* 프로필 입력 또는 수정 후 저장된 데이터 read */
        final SharedPreferences sharePref = getActivity().getPreferences(Context.MODE_PRIVATE);

        // 구글 계정의 이름 정보를 default
        String defaultName = "";
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Name
                defaultName = profile.getDisplayName();
            }
        }

        // default - birth: 오늘 날짜
        String userName = sharePref.getString("name", defaultName);
        int userHeight = sharePref.getInt("height", 0);
        int userWeight = sharePref.getInt("weight", 0);
        int userSex = sharePref.getInt("sex", 0);
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1900);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 1);
        long userBirth = sharePref.getLong("birth", calendar.getTimeInMillis());

        // ui
        EditText editName = root.findViewById(R.id.user_name);
        EditText editHeight = root.findViewById(R.id.user_height);;
        EditText editWeight = root.findViewById(R.id.user_weight);
        RadioGroup radioSex = root.findViewById(R.id.sex_radio);
        DatePicker dateBirth = root.findViewById(R.id.user_birthday);

        // set
        editName.setText(userName);
        editHeight.setText(Integer.toString(userHeight));
        editWeight.setText(Integer.toString(userWeight));
        radioSex.check(userSex);
        Date day = new Date(userBirth);
        dateBirth.updateDate(day.getYear(), day.getMonth(), day.getDate());
        Log.w(TAG, "Birthday: "+day.getYear()+", "+day.getMonth()+", "+day.getDate());

        /* 프로필 입력 또는 수정한 데이터를 write */
        Button button = root.findViewById(R.id.profileButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharePref.edit();

                // ui
                EditText editName = getView().findViewById(R.id.user_name);
                EditText editHeight = getView().findViewById(R.id.user_height);
                EditText editWeight = getView().findViewById(R.id.user_weight);
                RadioGroup radioSex = getView().findViewById(R.id.sex_radio);
                DatePicker dateBirth = getView().findViewById(R.id.user_birthday);

                // set
                editor.putString("name", editName.getText().toString());
                editor.putInt("height", Integer.parseInt(editHeight.getText().toString()));
                editor.putInt("weight", Integer.parseInt(editWeight.getText().toString()));
                editor.putInt("sex", radioSex.getCheckedRadioButtonId());
                Date day = new Date(dateBirth.getYear(), dateBirth.getMonth(), dateBirth.getDayOfMonth());
                editor.putLong("birth", day.getTime());

                editor.apply();
                Navigation.findNavController(getView()).navigate(R.id.action_profileFragment_to_navigation_home);
            }
        });
        return root;
    }
}