package boutlinetesttwitter.com.vishnu.boutlinetwitter;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends ActionBarActivity {

    ListView twitterFeedListView;
    ProgressDialog mProgressDialog;

    private ListAdapter listAdapter;
    private FeedsDBHelper helper;

    private Timer timer;
    private TimerTask timerTask;

    Cursor cursor;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        twitterFeedListView = (ListView) findViewById(R.id.twitterFeedListView);

        InternetState state = new InternetState(this);
        if (state.isConnectingToInternet()) {
            new getTwitterStatusFromInternet().execute();

        } else {
            getTweetsFromDb();
        }
    }

    private void getTweetsFromDb()
    {
        Toast.makeText(getApplicationContext(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
        updateUI();
    }


    private void insert(String myTweets) {
        helper = new FeedsDBHelper(MainActivity.this);
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.clear();
        values.put(FeedsContract.Columns.STATUS, myTweets);

        Uri uri = FeedsContract.CONTENT_URI;
        getApplicationContext().getContentResolver().insert(uri, values);
    }

    private void updateUI() {


        Uri uri = FeedsContract.CONTENT_URI;
        cursor = this.getContentResolver().query(uri, null, null, null, null);

        listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.tweets_row_item,
                cursor,
                new String[]{FeedsContract.Columns.STATUS},
                new int[]{R.id.tweets},
                0
        );

        twitterFeedListView.setAdapter(listAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        if (cursor!=null)
        {
            cursor.close();
        }
        if (db!=null)
        {
            db.close();
        }
    }

    public void onResume(){
        super.onResume();
        try {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            InternetState state = new InternetState(getApplicationContext());
                            if (state.isConnectingToInternet()) {
                                new getTwitterStatusFromInternet().execute();

                            } else {
                                getTweetsFromDb();
                            }
                        }
                    });
                }
            };
            timer.schedule(timerTask, 30000, 30000);
        } catch (IllegalStateException e){
            Log.i("timer", "resume error");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_refresh) {
            InternetState state = new InternetState(this);
            if (state.isConnectingToInternet()) {
                new getTwitterStatusFromInternet().execute();

            } else {
                getTweetsFromDb();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class getTwitterStatusFromInternet extends AsyncTask<Void, Void, Void> {

        ArrayAdapter<String> stringTweetAdapter;
        List<twitter4j.Status> tweets;
        List<String> stringStatuses = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Loading New Tweets");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {


            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(getString(R.string.OAuthConsumerKey))
                    .setOAuthConsumerSecret(getString(R.string.OAuthConsumerSecret))
                    .setOAuthAccessToken(getString(R.string.OAuthAccessToken))
                    .setOAuthAccessTokenSecret(getString(R.string.OAuthAccessTokenSecret));

            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();
            try {
                Query query = new Query("WorldCup");
                //query.count(15);
                QueryResult result;
                result = twitter.search(query);
                tweets = result.getTweets();


                for (twitter4j.Status tweet : tweets) {
                    String myTweets = ("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                    stringStatuses.add(myTweets);
                    insert(myTweets);


                }

            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to search tweets: " + te.getMessage());

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mProgressDialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stringTweetAdapter = new ArrayAdapter<>(MainActivity.this,
                            R.layout.tweets_row_item, R.id.tweets, stringStatuses);
                    twitterFeedListView.setAdapter(stringTweetAdapter);
                }
            });
        }
    }
}
