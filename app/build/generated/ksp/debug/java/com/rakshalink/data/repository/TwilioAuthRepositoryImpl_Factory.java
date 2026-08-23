package com.rakshalink.data.repository;

import com.rakshalink.data.preferences.UserPreferencesManager;
import com.rakshalink.data.remote.api.TwilioAuthApi;
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
public final class TwilioAuthRepositoryImpl_Factory implements Factory<TwilioAuthRepositoryImpl> {
  private final Provider<TwilioAuthApi> twilioAuthApiProvider;

  private final Provider<UserPreferencesManager> userPreferencesManagerProvider;

  public TwilioAuthRepositoryImpl_Factory(Provider<TwilioAuthApi> twilioAuthApiProvider,
      Provider<UserPreferencesManager> userPreferencesManagerProvider) {
    this.twilioAuthApiProvider = twilioAuthApiProvider;
    this.userPreferencesManagerProvider = userPreferencesManagerProvider;
  }

  @Override
  public TwilioAuthRepositoryImpl get() {
    return newInstance(twilioAuthApiProvider.get(), userPreferencesManagerProvider.get());
  }

  public static TwilioAuthRepositoryImpl_Factory create(
      Provider<TwilioAuthApi> twilioAuthApiProvider,
      Provider<UserPreferencesManager> userPreferencesManagerProvider) {
    return new TwilioAuthRepositoryImpl_Factory(twilioAuthApiProvider, userPreferencesManagerProvider);
  }

  public static TwilioAuthRepositoryImpl newInstance(TwilioAuthApi twilioAuthApi,
      UserPreferencesManager userPreferencesManager) {
    return new TwilioAuthRepositoryImpl(twilioAuthApi, userPreferencesManager);
  }
}
