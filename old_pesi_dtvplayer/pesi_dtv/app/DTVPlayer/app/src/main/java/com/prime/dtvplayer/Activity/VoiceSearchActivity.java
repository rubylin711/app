package com.prime.dtvplayer.Activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import androidx.leanback.app.SearchFragment;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.SpeechRecognitionCallback;
import android.util.Log;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.util.ArrayList;
import java.util.List;

public class VoiceSearchActivity extends DTVActivity {
    private static final String TAG = "VoiceSearchActivity";
    private static final int REQUEST_SPEECH = 1;
    private SearchFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_search);

        //addChannelData(getChannelList());
        printChannelData(this);
        mFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
        SpeechRecognitionCallback mSpeechRecognitionCallback = () -> {
            Log.d(TAG, "recognizeSpeech: ");
            startActivityForResult(mFragment.getRecognizerIntent(), REQUEST_SPEECH);
        };
        mFragment.setSpeechRecognitionCallback(mSpeechRecognitionCallback);
        mFragment.setOnItemViewClickedListener(new PlayChannelListener(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode +
                " resultCode=" + resultCode +
                " data=" + data);
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK) {
            mFragment.setSearchQuery(data, true);
        }
    }

    public List<SimpleChannel> getChannelList()
    {
        List<SimpleChannel> channelList = this.ViewHistory.getCurChannelList();
        if (channelList == null)
            return new ArrayList<>();
        else
            return channelList;
    }

    /*public void addChannelData (List<SimpleChannel> channelList)
    {
        ContentValues initialValues;
        long time = System.currentTimeMillis();
        for (int i = 0; i < channelList.size(); i++)
        {
            SimpleChannel channel = channelList.get(i);
            initialValues = new ContentValues();
            initialValues.put(ChannelDatabase.KEY_CHANNEL_ID, channel.getChannelId());
            initialValues.put(ChannelDatabase.KEY_CHANNEL_NUM, channel.getChannelNum());
            initialValues.put(ChannelDatabase.KEY_CHANNEL_NAME, channel.getChannelName());
            //initialValues.put(ChannelDatabase.KEY_CHANNEL_SKIP, channel.getChannelSkip());
            //initialValues.put(ChannelDatabase.KEY_PVR_SKIP, channel.getPVRSkip());
            //initialValues.put(ChannelDatabase.KEY_USER_LOCK, channel.getUserLock());
            //initialValues.put(ChannelDatabase.KEY_CA, channel.getCA());
            //initialValues.put(ChannelDatabase.KEY_TP_ID, channel.getTpId());
            ChannelDatabase.getDatabase(this).insert(initialValues);
            //Log.d(TAG, "addChannelData: "+channel.getChannelNum()+", "+channel.getChannelName());
        }
        Log.d(TAG, "addChannelData: time="+((System.currentTimeMillis()-time)));
        Log.d(TAG, "addChannelData: time="+((System.currentTimeMillis()-time)/1000));
    }*/

    public void printChannelData (Context context)
    {
        ChannelDatabase db = ChannelDatabase.getDatabase(context) ;
        Cursor cursor = db.getAll() ;
        //Cursor cursor = db.get("Hello Hello! can ");
        //Cursor cursor = db.get("Spider_Man");
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(ChannelDatabase.KEY_CHANNEL_ID));
            int type = cursor.getInt(cursor.getColumnIndex(ChannelDatabase.KEY_CHANNEL_TYPE));
            int num = cursor.getInt(cursor.getColumnIndex(ChannelDatabase.KEY_CHANNEL_NUM));
            String name = cursor.getString(cursor.getColumnIndex(ChannelDatabase.KEY_CHANNEL_NAME));
            /*int ch_skip = cursor.getInt(cursor.getColumnIndex(ChannelDatabase.KEY_CHANNEL_SKIP));
            int pvr_skip = cursor.getInt(cursor.getColumnIndex(ChannelDatabase.KEY_PVR_SKIP));
            int user_lock = cursor.getInt(cursor.getColumnIndex(ChannelDatabase.KEY_USER_LOCK));
            int ca = cursor.getInt(cursor.getColumnIndex(ChannelDatabase.KEY_CA));
            int tp_id = cursor.getInt(cursor.getColumnIndex(ChannelDatabase.KEY_TP_ID));*/
            Log.d(TAG, "printChannelData: id="+id
                    +", type="+type
                    +", num="+num
                    +", channel_name=" + name
                    /*+", ch_skip=" + ch_skip
                    +", pvr_skip=" + pvr_skip
                    +", user_lock=" + user_lock
                    +", ca=" + ca
                    +", tp_id=" + tp_id*/
            );
        }
    }

    private static class PlayChannelListener implements OnItemViewClickedListener {
        Context mContext;
        DTVActivity mDTV;

        PlayChannelListener(Context context)
        {
            mContext = context;
            mDTV = (DTVActivity) mContext;
        }

        @Override
        public void onItemClicked(Presenter.ViewHolder viewHolder, Object o, RowPresenter.ViewHolder viewHolder1, Row row)
        {
            SimpleChannel channel = (SimpleChannel) o;
            mDTV.AvControlPlayByChannelId(mDTV.ViewHistory.getPlayId(), channel.getChannelId(), ProgramInfo.ALL_TV_TYPE, 1);
            mDTV.finish();
        }
    }

    public static class ChannelDatabase extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        private static ChannelDatabase mChannelDatabase = null ;
        private static SQLiteDatabase mReadOnlyDB;
        private static final String DB_NAME = "ChannelDatabase" ;
        private static final String TABLE_NAME = "channel_item" ;

        public static final String KEY_CHANNEL_ID = "channel_id";
        public static final String KEY_CHANNEL_TYPE = "channel_type";
        public static final String KEY_CHANNEL_NUM = "channel_num";
        public static final String KEY_CHANNEL_NAME = "channel_name";
        /*public static final String KEY_CHANNEL_SKIP = "channel_skip";
        public static final String KEY_PVR_SKIP = "pvr_skip";
        public static final String KEY_USER_LOCK = "user_lock";
        public static final String KEY_CA = "ca";
        public static final String KEY_TP_ID = "tp_id";
        public static final String KEY_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
        public static final String KEY_DESCRIPTION = SearchManager.SUGGEST_COLUMN_TEXT_2;
        public static final String KEY_ICON = SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE;
        public static final String KEY_DATA_TYPE = SearchManager.SUGGEST_COLUMN_CONTENT_TYPE;
        public static final String KEY_IS_LIVE = SearchManager.SUGGEST_COLUMN_IS_LIVE;
        public static final String KEY_VIDEO_WIDTH = SearchManager.SUGGEST_COLUMN_VIDEO_WIDTH;
        public static final String KEY_VIDEO_HEIGHT = SearchManager.SUGGEST_COLUMN_VIDEO_HEIGHT;
        public static final String KEY_AUDIO_CHANNEL_CONFIG = SearchManager.SUGGEST_COLUMN_AUDIO_CHANNEL_CONFIG;
        public static final String KEY_PURCHASE_PRICE = SearchManager.SUGGEST_COLUMN_PURCHASE_PRICE;
        public static final String KEY_RENTAL_PRICE = SearchManager.SUGGEST_COLUMN_RENTAL_PRICE;
        public static final String KEY_RATING_STYLE = SearchManager.SUGGEST_COLUMN_RATING_STYLE;
        public static final String KEY_RATING_SCORE = SearchManager.SUGGEST_COLUMN_RATING_SCORE;
        public static final String KEY_PRODUCTION_YEAR = SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR;
        public static final String KEY_COLUMN_DURATION = SearchManager.SUGGEST_COLUMN_DURATION;
        public static final String KEY_ACTION = SearchManager.SUGGEST_COLUMN_INTENT_ACTION;
        public static final String KEY_INTENT_DATA = SearchManager.SUGGEST_COLUMN_INTENT_DATA;*/

        private static final String CREATE_APP_TABLE =
                "CREATE VIRTUAL TABLE " + TABLE_NAME + " USING fts4 ("
                        + KEY_CHANNEL_ID + " , "
                        + KEY_CHANNEL_TYPE + " , "
                        + KEY_CHANNEL_NUM + " , "
                        + KEY_CHANNEL_NAME + " );";
                        /*+ KEY_CHANNEL_SKIP + " , "
                        + KEY_PVR_SKIP + " , "
                        + KEY_USER_LOCK + " , "
                        + KEY_CA + " , "
                        + KEY_TP_ID + " , "
                        + KEY_NAME + " , "
                        + KEY_DESCRIPTION + " , "
                        + KEY_ICON + " , "
                        + KEY_DATA_TYPE + " , "
                        + KEY_IS_LIVE + " , "
                        + KEY_VIDEO_WIDTH + " , "
                        + KEY_VIDEO_HEIGHT + " , "
                        + KEY_AUDIO_CHANNEL_CONFIG + " , "
                        + KEY_PURCHASE_PRICE + " , "
                        + KEY_RENTAL_PRICE + " , "
                        + KEY_RATING_STYLE + " , "
                        + KEY_RATING_SCORE + " , "
                        + KEY_PRODUCTION_YEAR + " , "
                        + KEY_COLUMN_DURATION + " , "
                        + KEY_ACTION + " , "
                        + KEY_INTENT_DATA + " , "
                        + " ) ;" ;*/

        public ChannelDatabase(Context context)
        {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }

        public static ChannelDatabase getDatabase(Context context)
        {
            if ( mChannelDatabase == null ) {
                mChannelDatabase = new ChannelDatabase(context);
            }

            if (mReadOnlyDB == null || !mReadOnlyDB.isOpen()) {
                try {
                    mReadOnlyDB = SQLiteDatabase.openDatabase("/data/vendor/dtvdata/db_test", null, SQLiteDatabase.OPEN_READONLY);
                } catch (SQLiteCantOpenDatabaseException e) {
                    e.printStackTrace();
                    mReadOnlyDB = mChannelDatabase.getReadableDatabase();
                    mReadOnlyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                    mReadOnlyDB.execSQL(CREATE_APP_TABLE);
                }
                Log.d(TAG, "getDatabase: db = "+mReadOnlyDB.getPath());
            }

            return mChannelDatabase;
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            // do nothing
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            Log.d(TAG, "onUpgrade: onUpgrade i = " + i + ", i1 = " + i1 );
        }

        /*public static void dropTable()
        {
            mReadOnlyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        }

        public long insert(ContentValues cv)
        {
            long id = update(cv) ;
            //Log.d(TAG, "insert: id="+id);
            if ( id < 1 )
                id = mReadOnlyDB.insert(TABLE_NAME, null, cv);

            return id ;
        }

        public long update(ContentValues cv)
        {
            String where = KEY_CHANNEL_ID + "=" + cv.get(KEY_CHANNEL_ID);
            //Log.d(TAG, "update: where: "+where);
            return mReadOnlyDB.update(TABLE_NAME, cv, where, null) ;
        }

        public boolean delete(String name)
        {
            String where = KEY_CHANNEL_NAME + "='" + name + "'";
            return mReadOnlyDB.delete(TABLE_NAME, where , null) > 0;
        }*/

        public Cursor get(String queryText)
        {
            String selection = KEY_CHANNEL_NAME + " MATCH ? ";
            String multiSelection = selection;
            String special_symbol = " |:|;|-" + "|\\|" + "|\\." + "|\\*" + "|\\_" + "|\\+" + "|\\'" ;
            String[] selectText = queryText.split(special_symbol) ; //let a statement to many single word to search in sql.
            for ( int i = 0 ; i < selectText.length ; i++ )
            {
                selectText[i] = selectText[i] + "*" ;
                multiSelection = multiSelection + " OR " + selection ; // N words need N  "MATCH ?"
                Log.d(TAG, "get: selectText[" + i + "] = " + selectText[i]);
            }
            Log.d(TAG, "get: multiSelection : "+multiSelection);
            Cursor result = mReadOnlyDB.query(TABLE_NAME, null , multiSelection, selectText, null, null, null, null);
            return result;
        }

        public Cursor getAll()
        {
            Cursor result = mReadOnlyDB.query(
                    TABLE_NAME, null, null, null, null, null, null, null);

            return result;
        }

        public Cursor getByFuzzy (String queryText)
        {
            String selection = KEY_CHANNEL_NAME + " LIKE ? ";
            String multiSelection = selection;
            String special_symbol = " |:|;|-" + "|\\|" + "|\\." + "|\\*" + "|\\_" + "|\\+" + "|\\'" ;
            String[] selectText = queryText.split(special_symbol) ; //let a statement to many single word to search in sql.
            for ( int i = 0 ; i < selectText.length ; i++ )
            {
                selectText[i] = "%" + selectText[i] + "%" ;
                multiSelection = multiSelection + " OR " + selection ; // N words need N  "LIKE ?"
                Log.d(TAG, "getByFuzzy: selectText[" + i + "] = " + selectText[i]);
            }
            //Log.d(TAG, "getByFuzzy: multiSelection : "+multiSelection);
            return mReadOnlyDB.query(TABLE_NAME, null, multiSelection, selectText, null, null, null, null);
        }
    }
}

