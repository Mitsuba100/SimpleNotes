package net.stuple.simplenotes.util;

import android.content.Context;
import android.net.Uri;

import net.stuple.simplenotes.fragments.NoteFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManagment {
    public void FileCreator(Context context, String fileName, onFileCreatedListener listener) {
        // Ensure the file extension is correct
        if (!fileName.endsWith(".md")) {
            fileName += ".md";
        }

        // 1. Get the path to the "Notes" subfolder
        File baseDir = context.getExternalFilesDir(null);
        File notesFolder = new File(baseDir, "Notes");

        // 2. Create the "Notes" folder if it's missing
        if (!notesFolder.exists()) {
            notesFolder.mkdirs();
        }

        File newFile = new File(notesFolder, fileName);

        try {
            // 3. Create the actual file
            if (newFile.createNewFile()) {
                // Force write 0 bytes to ensure the OS registers it immediately
                FileOutputStream fos = new FileOutputStream(newFile);
                fos.write(new byte[0]);
                fos.flush();
                fos.close();

                listener.onSuccess(fileName);
            } else {
                listener.onError("File already exists!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            listener.onError("Failed to create file: " + e.getMessage());
        }
    }


    public interface onFileCreatedListener {
        void onSuccess(String fileName);
        void onError(String error);
    }
}
