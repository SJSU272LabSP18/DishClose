package demo.tensorflow.org.customvision_sample;

/**
 * Created by JigarSoni on 9/25/2016.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_RECIPES = "recipes";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TAG= "tag";
    private static final String KEY_INGREDIENTS= "ingredients";
    private static final String KEY_RECIPE= "recipe";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RECIPE_TABLE = "CREATE TABLE " + TABLE_RECIPES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TAG + " TEXT,"
                + KEY_INGREDIENTS+ " TEXT,"
                + KEY_RECIPE+ " TEXT" + ")";
        db.execSQL(CREATE_RECIPE_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addRecipe(String tag, String ingrgient, String recipe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TAG, tag); // Name
        values.put(KEY_INGREDIENTS, ingrgient); // Email
        values.put(KEY_RECIPE, recipe); // Email

        // Inserting Row
        long id = db.insert(TABLE_RECIPES, null, values);
        db.close(); // Closing database connection
        Log.d(TAG, "New RECIPE inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getRecipe() {
        ArrayList<String> arr = new ArrayList<>();
        HashMap<String, String> recipes = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_RECIPES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false)
        {
            arr.add(cursor.getString(1));
            recipes.put(cursor.getString(1),cursor.getString(1) );
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + recipes.toString());

        return recipes;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_RECIPES, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

}
