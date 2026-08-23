package com.rakshalink.di;

import com.rakshalink.data.local.dao.LocationDao;
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
public final class DatabaseModule_ProvideLocationDaoFactory implements Factory<LocationDao> {
  private final Provider<RakshaLinkDatabase> dbProvider;

  public DatabaseModule_ProvideLocationDaoFactory(Provider<RakshaLinkDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public LocationDao get() {
    return provideLocationDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideLocationDaoFactory create(
      Provider<RakshaLinkDatabase> dbProvider) {
    return new DatabaseModule_ProvideLocationDaoFactory(dbProvider);
  }

  public static LocationDao provideLocationDao(RakshaLinkDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideLocationDao(db));
  }
}
