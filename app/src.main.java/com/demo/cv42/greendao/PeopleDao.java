package com.demo.cv42.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.demo.cv42.face.People;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "PEOPLE".
*/
public class PeopleDao extends AbstractDao<People, Long> {

    public static final String TABLENAME = "PEOPLE";

    /**
     * Properties of entity People.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Name = new Property(1, String.class, "name", false, "NAME");
        public final static Property Feature = new Property(2, String.class, "feature", false, "FEATURE");
        public final static Property PointsStr = new Property(3, String.class, "pointsStr", false, "POINTS_STR");
    }


    public PeopleDao(DaoConfig config) {
        super(config);
    }
    
    public PeopleDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PEOPLE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"NAME\" TEXT," + // 1: name
                "\"FEATURE\" TEXT," + // 2: feature
                "\"POINTS_STR\" TEXT);"); // 3: pointsStr
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PEOPLE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, People entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
 
        String feature = entity.getFeature();
        if (feature != null) {
            stmt.bindString(3, feature);
        }
 
        String pointsStr = entity.getPointsStr();
        if (pointsStr != null) {
            stmt.bindString(4, pointsStr);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, People entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
 
        String feature = entity.getFeature();
        if (feature != null) {
            stmt.bindString(3, feature);
        }
 
        String pointsStr = entity.getPointsStr();
        if (pointsStr != null) {
            stmt.bindString(4, pointsStr);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public People readEntity(Cursor cursor, int offset) {
        People entity = new People( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // name
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // feature
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3) // pointsStr
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, People entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setFeature(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setPointsStr(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(People entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(People entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(People entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
