package com.rakshalink.services;

import com.rakshalink.data.preferences.UserPreferencesManager;
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
public final class SosService_MembersInjector implements MembersInjector<SosService> {
  private final Provider<UserPreferencesManager> userPreferencesManagerProvider;

  public SosService_MembersInjector(
      Provider<UserPreferencesManager> userPreferencesManagerProvider) {
    this.userPreferencesManagerProvider = userPreferencesManagerProvider;
  }

  public static MembersInjector<SosService> create(
      Provider<UserPreferencesManager> userPreferencesManagerProvider) {
    return new SosService_MembersInjector(userPreferencesManagerProvider);
  }

  @Override
  public void injectMembers(SosService instance) {
    injectUserPreferencesManager(instance, userPreferencesManagerProvider.get());
  }

  @InjectedFieldSignature("com.rakshalink.services.SosService.userPreferencesManager")
  public static void injectUserPreferencesManager(SosService instance,
      UserPreferencesManager userPreferencesManager) {
    instance.userPreferencesManager = userPreferencesManager;
  }
}
