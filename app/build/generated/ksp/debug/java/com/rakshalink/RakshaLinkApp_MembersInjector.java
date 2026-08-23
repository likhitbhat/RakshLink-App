package com.rakshalink;

import com.rakshalink.services.NotificationHelper;
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
public final class RakshaLinkApp_MembersInjector implements MembersInjector<RakshaLinkApp> {
  private final Provider<NotificationHelper> notificationHelperProvider;

  public RakshaLinkApp_MembersInjector(Provider<NotificationHelper> notificationHelperProvider) {
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public static MembersInjector<RakshaLinkApp> create(
      Provider<NotificationHelper> notificationHelperProvider) {
    return new RakshaLinkApp_MembersInjector(notificationHelperProvider);
  }

  @Override
  public void injectMembers(RakshaLinkApp instance) {
    injectNotificationHelper(instance, notificationHelperProvider.get());
  }

  @InjectedFieldSignature("com.rakshalink.RakshaLinkApp.notificationHelper")
  public static void injectNotificationHelper(RakshaLinkApp instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }
}
