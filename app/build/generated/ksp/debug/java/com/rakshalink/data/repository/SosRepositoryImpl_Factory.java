package com.rakshalink.data.repository;

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
public final class SosRepositoryImpl_Factory implements Factory<SosRepositoryImpl> {
  private final Provider<SupabaseClientProvider> supabaseProvider;

  public SosRepositoryImpl_Factory(Provider<SupabaseClientProvider> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SosRepositoryImpl get() {
    return newInstance(supabaseProvider.get());
  }

  public static SosRepositoryImpl_Factory create(
      Provider<SupabaseClientProvider> supabaseProvider) {
    return new SosRepositoryImpl_Factory(supabaseProvider);
  }

  public static SosRepositoryImpl newInstance(SupabaseClientProvider supabaseProvider) {
    return new SosRepositoryImpl(supabaseProvider);
  }
}
