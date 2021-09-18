package com.lubenard.oring_reminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.R;

public class NewDesignDetailsFragment extends Fragment {
    ProgressBar progress_bar;
    TextView progress_bar_text;
    int progr = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.new_deign_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress_bar = view.findViewById(R.id.progress_bar);
        progress_bar_text = view.findViewById(R.id.text_view_progress);

        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle("Test new design");

        Button decr = view.findViewById(R.id.button_decr);
        Button incr = view.findViewById(R.id.button_incr);

        incr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progr <= 90) {
                    progr += 10;
                    updateProgressBar();
                }
            }
        });

        decr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progr >= 10) {
                    progr -= 10;
                    updateProgressBar();
                }
            }
        });

    }

    private void updateProgressBar() {
        progress_bar.setProgress(progr);
        progress_bar_text.setText(progr + "%");
    }

}
