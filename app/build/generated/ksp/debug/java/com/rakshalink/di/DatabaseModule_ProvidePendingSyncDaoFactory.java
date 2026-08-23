package com.rakshalink.di;

import com.rakshalink.data.local.dao.PendingSyncDao;
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
public final class DatabaseModule_ProvidePendingSyncDaoFactory implements Factory<PendingSyncDao> {
  private final Provider<RakshaLinkDatabase> dbProvider;

  public DatabaseModule_ProvidePendingSyncDaoFactory(Provider<RakshaLinkDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PendingSyncDao get() {
    return providePendingSyncDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePendingSyncDaoFactory create(
      Provider<RakshaLinkDatabase> dbProvider) {
    return new DatabaseModule_ProvidePendingSyncDaoFactory(dbProvider);
  }

  public static PendingSyncDao providePendingSyncDao(RakshaLinkDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePendingSyncDao(db));
  }
}
