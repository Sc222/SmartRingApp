package ru.sc222.smartringapp.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ru.sc222.smartringapp.R;
import ru.sc222.smartringapp.ui.dialogs.SelectBluetoothDeviceDialog;
import ru.sc222.smartringapp.utils.PreferenceUtils;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        //TODO SHOW CONNECT TO DEVICE DIALOG on preference click
    }

    //todo move to separate file
    public static class SettingsFragment extends PreferenceFragmentCompat {
        private SelectBluetoothDeviceDialog dialog;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            Preference deviceAddress = findPreference(PreferenceUtils.DEVICE_ADDRESS);
            assert deviceAddress != null;
            deviceAddress.setSummary(PreferenceUtils.getCurrentDeviceAddress(getContext()));
            deviceAddress.setOnPreferenceClickListener(preference -> {
                dialog = new SelectBluetoothDeviceDialog(true, getActivity(), requireActivity().getApplicationContext(), getViewLifecycleOwner());
                dialog.show();
                dialog.setOnDismissListener(dialog -> deviceAddress.setSummary(PreferenceUtils.getCurrentDeviceAddress(getContext())));
                return true;
            });
        }

    }
}