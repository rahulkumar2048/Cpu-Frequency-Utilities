package com.performancetweaker.app.ui.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.performancetweaker.app.R;
import com.performancetweaker.app.utils.FANInterstialHelper;
import com.performancetweaker.app.utils.VmUtils;

import java.util.LinkedHashMap;

public class VirtualMemoryFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    Context context;
    PreferenceCategory preferenceCategory;
    EditTextPreference editTextPreferences[];
    LinkedHashMap<String, String> vmEntries = new LinkedHashMap<>();
    ProgressBar progressBar;
    FANInterstialHelper fanInterstialHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pref_container, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.loading_main);
        progressBar.setVisibility(View.VISIBLE);
        context = getActivity();
        fanInterstialHelper = FANInterstialHelper.getInstance(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        new PopulateVmEntries().execute();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        fanInterstialHelper.showAd();
        VmUtils.setVM(newValue.toString(), preference.getKey());
        preference.setSummary(VmUtils.getVMValue(preference.getKey()));
        return true;
    }

    private class PopulateVmEntries extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            vmEntries.clear();
            vmEntries = VmUtils.getVMfiles();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            addPreferencesFromResource(R.xml.virtual_memory_preference);

            preferenceCategory = (PreferenceCategory) findPreference("vm_pref");

            if (vmEntries != null && vmEntries.size() != 0) {
                editTextPreferences = new EditTextPreference[vmEntries.size()];
                int i = 0;
                for (LinkedHashMap.Entry<String, String> entry : vmEntries.entrySet()) {
                    editTextPreferences[i] = new EditTextPreference(context);

                    editTextPreferences[i].setKey(entry.getKey());
                    editTextPreferences[i].setTitle(entry.getKey());
                    editTextPreferences[i].setSummary(entry.getValue());
                    editTextPreferences[i].setDialogTitle(entry.getKey());
                    editTextPreferences[i].setDefaultValue(entry.getValue());
                    editTextPreferences[i].setOnPreferenceChangeListener(VirtualMemoryFragment.this);

                    preferenceCategory.addPreference(editTextPreferences[i]);
                    i++;
                }
                progressBar.setVisibility(View.GONE);
            }

        }
    }
}
