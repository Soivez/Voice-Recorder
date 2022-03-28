package com.example.voice_recorder.utils

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.example.voice_recorder.utils.Element

@Dao
interface ElementDao {
    @Query(value = "select * from Element")
    suspend fun getAll() : List<Element>

    @Insert(onConflict = REPLACE)
    suspend fun insertALl(vararg elements : Element)

    @Query("SELECT EXISTS(SELECT * FROM Element WHERE id=(:id))")
    suspend fun isExists(id : Int): Boolean

    @Query("DELETE FROM Element")
    suspend fun nukeTable()

    @Delete
    suspend fun deleteAll(vararg element : Element)
}