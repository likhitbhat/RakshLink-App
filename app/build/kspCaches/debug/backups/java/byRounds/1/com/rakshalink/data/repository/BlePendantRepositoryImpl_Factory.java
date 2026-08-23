package com.rakshalink.data.repository;

import android.content.Context;
import com.rakshalink.domain.repository.SosRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class BlePendantRepositoryImpl_Factory implements Factory<BlePendantRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<SosRepository> sosRepositoryProvider;

  public BlePendantRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<SosRepository> sosRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.sosRepositoryProvider = sosRepositoryProvider;
  }

  @Override
  public BlePendantRepositoryImpl get() {
    return newInstance(contextProvider.get(), sosRepositoryProvider.get());
  }

  public static BlePendantRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<SosRepository> sosRepositoryProvider) {
    return new BlePendantRepositoryImpl_Factory(contextProvider, sosRepositoryProvider);
  }

  public static BlePendantRepositoryImpl newInstance(Context context, SosRepository sosRepository) {
    return new BlePendantRepositoryImpl(context, sosRepository);
  }
}
