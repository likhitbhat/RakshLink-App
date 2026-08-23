package com.rakshalink.ui.auth;

import com.rakshalink.domain.repository.TwilioAuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class TwilioAuthViewModel_Factory implements Factory<TwilioAuthViewModel> {
  private final Provider<TwilioAuthRepository> twilioAuthRepositoryProvider;

  public TwilioAuthViewModel_Factory(Provider<TwilioAuthRepository> twilioAuthRepositoryProvider) {
    this.twilioAuthRepositoryProvider = twilioAuthRepositoryProvider;
  }

  @Override
  public TwilioAuthViewModel get() {
    return newInstance(twilioAuthRepositoryProvider.get());
  }

  public static TwilioAuthViewModel_Factory create(
      Provider<TwilioAuthRepository> twilioAuthRepositoryProvider) {
    return new TwilioAuthViewModel_Factory(twilioAuthRepositoryProvider);
  }

  public static TwilioAuthViewModel newInstance(TwilioAuthRepository twilioAuthRepository) {
    return new TwilioAuthViewModel(twilioAuthRepository);
  }
}
