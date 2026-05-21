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
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import net.stuple.simplenotes.databinding.SettingsFragmentBinding;
import net.stuple.simplenotes.util.FileUtil;

import java.io.IOException;

public class SettingsFragment extends Fragment {
    public SettingsFragmentBinding binding;
    private ActivityResultLauncher<Uri> openDocumentTreeLauncher;
    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String[]> importLauncher;
    private Uri notesFolderUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launcher for Folder Selection
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
                        notesFolderUri = uri;
                    }
                }
        );

        // Launcher for Exporting
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/markdown"),
                uri -> {
                    if (uri != null && notesFolderUri != null) {
                        try {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                            String currentNoteName = prefs.getString("current_note_name", null);

                            if (currentNoteName != null) {
                                DocumentFile notesDir = DocumentFile.fromTreeUri(requireContext(), notesFolderUri);
                                DocumentFile sourceFile = notesDir != null ? notesDir.findFile(currentNoteName) : null;
                                
                                if (sourceFile != null && sourceFile.exists()) {
                                    FileUtil.exportNote(requireContext(), sourceFile.getUri(), uri);
                                    Toast.makeText(getContext(), "Export successful!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Current note file not found.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "No note is currently open to export.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Export failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Launcher for Importing
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null && notesFolderUri != null) {
                        try {
                            DocumentFile sourceFile = DocumentFile.fromSingleUri(requireContext(), uri);
                            if (sourceFile != null && sourceFile.getName() != null) {
                                FileUtil.importNote(requireContext(), uri, notesFolderUri, sourceFile.getName());
                                Toast.makeText(getContext(), "Import successful!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Import failed", Toast.LENGTH_SHORT).show();
                        }
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
        
        binding.button2.setOnClickListener(v -> openDocumentTreeLauncher.launch(null));
        
        binding.button4.setOnClickListener(v -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            String currentNoteName = prefs.getString("current_note_name", "MyNote.md");
            exportLauncher.launch(currentNoteName);
        });
        
        binding.button5.setOnClickListener(v -> importLauncher.launch(new String[]{"text/markdown", "text/plain", "application/octet-stream"}));

        binding.textView3.setText("Version: " + currentVersion);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String savedUri = prefs.getString("notes_folder_uri", null);
        if (savedUri != null) {
            notesFolderUri = Uri.parse(savedUri);
            updatePathText(savedUri);
        }

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
                if (segments.length > 1) displayPath = segments[1];
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
