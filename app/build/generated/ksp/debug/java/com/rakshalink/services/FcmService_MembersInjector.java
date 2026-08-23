package com.rakshalink.services;

import com.rakshalink.data.remote.supabase.SupabaseClientProvider;
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
public final class FcmService_MembersInjector implements MembersInjector<FcmService> {
  private final Provider<SupabaseClientProvider> supabaseProvider;

  public FcmService_MembersInjector(Provider<SupabaseClientProvider> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  public static MembersInjector<FcmService> create(
      Provider<SupabaseClientProvider> supabaseProvider) {
    return new FcmService_MembersInjector(supabaseProvider);
  }

  @Override
  public void injectMembers(FcmService instance) {
    injectSupabaseProvider(instance, supabaseProvider.get());
  }

  @InjectedFieldSignature("com.rakshalink.services.FcmService.supabaseProvider")
  public static void injectSupabaseProvider(FcmService instance,
      SupabaseClientProvider supabaseProvider) {
    instance.supabaseProvider = supabaseProvider;
  }
}
