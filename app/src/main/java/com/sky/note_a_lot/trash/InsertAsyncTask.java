package com.sky.note_a_lot.trash;

import android.os.AsyncTask;

import com.sky.note_a_lot.entities.Note;
import com.sky.note_a_lot.dao.NoteDao;

public class InsertAsyncTask extends AsyncTask<Note, Void, Void> {

    private NoteDao mNoteDao;

    public InsertAsyncTask(NoteDao dao) {
        mNoteDao = dao;
    }


    @Override
    protected Void doInBackground(Note... notes) {
        mNoteDao.insertNotes(notes);
        return null;
    }
}
