package com.mysticraccoon.runningappyt.data.repositories

import com.mysticraccoon.runningappyt.data.dao.RunDao
import com.mysticraccoon.runningappyt.domain.Run
import javax.inject.Inject


class MainRepository @Inject constructor(val runDao: RunDao) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurnt()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurnt()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

}