package com.sky.note_a_lot.firebase;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myloadingbutton.MyLoadingButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.sky.note_a_lot.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sky.note_a_lot.activities.MainActivity;
import com.sky.note_a_lot.trash.FinishAnyActivityCallback;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.Objects;

import maes.tech.intentanim.CustomIntent;


public class CreateAccountActivity extends AppCompatActivity implements MyLoadingButton.MyLoadingButtonClick, FinishAnyActivityCallback{

    private EditText inputEmailId, inputPassword, inputConfirmPassword ;
    private LinearLayout layoutGoogle;
    private MyLoadingButton createAccountButton;
    private TextView loginTvButton, orButtonFun;
    private ImageView imageBackButton;
    private FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    private Dialog dialog;
    public static final String EXTRA_MESSAGE = "com.sky.ACCOUNT CREATED";

    // Static reference to the callback interface
    private static FinishAnyActivityCallback finishAnyActivityCallback;
    // Static method to set the callback from MainActivity
    public static void setFinishMainActivityCallback(FinishAnyActivityCallback callback) {
        finishAnyActivityCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        auth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
        initViews();
        setActionOnViews();
        showProgressDialog();
        findViewById(R.id.orButtonFun).setOnClickListener(v -> showCustomToast("Or sab badiya, tu bata", Toast.LENGTH_SHORT));
    }

    private void initViews() {
        imageBackButton = findViewById(R.id.imageBackButton);
        inputEmailId = findViewById(R.id.inputEmailId);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        layoutGoogle =  findViewById(R.id.layoutContinueWithGoogle);
        loginTvButton = findViewById(R.id.loginTvButton);
        createAccountButton= findViewById(R.id.createAccountButton);
        createAccountButton.showNormalButton();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setActionOnViews() {
        // ALTERNATIVE FOR onBackPressed(); METHOD
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };// Add the callback to the OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);

        imageBackButton.setOnClickListener(v -> {// Trigger the back button behavior
            callback.handleOnBackPressed();
        });


        createAccountButton.setMyButtonClickListener(this);
        createAccountButton.setAnimationDuration(300)
                .setButtonColor(R.color.colorAccent)
                .setButtonLabel("CREATE ACCOUNT")
                .setButtonLabelSize(15)
                .setProgressLoaderColor(R.color.black)
                .setButtonLabelColor(R.color.colorPrimary)
                .setProgressDoneIcon(getResources().getDrawable(R.drawable.ic_round_done))
                .setProgressErrorIcon(getResources().getDrawable(R.drawable.ic_round_close))
                .setNormalAfterError(true);

        loginTvButton.setOnClickListener(v->{
            UIUtil.hideKeyboard(CreateAccountActivity.this);
            LoginActivity.setFinishCreateAccountActivityCallback(this);
            startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
            CustomIntent.customType(CreateAccountActivity.this, "left-to-right");
        });

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(CreateAccountActivity.this, options);

        layoutGoogle.setOnClickListener(v -> {
            dialog.show();
            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent);
        });
        findViewById(R.id.hideKeyboard).setOnClickListener(view -> UIUtil.hideKeyboard(CreateAccountActivity.this));


    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult()
            , new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    dialog.show();

                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                            auth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
                                dialog.dismiss();
                                if (task.isSuccessful()) {
                                    if (finishAnyActivityCallback != null) {

                                        finishAnyActivityCallback.finishAnyActivity();  // Notify MainActivity to finish
                                        Log.d("Login", "Activity finished");
                                    }
                                    dialog.dismiss();
                                    // When task is successful redirect to profile activity display Toast
                                    showCustomToast("Firebase authentication successful", Toast.LENGTH_SHORT);

                                    startActivity(
                                            new Intent(CreateAccountActivity.this, MainActivity.class)
                                                    .putExtra(EXTRA_MESSAGE, "Account Created")
                                    );
                                    finish();

                                } else {
                                    // When task is unsuccessful display Toast
                                    showCustomToast("Authentication Failed :" + Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_LONG);
                                }
                            });
                        } catch (ApiException e) {
                            Log.d("Tag", Objects.requireNonNull(e.getMessage()));
                            dialog.dismiss();  // Dismiss dialog if an exception occurs
                            showCustomToast("Google Sign-in Failed"+Objects.requireNonNull(e.getMessage()), Toast.LENGTH_LONG);
                        }
                    } else {
                        showCustomToast("Some error occurred"+ result.getResultCode(), Toast.LENGTH_LONG);
                        dialog.dismiss(); // Hide loading dialog if the result code is not OK
                    }
                }
            });

    private void showCustomToast(String message, int toastLength) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = (TextView) layout.findViewById(R.id.toastText);

        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.setDuration(toastLength);
        toast.setView(layout);
        toast.show();
    }

    private void showProgressDialog() {
        dialog = new Dialog(CreateAccountActivity.this);
        dialog.setContentView(R.layout.layout_progress_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);
    }


    @Override
    public void onMyLoadingButtonClick() {
        UIUtil.hideKeyboard(CreateAccountActivity.this);
        createAccount();
    }

    public void createAccount () {
        String email = inputEmailId.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        boolean isValidated = validateData(email, password, confirmPassword);
        if (!isValidated) {
            createAccountButton.showProgressToNormalButton();
            return;
        }

        createAccountInFirebase(email, password);
    }

    public boolean validateData(String email, String password, String confirmPassword) {
        // To validate data input by the user

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            inputEmailId.setError("Email is invalid");
            return false;
        }
        if(password.length()<6){
            inputPassword.setError("Password length is too short");
            return false;
        }
        if(!password.equals(confirmPassword)){
            inputConfirmPassword.setError("Password not matched");
            return false;
        }
        return true;
    }

    public void createAccountInFirebase(String email, String password) {
        createAccountButton.showLoadingButton();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(CreateAccountActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            createAccountButton.showDoneButton();
                            //creating acc is done
                            showCustomToast("Success! Please verify your email.", Toast.LENGTH_LONG);

                            Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification();
                            firebaseAuth.signOut();
                            finish();
                        }else{
                            //failure
                            createAccountButton.showErrorButton();
                            showCustomToast(Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_LONG);
                        }
                    }
                }
        );
    }

    @Override
    public void finishAnyActivity() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(CreateAccountActivity.this, "right-to-left");
    }
}