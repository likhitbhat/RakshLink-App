package com.rakshalink.workers;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.rakshalink.domain.repository.LocationRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SyncWorker_Factory {
  private final Provider<LocationRepository> locationRepositoryProvider;

  public SyncWorker_Factory(Provider<LocationRepository> locationRepositoryProvider) {
    this.locationRepositoryProvider = locationRepositoryProvider;
  }

  public SyncWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, locationRepositoryProvider.get());
  }

  public static SyncWorker_Factory create(Provider<LocationRepository> locationRepositoryProvider) {
    return new SyncWorker_Factory(locationRepositoryProvider);
  }

  public static SyncWorker newInstance(Context context, WorkerParameters workerParams,
      LocationRepository locationRepository) {
    return new SyncWorker(context, workerParams, locationRepository);
  }
}
