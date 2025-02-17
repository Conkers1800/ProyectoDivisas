package com.example.marsphotos.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.marsphotos.data.DaoApiRate
import com.example.marsphotos.model.ApiRate
import com.example.marsphotos.network.DatabaseProvider
import com.example.marsphotos.network.MarsApi
import retrofit2.HttpException

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val listResult = MarsApi.retrofitService.getPhotos()
            val exchangeRateDao = DatabaseProvider.getDatabase(applicationContext).exchangeRateDao()
            val timestamp = System.currentTimeMillis()
            listResult.conversionRates.forEach { (targetCode, rate) ->
                exchangeRateDao.insert(
                    ApiRate(
                    baseCode = listResult.baseCode,
                    targetCode = targetCode,
                    rate = rate,
                    timestamp = timestamp
                )
                )
            }
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
