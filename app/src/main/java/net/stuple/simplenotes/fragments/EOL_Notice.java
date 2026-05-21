package net.stuple.simplenotes.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.stuple.simplenotes.R;
import net.stuple.simplenotes.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;

public class EOL_Notice extends Fragment {

    public EOL_Notice() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_e_o_l__notice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView eolTextView = view.findViewById(R.id.eol_text);

        try (InputStream is = requireContext().getAssets().open("eol-notice.txt")) {
            byte[] content = FileUtil.readStream(is);
            eolTextView.setText(new String(content));
        } catch (IOException e) {
            Log.e("EOL_Notice", "Error loading eol-notice.txt from assets", e);
            eolTextView.setText("Error: Could not load the notice.");
        }
    }
}
