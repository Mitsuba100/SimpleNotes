package net.stuple.simplenotes.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import net.stuple.simplenotes.databinding.SettingsFragmentBinding;

public class SettingsFragment extends Fragment {
    public SettingsFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentBinding.inflate(inflater, container, false);
        String currentVersion = "";
        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            currentVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {}

        SharedPreferences mainPrefs = requireContext().getSharedPreferences("main_preference", Context.MODE_PRIVATE);
        CheckBox checkBox = binding.automaticUpdateCheckBox;
        checkBox.setChecked(mainPrefs.getBoolean("show_update_alert", true));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> mainPrefs.edit().putBoolean("show_update_alert", isChecked).apply());


        binding.textView3.setText(new StringBuilder().append("Version: " + currentVersion).toString());
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
