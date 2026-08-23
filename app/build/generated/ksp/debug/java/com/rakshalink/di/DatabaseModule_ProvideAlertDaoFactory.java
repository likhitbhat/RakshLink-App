package com.rakshalink.di;

import com.rakshalink.data.local.dao.AlertDao;
import com.rakshalink.data.local.database.RakshaLinkDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideAlertDaoFactory implements Factory<AlertDao> {
  private final Provider<RakshaLinkDatabase> dbProvider;

  public DatabaseModule_ProvideAlertDaoFactory(Provider<RakshaLinkDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AlertDao get() {
    return provideAlertDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideAlertDaoFactory create(
      Provider<RakshaLinkDatabase> dbProvider) {
    return new DatabaseModule_ProvideAlertDaoFactory(dbProvider);
  }

  public static AlertDao provideAlertDao(RakshaLinkDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAlertDao(db));
  }
}
