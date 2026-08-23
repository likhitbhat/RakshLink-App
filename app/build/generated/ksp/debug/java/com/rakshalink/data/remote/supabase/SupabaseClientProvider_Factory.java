package com.rakshalink.data.remote.supabase;

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
public final class SupabaseClientProvider_Factory implements Factory<SupabaseClientProvider> {
  @Override
  public SupabaseClientProvider get() {
    return newInstance();
  }

  public static SupabaseClientProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SupabaseClientProvider newInstance() {
    return new SupabaseClientProvider();
  }

  private static final class InstanceHolder {
    private static final SupabaseClientProvider_Factory INSTANCE = new SupabaseClientProvider_Factory();
  }
}
