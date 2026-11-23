package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [CustomerEntity::class, JobEntity::class, PaymentEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class SkywallDatabase : RoomDatabase() {
  abstract fun customerDao(): CustomerDao

  companion object {
    @Volatile private var instance: SkywallDatabase? = null

    fun getInstance(context: Context): SkywallDatabase {
      return instance
        ?: synchronized(this) {
          instance
            ?: Room.databaseBuilder(
                context.applicationContext,
                SkywallDatabase::class.java,
                "skywall_customers.db",
              )
              .build()
              .also { instance = it }
        }
    }
  }
}
