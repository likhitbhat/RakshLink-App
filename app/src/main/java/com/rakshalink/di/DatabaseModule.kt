package com.rakshalink.di

import android.content.Context
import androidx.room.Room
import com.rakshalink.data.local.dao.AlertDao
import com.rakshalink.data.local.dao.LocationDao
import com.rakshalink.data.local.dao.PendingSyncDao
import com.rakshalink.data.local.dao.SafeZoneDao
import com.rakshalink.data.local.database.RakshaLinkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RakshaLinkDatabase {
        return Room.databaseBuilder(
            context,
            RakshaLinkDatabase::class.java,
            "rakshalink_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideLocationDao(db: RakshaLinkDatabase): LocationDao = db.locationDao()

    @Provides
    fun provideAlertDao(db: RakshaLinkDatabase): AlertDao = db.alertDao()

    @Provides
    fun provideSafeZoneDao(db: RakshaLinkDatabase): SafeZoneDao = db.safeZoneDao()

    @Provides
    fun providePendingSyncDao(db: RakshaLinkDatabase): PendingSyncDao = db.pendingSyncDao()
}
