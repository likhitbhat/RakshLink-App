package com.rakshalink;

import com.rakshalink.services.InactivityTracker;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<InactivityTracker> inactivityTrackerProvider;

  public MainActivity_MembersInjector(Provider<InactivityTracker> inactivityTrackerProvider) {
    this.inactivityTrackerProvider = inactivityTrackerProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<InactivityTracker> inactivityTrackerProvider) {
    return new MainActivity_MembersInjector(inactivityTrackerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectInactivityTracker(instance, inactivityTrackerProvider.get());
  }

  @InjectedFieldSignature("com.rakshalink.MainActivity.inactivityTracker")
  public static void injectInactivityTracker(MainActivity instance,
      InactivityTracker inactivityTracker) {
    instance.inactivityTracker = inactivityTracker;
  }
}
