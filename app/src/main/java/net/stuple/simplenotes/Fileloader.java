package net.stuple.simplenotes;

import android.content.Context;
import android.util.Log;
import net.stuple.simplenotes.databinding.ActivityMainBinding;
import net.stuple.simplenotes.databinding.NoteFragmentBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Fileloader {
    public void load(Context context, NoteFragmentBinding binding, String fileName) {
        try {
            InputStream is;
            File folder = new File(context.getExternalFilesDir(null), "notes");
            File savedFile = new File(folder, fileName);

            if (savedFile.exists()) {
                is = new FileInputStream(savedFile);
                Log.d("NotesApp", "Loading from External Folder: " + savedFile.getAbsolutePath());
            } else {
                is = context.getAssets().open(fileName);
                Log.d("NotesApp", "Loading from Assets");
            }

            Scanner sc = new Scanner(is);
            StringBuilder allContent = new StringBuilder();
            while (sc.hasNextLine()) {
                allContent.append(sc.nextLine()).append("\n");
            }

            binding.textviewNoteContent.setText(allContent.toString());
            sc.close();
            is.close();
        } catch (IOException e) {
            Log.e("NotesApp", "Error loading file", e);
        }
    }
}