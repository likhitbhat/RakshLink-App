package com.rakshalink.ui.wearer;

import android.content.Context;
import com.rakshalink.data.repository.NearbyPlacesRepository;
import com.rakshalink.domain.repository.AuthRepository;
import com.rakshalink.domain.repository.BlePendantRepository;
import com.rakshalink.domain.repository.EmergencyContactRepository;
import com.rakshalink.domain.repository.LocationRepository;
import com.rakshalink.domain.repository.SafeZoneRepository;
import com.rakshalink.domain.repository.SosRepository;
import com.rakshalink.services.FallDetectionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class WearerViewModel_Factory implements Factory<WearerViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<SosRepository> sosRepositoryProvider;

  private final Provider<LocationRepository> locationRepositoryProvider;

  private final Provider<SafeZoneRepository> safeZoneRepositoryProvider;

  private final Provider<BlePendantRepository> bleRepositoryProvider;

  private final Provider<EmergencyContactRepository> contactRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<NearbyPlacesRepository> nearbyPlacesRepositoryProvider;

  private final Provider<FallDetectionManager> fallDetectionManagerProvider;

  public WearerViewModel_Factory(Provider<Context> contextProvider,
      Provider<SosRepository> sosRepositoryProvider,
      Provider<LocationRepository> locationRepositoryProvider,
      Provider<SafeZoneRepository> safeZoneRepositoryProvider,
      Provider<BlePendantRepository> bleRepositoryProvider,
      Provider<EmergencyContactRepository> contactRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<NearbyPlacesRepository> nearbyPlacesRepositoryProvider,
      Provider<FallDetectionManager> fallDetectionManagerProvider) {
    this.contextProvider = contextProvider;
    this.sosRepositoryProvider = sosRepositoryProvider;
    this.locationRepositoryProvider = locationRepositoryProvider;
    this.safeZoneRepositoryProvider = safeZoneRepositoryProvider;
    this.bleRepositoryProvider = bleRepositoryProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.nearbyPlacesRepositoryProvider = nearbyPlacesRepositoryProvider;
    this.fallDetectionManagerProvider = fallDetectionManagerProvider;
  }

  @Override
  public WearerViewModel get() {
    return newInstance(contextProvider.get(), sosRepositoryProvider.get(), locationRepositoryProvider.get(), safeZoneRepositoryProvider.get(), bleRepositoryProvider.get(), contactRepositoryProvider.get(), authRepositoryProvider.get(), nearbyPlacesRepositoryProvider.get(), fallDetectionManagerProvider.get());
  }

  public static WearerViewModel_Factory create(Provider<Context> contextProvider,
      Provider<SosRepository> sosRepositoryProvider,
      Provider<LocationRepository> locationRepositoryProvider,
      Provider<SafeZoneRepository> safeZoneRepositoryProvider,
      Provider<BlePendantRepository> bleRepositoryProvider,
      Provider<EmergencyContactRepository> contactRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<NearbyPlacesRepository> nearbyPlacesRepositoryProvider,
      Provider<FallDetectionManager> fallDetectionManagerProvider) {
    return new WearerViewModel_Factory(contextProvider, sosRepositoryProvider, locationRepositoryProvider, safeZoneRepositoryProvider, bleRepositoryProvider, contactRepositoryProvider, authRepositoryProvider, nearbyPlacesRepositoryProvider, fallDetectionManagerProvider);
  }

  public static WearerViewModel newInstance(Context context, SosRepository sosRepository,
      LocationRepository locationRepository, SafeZoneRepository safeZoneRepository,
      BlePendantRepository bleRepository, EmergencyContactRepository contactRepository,
      AuthRepository authRepository, NearbyPlacesRepository nearbyPlacesRepository,
      FallDetectionManager fallDetectionManager) {
    return new WearerViewModel(context, sosRepository, locationRepository, safeZoneRepository, bleRepository, contactRepository, authRepository, nearbyPlacesRepository, fallDetectionManager);
  }
}
