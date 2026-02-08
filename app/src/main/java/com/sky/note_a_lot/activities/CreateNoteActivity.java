package com.sky.note_a_lot.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.sky.note_a_lot.R;
import com.sky.note_a_lot.database.NotesDatabase;
import com.sky.note_a_lot.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import maes.tech.intentanim.CustomIntent;


public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteText;
    private TextView textDateTime;
    private View viewSubtitleIndicator;
    private String selectedNoteColor;
    private String selectedImagePath;
    private ImageView imageBack, imageSave;
    private ImageView imageNote;
    private ImageView imageColor1, imageColor2, imageColor3, imageColor4, imageColor5;
    private TextView textWebURL;
    private LinearLayout layoutWebURL;
    private ScrollView scrollView;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private AlertDialog dialogAddURL;
    private AlertDialog dialogDeleteNote;
    private Note alreadyAvialableNote;
    String dateTime;
    String animationType = "LandAndroid";
    String colorString;
    Boolean returnBack = false;
    Boolean backButton = false;
    private LinearLayout layoutMiscellaneous;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        inputNoteText = findViewById(R.id.inputNote);

        initViews();
        setActionOnViews();


        int myColor = getResources().getColor(R.color.colorDefaultNoteColor);
        colorString = String.format("#%06X", (0xFFFFFF & myColor));
        selectedNoteColor = colorString;
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            animationType = "isViewOrUpdate";
            alreadyAvialableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.imageRemoveWebURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebURL.setText(null);
                layoutWebURL.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                selectedImagePath = "";
            }
        });

        if (getIntent().getBooleanExtra("isFromQuickActions", false)) {
            animationType = "isFromQuickActions";
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null) {
                if (type.equals("image")) {
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    Glide.with(imageNote.getContext()).load(selectedImagePath).into(imageNote);
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                } else if (type.equals("URL")) {
                    textWebURL.setText(getIntent().getStringExtra("URL"));
                    layoutWebURL.setVisibility(View.VISIBLE);
                }
            }
        }
        initMiscellaneous();
        inputNoteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Scroll to bottom after text changes
//                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

                scrollToCursor();
            }


        });

        inputNoteText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollToCursor();
            }
        });
        setSubtitleIndicatorColor();
    }

    private void initViews() {
        imageBack = findViewById(R.id.imageBack);
        imageSave = findViewById(R.id.imageSave);
        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        textDateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textWebURL = findViewById(R.id.textWebURL);
        layoutWebURL = findViewById(R.id.layoutWebURL);
        scrollView = findViewById(R.id.createNoteScrollView);
    }

    private void setActionOnViews() {
        imageBack.setOnClickListener(v -> {
            UIUtil.hideKeyboard(CreateNoteActivity.this);
            backButton = true;
            onBackPressed();
        });

        findViewById(R.id.constraintHideKeyboard).setOnClickListener(v -> UIUtil.hideKeyboard(CreateNoteActivity.this));

        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.getDefault())
                        .format(new Date())
        );


        KeyboardVisibilityEvent.setEventListener(CreateNoteActivity.this, isOpen -> {
            if (!isOpen) {
                inputNoteTitle.clearFocus();
                inputNoteText.clearFocus();
            }
        });

//        inputNoteText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (bottomSheetBehavior.getState()== BottomSheetBehavior.STATE_EXPANDED) {
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // Scroll to bottom after text changes
//                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
//            }
//        });

        imageSave.setOnClickListener(v -> {
            saveNote(true);
//            CustomIntent.customType(CreateNoteActivity.this, "right-to-left");
        });
    }

    private void   setViewOrUpdateNote() {

//        String titleText = alreadyAvialableNote.getTitle();
//        if (titleText.length() <= 120 ) {
//            String textText = alreadyAvialableNote.getNoteText();
//            if (textText.length() <= 120) {
//                if (titleText.equals(textText)) {
//                    inputNoteTitle.setText("");
//                } else {
//                    inputNoteTitle.setText(alreadyAvialableNote.getTitle());
//                }
//            } else if (textText.length() > 120){
//                textText = textText.substring(0, 120);
//                if (titleText.equals(textText)) {
//                    inputNoteTitle.setText("");
//                } else {
//                    inputNoteTitle.setText(alreadyAvialableNote.getTitle());
//                }
//            }
//        }
//        else if (titleText.length() >120){
//            inputNoteTitle.setText(alreadyAvialableNote.getTitle());
//        }
//
//
//        String subtext = alreadyAvialableNote.getSubtitle();
//        String notetext = alreadyAvialableNote.getNoteText();
//        if (subtext.equals("-------") || subtext.endsWith(".....")) {
//            inputNoteSubtitle.setText("");
//        } else if (subtext.length() < 80) {
//            if (notetext.length() > 120) {
//                if (120+subtext.length() == notetext.length()) {
//                    notetext = notetext.substring(120, 120 + subtext.length());
//
//                    if (notetext.equals(subtext)) {
//                        inputNoteSubtitle.setText("");
//                    } else {
//                        inputNoteSubtitle.setText(alreadyAvialableNote.getSubtitle());
//                    }
//                } else if (notetext.equals(subtext)) {
//                    inputNoteSubtitle.setText("");
//                } else {
//                    inputNoteSubtitle.setText(alreadyAvialableNote.getSubtitle());
//                }
//            } else if (notetext.length() >=200 && subtext.endsWith(".....")) {
//                inputNoteSubtitle.setText("");
//            } else if (notetext.equals(subtext)) {
//                inputNoteSubtitle.setText("");
//            } else {
//                inputNoteSubtitle.setText(alreadyAvialableNote.getSubtitle());
//            }
//        } else {
//            inputNoteSubtitle.setText(alreadyAvialableNote.getSubtitle());
//        }
//
//
//        String subtitleText = alreadyAvialableNote.getSubtitle();
//        String titleTextTitle = alreadyAvialableNote.getTitle();
//
//        if (titleTextTitle.length()==120 && subtitleText.endsWith(".....  ") ) {
//            inputNoteTitle.setText("");
//            String finalForSub = titleTextTitle+subtitleText.substring(0,subtitleText.length()-7);
//            inputNoteSubtitle.setText(finalForSub);
//        }

        inputNoteTitle.setText(alreadyAvialableNote.getTitle());
        inputNoteText.setText(alreadyAvialableNote.getNoteText());
        textDateTime.setText(alreadyAvialableNote.getDateTime());

        if (alreadyAvialableNote.getImagePath() != null && !alreadyAvialableNote.getImagePath().trim().isEmpty()) {
//            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvialableNote.getImagePath()));
            Glide.with(imageNote.getContext()).load(alreadyAvialableNote.getImagePath()).into(imageNote);
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvialableNote.getImagePath();
        }

        if (alreadyAvialableNote.getWebLink() != null && !alreadyAvialableNote.getWebLink().trim().isEmpty()) {
            textWebURL.setText(alreadyAvialableNote.getWebLink());
            layoutWebURL.setVisibility(View.VISIBLE);
        }
    }

    private void saveNote(Boolean showToast) {
        UIUtil.hideKeyboard(CreateNoteActivity.this);

        final Note note = new Note();
        if (showToast) {
            if (inputNoteTitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty()) {
                showCustomToast("Note can't be empty!", Toast.LENGTH_SHORT);
                return;
            }
        } else {
            if (inputNoteTitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty()) {
                returnBack = true;
                return;
            }
        }


//        String noteKaText = inputNoteText.getText().toString();
//        String noteKaSubtext = inputNoteText.getText().toString();
//        if (inputNoteTitle.getText().toString().trim().isEmpty() && inputNoteSubtitle.getText().toString().trim().isEmpty()) {
//            if (noteKaText.length() >= 200) {
//                noteKaText = noteKaText.substring(0, 120);
//                noteKaSubtext = noteKaSubtext.substring(120, 200);
//                noteKaSubtext += ".....";
//                note.setTitle(noteKaText);
//                note.setSubtitle(noteKaSubtext);
//            } else if (120 < noteKaText.length() && noteKaText.length() < 200){
//                noteKaText = noteKaText.substring(0, 120);
//                noteKaSubtext = noteKaSubtext.substring(120, noteKaSubtext.length());
//                note.setTitle(noteKaText);
//                note.setSubtitle(noteKaSubtext);
//            } else {
//                noteKaText = noteKaText.substring(0, noteKaText.length());
//                note.setTitle(noteKaText);
//                note.setSubtitle("-------");
//            }
//
//        } else if (!inputNoteTitle.getText().toString().trim().isEmpty() && !inputNoteText.getText().toString().trim().isEmpty()
//        && inputNoteSubtitle.getText().toString().trim().isEmpty()) {
//
//            String textOfNote = inputNoteText.getText().toString();
//            if (textOfNote.length() >= 80) {
//                textOfNote = textOfNote.substring(0, 80);
//                note.setSubtitle(textOfNote+".....");
//            } else {
//                note.setSubtitle(textOfNote);
//            }
//            note.setTitle(inputNoteTitle.getText().toString());
//
//        }
//        else if (!inputNoteTitle.getText().toString().trim().isEmpty() && inputNoteSubtitle.getText().toString().trim().isEmpty()
//        && inputNoteText.getText().toString().trim().isEmpty()) {
//
//            note.setSubtitle("-------");
//            note.setTitle(inputNoteTitle.getText().toString());
//
//        }
//        else if (inputNoteTitle.getText().toString().trim().isEmpty() && !inputNoteSubtitle.getText().toString().trim().isEmpty()
//                && !inputNoteText.getText().toString().trim().isEmpty()) {
//
//            String textOfSub = inputNoteSubtitle.getText().toString();
//            String textOfSub2 = inputNoteSubtitle.getText().toString();
//
//            if (textOfSub.length() >= 120) {
//                textOfSub = textOfSub.substring(0, 120);
//                note.setTitle(textOfSub);
//                textOfSub2 = textOfSub2.substring(120);
//                note.setSubtitle(textOfSub2+".....  ");
//            } else {
//                note.setTitle(textOfSub);
//                note.setSubtitle("-------");
//            }
//
//        } else {
//            note.setTitle(inputNoteTitle.getText().toString());
//            note.setSubtitle(inputNoteSubtitle.getText().toString());
//        }
//        inputNoteTitle.getText().toString().trim().isEmpty()
//                && inputNoteSubtitle.getText().toString().trim().isEmpty())
//        String noteTextText = inputNoteText.getText().toString();
//        String noteTitleText = inputNoteTitle.getText().toString();
//        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
//
//        }

        note.setTitle(inputNoteTitle.getText().toString().trim());
        note.setNoteText(inputNoteText.getText().toString().trim());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if (layoutWebURL.getVisibility() == View.VISIBLE) {
            note.setWebLink(textWebURL.getText().toString());
        }

        if (alreadyAvialableNote != null) {
            note.setId(alreadyAvialableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }

        }

        new SaveNoteTask().execute();

    }

    private void initMiscellaneous() {

        layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);

        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(v -> {

            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        imageColor2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
        imageColor3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
        imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);

        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            layoutMiscellaneous.findViewById(R.id.layoutColor5).setVisibility(View.GONE);
        }

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(
                v -> changeSelectedColor(colorString, R.drawable.ic_done, 0, 0, 0, 0));

        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener
                (v -> changeSelectedColor("#FDBE3B", 0, R.drawable.ic_done, 0, 0, 0));

        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(
                v -> changeSelectedColor("#FF4842", 0, 0, R.drawable.ic_done, 0, 0));

        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(
                v -> changeSelectedColor("#3A52Fc", 0, 0, 0, R.drawable.ic_done, 0));

        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(
                v -> changeSelectedColor("#000000", 0, 0, 0, 0, R.drawable.ic_done));

        if (alreadyAvialableNote != null && alreadyAvialableNote.getColor() != null && !alreadyAvialableNote.getColor().trim().isEmpty()) {
            switch (alreadyAvialableNote.getColor()) {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52Fc":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;

            }
        }

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(v -> {
            UIUtil.hideKeyboard(CreateNoteActivity.this);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(CreateNoteActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(CreateNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                }
            } else {
                selectImage();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddURLDialog();
        });

        if (alreadyAvialableNote != null) {
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(v -> {
                UIUtil.hideKeyboard(CreateNoteActivity.this);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteNoteDialog();
            });
        }
    }

    private void showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);

            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.textDeleteNote).setOnClickListener(v -> {

                @SuppressLint("StaticFieldLeak")
                class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NotesDatabase.getDatabase(getApplicationContext()).noteDao()
                                .deleteNote(alreadyAvialableNote);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        Intent intent = new Intent();
                        intent.putExtra("isNoteDeleted", true);
                        setResult(RESULT_OK, intent);
                        animationType = "isNoteDeleted";
//                        CustomIntent.customType(CreateNoteActivity.this, "fadein-to-fadeout");
                        finish();
                    }
                }
                new DeleteNoteTask().execute();
            });
            view.findViewById(R.id.textCancel).setOnClickListener(v -> dialogDeleteNote.dismiss());
        }
        dialogDeleteNote.show();
    }

    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void changeSelectedColor(String noteColor, int id1, int id2, int id3, int id4, int id5) {
        selectedNoteColor = noteColor;
        imageColor1.setImageResource(id1);
        imageColor2.setImageResource(id2);
        imageColor3.setImageResource(id3);
        imageColor4.setImageResource(id4);
        imageColor5.setImageResource(id5);
        setSubtitleIndicatorColor();
    }

//    private void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);

    /// /        if (intent.resolveActivity(getPackageManager()) != null) {
    /// /        }
//    }
    private void selectImage() {
        ImagePicker.Companion.with(CreateNoteActivity.this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                showCustomToast("Permission Denied!", Toast.LENGTH_SHORT);
            }
        }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {

//                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
//                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                        imageNote.setImageBitmap(bitmap);

                        Glide.with(imageNote.getContext()).load(selectedImageUri).into(imageNote);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);


                    } catch (Exception e) {
                        showCustomToast(e.getMessage(), Toast.LENGTH_LONG);
                    }
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            showCustomToast("An Error occurred", Toast.LENGTH_LONG);
        } else {
            return;
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

    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(v -> {
                if (inputURL.getText().toString().trim().isEmpty()) {
                    showCustomToast("Enter URL", Toast.LENGTH_SHORT);
                } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                    showCustomToast("Enter valid URL", Toast.LENGTH_SHORT);
                } else {
                    UIUtil.hideKeyboard(view.getContext(), inputURL);
                    textWebURL.setText(inputURL.getText().toString());
                    layoutWebURL.setVisibility(View.VISIBLE);
                    dialogAddURL.dismiss();
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(v -> {
                UIUtil.hideKeyboard(view.getContext(), inputURL);
                dialogAddURL.dismiss();
            });
        }
        dialogAddURL.show();
    }

    private void scrollToCursor() {
        inputNoteText.post(new Runnable() {
            @Override
            public void run() {

                int cursorPosition = inputNoteText.getSelectionStart();
                Layout layout = inputNoteText.getLayout();

                if (layout != null) {

                    int line = layout.getLineForOffset(cursorPosition);
                    int lineTop = layout.getLineTop(line);
                    int lineBottom = layout.getLineBottom(line);

                    // Create rectangle around cursor line
                    Rect rect = new Rect();
                    rect.left = 0;
                    rect.top = lineTop;
                    rect.right = inputNoteText.getWidth();
                    rect.bottom = lineBottom;

                    // Request ScrollView to make this rectangle visible
                    inputNoteText.requestRectangleOnScreen(rect, true);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backButton) {
            super.onBackPressed();
            finish();
        } else {
            saveNote(false);
            if (returnBack) {
                super.onBackPressed();
                finish();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        String finalAnimation = "right-to-left";
        if (animationType.equals("isViewOrUpdate") || animationType.equals("isNoteDeleted")) {
            finalAnimation = "fadein-to-fadeout";
        } else if (animationType.equals("isFromQuickActions")) {
            finalAnimation = "up-to-bottom";
        } else {
            finalAnimation = "right-to-left";
        }

        CustomIntent.customType(CreateNoteActivity.this, finalAnimation);
    }
}
