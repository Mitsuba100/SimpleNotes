package net.stuple.simplenotes;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import net.stuple.simplenotes.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GithubUpdateChecker.checkForUpdate(this, new GithubUpdateChecker.onFinishedUpdateRequest() {
            @Override
            public void onFinishedUpdateRequest(boolean exit) {
                if (!exit) {
                    start();
                }
            }
        });
    }

    public void start() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((navController1, destination, bundle) -> {
            if (destination.getId() == R.id.settingsFragment) {
                binding.floatingActionButton2.setVisibility(View.GONE);
            } else {
                binding.floatingActionButton2.setVisibility(View.VISIBLE); // ist so weil ich davor das problem hatte wo es gecrashed ist weil der Button noch immer da war
            }
        });

        binding.floatingActionButton2.setOnClickListener(v -> {
            if (navController.getCurrentDestination().getId() == R.id.noteFragment) {
                navController.navigate(R.id.action_noteFragment_to_settingsFragment);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
