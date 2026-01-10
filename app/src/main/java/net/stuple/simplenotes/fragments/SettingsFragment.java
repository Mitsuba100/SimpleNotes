package net.stuple.simplenotes.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.stuple.simplenotes.databinding.SettingsFragmentBinding;

public class SettingsFragment extends Fragment {
    public SettingsFragmentBinding binding;
    private ActivityResultLauncher<Uri> openDocumentTreeLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openDocumentTreeLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                uri -> {
                    if (uri != null) {
                        requireContext().getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                        prefs.edit().putString("notes_folder_uri", uri.toString()).apply();

                        Toast.makeText(getContext(), "Notes folder changed successfully!", Toast.LENGTH_SHORT).show();
                        
                        updatePathText(uri.toString());
                    }
                }
        );
    }

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
        
        binding.button2.setOnClickListener(v -> {
            openDocumentTreeLauncher.launch(null);
        });

        binding.textView3.setText("Version: " + currentVersion);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String savedUri = prefs.getString("notes_folder_uri", "Not set");
        updatePathText(savedUri);

        return binding.getRoot();
    }

    private void updatePathText(String uriString) {
        if (binding == null) return;
        
        String displayPath = uriString;
        if (uriString != null && uriString.startsWith("content://")) {
            Uri uri = Uri.parse(uriString);
            String path = uri.getPath();
            if (path != null) {
                String[] segments = path.split(":");
                if (segments.length > 1) {
                    displayPath = segments[1];
                }
            }
        }
        binding.textView2.setText("Path: " + displayPath);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
