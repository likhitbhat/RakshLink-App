package com.rakshalink.services;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class InactivityTracker_Factory implements Factory<InactivityTracker> {
  @Override
  public InactivityTracker get() {
    return newInstance();
  }

  public static InactivityTracker_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static InactivityTracker newInstance() {
    return new InactivityTracker();
  }

  private static final class InstanceHolder {
    private static final InactivityTracker_Factory INSTANCE = new InactivityTracker_Factory();
  }
}
