package net.stuple.simplenotes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import net.stuple.simplenotes.databinding.NoteFragmentBinding;
import java.io.File;

public class NoteFragment extends Fragment {
    public String[] files = {"note1.md"};
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
        binding.filename.setText(files[whatfile]);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String currentFile = files[whatfile];

        Fileloader myLoader = new Fileloader();
        myLoader.load(requireContext(), binding, currentFile);
        binding.filename.setText(files[whatfile]);
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Filewirter myWriter = new Filewirter();
                myWriter.writer(requireContext(), binding, files[whatfile]);
            }
        });
        binding.settingsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_noteFragment_to_settingsFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}