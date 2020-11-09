package info.pkappstudio.tasko.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import info.pkappstudio.tasko.R;
import info.pkappstudio.tasko.customclasses.CustomProgress;

import maes.tech.intentanim.CustomIntent;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private int RC_SIGN_IN = 1;
    private View mContextView;
    private FirebaseUser mCurrentUser;
    private CustomProgress customProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();

        mContextView = findViewById(R.id.linear_layout);

        customProgress = new CustomProgress(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void signInWithGoogle(View view) {
        customProgress.startProgress();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                customProgress.stopProgress();
                String message = "Sign in with google unsuccessful";
                showSnakbar(message);
                Log.i("Login_Error", e.toString());
            }
        }
    }

    private void firebaseAuthWithGoogle(String token) {
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    customProgress.stopProgress();
                    String message = "Sign in with google successful";
                    showSnakbar(message);
                    mCurrentUser = mAuth.getCurrentUser();
                    openMainActivity(mCurrentUser);
                } else {
                    customProgress.stopProgress();
                    String message = task.getException().getMessage();
                    showSnakbar(message);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mCurrentUser = mAuth.getCurrentUser();
        openMainActivity(mCurrentUser);
    }

    private void openMainActivity(FirebaseUser mCurrentUser) {
        if (mCurrentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            CustomIntent.customType(LoginActivity.this, "left-to-right");
            finish();
        }
    }

    private void showSnakbar(String message) {
        Snackbar.make(mContextView, message, Snackbar.LENGTH_SHORT)
                .show();
    }

    public void skipLogin(View view) {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        CustomIntent.customType(LoginActivity.this, "left-to-right");
        finish();
    }
}
