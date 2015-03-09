package boutlinetesttwitter.com.vishnu.boutlinetwitter;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        twitterFeedListView = (ListView) findViewById(R.id.twitterFeedListView);

        InternetState state = new InternetState(this);


        if (state.isConnectingToInternet()) {
            new getTwitterStatusFromInternet().execute();

        } else {
            updateUI();
        }
    }

    private void insert(String myTweets) {
        helper = new FeedsDBHelper(MainActivity.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.clear();
        values.put(FeedsContract.Columns.STATUS, myTweets);

        Uri uri = FeedsContract.CONTENT_URI;
        getApplicationContext().getContentResolver().insert(uri, values);
    }

    private void updateUI() {
        Uri uri = FeedsContract.CONTENT_URI;
        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);

        listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.tweets_row_item,
                cursor,
                new String[]{FeedsContract.Columns.STATUS},
                new int[]{R.id.task_description},
                0
        );

        twitterFeedListView.setAdapter(listAdapter);
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
            new getTwitterStatusFromInternet().execute();
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
            cb.setOAuthConsumerKey("ConsumerKey")
                    .setOAuthConsumerSecret("ConsumerSecret")
                    .setOAuthAccessToken("AccessToken")
                    .setOAuthAccessTokenSecret("AccessTokenSecret");

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
                            R.layout.tweets_row_item, R.id.task_description, stringStatuses);
                    twitterFeedListView.setAdapter(stringTweetAdapter);
                }
            });
        }
    }
}
