package net.stuple.simplenotes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.stuple.simplenotes.databinding.NoteFragmentBinding;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class NoteFragment extends Fragment {
    public String[] files = {"FirstNote.md"};
    public int whatfile = 0;
    private File folder;
    private NoteFragmentBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folder = new File(requireContext().getExternalFilesDir(null), "notes");
        if (!folder.exists()) {
            boolean isCreated = folder.mkdirs();
            if (isCreated) {
                Log.d("NotesApp", "Folder created at: " + folder.getAbsolutePath());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = NoteFragmentBinding.inflate(inflater, container, false);
        binding.filename.setText("Note: "+files[whatfile]);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String currentFile = files[whatfile];
        loadNote(currentFile);

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote(currentFile);
            }
        });
    }

    private void loadNote(String fileName) {
        try {
            File savedFile = new File(folder, fileName);
            byte[] content;

            if (savedFile.exists()) {
                content = FileUtil.readFile(savedFile);
                Log.d("NotesApp", "Loading from External Folder: " + savedFile.getAbsolutePath());
            } else {
                try (InputStream is = requireContext().getAssets().open(fileName)) {
                    content = FileUtil.readStream(is);
                    Log.d("NotesApp", "Loading from Assets");
                }
            }

            binding.textviewNoteContent.setText(new String(content));
        } catch (IOException e) {
            Log.e("NotesApp", "Error loading file", e);
        }
    }

    private void saveNote(String fileName) {
        String userText = binding.textviewNoteContent.getText().toString();
        File file = new File(folder, fileName);

        try {
            FileUtil.writeFile(file, userText.getBytes());
            Toast.makeText(requireContext(), "Note Saved! :D", Toast.LENGTH_SHORT).show();
            Log.d("NotesApp", "Saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("NotesApp", "Save failed", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
