package com.rakshalink.data.repository;

import com.rakshalink.data.local.dao.LocationDao;
import com.rakshalink.data.remote.supabase.SupabaseClientProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class LocationRepositoryImpl_Factory implements Factory<LocationRepositoryImpl> {
  private final Provider<LocationDao> locationDaoProvider;

  private final Provider<SupabaseClientProvider> supabaseProvider;

  public LocationRepositoryImpl_Factory(Provider<LocationDao> locationDaoProvider,
      Provider<SupabaseClientProvider> supabaseProvider) {
    this.locationDaoProvider = locationDaoProvider;
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public LocationRepositoryImpl get() {
    return newInstance(locationDaoProvider.get(), supabaseProvider.get());
  }

  public static LocationRepositoryImpl_Factory create(Provider<LocationDao> locationDaoProvider,
      Provider<SupabaseClientProvider> supabaseProvider) {
    return new LocationRepositoryImpl_Factory(locationDaoProvider, supabaseProvider);
  }

  public static LocationRepositoryImpl newInstance(LocationDao locationDao,
      SupabaseClientProvider supabaseProvider) {
    return new LocationRepositoryImpl(locationDao, supabaseProvider);
  }
}
