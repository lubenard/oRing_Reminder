package com.lubenard.oring_reminder.pages.calculator;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.utils.Log;

public class CalculatorsFragment extends BottomSheetDialogFragment {

    private final static String TAG = "CalculatorFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.calculators_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireDialog().getWindow().setWindowAnimations(R.style.bottom_dialog_animation);
        ((View) getView().getParent()).setBackgroundColor(Color.TRANSPARENT);

        EditText editTextConcentration = view.findViewById(R.id.editTextConcentration);
        EditText editTextPercentOfMobility = view.findViewById(R.id.editTextPercentMobility);
        EditText editTextVolumeOfSperm = view.findViewById(R.id.editTextSpermVolume);

        ImageButton iconConcentration = view.findViewById(R.id.iconConcentration);
        ImageButton iconPercentageMobility = view.findViewById(R.id.iconPercentageMobility);
        ImageButton iconVolume = view.findViewById(R.id.iconVolume);

        TextView concentrationTips = view.findViewById(R.id.contraception_tips);
        TextView mobilityTips = view.findViewById(R.id.mobility_tips);
        TextView volumeTips = view.findViewById(R.id.volume_tips);

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
                    iconConcentration.setVisibility(View.VISIBLE);
                    concentrationTips.setVisibility(View.VISIBLE);
                    if (concentration == 0) {
                        iconConcentration.setBackgroundResource(R.drawable.check_calculator);
                        concentrationTips.setBackgroundResource(R.drawable.valid_calculator_text);
                        concentrationTips.setText(R.string.calculator_contraception_100_safe);
                    } else if (concentration > 0 && concentration <= 100_000) {
                        iconConcentration.setBackgroundResource(R.drawable.warning_calculator);
                        concentrationTips.setBackgroundResource(R.drawable.warning_calculator_text);
                        concentrationTips.setText(R.string.calculator_contraception_99_safe);
                    } else if (concentration > 100_000 && concentration < 1_000_000) {
                        iconConcentration.setBackgroundResource(R.drawable.warning_calculator);
                        concentrationTips.setBackgroundResource(R.drawable.warning_calculator_text);
                        concentrationTips.setText(R.string.calculator_contraception_90_safe);
                    } else if (concentration >= 1_000_000){
                        iconConcentration.setBackgroundResource(R.drawable.exclamation_triangle_poly);
                        concentrationTips.setBackgroundResource(R.drawable.error_calculator_text);
                        concentrationTips.setText(R.string.calculator_contraception_NOT_safe);
                    }
                } else {
                    iconConcentration.setVisibility(View.INVISIBLE);
                    concentrationTips.setVisibility(View.GONE);
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
                    iconPercentageMobility.setVisibility(View.VISIBLE);
                    mobilityTips.setVisibility(View.VISIBLE);
                    if (mobilityPercentage == 0) {
                        iconPercentageMobility.setBackgroundResource(R.drawable.check_calculator);
                        mobilityTips.setBackgroundResource(R.drawable.valid_calculator_text);
                        mobilityTips.setText(R.string.calculator_contraception_perfect_mobility);
                    } else if (mobilityPercentage > 0 && mobilityPercentage < 15) {
                        iconPercentageMobility.setBackgroundResource(R.drawable.check_calculator);
                        mobilityTips.setBackgroundResource(R.drawable.valid_calculator_text);
                        mobilityTips.setText(R.string.calculator_contraception_very_good);
                    } else if (mobilityPercentage >= 15 && mobilityPercentage < 20) {
                        iconPercentageMobility.setBackgroundResource(R.drawable.warning_calculator);
                        mobilityTips.setBackgroundResource(R.drawable.valid_calculator_text);
                        mobilityTips.setText(R.string.calculator_contraception_correct_mobility);
                    } else if (mobilityPercentage >= 20 && mobilityPercentage < 32) {
                        iconPercentageMobility.setBackgroundResource(R.drawable.warning_calculator);
                        mobilityTips.setBackgroundResource(R.drawable.warning_calculator_text);
                        mobilityTips.setText(R.string.calculator_contraception_consult_specialist_if_doubt);
                    } else if (mobilityPercentage >= 32) {
                        iconPercentageMobility.setBackgroundResource(R.drawable.exclamation_triangle_poly);
                        mobilityTips.setBackgroundResource(R.drawable.error_calculator_text);
                        mobilityTips.setText(R.string.calculator_contraception_fertile);
                    }
                } else {
                    iconPercentageMobility.setVisibility(View.INVISIBLE);
                }
            }
        });

        editTextVolumeOfSperm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    float volume = Float.parseFloat(editable.toString());
                    iconVolume.setVisibility(View.VISIBLE);
                    if (volume >= 1.5f && volume <= 6f) {
                        iconVolume.setBackgroundResource(R.drawable.check_calculator);
                    } else {
                        iconVolume.setBackgroundResource(R.drawable.warning_calculator);
                        volumeTips.setVisibility(View.VISIBLE);
                        volumeTips.setBackgroundResource(R.drawable.warning_calculator_text);
                        volumeTips.setText(R.string.calculator_contraception_sperm_quantity_consult_specialist);
                    }
                } else {
                    iconVolume.setVisibility(View.GONE);
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
