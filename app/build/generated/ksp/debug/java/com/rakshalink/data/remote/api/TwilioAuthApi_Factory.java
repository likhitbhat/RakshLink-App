package com.rakshalink.data.remote.api;

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
public final class TwilioAuthApi_Factory implements Factory<TwilioAuthApi> {
  @Override
  public TwilioAuthApi get() {
    return newInstance();
  }

  public static TwilioAuthApi_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TwilioAuthApi newInstance() {
    return new TwilioAuthApi();
  }

  private static final class InstanceHolder {
    private static final TwilioAuthApi_Factory INSTANCE = new TwilioAuthApi_Factory();
  }
}
