package net.stuple.simplenotes.util;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManagment {

    // Define supported extensions
    private static final String[] SUPPORTED_EXTENSIONS = {".note", ".txt", ".md"};

    public void FileCreator(Context context, String fileName, onFileCreatedListener listener) {
        boolean hasExtension = false;
        
        // Check if the fileName already ends with a supported extension
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (fileName.toLowerCase().endsWith(ext)) {
                hasExtension = true;
                break;
            }
        }

        // Default to .md if no supported extension is found
        if (!hasExtension) {
            fileName += ".txt";
        }

        // Get the path to the "Notes" subfolder
        File baseDir = context.getExternalFilesDir(null);
        File notesFolder = new File(baseDir, "Notes");

        // Create the "Notes" folder if it's missing
        if (!notesFolder.exists()) {
            notesFolder.mkdirs();
        }

        File newFile = new File(notesFolder, fileName);

        try {
            if (newFile.createNewFile()) {
                // Force write 0 bytes to ensure the OS registers it immediately
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    fos.write(new byte[0]);
                    fos.flush();
                }
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
