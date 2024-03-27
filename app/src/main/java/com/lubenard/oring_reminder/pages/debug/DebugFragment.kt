package com.lubenard.oring_reminder.pages.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.lubenard.oring_reminder.MainActivity
import com.lubenard.oring_reminder.R
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager
import java.util.Calendar

class DebugFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.debug_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requireActivity().setTitle(R.string.debug_menu_title)

        val buttonSendNotif = view.findViewById<Button>(R.id.debug_send_notif)
        val enableLogSwitch = view.findViewById<SwitchMaterial>(R.id.debug_enable_logs)

        buttonSendNotif.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.set(Calendar.SECOND, 15)
            SessionsAlarmsManager.setAlarm(context, cal, -1, false)
        }

        enableLogSwitch.setOnCheckedChangeListener { _, isChecked ->
            MainActivity.getSettingsManager().setIsLoggingEnabled(isChecked)
        }
    }
}
