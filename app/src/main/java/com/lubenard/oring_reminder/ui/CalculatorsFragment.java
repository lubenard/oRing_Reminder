package com.lubenard.oring_reminder.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.R;

public class CalculatorsFragment extends Fragment {

    private final static String TAG = "CalculatorFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.calculators_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.calculators_title);

        EditText numberOfSpermato = view.findViewById(R.id.editTextNumberOfSperma);
        EditText percentOfMobility = view.findViewById(R.id.editTextPercentMobility);
        Button computeFertility = view.findViewById(R.id.computeFertilityButton);
        TextView answerFertility = view.findViewById(R.id.answerFertilityCompute);

        computeFertility.setOnClickListener(view1 -> {
            String numberOfSpermatoString = numberOfSpermato.getText().toString();
            String percentOfMobilityString = percentOfMobility.getText().toString();
            int computation;
            // YES, we need to to this, because parseInt cannot convert a number if there is spaces in them...
            numberOfSpermatoString = numberOfSpermatoString.replace(" ", "");
            percentOfMobilityString = percentOfMobilityString.replace(" ", "");
            if (numberOfSpermatoString.isEmpty() || percentOfMobilityString.isEmpty())
                Toast.makeText(getContext(), getString(R.string.please_fill_required_infos), Toast.LENGTH_LONG).show();
            else {
                computation = (int) (Integer.parseInt(numberOfSpermatoString) * (Integer.parseInt(percentOfMobilityString) * 0.01));
                if (computation < 1000000) {
                    answerFertility.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                    answerFertility.setText(getString(R.string.you_are_contracepted) + computation + getString(R.string.spermo_per_ml));
                } else {
                    answerFertility.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
                    answerFertility.setText(getString(R.string.you_are_not_contracepted) + computation + getString(R.string.spermo_per_ml));
                }
            }
        });
    }
}
