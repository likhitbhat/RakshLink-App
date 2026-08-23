package com.rakshalink.di;

import com.rakshalink.data.local.dao.SafeZoneDao;
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
public final class DatabaseModule_ProvideSafeZoneDaoFactory implements Factory<SafeZoneDao> {
  private final Provider<RakshaLinkDatabase> dbProvider;

  public DatabaseModule_ProvideSafeZoneDaoFactory(Provider<RakshaLinkDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SafeZoneDao get() {
    return provideSafeZoneDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSafeZoneDaoFactory create(
      Provider<RakshaLinkDatabase> dbProvider) {
    return new DatabaseModule_ProvideSafeZoneDaoFactory(dbProvider);
  }

  public static SafeZoneDao provideSafeZoneDao(RakshaLinkDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSafeZoneDao(db));
  }
}
