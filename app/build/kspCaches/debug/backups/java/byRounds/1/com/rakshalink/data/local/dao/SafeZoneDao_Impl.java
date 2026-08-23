package com.rakshalink.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.rakshalink.data.local.entities.CachedSafeZoneEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SafeZoneDao_Impl implements SafeZoneDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CachedSafeZoneEntity> __insertionAdapterOfCachedSafeZoneEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSafeZone;

  public SafeZoneDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCachedSafeZoneEntity = new EntityInsertionAdapter<CachedSafeZoneEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_safe_zones` (`id`,`userId`,`name`,`latitude`,`longitude`,`radiusMeters`,`entryNotification`,`exitNotification`,`isEnabled`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedSafeZoneEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getName());
        statement.bindDouble(4, entity.getLatitude());
        statement.bindDouble(5, entity.getLongitude());
        statement.bindDouble(6, entity.getRadiusMeters());
        final int _tmp = entity.getEntryNotification() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.getExitNotification() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.isEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
      }
    };
    this.__preparedStmtOfDeleteSafeZone = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_safe_zones WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSafeZones(final List<CachedSafeZoneEntity> zones,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedSafeZoneEntity.insert(zones);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSafeZone(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSafeZone.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSafeZone.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CachedSafeZoneEntity>> getActiveSafeZones() {
    final String _sql = "SELECT * FROM cached_safe_zones WHERE isEnabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cached_safe_zones"}, new Callable<List<CachedSafeZoneEntity>>() {
      @Override
      @NonNull
      public List<CachedSafeZoneEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRadiusMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "radiusMeters");
          final int _cursorIndexOfEntryNotification = CursorUtil.getColumnIndexOrThrow(_cursor, "entryNotification");
          final int _cursorIndexOfExitNotification = CursorUtil.getColumnIndexOrThrow(_cursor, "exitNotification");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<CachedSafeZoneEntity> _result = new ArrayList<CachedSafeZoneEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedSafeZoneEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final float _tmpRadiusMeters;
            _tmpRadiusMeters = _cursor.getFloat(_cursorIndexOfRadiusMeters);
            final boolean _tmpEntryNotification;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEntryNotification);
            _tmpEntryNotification = _tmp != 0;
            final boolean _tmpExitNotification;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfExitNotification);
            _tmpExitNotification = _tmp_1 != 0;
            final boolean _tmpIsEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_2 != 0;
            _item = new CachedSafeZoneEntity(_tmpId,_tmpUserId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpRadiusMeters,_tmpEntryNotification,_tmpExitNotification,_tmpIsEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
