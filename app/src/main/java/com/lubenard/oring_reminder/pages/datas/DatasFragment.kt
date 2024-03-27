package com.lubenard.oring_reminder.pages.datas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.lubenard.oring_reminder.MainActivity
import com.lubenard.oring_reminder.R
import com.lubenard.oring_reminder.utils.DateUtils
import java.util.concurrent.TimeUnit

class DatasFragment: Fragment() {

    val TAG: String = "DataFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.datas_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requireActivity().setTitle(R.string.data_fragment_title)

        val dbManager = MainActivity.getDbManager()

        val datas = dbManager.getAllDatasForAllEntrys()

        val numberOfEntries = view.findViewById<TextView>(R.id.number_of_entries)
        val lastEntry = view.findViewById<TextView>(R.id.last_entry)
        val firstEntry = view.findViewById<TextView>(R.id.first_entry)
        val timeBetweenFirstAndLastEntry = view.findViewById<TextView>(R.id.converted_time_between_first_and_last_entries)

        numberOfEntries.text = String.format(getString(R.string.number_of_entries), datas.size)

        val lastEntryData: String
        val firstEntryData: String
        val timeBetweenLastAndFirstData: String

        if (datas.size > 0) {
            lastEntryData = datas[datas.size - 1].getStartDate().split(" ")[0]
            firstEntryData = datas[0].getEndDate().split(" ")[0]

            val seconds: Int = DateUtils.getDateDiff(datas[0].getStartDate(), datas[datas.size - 1].getStartDate(), TimeUnit.SECONDS).toInt()
            val weeks: Int = seconds / 604800
            val days: Int = (seconds % 604800) / 86400
            val hours: Int = ((seconds % 604800) % 86400) / 3600
            val minutes: Int = (((seconds % 604800) % 86400) % 3600) / 60
            timeBetweenLastAndFirstData = String.format(getString(R.string.time_worn_appr), weeks, days, hours, minutes)
        } else {
            lastEntryData = getString(R.string.not_set_yet)
            firstEntryData = getString(R.string.not_set_yet)
            timeBetweenLastAndFirstData = getString(R.string.not_set_yet)
        }

        lastEntry.text = String.format(getString(R.string.last_entry), lastEntryData)
        firstEntry.text = String.format(getString(R.string.first_entry), firstEntryData)
        timeBetweenFirstAndLastEntry.text = timeBetweenLastAndFirstData
    }
}
