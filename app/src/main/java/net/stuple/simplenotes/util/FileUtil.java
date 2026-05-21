package net.stuple.simplenotes.util;

import android.content.Context;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public final class FileUtil {

    public static final String[] SUPPORTED_EXTENSIONS = {".md", ".txt", ".note", ".html"};

    public static byte[] readFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return readStream(fis);
        }
    }

    public static byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = is.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }

    public static void writeFile(File file, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

    public static byte[] loadNote(Context context, Uri notesFolderUri, String fileName) throws IOException {
        DocumentFile notesDir = DocumentFile.fromTreeUri(context, notesFolderUri);
        DocumentFile noteFile = (notesDir != null) ? notesDir.findFile(fileName) : null;

        if (noteFile != null && noteFile.exists()) {
            try (InputStream is = context.getContentResolver().openInputStream(noteFile.getUri())) {
                return readStream(is);
            }
        } else {
            try (InputStream is = context.getAssets().open(fileName)) {
                return readStream(is);
            }
        }
    }

    public static void saveNote(Context context, Uri notesFolderUri, String fileName, String content) throws IOException {
        DocumentFile notesDir = DocumentFile.fromTreeUri(context, notesFolderUri);
        if (notesDir == null) {
            throw new IOException("Notes directory not found or is invalid.");
        }

        DocumentFile noteFile = notesDir.findFile(fileName);
        if (noteFile == null || !noteFile.exists()) {
            String mimeType = "text/markdown";
            if (fileName.endsWith(".txt")) mimeType = "text/plain";
            else if (fileName.endsWith(".html")) mimeType = "text/html";
            
            noteFile = notesDir.createFile(mimeType, fileName);
            if (noteFile == null) {
                throw new IOException("Failed to create note file.");
            }
        }

        try (OutputStream os = context.getContentResolver().openOutputStream(noteFile.getUri())) {
            Objects.requireNonNull(os).write(content.getBytes());
        }
    }

    public static String[] refreshLocalFiles(Context context, Uri notesFolderUri) {
        DocumentFile notesDir = DocumentFile.fromTreeUri(context, notesFolderUri);
        if (notesDir == null || !notesDir.isDirectory()) {
            return new String[]{"FirstNote.md"};
        }

        ArrayList<String> fileList = new ArrayList<>();
        for (DocumentFile file : notesDir.listFiles()) {
            if (file.isFile() && file.getName() != null) {
                String name = file.getName().toLowerCase();
                for (String ext : SUPPORTED_EXTENSIONS) {
                    if (name.endsWith(ext)) {
                        fileList.add(file.getName());
                        break;
                    }
                }
            }
        }

        if (!fileList.isEmpty()) {
            return fileList.toArray(new String[0]);
        } else {
            return new String[]{"FirstNote.md"};
        }
    }

    public static boolean deleteNote(Context context, Uri notesFolderUri, String fileName) {
        DocumentFile notesDir = DocumentFile.fromTreeUri(context, notesFolderUri);
        if (notesDir != null) {
            DocumentFile noteFile = notesDir.findFile(fileName);
            if (noteFile != null && noteFile.exists()) {
                return noteFile.delete();
            }
        }
        return false;
    }

    public static boolean renameNote(Context context, Uri notesFolderUri, String oldName, String newName) {
        DocumentFile notesDir = DocumentFile.fromTreeUri(context, notesFolderUri);
        if (notesDir != null) {
            DocumentFile noteFile = notesDir.findFile(oldName);
            if (noteFile != null && noteFile.exists()) {
                return noteFile.renameTo(newName);
            }
        }
        return false;
    }

    public static void exportNote(Context context, Uri sourceUri, Uri destinationUri) throws IOException {
        try (InputStream is = context.getContentResolver().openInputStream(sourceUri);
             OutputStream os = context.getContentResolver().openOutputStream(destinationUri)) {
            if (is == null || os == null) throw new IOException("Failed to open stream");
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
        }
    }

    public static void importNote(Context context, Uri sourceUri, Uri notesFolderUri, String fileName) throws IOException {
        DocumentFile notesDir = DocumentFile.fromTreeUri(context, notesFolderUri);
        if (notesDir == null) throw new IOException("Notes folder not found");

        DocumentFile targetFile = notesDir.findFile(fileName);
        if (targetFile == null) {
             String mimeType = "text/markdown";
            if (fileName.endsWith(".txt")) mimeType = "text/plain";
            targetFile = notesDir.createFile(mimeType, fileName);
        }
        if (targetFile == null) throw new IOException("Failed to create target file");

        try (InputStream is = context.getContentResolver().openInputStream(sourceUri);
             OutputStream os = context.getContentResolver().openOutputStream(targetFile.getUri())) {
            if (is == null || os == null) throw new IOException("Failed to open stream");
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
        }
    }
}
