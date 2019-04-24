package com.example.android.peto;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText mLogingEmailText;
    private EditText mLogingPasswordText;
    private Button mLogingButton;
    private Button mRegesterButton;
    private ProgressBar mLoginPB;


    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLogingEmailText = (EditText) findViewById(R.id.et_email_register);
        mLogingPasswordText = (EditText) findViewById(R.id.ed_password_register);
        mLogingButton = (Button) findViewById(R.id.bt_register);
        mRegesterButton = (Button) findViewById(R.id.bt_have_account);
        mLoginPB = (ProgressBar) findViewById(R.id.login_progressBar);

        // Firebase instance
        mFirebaseAuth = FirebaseAuth.getInstance();


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMainActivity();
        } else {

            mLogingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String loginEmail = mLogingEmailText.getText().toString();
                    String logingPassword = mLogingPasswordText.getText().toString();

                    if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(logingPassword)) {
                        mLoginPB.setVisibility(View.VISIBLE);

                        // Sing in and check if complete
                        mFirebaseAuth.signInWithEmailAndPassword(loginEmail, logingPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    sendToMainActivity();
                                } else {
                                    String errorMsg = task.getException().getMessage();
                                    Toast.makeText(LoginActivity.this, "error: " + errorMsg, Toast.LENGTH_LONG).show();
                                }
                                mLoginPB.setVisibility(View.INVISIBLE);

                            }
                        });
                    }
                }
            });

            // Move to Register activity
            mRegesterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
