package net.stuple.simplenotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import net.stuple.simplenotes.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    public String[] files = {"note1.md"};
    public int whatfile = 0;
    private File folder;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GithubUpdateChecker.checkForUpdate(this, exitActivity -> start());

    }
    private void start() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.textView4.setText("Note: "+files[whatfile]);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(

                navController.getGraph().getId()
        ).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


//        SharedPreferences mainPrefs = getSharedPreferences("main_preference", Context.MODE_PRIVATE);
//        CheckBox checkBox = findViewById(R.id.automaticUpdateCheckBox);
//        checkBox.setChecked(mainPrefs.getBoolean("show_update_alert", true));
//        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> mainPrefs.edit().putBoolean("show_update_alert", isChecked).apply());
    }
    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

}