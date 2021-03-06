package agostinisalome.it.mobilenode.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alessandro on 19/04/2017.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Database.db";
    public static final String MESSAGE_TABLE_NAME = "Filtered";
    public static final String MESSAGE_COLUMN_ID = "id";
    public static final String MESSAGE_COLUMN_DATE = "date";
    public static final String MESSAGE_COLUMN_TOPIC = "topic";
    public static final String MESSAGE_COLUMN_CONTENT = "content";


    public static final String TOPIC_TABLE_NAME = "Topic";
    public static final String TOPIC_COLUMN_ID = "id";
    public static final String TOPIC_COLUMN_NAME = "name";
    public static final String TOPIC_COLUMN_CHECKPOINT = "checkpoint";

    public static final String FILTER_TABLE_NAME = "TopicFiltered";
    public static final String FILTER_COLUMN_ID = "id";
    public static final String FILTER_COLUMN_NAME = "topic";
    public static final String FILTER_COLUMN_FILTER = "filter";


    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table Filtered " +
                        "(id integer primary key, date date,topic text,content text)"
        );

        db.execSQL(
                "create table " + TOPIC_TABLE_NAME + " " +
                        "(" + TOPIC_COLUMN_ID + " integer primary key," + TOPIC_COLUMN_NAME + " text," + TOPIC_COLUMN_CHECKPOINT + " date)"
        );
        db.execSQL(
                "create table " + FILTER_TABLE_NAME + " " +
                        "(" + FILTER_COLUMN_ID + " integer primary key," + FILTER_COLUMN_NAME + " text," + FILTER_COLUMN_FILTER + " text)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS Filtered");
        db.execSQL("DROP TABLE IF EXISTS Topic");
        db.execSQL("DROP TABLE IF EXISTS TopicFiltered");
        onCreate(db);
    }

    public boolean insertTableFiltered (Date date, String topic, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", String.valueOf(date));
        contentValues.put("topic", topic);
        contentValues.put("content", content);
        Log.e("CIAO",topic+" "+content);

        db.insert("Filtered", null, contentValues);
        return true;
    }

    public boolean insertTableTopic ( String name, Date checkpoint) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("name", name);
        contentValues.put("checkpoint", String.valueOf(checkpoint));


        db.insert("Topic", null, contentValues);
        return true;
    }

    public boolean insertFilterTopic ( String name, String filter) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("topic", name);
        contentValues.put("filter", filter);


        db.insert(FILTER_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertNotExistTableTopic ( String name, Date checkpoint) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("name", name);
        contentValues.put("checkpoint", String.valueOf(checkpoint));
        Cursor c= getDataTopic(name);
        if( (!c.moveToFirst()) || c.getCount() == 0 ) {
            Log.e("null",name);
            db.insert("Topic", null, contentValues);
        }
        else {
            Log.e("idtopic",getIDTopic(name).toString());
            updateTableTopic(getIDTopic(name), name, checkpoint);
        }
        return true;
    }

    public boolean insertNotExistTableFiltered ( String name, String filter) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("topic", name);
        contentValues.put("filter", filter);
        Cursor c= getDataTopicFiltered(name);
        if( (!c.moveToFirst()) || c.getCount() == 0 ) {
            Log.e("null",name);
            db.insert(FILTER_TABLE_NAME, null, contentValues);
        }
        else {
            Log.e("idFilterTopic",getIDTopicFiltered(name).toString());
            updateTableTopicFilter(getIDTopicFiltered(name), name, filter);
        }
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Filtered where id = '"+id+"'", null );
        return res;
    }

    public Cursor getDataTopic(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select id from Topic where name = '"+name+"'", null );
        return res;
    }
    public Cursor getDataTopicFiltered(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select id from "+FILTER_TABLE_NAME+" where topic = '"+name+"'", null );
        return res;
    }

    public Integer getIDTopic(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select id from Topic where "+ TOPIC_COLUMN_NAME+" = '"+name+"'", null );
        res.moveToFirst();
        Log.e("getIDTopic",""+res.getInt(res.getColumnIndex(TOPIC_COLUMN_ID)));
        return res.getInt(res.getColumnIndex(TOPIC_COLUMN_ID));
   }

   public Integer getIDTopicFiltered(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select id from "+FILTER_TABLE_NAME+" where "+ FILTER_COLUMN_NAME+" = '"+name+"'", null );
        res.moveToFirst();
        Log.e("getIDTopicFilter",""+res.getInt(res.getColumnIndex(FILTER_COLUMN_ID)));
        return res.getInt(res.getColumnIndex(FILTER_COLUMN_ID));
    }
    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, MESSAGE_TABLE_NAME);
        return numRows;
    }

    public boolean updateTableFiltered (Integer id, Date date, String topic, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", String.valueOf(date));
        contentValues.put("topic", topic);
        contentValues.put("content", content);

        db.update("Filtered", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public boolean updateTableTopic (Integer id,String name, Date checkpoint) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("checkpoint", String.valueOf(checkpoint));
        contentValues.put("name", name);


        db.update("Topic", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public boolean updateTableTopicFilter (Integer id,String name, String filter) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("filter", filter);
        contentValues.put("topic", name);


        db.update(FILTER_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer delete (Integer id, String table_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(table_name,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllFilteredMessage() throws JSONException {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Filtered", null );

        res.moveToFirst();

        while(res.isAfterLast() == false){

            JSONObject json = new JSONObject();
            json.put("TOPIC",res.getString(res.getColumnIndex(MESSAGE_COLUMN_TOPIC)));
            json.put("DATE",res.getString(res.getColumnIndex(MESSAGE_COLUMN_DATE)));
            json.put("CONTENT",res.getString(res.getColumnIndex(MESSAGE_COLUMN_CONTENT)));
            array_list.add(json.toString());
            res.moveToNext();
        }
        return array_list;
    }
    public ArrayList<String> getFilteredMessageByTopicName(String name) throws JSONException {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Filtered where topic = '"+name+"' order by id DESC", null );

        res.moveToFirst();

        while(res.isAfterLast() == false){

            JSONObject json = new JSONObject();
            json.put("TOPIC",res.getString(res.getColumnIndex(MESSAGE_COLUMN_TOPIC)));
            json.put("DATE",res.getString(res.getColumnIndex(MESSAGE_COLUMN_DATE)));
            json.put("CONTENT",res.getString(res.getColumnIndex(MESSAGE_COLUMN_CONTENT)));
            array_list.add(json.toString());
            res.moveToNext();
        }
        return array_list;
    }


    public ArrayList<String> getAllTopic() throws JSONException {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Topic", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){

            JSONObject json = new JSONObject();

            json.put(TOPIC_COLUMN_NAME,res.getString(res.getColumnIndex(TOPIC_COLUMN_NAME)));
            json.put(TOPIC_COLUMN_CHECKPOINT,res.getString(res.getColumnIndex(TOPIC_COLUMN_CHECKPOINT)));
            array_list.add(json.toString());
            res.moveToNext();
        }

        return array_list;
    }

    public ArrayList<String> getAllTopicFilter() throws JSONException {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+FILTER_TABLE_NAME+"", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){

            JSONObject json = new JSONObject();

            json.put(FILTER_COLUMN_NAME,res.getString(res.getColumnIndex(FILTER_COLUMN_NAME)));
            json.put(FILTER_COLUMN_FILTER,res.getString(res.getColumnIndex(FILTER_COLUMN_FILTER)));
            array_list.add(json.toString());
            res.moveToNext();
        }

        return array_list;
    }

    public ArrayList<String> getFilterTableView(List<String> topics) throws JSONException {
        ArrayList<String> temp= new ArrayList<>();
        ArrayList<String> filtered= null;
        JSONArray jsonArray;

        try {
            filtered = getAllTopicFilter();
            jsonArray= new JSONArray(filtered);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i =0; i< topics.size(); i++){
            temp.add(topics.get(i));
            temp.add(Util.getTopicOccurrency(topics.get(i),filtered));
        }


        return temp;
    }


}
