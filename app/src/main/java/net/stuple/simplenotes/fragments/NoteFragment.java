package net.stuple.simplenotes.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.stuple.simplenotes.databinding.NoteFragmentBinding;
import net.stuple.simplenotes.util.FileUtil;

import java.io.IOException;

public class NoteFragment extends Fragment {
    public String[] files = {"FirstNote.md"};
    public int whatfile = 0;
    private Uri notesFolderUri;

    private NoteFragmentBinding binding;

    private ActivityResultLauncher<Uri> openDocumentTreeLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
                        refreshLocalFiles();
                        loadNote(files[whatfile]);
                    }
                }
        );

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String uriString = prefs.getString("notes_folder_uri", null);

        if (uriString == null) {
            openFolderPicker();
        } else {
            notesFolderUri = Uri.parse(uriString);
            boolean hasPermission = requireContext().getContentResolver().getPersistedUriPermissions().stream()
                    .anyMatch(p -> p.getUri().equals(notesFolderUri) && p.isReadPermission() && p.isWritePermission());

            if (hasPermission) {
                refreshLocalFiles();
            } else {
                Toast.makeText(getContext(), "Permission for notes folder lost. Please select it again.", Toast.LENGTH_LONG).show();
                openFolderPicker();
            }
        }
    }

    private void openFolderPicker() {
        Toast.makeText(getContext(), "Please select the folder where you want to store your notes.", Toast.LENGTH_LONG).show();
        openDocumentTreeLauncher.launch(null);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = NoteFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (notesFolderUri != null) {
            loadNote(files[whatfile]);
        }

        binding.floatingActionButton.setOnClickListener(view1 -> saveNote(files[whatfile]));

        binding.filename.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenu().add("New Note");
            popup.getMenu().add("Select Note");

            popup.setOnMenuItemClickListener(item -> {
                String clickedTitle = item.getTitle().toString();
                if (clickedTitle.equals("New Note")) {
                    showFileNameDialog();
                    return true;
                } else if (clickedTitle.equals("Select Note")) {
                    showFileSelectorDialog();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void loadNote(String fileName) {
        if (notesFolderUri == null) {
            if (binding != null) binding.textviewNoteContent.setText("");
            return;
        }
        try {
            byte[] content = FileUtil.loadNote(requireContext(), notesFolderUri, fileName);
            if (binding != null) {
                binding.textviewNoteContent.setText(new String(content));
                binding.filename.setText("Note: " + fileName);
            }
        } catch (IOException e) {
            Log.e("NotesApp", "Error loading note", e);
            Toast.makeText(getContext(), "Error loading note.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNote(String fileName) {
        if (notesFolderUri == null) {
            Toast.makeText(requireContext(), "Please select a notes folder first.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fileName == null || fileName.isEmpty()) {
            Log.e("NotesApp", "Save failed: file name is null or empty");
            return;
        }

        String userText = binding.textviewNoteContent.getText().toString();
        try {
            FileUtil.saveNote(requireContext(), notesFolderUri, fileName, userText);
            Toast.makeText(requireContext(), "Note Saved! :D", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("NotesApp", "Save failed", e);
            Toast.makeText(requireContext(), "Error saving note.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFileSelectorDialog() {
        if (files == null || files.length == 0) {
            Toast.makeText(getContext(), "No notes found.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select a Note")
                .setItems(files, (dialog, which) -> {
                    whatfile = which;
                    String selectedFile = files[whatfile];
                    loadNote(selectedFile);
                })
                .show();
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
                        binding.textviewNoteContent.setText("");
                        saveNote(fileName);
                        refreshLocalFiles();
                        for (int i = 0; i < files.length; i++) {
                            if (files[i].equals(fileName)) {
                                whatfile = i;
                                break;
                            }
                        }
                        loadNote(fileName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void refreshLocalFiles() {
        if (notesFolderUri != null) {
            files = FileUtil.refreshLocalFiles(requireContext(), notesFolderUri);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
