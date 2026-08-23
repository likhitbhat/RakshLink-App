package com.rakshalink.data.local.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.rakshalink.data.local.dao.AlertDao;
import com.rakshalink.data.local.dao.AlertDao_Impl;
import com.rakshalink.data.local.dao.LocationDao;
import com.rakshalink.data.local.dao.LocationDao_Impl;
import com.rakshalink.data.local.dao.PendingSyncDao;
import com.rakshalink.data.local.dao.PendingSyncDao_Impl;
import com.rakshalink.data.local.dao.SafeZoneDao;
import com.rakshalink.data.local.dao.SafeZoneDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RakshaLinkDatabase_Impl extends RakshaLinkDatabase {
  private volatile LocationDao _locationDao;

  private volatile AlertDao _alertDao;

  private volatile SafeZoneDao _safeZoneDao;

  private volatile PendingSyncDao _pendingSyncDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_locations` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `accuracy` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `isSynced` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_alerts` (`id` TEXT NOT NULL, `wearerId` TEXT NOT NULL, `wearerName` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `latitude` REAL, `longitude` REAL, `timestamp` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, `isResolved` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_safe_zones` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `name` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `radiusMeters` REAL NOT NULL, `entryNotification` INTEGER NOT NULL, `exitNotification` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_sync` (`syncId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemType` TEXT NOT NULL, `payloadJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b99139687174a067f949cc93dcca5c43')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `cached_locations`");
        db.execSQL("DROP TABLE IF EXISTS `cached_alerts`");
        db.execSQL("DROP TABLE IF EXISTS `cached_safe_zones`");
        db.execSQL("DROP TABLE IF EXISTS `pending_sync`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCachedLocations = new HashMap<String, TableInfo.Column>(7);
        _columnsCachedLocations.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedLocations.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedLocations.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedLocations.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedLocations.put("accuracy", new TableInfo.Column("accuracy", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedLocations.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedLocations.put("isSynced", new TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedLocations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedLocations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedLocations = new TableInfo("cached_locations", _columnsCachedLocations, _foreignKeysCachedLocations, _indicesCachedLocations);
        final TableInfo _existingCachedLocations = TableInfo.read(db, "cached_locations");
        if (!_infoCachedLocations.equals(_existingCachedLocations)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_locations(com.rakshalink.data.local.entities.CachedLocationEntity).\n"
                  + " Expected:\n" + _infoCachedLocations + "\n"
                  + " Found:\n" + _existingCachedLocations);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedAlerts = new HashMap<String, TableInfo.Column>(11);
        _columnsCachedAlerts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("wearerId", new TableInfo.Column("wearerId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("wearerName", new TableInfo.Column("wearerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("message", new TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("latitude", new TableInfo.Column("latitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("longitude", new TableInfo.Column("longitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("isRead", new TableInfo.Column("isRead", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedAlerts.put("isResolved", new TableInfo.Column("isResolved", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedAlerts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedAlerts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedAlerts = new TableInfo("cached_alerts", _columnsCachedAlerts, _foreignKeysCachedAlerts, _indicesCachedAlerts);
        final TableInfo _existingCachedAlerts = TableInfo.read(db, "cached_alerts");
        if (!_infoCachedAlerts.equals(_existingCachedAlerts)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_alerts(com.rakshalink.data.local.entities.CachedAlertEntity).\n"
                  + " Expected:\n" + _infoCachedAlerts + "\n"
                  + " Found:\n" + _existingCachedAlerts);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedSafeZones = new HashMap<String, TableInfo.Column>(9);
        _columnsCachedSafeZones.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedSafeZones.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedSafeZones.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedSafeZones.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedSafeZones.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedSafeZones.put("radiusMeters", new TableInfo.Column("radiusMeters", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedSafeZones.put("entryNotification", new TableInfo.Column("entryNotification", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedSafeZones.put("exitNotification", new TableInfo.Column("exitNotification", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedSafeZones.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedSafeZones = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedSafeZones = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedSafeZones = new TableInfo("cached_safe_zones", _columnsCachedSafeZones, _foreignKeysCachedSafeZones, _indicesCachedSafeZones);
        final TableInfo _existingCachedSafeZones = TableInfo.read(db, "cached_safe_zones");
        if (!_infoCachedSafeZones.equals(_existingCachedSafeZones)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_safe_zones(com.rakshalink.data.local.entities.CachedSafeZoneEntity).\n"
                  + " Expected:\n" + _infoCachedSafeZones + "\n"
                  + " Found:\n" + _existingCachedSafeZones);
        }
        final HashMap<String, TableInfo.Column> _columnsPendingSync = new HashMap<String, TableInfo.Column>(4);
        _columnsPendingSync.put("syncId", new TableInfo.Column("syncId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingSync.put("itemType", new TableInfo.Column("itemType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingSync.put("payloadJson", new TableInfo.Column("payloadJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingSync.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPendingSync = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPendingSync = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPendingSync = new TableInfo("pending_sync", _columnsPendingSync, _foreignKeysPendingSync, _indicesPendingSync);
        final TableInfo _existingPendingSync = TableInfo.read(db, "pending_sync");
        if (!_infoPendingSync.equals(_existingPendingSync)) {
          return new RoomOpenHelper.ValidationResult(false, "pending_sync(com.rakshalink.data.local.entities.PendingSyncEntity).\n"
                  + " Expected:\n" + _infoPendingSync + "\n"
                  + " Found:\n" + _existingPendingSync);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "b99139687174a067f949cc93dcca5c43", "bad7a5d3bfc9443cf117f9470565a5ec");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "cached_locations","cached_alerts","cached_safe_zones","pending_sync");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `cached_locations`");
      _db.execSQL("DELETE FROM `cached_alerts`");
      _db.execSQL("DELETE FROM `cached_safe_zones`");
      _db.execSQL("DELETE FROM `pending_sync`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(LocationDao.class, LocationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AlertDao.class, AlertDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SafeZoneDao.class, SafeZoneDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PendingSyncDao.class, PendingSyncDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public LocationDao locationDao() {
    if (_locationDao != null) {
      return _locationDao;
    } else {
      synchronized(this) {
        if(_locationDao == null) {
          _locationDao = new LocationDao_Impl(this);
        }
        return _locationDao;
      }
    }
  }

  @Override
  public AlertDao alertDao() {
    if (_alertDao != null) {
      return _alertDao;
    } else {
      synchronized(this) {
        if(_alertDao == null) {
          _alertDao = new AlertDao_Impl(this);
        }
        return _alertDao;
      }
    }
  }

  @Override
  public SafeZoneDao safeZoneDao() {
    if (_safeZoneDao != null) {
      return _safeZoneDao;
    } else {
      synchronized(this) {
        if(_safeZoneDao == null) {
          _safeZoneDao = new SafeZoneDao_Impl(this);
        }
        return _safeZoneDao;
      }
    }
  }

  @Override
  public PendingSyncDao pendingSyncDao() {
    if (_pendingSyncDao != null) {
      return _pendingSyncDao;
    } else {
      synchronized(this) {
        if(_pendingSyncDao == null) {
          _pendingSyncDao = new PendingSyncDao_Impl(this);
        }
        return _pendingSyncDao;
      }
    }
  }
}
