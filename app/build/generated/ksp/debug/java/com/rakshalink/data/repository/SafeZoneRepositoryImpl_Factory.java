package com.rakshalink.data.repository;

import com.rakshalink.data.local.dao.SafeZoneDao;
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
public final class SafeZoneRepositoryImpl_Factory implements Factory<SafeZoneRepositoryImpl> {
  private final Provider<SafeZoneDao> safeZoneDaoProvider;

  private final Provider<SupabaseClientProvider> supabaseProvider;

  public SafeZoneRepositoryImpl_Factory(Provider<SafeZoneDao> safeZoneDaoProvider,
      Provider<SupabaseClientProvider> supabaseProvider) {
    this.safeZoneDaoProvider = safeZoneDaoProvider;
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SafeZoneRepositoryImpl get() {
    return newInstance(safeZoneDaoProvider.get(), supabaseProvider.get());
  }

  public static SafeZoneRepositoryImpl_Factory create(Provider<SafeZoneDao> safeZoneDaoProvider,
      Provider<SupabaseClientProvider> supabaseProvider) {
    return new SafeZoneRepositoryImpl_Factory(safeZoneDaoProvider, supabaseProvider);
  }

  public static SafeZoneRepositoryImpl newInstance(SafeZoneDao safeZoneDao,
      SupabaseClientProvider supabaseProvider) {
    return new SafeZoneRepositoryImpl(safeZoneDao, supabaseProvider);
  }
}
