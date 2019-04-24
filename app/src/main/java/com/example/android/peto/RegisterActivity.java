package com.example.android.peto;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText mEmailRegister;
    private EditText mPasswordRegister;
    private EditText mConfirmPassword;
    private Button mRegisterBt;
    private Button mLoginBt;
    private ProgressBar mRegisterPb;
    private Toolbar mToolbar;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailRegister = (EditText) findViewById(R.id.et_email_register);
        mPasswordRegister = (EditText) findViewById(R.id.ed_password_register);
        mConfirmPassword = (EditText) findViewById(R.id.et_confirm_password_register);
        mRegisterBt = (Button) findViewById(R.id.bt_register);
        mLoginBt = (Button) findViewById(R.id.bt_have_account);
        mRegisterPb = (ProgressBar) findViewById(R.id.register_progressBar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_register);

        setSupportActionBar(mToolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();


        mLoginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToLoginActivity();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            sentToMainActivity();
        } else {
            mRegisterBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = mEmailRegister.getText().toString();
                    String password = mPasswordRegister.getText().toString();
                    String confirmPassword = mConfirmPassword.getText().toString();
                    if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
                        if (confirmPassword.equals(password)) {
                            mRegisterPb.setVisibility(View.VISIBLE);

                            mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        String errorMsg = task.getException().getMessage();
                                        Toast.makeText(RegisterActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                    mRegisterPb.setVisibility(View.INVISIBLE);
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, getString(R.string.error_confirm_msg), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

    }

    private void sentToMainActivity() {

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
