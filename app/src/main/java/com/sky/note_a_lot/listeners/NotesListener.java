package com.sky.note_a_lot.listeners;

import android.view.View;

import com.sky.note_a_lot.entities.Note;

public interface NotesListener {

    void onNoteClicked(View view, Note note, int position);
//    void onNoteLongClicked(View view, Note note, int position);

}
