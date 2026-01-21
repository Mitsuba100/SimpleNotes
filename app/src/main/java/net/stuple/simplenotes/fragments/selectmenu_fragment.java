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
import androidx.appcompat.widget.PopupMenu;
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

        myListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            String selectedFile = files[position];

            PopupMenu popup = new PopupMenu(requireContext(), view1);
            popup.getMenu().add("Delete");
            popup.getMenu().add("Rename");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Delete")) {
                    confirmDelete(selectedFile);
                    return true;
                } else if (item.getTitle().equals("Rename")) {
                    showRenameDialog(selectedFile);
                    return true;
                }
                return false;
            });

            popup.show();
            return true;
        });
    }

    private void showRenameDialog(String selectedFile) {
        final EditText input = new EditText(requireContext());
        input.setText(selectedFile);

        new AlertDialog.Builder(requireContext())
                .setTitle("Rename Note")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(selectedFile)) {
                        if (!newName.endsWith(".md")) newName += ".md";
                        if (FileUtil.renameNote(requireContext(), notesFolderUri, selectedFile, newName)) {
                            Toast.makeText(getContext(), "Note renamed", Toast.LENGTH_SHORT).show();
                            updateFileList();
                        } else {
                            Toast.makeText(getContext(), "Rename failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(String selectedFile) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete '" + selectedFile + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (FileUtil.deleteNote(requireContext(), notesFolderUri, selectedFile)) {
                        Toast.makeText(getContext(), "Note deleted", Toast.LENGTH_SHORT).show();
                        updateFileList();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete note", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void updateFileList() {
        if (getContext() == null) return;

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
