package com.rakshalink.services;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.rakshalink.domain.repository.LocationRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
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
public final class LocationForegroundService_MembersInjector implements MembersInjector<LocationForegroundService> {
  private final Provider<FusedLocationProviderClient> fusedLocationClientProvider;

  private final Provider<LocationRepository> locationRepositoryProvider;

  public LocationForegroundService_MembersInjector(
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<LocationRepository> locationRepositoryProvider) {
    this.fusedLocationClientProvider = fusedLocationClientProvider;
    this.locationRepositoryProvider = locationRepositoryProvider;
  }

  public static MembersInjector<LocationForegroundService> create(
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<LocationRepository> locationRepositoryProvider) {
    return new LocationForegroundService_MembersInjector(fusedLocationClientProvider, locationRepositoryProvider);
  }

  @Override
  public void injectMembers(LocationForegroundService instance) {
    injectFusedLocationClient(instance, fusedLocationClientProvider.get());
    injectLocationRepository(instance, locationRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.rakshalink.services.LocationForegroundService.fusedLocationClient")
  public static void injectFusedLocationClient(LocationForegroundService instance,
      FusedLocationProviderClient fusedLocationClient) {
    instance.fusedLocationClient = fusedLocationClient;
  }

  @InjectedFieldSignature("com.rakshalink.services.LocationForegroundService.locationRepository")
  public static void injectLocationRepository(LocationForegroundService instance,
      LocationRepository locationRepository) {
    instance.locationRepository = locationRepository;
  }
}
