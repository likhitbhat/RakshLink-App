package com.rakshalink.data.repository;

import com.rakshalink.data.preferences.UserPreferencesManager;
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<SupabaseClientProvider> supabaseProvider;

  private final Provider<UserPreferencesManager> userPreferencesManagerProvider;

  public AuthRepositoryImpl_Factory(Provider<SupabaseClientProvider> supabaseProvider,
      Provider<UserPreferencesManager> userPreferencesManagerProvider) {
    this.supabaseProvider = supabaseProvider;
    this.userPreferencesManagerProvider = userPreferencesManagerProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(supabaseProvider.get(), userPreferencesManagerProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<SupabaseClientProvider> supabaseProvider,
      Provider<UserPreferencesManager> userPreferencesManagerProvider) {
    return new AuthRepositoryImpl_Factory(supabaseProvider, userPreferencesManagerProvider);
  }

  public static AuthRepositoryImpl newInstance(SupabaseClientProvider supabaseProvider,
      UserPreferencesManager userPreferencesManager) {
    return new AuthRepositoryImpl(supabaseProvider, userPreferencesManager);
  }
}
