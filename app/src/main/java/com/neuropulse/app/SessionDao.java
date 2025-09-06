package com.neuropulse.app;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SessionDao {

    @Insert
    void insert(SessionEntity session);

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    List<SessionEntity> getAllSessions();

    @Query("SELECT * FROM sessions ORDER BY sessionEndTs DESC LIMIT 20")
    List<SessionEntity> getRecentSessions();

    @Query("DELETE FROM sessions")
    void clearAll();
}
