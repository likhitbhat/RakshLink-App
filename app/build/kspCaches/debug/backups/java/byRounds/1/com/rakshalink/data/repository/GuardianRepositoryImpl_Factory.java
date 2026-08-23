package com.rakshalink.data.repository;

import com.rakshalink.data.local.dao.AlertDao;
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
public final class GuardianRepositoryImpl_Factory implements Factory<GuardianRepositoryImpl> {
  private final Provider<AlertDao> alertDaoProvider;

  private final Provider<SupabaseClientProvider> supabaseProvider;

  public GuardianRepositoryImpl_Factory(Provider<AlertDao> alertDaoProvider,
      Provider<SupabaseClientProvider> supabaseProvider) {
    this.alertDaoProvider = alertDaoProvider;
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public GuardianRepositoryImpl get() {
    return newInstance(alertDaoProvider.get(), supabaseProvider.get());
  }

  public static GuardianRepositoryImpl_Factory create(Provider<AlertDao> alertDaoProvider,
      Provider<SupabaseClientProvider> supabaseProvider) {
    return new GuardianRepositoryImpl_Factory(alertDaoProvider, supabaseProvider);
  }

  public static GuardianRepositoryImpl newInstance(AlertDao alertDao,
      SupabaseClientProvider supabaseProvider) {
    return new GuardianRepositoryImpl(alertDao, supabaseProvider);
  }
}
