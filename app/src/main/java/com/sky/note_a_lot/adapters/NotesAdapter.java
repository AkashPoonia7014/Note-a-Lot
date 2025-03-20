package com.sky.note_a_lot.adapters;



import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.widget.TextView;

import com.sky.note_a_lot.R;
import com.sky.note_a_lot.entities.Note;
import com.sky.note_a_lot.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import androidx.core.content.ContextCompat;


public class  NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> notesSource;
    private Context context;

    public NotesAdapter(Context context, List<Note> notes, NotesListener notesListener) {
        this.context = context;
        this.notes = notes;
        this.notesListener = notesListener;
        notesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.setNote(notes.get(position), context);
        holder.layoutNote.setOnClickListener(v -> notesListener.onNoteClicked(holder.layoutNote, notes.get(position), position));
        /*holder.layoutNote.setOnLongClickListener(v -> {
            notesListener.onNoteLongClicked(holder.layoutNote, notes.get(position), position);
            return true;
        });*/

        int myColor1 = ContextCompat.getColor(holder.itemView.getContext(), R.color.colorDefaultNoteColor);
        String colorString1 = String.format("#%06X", (0xFFFFFF & myColor1));
        GradientDrawable gradientDrawable = (GradientDrawable) holder.layoutNote.getBackground();
        Note faltuNote = notes.get(position);

        if ((holder.itemView.getContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            // Apply dark theme settings (This actually applies light theme)

            if (Objects.equals(faltuNote.getColor(), "#333333") || Objects.equals(faltuNote.getColor(), "#F1F1F1")) {

                gradientDrawable.setColor(Color.parseColor(colorString1));
            } else {
                gradientDrawable.setColor(Color.parseColor(faltuNote.getColor()));

            }

            if (Objects.equals(faltuNote.getColor(), "#000000")) {
                gradientDrawable.setColor(Color.parseColor("#F1F1F1"));
            }

        }else if((holder.itemView.getContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
            // Apply light theme settings (This actually dark theme )
            if (Objects.equals(faltuNote.getColor(), "#333333") || Objects.equals(faltuNote.getColor(), "#F1F1F1")) {

                gradientDrawable.setColor(Color.parseColor(colorString1));
            } else {
                gradientDrawable.setColor(Color.parseColor(faltuNote.getColor()));
            }
        }else {
            if (faltuNote.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(faltuNote.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor(colorString1));
            }
        }


    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textText, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        int myColor;
        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textText = itemView.findViewById(R.id.textText);
            textDateTime= itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);

        }

        void setNote(Note note, Context context) {
            try {
                if (note.getTitle().trim().isEmpty()) {
                    textTitle.setVisibility(View.GONE);
                    float marginInDp = 10.40f;
                    float density = context.getResources().getDisplayMetrics().density;
                    float marginInPx = marginInDp * density;

                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) textText.getLayoutParams();
                    layoutParams.topMargin = Math.round(marginInPx);
                    textText.setLayoutParams(layoutParams);

                } else {
                    float marginInDp = 0f;
                    float density = context.getResources().getDisplayMetrics().density;
                    float marginInPx = marginInDp * density;

                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) textText.getLayoutParams();
                    layoutParams.topMargin = Math.round(marginInPx);
                    textText.setLayoutParams(layoutParams);

                    textTitle.setText(note.getTitle());
                    textTitle.setVisibility(View.VISIBLE);
                }

                if (note.getNoteText().trim().isEmpty()) {
                    textText.setVisibility(View.GONE);
                }else {
                    textText.setText(note.getNoteText());
                    textText.setVisibility(View.VISIBLE);
                }
                textDateTime.setText(note.getDateTime());

                GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
                if (note.getColor() != null) {
                    gradientDrawable.setColor(Color.parseColor(note.getColor()));
                } else {
                    myColor = ContextCompat.getColor(itemView.getContext(), R.color.colorDefaultNoteColor);
                    String colorString = String.format("#%06X", (0xFFFFFF & myColor));
                    gradientDrawable.setColor(Color.parseColor(colorString));

                }

                if (note.getImagePath() != null) {
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                    imageNote.setVisibility(View.VISIBLE);
                } else {
                    imageNote.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.d("Adapter", "Adapter set Note error" + e.getMessage());
            }
        }
    }


    public void searchNotes (final String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    notes = notesSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : notesSource) {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {

        if (timer != null) {
            timer.cancel();
        }
    }
}
