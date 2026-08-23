package com.rakshalink.ui.guardian;

import com.rakshalink.domain.repository.GuardianRepository;
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
public final class GuardianViewModel_Factory implements Factory<GuardianViewModel> {
  private final Provider<GuardianRepository> guardianRepositoryProvider;

  public GuardianViewModel_Factory(Provider<GuardianRepository> guardianRepositoryProvider) {
    this.guardianRepositoryProvider = guardianRepositoryProvider;
  }

  @Override
  public GuardianViewModel get() {
    return newInstance(guardianRepositoryProvider.get());
  }

  public static GuardianViewModel_Factory create(
      Provider<GuardianRepository> guardianRepositoryProvider) {
    return new GuardianViewModel_Factory(guardianRepositoryProvider);
  }

  public static GuardianViewModel newInstance(GuardianRepository guardianRepository) {
    return new GuardianViewModel(guardianRepository);
  }
}
