package com.rakshalink.services;

import android.content.Context;
import com.rakshalink.domain.repository.LocationRepository;
import com.rakshalink.domain.repository.SosRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class FallDetectionManager_Factory implements Factory<FallDetectionManager> {
  private final Provider<Context> contextProvider;

  private final Provider<SosRepository> sosRepositoryProvider;

  private final Provider<LocationRepository> locationRepositoryProvider;

  public FallDetectionManager_Factory(Provider<Context> contextProvider,
      Provider<SosRepository> sosRepositoryProvider,
      Provider<LocationRepository> locationRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.sosRepositoryProvider = sosRepositoryProvider;
    this.locationRepositoryProvider = locationRepositoryProvider;
  }

  @Override
  public FallDetectionManager get() {
    return newInstance(contextProvider.get(), sosRepositoryProvider.get(), locationRepositoryProvider.get());
  }

  public static FallDetectionManager_Factory create(Provider<Context> contextProvider,
      Provider<SosRepository> sosRepositoryProvider,
      Provider<LocationRepository> locationRepositoryProvider) {
    return new FallDetectionManager_Factory(contextProvider, sosRepositoryProvider, locationRepositoryProvider);
  }

  public static FallDetectionManager newInstance(Context context, SosRepository sosRepository,
      LocationRepository locationRepository) {
    return new FallDetectionManager(context, sosRepository, locationRepository);
  }
}
