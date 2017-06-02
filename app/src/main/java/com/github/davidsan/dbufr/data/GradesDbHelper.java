package com.github.davidsan.dbufr.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.davidsan.dbufr.data.GradesContract.CourseEntry;
import com.github.davidsan.dbufr.data.GradesContract.GradesEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a local database for grades data
 */
public class GradesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "grades.db";
    private SQLiteDatabase mybase;

    public GradesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
// Create a table to hold courses.  A course consists of the CASPER code string
// and the corresponding name.
        final String SQL_CREATE_COURSE_TABLE = "CREATE TABLE " + CourseEntry.TABLE_NAME + " (" +
                CourseEntry._ID + " INTEGER PRIMARY KEY," +
                CourseEntry.COLUMN_CASPER_CODE
                + " TEXT UNIQUE NOT NULL, " +
                CourseEntry.COLUMN_COURSE_NAME + " TEXT NOT NULL, " +
                "UNIQUE (" + CourseEntry.COLUMN_CASPER_CODE
                + ") ON CONFLICT IGNORE" +
                " );";

// Create a table to hold grades.  A grade consists of the CASPER code, the type,
// the description, the date, the grade, and the maximum grade.
        final String SQL_CREATE_GRADE_TABLE = "CREATE TABLE " + GradesEntry.TABLE_NAME + " (" +
                GradesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GradesEntry.COLUMN_COURSE_KEY + " INTEGER NOT NULL, " +

                GradesEntry.COLUMN_CASPER + " TEXT NOT NULL, " +
                GradesEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                GradesEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                GradesEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                GradesEntry.COLUMN_GRADE + " REAL NOT NULL, " +
                GradesEntry.COLUMN_MAX_GRADE + " REAL NOT NULL, " +
                GradesEntry.COLUMN_STUDENT_ID + " INTEGER NOT NULL, " +

                // Set the course column as foreign key to course table
                " FOREIGN KEY (" + GradesEntry.COLUMN_COURSE_KEY
                + ") REFERENCES " + CourseEntry.TABLE_NAME + " ("
                + CourseEntry._ID + "), " +

                // One grade per casper + date + type + description + student id
                " UNIQUE (" + GradesEntry.COLUMN_COURSE_KEY + ", "
                + GradesEntry.COLUMN_DATE + ", " + GradesEntry.COLUMN_TYPE
                + ", " + GradesEntry.COLUMN_DESCRIPTION
                + ", " + GradesEntry.COLUMN_STUDENT_ID
                + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_COURSE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_GRADE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CourseEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GradesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    protected synchronized void openDb() {
        mybase = getWritableDatabase();
    }

    protected synchronized void closeDb() {
        mybase.close();
    }

    public List<String> getAllYears() {
        openDb();
        Cursor c = mybase.rawQuery("SELECT DISTINCT " + GradesEntry.COLUMN_DATE + " FROM " + GradesEntry.TABLE_NAME, null);

        c.moveToFirst();
        List liste = new ArrayList<String>();
        int nb = 0;
        int size = c.getCount(); // Nombre de rang
        while (nb < size) {
            String year = c.getString(0).substring(0, 4);
            if (!liste.contains(year)) liste.add(year);
            c.moveToNext();
            nb++;
        }

        c.close();
        closeDb();
        return liste;
    }
}
