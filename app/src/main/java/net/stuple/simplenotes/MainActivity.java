package net.stuple.simplenotes;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import net.stuple.simplenotes.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    Scanner sc = new Scanner(System.in);
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    String [] files = {"note1.md"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String currentFile = files[0];
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String cleanName = currentFile.substring(currentFile.lastIndexOf("/") + 1);

        cleanName = cleanName.substring(0, 1).toUpperCase() + cleanName.substring(1);
        binding.filename2.setText(cleanName);

        setSupportActionBar(binding.toolbar);
        try {
            // 1. Open the file from assets
            InputStream is = getAssets().open("note1.md");
            Scanner sc = new Scanner(is);

            // 2. Create a StringBuilder to hold all the lines
            StringBuilder allContent = new StringBuilder();

            // 3. Loop through the file and collect the text
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Log.d("NotesApp", "Read line: " + line);

                // Append the line AND a newline character so it doesn't all bunch up
                allContent.append(line).append("\n");
            }

            // 4. Finally, set the text once with the complete content
            binding.contentMain.textviewNoteContent.setText(allContent.toString());

            sc.close();
            is.close();

        } catch (IOException e) {
            Log.e("NotesApp", "Error reading file", e);
        }


    }
}