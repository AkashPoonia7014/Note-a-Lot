package com.sky.note_a_lot.firebase;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

//import com.example.myloadingbutton.MyLoadingButton;
import com.example.myloadingbutton.MyLoadingButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sky.note_a_lot.R;
import com.sky.note_a_lot.activities.MainActivity;
import com.sky.note_a_lot.trash.FinishAnyActivityCallback;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.Objects;

import maes.tech.intentanim.CustomIntent;

public class LoginActivity extends AppCompatActivity implements MyLoadingButton.MyLoadingButtonClick{

    MyLoadingButton loginLoadingButton;
    private TextView loginOrFunButton;
    private EditText inputLoginEmailId, inputLoginPassword;
    private LinearLayout layoutContinueWithGoogle;
    private FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    Dialog dialog;
    public static final String NEW_EXTRA_MESSAGE = "com.sky.LOGIN";
    public static final String ID_TOKEN_SUPABASE = "com.sky.LOGIN.ID_TOKEN";
    public static final String EMAIL_LOGIN_MESSAGE = "com.sky.EMAIL_LOGIN";

    private static FinishAnyActivityCallback finishAnyActivityCallback, finishMainActivityCallback;
    public static void setFinishCreateAccountActivityCallback(FinishAnyActivityCallback callback) {
        finishAnyActivityCallback = callback;
    }
    public static void setFinishMainActivityCallback(FinishAnyActivityCallback callback) {
        finishMainActivityCallback = callback;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
        initView();
        setActionOnViews();
        showProgressDialog();
        findViewById(R.id.loginOrButtonFun).setOnClickListener(v -> showCustomToast("Or sab ghara hi", Toast.LENGTH_SHORT));
    }
    private void initView() {
        inputLoginEmailId = findViewById(R.id.inputLoginEmailId);
        inputLoginPassword = findViewById(R.id.inputLoginPassword);
        loginLoadingButton = findViewById(R.id.loginLoadingButton);
        layoutContinueWithGoogle = findViewById(R.id.loginLayoutContinueWithGoogle);
        loginLoadingButton.showNormalButton();

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setActionOnViews() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {finish();}
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        findViewById(R.id.imageLoginBackButton).setOnClickListener(view -> callback.handleOnBackPressed());

        loginLoadingButton.setMyButtonClickListener(this);
        loginLoadingButton.setAnimationDuration(300)
                .setButtonColor(R.color.colorAccent)
                .setButtonLabel("LOGIN")
                .setButtonLabelSize(15)
                .setProgressLoaderColor(R.color.black)
                .setButtonLabelColor(R.color.colorPrimary)
                .setProgressDoneIcon(getResources().getDrawable(R.drawable.ic_round_done))
                .setProgressErrorIcon(getResources().getDrawable(R.drawable.ic_round_close))
                .setNormalAfterError(true);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, options);
        layoutContinueWithGoogle.setOnClickListener(v -> {
            dialog.show();
            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent);
        });

        findViewById(R.id.createTvButton).setOnClickListener(view -> {
            UIUtil.hideKeyboard(LoginActivity.this);
            if (finishAnyActivityCallback != null ) {
                finishAnyActivityCallback.finishAnyActivity();
                Log.d("Login", "Create Activity finished");
            }
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            CustomIntent.customType(LoginActivity.this, "right-to-left");
            finish();
        });

        findViewById(R.id.loginHideKeyboard).setOnClickListener(view -> UIUtil.hideKeyboard(LoginActivity.this));

        inputLoginEmailId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginLoadingButton.showNormalButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
//                                    if (finishAnyActivityCallback != null ) {
//                                        finishAnyActivityCallback.finishAnyActivity();
//                                        Log.d("Login", "Create Activity finished");
//                                    }
//                                    if (finishMainActivityCallback !=null) {
//                                        finishMainActivityCallback.finishAnyActivity();
//                                        Log.d("Login", "Main Activity finished");
//                                    }
                                    dialog.dismiss();
                                    showCustomToast("Firebase authentication successful", Toast.LENGTH_SHORT);
                                    startActivity(
                                            new Intent(LoginActivity.this, MainActivity.class)
                                                    .putExtra(NEW_EXTRA_MESSAGE, "Account Created")
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
                        showCustomToast("Some error occurred", Toast.LENGTH_LONG);
                        dialog.dismiss(); // Hide loading dialog if the result code is not OK
                    }
                }
            });

    private void showProgressDialog() {
        dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.layout_progress_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);
    }

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

    @Override
    public void onMyLoadingButtonClick() {
        UIUtil.hideKeyboard(LoginActivity.this);
        loginUser();
    }

    public void loginUser(){
        String email  = inputLoginEmailId.getText().toString();
        String password  = inputLoginPassword.getText().toString();

        boolean isValidated = validateData(email,password);
        if(!isValidated){
            showCustomToast("Fucking can't even type the credentials correct, Pogo dekh",Toast.LENGTH_LONG);
            return;
        }
        loginAccountInFirebase(email,password);
    }

    public void loginAccountInFirebase(String email,String password){
        loginLoadingButton.showLoadingButton();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    if(Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified()){
                        loginLoadingButton.showDoneButton();

                        if (finishAnyActivityCallback != null ) {
                            finishAnyActivityCallback.finishAnyActivity();
                            Log.d("Login", "Create Activity finished");
                        }
                        if (finishMainActivityCallback !=null) {
                            finishMainActivityCallback.finishAnyActivity();
                            Log.d("Login", "Main Activity finished");
                        }
                        showCustomToast(" Success! ", Toast.LENGTH_SHORT);
                        startActivity(new Intent(LoginActivity.this,MainActivity.class)
                                .putExtra(EMAIL_LOGIN_MESSAGE, "isAEmailLogin"));
                        finish();
                    }else{
                        loginLoadingButton.showErrorButton();
                        showCustomToast("Email not verified, Please verify your email.", Toast.LENGTH_LONG);
                    }

                }else{
                    loginLoadingButton.showErrorButton();
                    showCustomToast(Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_LONG);
                }
            }
        });
    }

    public boolean validateData(String email,String password){
        //validate the data that are input by user.

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            inputLoginEmailId.setError("Email is invalid");
            return false;
        }
        if(password.length()<6){
            inputLoginPassword.setError("Password length is invalid");
            return false;
        }
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(LoginActivity.this, "right-to-left");
    }
}