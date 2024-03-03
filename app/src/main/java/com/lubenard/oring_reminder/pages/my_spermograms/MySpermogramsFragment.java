package com.lubenard.oring_reminder.pages.my_spermograms;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.Spermograms;
import com.lubenard.oring_reminder.pages.spermograms_details.SpermogramsDetailsFragment;
import com.lubenard.oring_reminder.ui.adapters.CustomSpermoListAdapter;
import com.lubenard.oring_reminder.utils.Log;


public class MySpermogramsFragment extends Fragment implements CustomSpermoListAdapter.onListItemClickListener{
    private static RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private MySpermogramsViewModel mySpermogramsViewModel;
    private static CustomSpermoListAdapter adapter;
    private static CustomSpermoListAdapter.onListItemClickListener onListItemClickListener;

    private final static String TAG = "MySpermogramsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.my_spermograms_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.my_spermo_title_fragment);

        FloatingActionButton fab = view.findViewById(R.id.fab);

        recyclerView = view.findViewById(R.id.spermo_list);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        onListItemClickListener = this;

        fab.setOnClickListener(v -> selectSpermoFromFiles());

        mySpermogramsViewModel = new ViewModelProvider(requireActivity()).get(MySpermogramsViewModel.class);

        mySpermogramsViewModel.spermoList.observe(getViewLifecycleOwner(), spermoList -> {
            view.findViewById(R.id.spermo_list_spinner).setVisibility(View.GONE);
            if (spermoList.isEmpty()) view.findViewById(R.id.spermo_list_no_data_text).setVisibility(View.VISIBLE);
            adapter = new CustomSpermoListAdapter(spermoList, onListItemClickListener);
            recyclerView.setAdapter(adapter);
        });

        mySpermogramsViewModel.loadSpermoList();
    }

    /**
     * onClickManager handling clicks on the spermogram List
     */
    @Override
    public void onListItemClickListener(int position) {
        Spermograms dataModel = mySpermogramsViewModel.spermoList.getValue().get(position);
        Log.d(TAG, "Element " + dataModel.getId());
        SpermogramsDetailsFragment fragment = new SpermogramsDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("entryId", dataModel.getId());
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, null)
                .addToBackStack(null).commit();
    }

    /**
     * Each time the app is resumed, fetch new entry
     */
    @Override
    public void onResume() {
        super.onResume();
        mySpermogramsViewModel.loadSpermoList();
    }

    /**
     * Open action intent to choose a file
     */
    private void selectSpermoFromFiles() {
        Intent dataToFileChooser = new Intent(Intent.ACTION_GET_CONTENT);
        dataToFileChooser.setType("application/pdf");
        try {
            startActivityForResult(dataToFileChooser, 1);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Failed to open a Intent to import Spermogram.");
            Toast.makeText(getContext(), R.string.toast_error_custom_path_backup_restore_fail, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getDataString() != null) {
            mySpermogramsViewModel.saveSpermoOnLocalStorage(requireContext(), data);
        }
    }
}
