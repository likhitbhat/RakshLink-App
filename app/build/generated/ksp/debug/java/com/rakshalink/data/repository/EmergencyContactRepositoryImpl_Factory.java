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
public final class EmergencyContactRepositoryImpl_Factory implements Factory<EmergencyContactRepositoryImpl> {
  private final Provider<SupabaseClientProvider> supabaseProvider;

  public EmergencyContactRepositoryImpl_Factory(Provider<SupabaseClientProvider> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public EmergencyContactRepositoryImpl get() {
    return newInstance(supabaseProvider.get());
  }

  public static EmergencyContactRepositoryImpl_Factory create(
      Provider<SupabaseClientProvider> supabaseProvider) {
    return new EmergencyContactRepositoryImpl_Factory(supabaseProvider);
  }

  public static EmergencyContactRepositoryImpl newInstance(
      SupabaseClientProvider supabaseProvider) {
    return new EmergencyContactRepositoryImpl(supabaseProvider);
  }
}
