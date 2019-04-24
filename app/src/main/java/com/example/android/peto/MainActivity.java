package com.example.android.peto;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FloatingActionButton mAddNewPostFB;
    private BottomNavigationView bottomNavigationView;

    private String mUserId;

    // Firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;

    private HomeFragment mHomeFragment;
    private AccountFragment mAccountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddNewPostFB = (FloatingActionButton) findViewById(R.id.add_post_fb);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.app_name));

        // Firebase instance
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = mFirebaseFirestore.getInstance();


        if (mFirebaseAuth.getCurrentUser() != null) {
            //Fragment
            mHomeFragment = new HomeFragment();
            mAccountFragment = new AccountFragment();


            bottomNavigationView = findViewById(R.id.bottomNavigationView);

            initializeFragment();
            mAddNewPostFB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(intent);
                }
            });

            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
                    switch (item.getItemId()) {
                        case R.id.bottom_action_home:
                            replaceFragment(mHomeFragment, currentFragment);
                            return true;



                        case R.id.bottom_action_account:
                            replaceFragment(mAccountFragment, currentFragment);
                            return true;

                        default:
                            return false;
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Sign in

            mUserId = mFirebaseAuth.getCurrentUser().getUid();
            mFirebaseFirestore.collection("Users").document(mUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        String errorMsg = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Sign out
            sendToLoginActivity();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void sendToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void logout() {
        mFirebaseAuth.signOut();
        sendToLoginActivity();
    }


    private void initializeFragment() {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, mHomeFragment);
        fragmentTransaction.add(R.id.main_container, mAccountFragment);

        fragmentTransaction.hide(mAccountFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment == mHomeFragment) {

            fragmentTransaction.hide(mAccountFragment);

        }

        if (fragment == mAccountFragment) {

            fragmentTransaction.hide(mHomeFragment);
        }


        fragmentTransaction.show(fragment);

        fragmentTransaction.commit();

    }
}
