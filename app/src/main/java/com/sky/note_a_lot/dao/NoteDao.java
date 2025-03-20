package com.sky.note_a_lot.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sky.note_a_lot.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    long[] insertNotes(Note... notes);

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List <Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);


    @Delete
    void deleteNote(Note note);
}
