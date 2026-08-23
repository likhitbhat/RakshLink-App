package com.rakshalink.services;

import com.rakshalink.domain.repository.BlePendantRepository;
import com.rakshalink.domain.repository.SosRepository;
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
public final class BleService_MembersInjector implements MembersInjector<BleService> {
  private final Provider<BlePendantRepository> bleRepositoryProvider;

  private final Provider<SosRepository> sosRepositoryProvider;

  public BleService_MembersInjector(Provider<BlePendantRepository> bleRepositoryProvider,
      Provider<SosRepository> sosRepositoryProvider) {
    this.bleRepositoryProvider = bleRepositoryProvider;
    this.sosRepositoryProvider = sosRepositoryProvider;
  }

  public static MembersInjector<BleService> create(
      Provider<BlePendantRepository> bleRepositoryProvider,
      Provider<SosRepository> sosRepositoryProvider) {
    return new BleService_MembersInjector(bleRepositoryProvider, sosRepositoryProvider);
  }

  @Override
  public void injectMembers(BleService instance) {
    injectBleRepository(instance, bleRepositoryProvider.get());
    injectSosRepository(instance, sosRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.rakshalink.services.BleService.bleRepository")
  public static void injectBleRepository(BleService instance, BlePendantRepository bleRepository) {
    instance.bleRepository = bleRepository;
  }

  @InjectedFieldSignature("com.rakshalink.services.BleService.sosRepository")
  public static void injectSosRepository(BleService instance, SosRepository sosRepository) {
    instance.sosRepository = sosRepository;
  }
}
