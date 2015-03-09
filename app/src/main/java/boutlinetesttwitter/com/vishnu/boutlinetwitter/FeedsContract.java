package boutlinetesttwitter.com.vishnu.boutlinetwitter;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Vishnu on 09-Mar-15.
 */
public class FeedsContract {

    public static final String DB_NAME = "boutlinetesttwitter.com.vishnu.boutlinetwitter.feeds";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "feeds";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/example.tasksDB/" + TABLE;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/example/tasksDB" + TABLE;
    public static final String AUTHORITY = "boutlinetesttwitter.com.vishnu.boutlinetwitter.twitterFeeds";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE);
    public static final int TASKS_LIST = 1;
    public static final int TASKS_ITEM = 2;

    public class Columns {
        public static final String STATUS = "status";
        public static final String _ID = BaseColumns._ID;
    }
}
