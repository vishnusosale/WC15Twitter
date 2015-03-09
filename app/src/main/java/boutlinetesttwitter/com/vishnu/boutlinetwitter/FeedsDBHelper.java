package boutlinetesttwitter.com.vishnu.boutlinetwitter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class FeedsDBHelper extends SQLiteOpenHelper {

    public FeedsDBHelper(Context context) {
        super(context, FeedsContract.DB_NAME, null, FeedsContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
        String sqlQuery =
                String.format("CREATE TABLE %s (" +
                                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT)", FeedsContract.TABLE,
                        FeedsContract.Columns.STATUS);

        Log.d("TaskDBHelper", "Query to form table: " + sqlQuery);
        sqlDB.execSQL(sqlQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
        sqlDB.execSQL("DROP TABLE IF EXISTS " + FeedsContract.TABLE);
        onCreate(sqlDB);
    }
}
