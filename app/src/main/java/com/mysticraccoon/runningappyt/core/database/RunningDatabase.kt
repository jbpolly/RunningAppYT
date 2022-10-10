package com.mysticraccoon.runningappyt.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mysticraccoon.runningappyt.data.dao.RunDao
import com.mysticraccoon.runningappyt.domain.Run

@Database(entities = [Run::class], version = 1)
@TypeConverters(Converters::class)
abstract class RunningDatabase: RoomDatabase() {

    abstract fun getRunDao(): RunDao

}