package com.sky.note_a_lot.trash;


import android.content.Context;


import com.sky.note_a_lot.dao.NoteDao;
import com.sky.note_a_lot.entities.Note;
import com.sky.note_a_lot.database.NotesDatabase;

public class NoteRepo {

    private NotesDatabase mNoteDatabase;
    private NoteDao imageDao;

    public NoteRepo(Context context) {
        mNoteDatabase = NotesDatabase.getDatabase(context);
        imageDao = mNoteDatabase.noteDao();
    }


    public void insertNoteTask(Note... note) {
        new InsertAsyncTask(mNoteDatabase.noteDao()).execute(note);
    }

}

