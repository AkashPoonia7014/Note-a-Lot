package com.sky.note_a_lot.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sky.note_a_lot.R;
import com.sky.note_a_lot.adapters.NotesAdapter;
import com.sky.note_a_lot.database.NotesDatabase;
import com.sky.note_a_lot.entities.Note;
import com.sky.note_a_lot.firebase.CreateAccountActivity;
import com.sky.note_a_lot.firebase.LoginActivity;
import com.sky.note_a_lot.listeners.NotesListener;
import com.sky.note_a_lot.trash.FinishAnyActivityCallback;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import maes.tech.intentanim.CustomIntent;

public class MainActivity extends AppCompatActivity implements NotesListener, NavigationView.OnNavigationItemSelectedListener, FinishAnyActivityCallback {

    //    String abhiImagePath;
//    private NoteRepo mNoteRepository;
//    private NotesDatabase db;
//    String finalAbhiImage;
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;
    public static final String EXTRA_MESSAGE = "com.sky.ACCOUNT CREATED";
    public static final String NEW_EXTRA_MESSAGE = "com.sky.LOGIN";
    public static final String EMAIL_LOGIN_MESSAGE = "com.sky.EMAIL_LOGIN";
    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private AlertDialog dialogAddURL;
    private int noteClickedPosition = -1;
    private TextView textEmpty;
    public static final String KEY_FIRST_LAUNCH = "com.example.note_a_lot.first.launch";
    private ImageView imageUser, imageEmpty, layoutStyle;
    private FloatingActionButton imageAddNoteMain;
    private BottomAppBar bottomAppBar;
    private EditText inputSearch;
    private DrawerLayout drawerLayout;
    private ConstraintLayout contentView;
    private NavigationView navigationView;
    private static final float END_SCALE = 0.8f;
    private int styleNumber, spanCount;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private AlertDialog dialogChangeTheme;
    private androidx.appcompat.view.ActionMode actionMode;
    private View navHeaderView;
    private ImageView  headerUserImage, headerAppIcon;
    private TextView headerUserName, headerUserEmail;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private String emailPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Retrieve the saved theme preference
        SharedPreferences sharedPreferences = getSharedPreferences("theme_preferences", MODE_PRIVATE);
        int nightMode = sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        // Apply the saved theme preference
        AppCompatDelegate.setDefaultNightMode(nightMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //        mNoteRepository = new NoteRepo(this);
        //        defaultFirstNote();

        initViews();
        setNavigationView();
        setActionOnViews();


        if (getIntent().getExtras() != null) {
            saveEmailLoginPreference(getIntent().getStringExtra(EMAIL_LOGIN_MESSAGE));
        }
        emailPreference = loadEmailLoginPreference();
        if (!Objects.equals(emailPreference, "isAEmailLogin")) {
            checkIfUserLoggedIn();
            setupAuthStateListener();
        } else {
            if (auth.getCurrentUser() != null)
                headerUserEmail.setText(auth.getCurrentUser().getEmail());
        }


        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_MESSAGE) || Objects.requireNonNull(intent).hasExtra(NEW_EXTRA_MESSAGE)) {
            if (!drawerLayout.isDrawerVisible(GravityCompat.END))
                drawerLayout.openDrawer(GravityCompat.END);
        }

    }

    private void initViews() {
        drawerLayout = findViewById(R.id.mainDrawerLayout);
        navigationView = findViewById(R.id.mainNavigationView);
        contentView = findViewById(R.id.contentView);
        inputSearch =findViewById(R.id.inputSearch);
        imageUser = findViewById(R.id.imageUser);
        imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageEmpty = findViewById(R.id.imageEmpty);
        textEmpty = findViewById(R.id.textEmpty);
        layoutStyle = findViewById(R.id.layoutStyle);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        navHeaderView = navigationView.getHeaderView(0);
        headerUserImage = navHeaderView.findViewById(R.id.navViewUserImage);
        headerAppIcon = navHeaderView.findViewById(R.id.navViewAppIcon);
        headerUserName = navHeaderView.findViewById(R.id.navViewUserName);
        headerUserEmail = navHeaderView.findViewById(R.id.navViewEmail);

        auth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setActionOnViews() {

        KeyboardVisibilityEvent.setEventListener(MainActivity.this, isOpen -> {
            if (!isOpen) {
                inputSearch.clearFocus();
            }
        });

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!noteList.isEmpty()) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });
        settingRecyclerView();

        if (spanCount == 1) {
            layoutStyle.setImageResource(R.drawable.ic_linear_layout);
        } else {
            layoutStyle.setImageResource(R.drawable.ic_grid_layout);
        }

        layoutStyle.setOnClickListener(v -> {

            UIUtil.hideKeyboard(MainActivity.this);
            // Toggle span count between 1 and 2
            if (staggeredGridLayoutManager.getSpanCount() == 1) {
                staggeredGridLayoutManager.setSpanCount(2);
                layoutStyle.setImageResource(R.drawable.ic_grid_layout);
            } else {
                staggeredGridLayoutManager.setSpanCount(1);
                layoutStyle.setImageResource(R.drawable.ic_linear_layout);
            }
            notesAdapter.notifyDataSetChanged();
            saveSpanCountPreference(staggeredGridLayoutManager.getSpanCount());

        });

        imageAddNoteMain.setOnClickListener(v -> {
            UIUtil.hideKeyboard(MainActivity.this);
            startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class),REQUEST_CODE_ADD_NOTE);
            CustomIntent.customType(MainActivity.this, "left-to-right");
            inputSearch.setText(null);
        });

        bottomAppBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.imageAddNote) {
                UIUtil.hideKeyboard(MainActivity.this);
                startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE);
                CustomIntent.customType(MainActivity.this, "bottom-to-up");
                inputSearch.setText(null);
            } else if (itemId == R.id.imageAddImage) {
                UIUtil.hideKeyboard(MainActivity.this);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                REQUEST_CODE_STORAGE_PERMISSION);
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_STORAGE_PERMISSION);
                    }
                } else {
                    selectImage();
                }
            } else if (itemId == R.id.imageAddWebLink) {
                showAddURLDialog();
            }
            return false;
        });

        findViewById(R.id.textMyNotes).setOnClickListener(view -> UIUtil.hideKeyboard(MainActivity.this));
    }

    private void settingRecyclerView() {

        spanCount = loadSpanCountPreference();
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        notesRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(MainActivity.this,noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);
    }

    private void saveSpanCountPreference(int spanCount) {
        SharedPreferences sharedPreferences = getSharedPreferences("LayoutPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("spanCount", spanCount);
        editor.apply();
    }

    private int loadSpanCountPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("LayoutPreferences", MODE_PRIVATE);
        return sharedPreferences.getInt("spanCount", 2); // Default is 2 (two columns)
    }

    private void setNavigationView() {
        Objects.requireNonNull(navigationView.getMenu().findItem(R.id.menu_website).getIcon()).setColorFilter(Color.BLACK,PorterDuff.Mode.SRC_IN);
        if (auth.getCurrentUser() != null) {
            navigationView.getMenu().findItem(R.id.menu_signIn_logIn).setTitle("Sign out");
        } else {
            navigationView.getMenu().findItem(R.id.menu_signIn_logIn).setTitle(R.string.sign_in_log_in);
        }

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        imageUser.setOnClickListener(v -> {
            UIUtil.hideKeyboard(MainActivity.this);
            if (drawerLayout.isDrawerVisible(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
        });
        animateNavigationDrawer();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int menuItemId = item.getItemId();
        if (menuItemId == R.id.menu_signIn_logIn) {
            if (auth.getCurrentUser() != null) {
                // User is signed in, perform sign-out
                signOutUser();
            } else { // user is not signed in
                // Set the callback for CreateAccountActivity
                CreateAccountActivity.setFinishMainActivityCallback(this);
                LoginActivity.setFinishMainActivityCallback(this);

                startActivity(new Intent(MainActivity.this, CreateAccountActivity.class));
                CustomIntent.customType(MainActivity.this, "left-to-right");
                inputSearch.setText(null);

            }

        } else if (menuItemId == R.id.menu_sync) {
            showCustomToast("Saas to len de be", Toast.LENGTH_SHORT);
            drawerLayout.closeDrawer(GravityCompat.END);

        } else if (menuItemId == R.id.menu_app_theme) {
            UIUtil.hideKeyboard(MainActivity.this);
            showChangeThemeDialog();
            inputSearch.setText(null);
            drawerLayout.closeDrawer(GravityCompat.END);

        } else if (menuItemId == R.id.menu_website) {
            String websiteUrl = "https://skyapps.vercel.app/";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl)));
            CustomIntent.customType(MainActivity.this, "fadein-to-fadeout");
            inputSearch.setText(null);
//            drawerLayout.closeDrawer(GravityCompat.END);
        }
        return false;
    }
    private void animateNavigationDrawer() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffsetDiff - xOffset;
                contentView.setTranslationX(xTranslation);
            }
        });
    }

    private void checkIfUserLoggedIn() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            updateUserUI(currentUser);  // Update with logged-in user info
        } else {
            resetUserUI();  // Reset UI for logged-out state
        }
    }

    private void updateUserUI(FirebaseUser user) {
        if (isDestroyed() || isFinishing()) {
            return;  // Don't go forward if activity is being destroyed
        }
        // Load user info into the views
        headerAppIcon.setVisibility(View.GONE);
        headerUserImage.setVisibility(View.VISIBLE);
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(imageUser);  // Load profile picture
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(headerUserImage);  // Load profile picture in navigation header
        }

//      Glide.with(MainActivity.this).load(Objects.requireNonNull(user.getPhotoUrl()).into(imageUser);
//      Glide.with(MainActivity.this).load(Objects.requireNonNull(user.getPhotoUrl()).into(headerUserImage);       // CAN ALSO USE THIS METHOD INSTEAD OF IF
        headerUserName.setText(user.getDisplayName());
        headerUserEmail.setText(user.getEmail());

        // Update menu title to Sign Out
        navigationView.getMenu().findItem(R.id.menu_signIn_logIn).setTitle("Sign out");
    }

    private void resetUserUI() {
        // Reset navigation drawer header
        headerUserName.setText(R.string.app_name);
        headerUserEmail.setText(R.string.because_who_needs_memory_anyway);

        // Set the default profile image
        imageUser.setImageResource(R.drawable.ic_note_a_pic);
        headerAppIcon.setVisibility(View.VISIBLE);
        headerUserImage.setVisibility(View.GONE);

        // Update menu title to Sign In / Log In
        navigationView.getMenu().findItem(R.id.menu_signIn_logIn).setTitle(R.string.sign_in_log_in);
    }

    private void setupAuthStateListener() {
        FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in, update UI
                updateUserUI(user);
            } else {
                // User is signed out, reset UI
                resetUserUI();
            }
        };
        auth.addAuthStateListener(authStateListener);
    }

    private void signOutUser() {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            FirebaseAuth.getInstance().signOut();  // Sign out from Firebase
            showCustomToast("Signed out successfully", Toast.LENGTH_SHORT);
            resetUserUI();  // Reset UI after sign-out
            drawerLayout.closeDrawer(GravityCompat.END);  // Close drawer after action
        });
    }

    /*private void defaultFirstNote() {

        SharedPreferences mSharedPref ;
        mSharedPref = this.getPreferences(Context.MODE_PRIVATE);
        boolean isFirstLaunch = mSharedPref.getBoolean(KEY_FIRST_LAUNCH, true);

        if (isFirstLaunch) {
            String[] dummyNoteTitles = getResources().getStringArray(R.array.dummy_note_titles);
            String[] dummyNoteSub = getResources().getStringArray(R.array.dummy_note_subtitles);
            String[] dummyNoteText = getResources().getStringArray(R.array.dummy_note_text);
            String[] dummyNoteColor = getResources().getStringArray(R.array.dummy_note_color);
            String[] dummyNoteWeb= getResources().getStringArray(R.array.dummy_note_url);

            List<Note> dummyNotes = new ArrayList<>();
            String time = new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.getDefault())
                    .format(new Date());

            for (int i = dummyNoteTitles.length - 1; i >= 0; i--) {
                Note note = new Note();
                note.setTitle(dummyNoteTitles[i]);
                note.setSubtitle(dummyNoteSub[i]);
                note.setNoteText(dummyNoteText[i]);
                note.setDateTime(time);
                note.setColor(dummyNoteColor[i]);
                note.setImagePath("");
                note.setWebLink(dummyNoteWeb[i]);
                dummyNotes.add(note);
            }
            mNoteRepository.insertNoteTask(dummyNotes.toArray(new Note[0]));
            mSharedPref.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        }
    }*/

//    private void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
////        if (intent.resolveActivity(getPackageManager()) != null) {
////        }
//    }


    private void selectImage() {
        ImagePicker.Companion.with(MainActivity.this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                showCustomToast("Permission Denied!",Toast.LENGTH_SHORT);
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    public void showCustomToast(String message, int toastLength) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);

        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 250);
        toast.setDuration(toastLength);
        toast.setView(layout);
        toast.show();

//        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
//        View view = toast.getView();
        //Gets the actual oval background of the Toast then sets the colour filter

//        Objects.requireNonNull(view).getBackground().setColorFilter(getResources().getColor(R.color.colorDefaultNoteColor), PorterDuff.Mode.SRC_IN);

//        assert view != null;
//        view.getBackground().setColorFilter(getResources().getColor(R.color.colorDefaultNoteColor), PorterDuff.Mode.SRC_IN

//        view != null ? view.getBackground().setColorFilter(getResources().getColor(R.color.colorDefaultNoteColor), PorterDuff.Mode.SRC_IN) : 0;


//              these are all the work around for this "if()" we have used here but we can also use this without using a if statement
//               but then it will show us yellow line error because we have to handle that if view is empty or is null

//        if (view != null) {
//            view.getBackground().setColorFilter(getResources().getColor(R.color.colorDefaultNoteColor), PorterDuff.Mode.SRC_IN);
//
//            //Gets the TextView from the Toast so it can be edited
//            TextView text = view.findViewById(android.R.id.message);
//            text.setTextColor(getResources().getColor(R.color.colorAccent));
//            toast.show();
//        }
    }

    @Override
    public void onNoteClicked(View view, Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
        CustomIntent.customType(MainActivity.this, "fadein-to-fadeout");

    }


    private void getNotes(final int requestCode, final Boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);

                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if (isNoteDeleted) {
                        notesAdapter.notifyDataSetChanged();
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }

                if (noteList.size() != 0) {
                    imageEmpty.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.GONE);
                } else {
                    imageEmpty.setVisibility(View.VISIBLE);
                    textEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
        new GetNotesTask().execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }

        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                        CustomIntent.customType(MainActivity.this, "bottom-to-up");

                    } catch (Exception e) {
                        showCustomToast(e.getMessage(),Toast.LENGTH_SHORT);
                    }
                }
            }

        }
    }

    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddURL =builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(v -> {
                if (inputURL.getText().toString().trim().isEmpty()) {
                    showCustomToast("Enter URL",Toast.LENGTH_SHORT);
                } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                    showCustomToast("Enter valid URL",Toast.LENGTH_SHORT);
                } else {
                    dialogAddURL.dismiss();
                    UIUtil.hideKeyboard(view.getContext(), inputURL);
                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    intent.putExtra("isFromQuickActions", true);
                    intent.putExtra("quickActionType", "URL");
                    intent.putExtra("URL", inputURL.getText().toString());
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    CustomIntent.customType(MainActivity.this, "bottom-to-up");
                    inputSearch.setText(null);
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v -> {
                UIUtil.hideKeyboard(view.getContext(), inputURL);
                dialogAddURL.dismiss();
            });
        }
        dialogAddURL.show();
    }

    private void showChangeThemeDialog() {
        boolean alreadyDark;
        boolean alreadyLight;
        TextView textDarkTheme,textLightTheme;

        if (dialogChangeTheme == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_change_theme,
                    (ViewGroup) findViewById(R.id.layoutChangeThemeContainer)
            );
            builder.setView(view);

            dialogChangeTheme = builder.create();
            if (dialogChangeTheme.getWindow() != null) {
                dialogChangeTheme.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            textDarkTheme = view.findViewById(R.id.textDarkTheme);
            textLightTheme = view.findViewById(R.id.textLightTheme);


            if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
               alreadyDark = true;
               alreadyLight = false;
            } else {
                alreadyDark = false;
                alreadyLight = true;
            }

            textDarkTheme.setOnClickListener(v -> {
                if (alreadyDark) {
                    showCustomToast(getResources().getString(R.string.developer_is_blind),Toast.LENGTH_LONG);
                    colorBlindTest();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    saveThemePreference(AppCompatDelegate.MODE_NIGHT_YES);
                    dialogChangeTheme.dismiss();
//                    recreate(); // Recreate activity to apply theme change
                }
            });

            textLightTheme.setOnClickListener(v -> {
                if (alreadyLight) {
                    showCustomToast(getResources().getString(R.string.user_is_blind),Toast.LENGTH_LONG);
                    colorBlindTest();

                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    saveThemePreference(AppCompatDelegate.MODE_NIGHT_NO);
                    dialogChangeTheme.dismiss();
//                    recreate(); // Recreate activity to apply theme change
                }
            });
        } else {
            alreadyLight = false;
            alreadyDark = false;
        }
        dialogChangeTheme.show();
    }

    private void colorBlindTest() {
        String blindUrl = getResources().getString(R.string.color_blind_test);
        Intent blindIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(blindUrl));
        startActivity(blindIntent);
        inputSearch.setText(null);
    }

    private void saveThemePreference(int mode) {
        SharedPreferences sharedPreferences = getSharedPreferences("theme_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("night_mode", mode);
        editor.apply();
    }
    private void saveEmailLoginPreference(String message) {
        SharedPreferences sharedPreferences = getSharedPreferences("EmailPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("emailLogin", message);
        editor.apply();
    }

    private String loadEmailLoginPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("EmailPreferences", MODE_PRIVATE);
        return sharedPreferences.getString("emailLogin", null); // Default is 2 (two columns)
    }

    private void popupSnackbarForCompleteUpdate() {

        Snackbar snackbar =
                Snackbar.make(
                        MainActivity.this.findViewById(R.id.bottomBarContainerLayout),
                        "New update is ready!",
                        Snackbar.LENGTH_SHORT);

        snackbar.setAction("Install", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomToast("Install", Toast.LENGTH_SHORT);
            }
        });

        snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void finishAnyActivity() {
        finish();
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerVisible(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        else
            super.onBackPressed();
    }
//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = auth.getCurrentUser();
//        updateUI(currentUser);
//    }
}