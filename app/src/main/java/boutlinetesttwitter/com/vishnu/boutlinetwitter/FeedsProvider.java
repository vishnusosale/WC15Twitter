package boutlinetesttwitter.com.vishnu.boutlinetwitter;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Vishnu on 09-Mar-15.
 */
public class FeedsProvider extends ContentProvider {

    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(FeedsContract.AUTHORITY, FeedsContract.TABLE, FeedsContract.TASKS_LIST);
        uriMatcher.addURI(FeedsContract.AUTHORITY, FeedsContract.TABLE + "/#", FeedsContract.TASKS_ITEM);
    }
    private SQLiteDatabase db;
    private FeedsDBHelper taskDBHelper;

    @Override
    public boolean onCreate() {
        boolean ret = true;
        taskDBHelper = new FeedsDBHelper(getContext());
        db = taskDBHelper.getWritableDatabase();

        if (db == null) {
            ret = false;
        }

        if (db.isReadOnly()) {
            db.close();
            db = null;
            ret = false;
        }

        return ret;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FeedsContract.TABLE);

        switch (uriMatcher.match(uri)) {
            case FeedsContract.TASKS_LIST:
                break;

            case FeedsContract.TASKS_ITEM:
                qb.appendWhere(FeedsContract.Columns._ID + " = " + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, null);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)) {
            case FeedsContract.TASKS_LIST:
                return FeedsContract.CONTENT_TYPE;

            case FeedsContract.TASKS_ITEM:
                return FeedsContract.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        if (uriMatcher.match(uri) != FeedsContract.TASKS_LIST) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        long id = db.insert(FeedsContract.TABLE, null, contentValues);

        if (id > 0) {
            return ContentUris.withAppendedId(uri, id);
        }
        throw new SQLException("Error inserting into table: " + FeedsContract.TABLE);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int deleted = 0;

        switch (uriMatcher.match(uri)) {
            case FeedsContract.TASKS_LIST:
                db.delete(FeedsContract.TABLE, selection, selectionArgs);
                break;

            case FeedsContract.TASKS_ITEM:
                String where = FeedsContract.Columns._ID + " = " + uri.getLastPathSegment();
                if (!selection.isEmpty()) {
                    where += " AND " + selection;
                }

                deleted = db.delete(FeedsContract.TABLE, where, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        int updated = 0;

        switch (uriMatcher.match(uri)) {
            case FeedsContract.TASKS_LIST:
                db.update(FeedsContract.TABLE, contentValues, s, strings);
                break;

            case FeedsContract.TASKS_ITEM:
                String where = FeedsContract.Columns._ID + " = " + uri.getLastPathSegment();
                if (!s.isEmpty()) {
                    where += " AND " + s;
                }
                updated = db.update(FeedsContract.TABLE, contentValues, where, strings);
                break;

            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        return updated;
    }
}