package net.stuple.simplenotes.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import net.stuple.simplenotes.R;
import net.stuple.simplenotes.util.FileUtil;

import java.io.IOException;

public class selectmenu_fragment extends Fragment {

    private Uri notesFolderUri;
    private ActivityResultLauncher<Uri> openDocumentTreeLauncher;
    private NavController navController;
    private ListView myListView;
    private String[] files = {};

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

                        notesFolderUri = uri;
                        Toast.makeText(getContext(), "Notes folder selected!", Toast.LENGTH_SHORT).show();
                        updateFileList(); 
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selectmenu_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        myListView = view.findViewById(R.id.listview);

        myListView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedFile = files[position];
            Bundle bundle = new Bundle();
            bundle.putString("filename", selectedFile);
            navController.navigate(R.id.action_selectmenu_fragment_to_noteFragment, bundle);
        });

        Button newNoteButton = view.findViewById(R.id.button);
        newNoteButton.setOnClickListener(v -> {
            if (notesFolderUri != null) {
                showFileNameDialog();
            } else {
                Toast.makeText(getContext(), "Please select a notes folder first.", Toast.LENGTH_LONG).show();
                openFolderPicker();
            }
        });
    }

    public void updateFileList() {
        if (getContext() == null) return;

        // Always re-read the URI from preferences to handle changes from other fragments
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String uriString = prefs.getString("notes_folder_uri", null);

        if (uriString != null) {
            notesFolderUri = Uri.parse(uriString);
            files = FileUtil.refreshLocalFiles(requireContext(), notesFolderUri);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, files);
            if (myListView != null) {
                myListView.setAdapter(adapter);
            }
        }
    }

    private void openFolderPicker() {
        Toast.makeText(getContext(), "Please select a folder to store your notes.", Toast.LENGTH_LONG).show();
        openDocumentTreeLauncher.launch(null);
    }

    private void showFileNameDialog() {
        final EditText input = new EditText(requireContext());

        new AlertDialog.Builder(requireContext())
                .setTitle("Create New Note")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        String fileName = name.endsWith(".md") ? name : name + ".md";
                        try {
                            FileUtil.saveNote(requireContext(), notesFolderUri, fileName, "");
                            Toast.makeText(getContext(), "Note '" + fileName + "' created!", Toast.LENGTH_SHORT).show();

                            Bundle bundle = new Bundle();
                            bundle.putString("filename", fileName);
                            navController.navigate(R.id.action_selectmenu_fragment_to_noteFragment, bundle);

                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Error creating note.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFileList(); 
    }
}
