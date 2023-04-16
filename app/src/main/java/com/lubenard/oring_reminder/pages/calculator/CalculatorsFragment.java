package com.lubenard.oring_reminder.pages.calculator;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.utils.Log;

public class CalculatorsFragment extends DialogFragment {

    private final static String TAG = "CalculatorFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.calculators_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fix widget to bottom and makes the dialog take up the full width
        Window window = requireDialog().getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes());
        lp.width = 1000;
        lp.height = WRAP_CONTENT;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(lp);
        window.setGravity(Gravity.BOTTOM);

        EditText editTextConcentration = view.findViewById(R.id.editTextConcentration);
        EditText editTextPercentOfMobility = view.findViewById(R.id.editTextPercentMobility);
        EditText editTextVolumeOfSperm = view.findViewById(R.id.editTextSpermVolume);

        ImageButton iconConcentration = view.findViewById(R.id.iconConcentration);
        ImageButton iconPercentageMobility = view.findViewById(R.id.iconPercentageMobility);

        ImageButton close_fragment_button = view.findViewById(R.id.create_new_break_cancel);
        close_fragment_button.setOnClickListener(v -> dismiss());

        editTextConcentration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "Editable is " + editable);
                if (editable.length() != 0) {
                    int concentration = Integer.parseInt(editable.toString());
                    if (concentration == 0)
                        iconConcentration.setBackgroundResource(R.drawable.check_calculator);
                    else if (concentration > 0 && concentration < 100_000)
                        iconConcentration.setBackgroundResource(R.drawable.warning_calculator);
                }
            }
        });


        editTextPercentOfMobility.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    int mobilityPercentage = Integer.parseInt(editable.toString());
                    if (mobilityPercentage > 32)
                        iconPercentageMobility.setBackgroundResource(R.drawable.warning_calculator);
                    else
                        iconPercentageMobility.setBackgroundResource(R.drawable.check_calculator);
                }
            }
        });

        /*computeFertility.setOnClickListener(view1 -> {
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
        });*/
    }
}
