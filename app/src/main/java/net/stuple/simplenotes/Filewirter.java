package net.stuple.simplenotes;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import net.stuple.simplenotes.databinding.ActivityMainBinding;
import net.stuple.simplenotes.databinding.NoteFragmentBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Filewirter {
    public void writer(Context context, NoteFragmentBinding binding, String fileName) {
        String userText = binding.textviewNoteContent.getText().toString();

        File folder = new File(context.getExternalFilesDir(null), "notes");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(userText.getBytes());
            Toast.makeText(context, "Note Saved! :D", Toast.LENGTH_SHORT).show();
            Log.d("NotesApp", "Saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("NotesApp", "Save failed", e);
        }
    }
}