package com.lubenard.oring_reminder.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.CustomListAdapter;
import com.lubenard.oring_reminder.custom_components.CustomListPausesAdapter;
import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class NewDesignDetailsFragment extends Fragment implements CustomListAdapter.onListItemClickListener {
    ProgressBar progress_bar;
    TextView progress_bar_text;
    int progr = 0;

    private static ViewGroup viewGroup;
    private CustomListPausesAdapter adapter;
    private ArrayList<RingModel> dataModels;
    private DbManager dbManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.new_design_home_fragment, container, false);
    }

    private void updateHistoryList() {
        dataModels.clear();
        ArrayList<RingModel> entrysDatas = dbManager.getHistoryForMainView(true);
        //dataModels.addAll(entrysDatas);

        for (int i = 0; i != entrysDatas.size(); i++) {

            LayoutInflater inflater = (LayoutInflater) getActivity().
                    getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.main_history_one_elem, null);
            view.setTag(Integer.toString((int) entrysDatas.get(i).getId()));
            TextView textView_date = view.findViewById(R.id.main_history_date);
            textView_date.setText(entrysDatas.get(i).getDatePut().split(" ")[0]);

            TextView textView_hour_from = view.findViewById(R.id.custom_view_date_weared_to);
            textView_hour_from.setText(entrysDatas.get(i).getDatePut().split(" ")[1]);

            TextView textView_hour_to = view.findViewById(R.id.custom_view_date_weared_from);
            textView_hour_to.setText(entrysDatas.get(i).getDateRemoved().split(" ")[1]);
            viewGroup.addView(view);

            //view.setOnClickListener(clickInLinearLayout());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbManager = MainActivity.getDbManager();
        dataModels = new ArrayList<>();

        viewGroup = view.findViewById(R.id.list_history);

        updateHistoryList();

        progress_bar = view.findViewById(R.id.progress_bar);
        progress_bar_text = view.findViewById(R.id.text_view_progress);

        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle("oRing - Reminder");

    }

    /**
     * onClickManager handling clicks on the main List
     */
    @Override
    public void onListItemClickListener(int position) {
        RingModel dataModel= dataModels.get(position);
        Log.d("MainView", "Element " + dataModel.getId());
        EntryDetailsFragment fragment = new EntryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("entryId", dataModel.getId());
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, null)
                .addToBackStack(null).commit();
    }

    private void updateProgressBar() {
        progress_bar.setProgress(progr);
        progress_bar_text.setText(progr + "%");
    }

}
