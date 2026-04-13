package our.cse476.application;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import our.cse476.application.databinding.ActivityLoginBinding;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginOnClick();
            }
        });

        binding.createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccountOnClick();
            }
        });
    }

    private void loginOnClick() {
        String username = binding.username.getText().toString();
        String password = binding.password.getText().toString();

        RemoteStorage remoteStorage = new RemoteStorage();
        remoteStorage.downloadUser(username, password, new RemoteStorage.DownloadCallback() {
            @Override
            public void onSuccess(Map<String, Object> userData) {
                remoteStorage.LogDatabase("users");

                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                intent.putExtra("EXTRA_USERNAME", username);
                intent.putExtra("EXTRA_PASSWORD", password);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Download User Failed: " + e.getMessage());
                // Show error message
                Toast.makeText(LoginActivity.this, "Username or passworda incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAccountOnClick() {
        String username = binding.username.getText().toString();
        String password = binding.password.getText().toString();

        Map<String, Object> combo = new HashMap<>();
        combo.put("username", username);
        combo.put("password", password);

        RemoteStorage remoteStorage = new RemoteStorage();
        remoteStorage.uploadUser(combo, new RemoteStorage.UploadCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Upload User Successful");
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                intent.putExtra("EXTRA_USERNAME", username);
                intent.putExtra("EXTRA_PASSWORD", password);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Upload User Failed: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", binding.username.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        binding.username.setText(savedInstanceState.getString("username"));
    }
}