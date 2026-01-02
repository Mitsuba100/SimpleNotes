package net.stuple.simplenotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import net.stuple.simplenotes.SettingsFragment;
import net.stuple.simplenotes.databinding.SettingsFragmentBinding;

public class GithubUpdateChecker {
    public String currentVersion = "1.1";


    public static SharedPreferences getMainPreference(Context context) {
        return context.getSharedPreferences("main_preference", Context.MODE_PRIVATE);
    }

    public static void checkForUpdate(Activity context, onFinishedUpdateRequest listener) {

        if (!getMainPreference(context).getBoolean("show_update_alert", true)) {
            listener.onFinishedUpdateRequest(false); // Continue if updates are disabled
            return;
        }

        Request request = new Request.Builder()
                .url("https://api.github.com/repos/Mitsuba100/SimpleNotes/releases/latest")
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                context.runOnUiThread(() -> listener.onFinishedUpdateRequest(false));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    String latestVersion = json.replaceAll(".*\"tag_name\"\\s*:\\s*\"([^\"]+)\".*", "$1");

                    String currentVersion = "1.1";
                    try {
                        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                        currentVersion = pInfo.versionName;
                    } catch (PackageManager.NameNotFoundException ignored) {}

                    final String finalCurrentVersion = currentVersion;

                    // IF VERSIONS ARE DIFFERENT
                    if (!latestVersion.trim().equals(currentVersion.trim())) {
                        context.runOnUiThread(() -> {
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.update_alert_update_available)
                                    .setMessage(context.getString(R.string.update_alert_text) + "\n" +
                                            context.getString(R.string.update_alert_current_version, finalCurrentVersion) + "\n" +
                                            context.getString(R.string.update_alert_latest_version, latestVersion))
                                    .setPositiveButton(R.string.update_alert_option_show_version, (dialog, which) -> {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Mitsuba100/SimpleNotes/releases/latest"));
                                        context.startActivity(intent);
                                        listener.onFinishedUpdateRequest(true);
                                    })
                                    .setNeutralButton(R.string.update_alert_option_dont_show_again, (dialog, which) -> {
                                        getMainPreference(context).edit().putBoolean("show_update_alert", false).apply();
                                        listener.onFinishedUpdateRequest(false);
                                    })
                                    .setNegativeButton(R.string.update_alert_option_later, (dialog, which) ->
                                            listener.onFinishedUpdateRequest(false)) // Changed to false so app continues
                                    .setCancelable(false)
                                    .show();
                        });
                    }
                    // IF NO UPDATE IS NEEDED
                    else {
                        context.runOnUiThread(() -> listener.onFinishedUpdateRequest(false));
                    }
                } else {
                    // Response not successful (e.g. 404), continue to app
                    context.runOnUiThread(() -> listener.onFinishedUpdateRequest(false));
                }
            }
        });
    }

    public interface onFinishedUpdateRequest {
        void onFinishedUpdateRequest(boolean exitActivity);

    }
}
