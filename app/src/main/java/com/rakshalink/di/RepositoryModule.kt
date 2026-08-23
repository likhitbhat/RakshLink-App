package com.rakshalink.di

import com.rakshalink.data.repository.AuthRepositoryImpl
import com.rakshalink.data.repository.BlePendantRepositoryImpl
import com.rakshalink.data.repository.EmergencyContactRepositoryImpl
import com.rakshalink.data.repository.GuardianRepositoryImpl
import com.rakshalink.data.repository.LocationRepositoryImpl
import com.rakshalink.data.repository.SafeZoneRepositoryImpl
import com.rakshalink.data.repository.SosRepositoryImpl
import com.rakshalink.domain.repository.AuthRepository
import com.rakshalink.domain.repository.BlePendantRepository
import com.rakshalink.domain.repository.EmergencyContactRepository
import com.rakshalink.domain.repository.GuardianRepository
import com.rakshalink.domain.repository.LocationRepository
import com.rakshalink.domain.repository.SafeZoneRepository
import com.rakshalink.domain.repository.SosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindSosRepository(impl: SosRepositoryImpl): SosRepository

    @Binds
    @Singleton
    abstract fun bindSafeZoneRepository(impl: SafeZoneRepositoryImpl): SafeZoneRepository

    @Binds
    @Singleton
    abstract fun bindGuardianRepository(impl: GuardianRepositoryImpl): GuardianRepository

    @Binds
    @Singleton
    abstract fun bindBlePendantRepository(impl: BlePendantRepositoryImpl): BlePendantRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyContactRepository(impl: EmergencyContactRepositoryImpl): EmergencyContactRepository

    @Binds
    @Singleton
    abstract fun bindTwilioAuthRepository(impl: com.rakshalink.data.repository.TwilioAuthRepositoryImpl): com.rakshalink.domain.repository.TwilioAuthRepository
}
