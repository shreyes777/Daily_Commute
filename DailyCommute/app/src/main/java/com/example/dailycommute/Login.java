package com.example.dailycommute;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileOutputStream;
import java.io.IOException;

public class Login extends AppCompatActivity {

    private TextView register;
    private EditText emailField, passwordField;
    private Button loginButton;
    private SignInButton googleSignInButton;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    int RC_SIGN_IN = 20;
    public static String EMAIL="oK";
    public Intent send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);




        register = findViewById(R.id.signup_text_btn);
        emailField = findViewById(R.id.email_inputfield_login);
        passwordField = findViewById(R.id.password_inputfield_login);
        loginButton = findViewById(R.id.login_btn);
        googleSignInButton = findViewById(R.id.google_sign_in_button);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();



        googleSignInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        loginButton.setOnClickListener(v ->
        {
            loginUser();
        });
        register.setOnClickListener(v ->
        {
            startActivity(new Intent(Login.this, SignUp.class));
        });
    }

    private void signIn()
    {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful())
            {
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(Login.this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show();
                MainScreens();
            } else {
                Toast.makeText(Login.this, "Google Sign-In Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void MainScreens()
    {
        finish();
        Intent intent = new Intent(Login.this, MainScreen.class);
        startActivity(intent);
    }

    private void loginUser()
    {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            Toast.makeText(Login.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task ->
        {
            if (task.isSuccessful()) {
                String okay=emailField.getText().toString();
                //Trimming is required as @ or . are not allowed in "name"
                String replaced1=okay.replace(".","_");
                String replaced2=replaced1.replace("@","_");
                writeToFile("EM.txt",replaced2,Login.this);
                Toast.makeText(Login.this, replaced2, Toast.LENGTH_LONG).show();



                startActivity(new Intent(Login.this, MainScreen.class));
                finish();
            }
            else
            {
                Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void writeToFile(String fileName, String fileContents, Context context) {
        FileOutputStream spos = null;
        try {
            spos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            spos.write(fileContents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (spos != null) {
                try {
                    spos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
