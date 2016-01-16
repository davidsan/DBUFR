package com.github.davidsan.dbufr;
/*
 * Copyright (C) 2014 David San
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.github.davidsan.dbufr.data.GradesContract.CourseEntry;
import com.github.davidsan.dbufr.data.GradesContract.GradesEntry;
import com.github.davidsan.dbufr.data.GradesDbHelper;

import java.util.Map;
import java.util.Set;

public class TestDb extends AndroidTestCase {

  public static final String LOG_TAG = TestDb.class.getSimpleName();
  static final String TEST_DATE = "201402";

  public void testCreateDb() throws Throwable {
    mContext.deleteDatabase(GradesDbHelper.DATABASE_NAME);
    SQLiteDatabase db = new GradesDbHelper(
        this.mContext).getWritableDatabase();
    assertEquals(true, db.isOpen());
    db.close();
  }

  public void testInsertReadDb() {

    GradesDbHelper dbHelper = new GradesDbHelper(mContext);
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    ContentValues testValues = createMI123CourseValues();

    long courseRowId;
    courseRowId = db.insert(CourseEntry.TABLE_NAME, null, testValues);

    assertTrue(courseRowId != -1);
    Log.d(LOG_TAG, "New row id: " + courseRowId);

    Cursor cursor = db.query(CourseEntry.TABLE_NAME,
                             null,
                             null,
                             null,
                             null,
                             null,
                             null
    );
    validateCursor(cursor, testValues);

    ContentValues gradeValues = createGradeValues(courseRowId);

    long gradeRowId = db.insert(GradesEntry.TABLE_NAME, null, gradeValues);
    assertTrue(gradeRowId != -1);

    Cursor gradeCursor = db.query(
        GradesEntry.TABLE_NAME,
        null,
        null,
        null,
        null,
        null,
        null
    );

    validateCursor(gradeCursor, gradeValues);

    dbHelper.close();
  }

  static ContentValues createMI123CourseValues() {
    ContentValues testValues = new ContentValues();
    testValues.put(CourseEntry.COLUMN_CASPER_CODE, "MI123");
    testValues.put(CourseEntry.COLUMN_COURSE_NAME, "My awesome course");
    return testValues;
  }

  static ContentValues createGradeValues(long courseRowId) {
    ContentValues gradeValues = new ContentValues();
    gradeValues.put(GradesEntry.COLUMN_COURSE_KEY, courseRowId);
    gradeValues.put(GradesEntry.COLUMN_DATE, TEST_DATE);
    gradeValues.put(GradesEntry.COLUMN_TYPE, "Examen");
    gradeValues.put(GradesEntry.COLUMN_DESCRIPTION, "Examen final");
    gradeValues.put(GradesEntry.COLUMN_GRADE, "19");
    gradeValues.put(GradesEntry.COLUMN_MAX_GRADE, "20");
    gradeValues.put(GradesEntry.COLUMN_CASPER, "MI123");
    gradeValues.put(GradesEntry.COLUMN_STUDENT_ID, "3123211");

    return gradeValues;
  }

  static void validateCursor(Cursor cursor, ContentValues expectedValues) {
    assertTrue(cursor.moveToFirst());
    Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
    for (Map.Entry<String, Object> entry : valueSet) {
      String columnName = entry.getKey();
      int idx = cursor.getColumnIndex(columnName);
      assertFalse(idx == -1);
      String expectedValue = entry.getValue().toString();
      assertEquals(expectedValue, cursor.getString(idx));
    }
    cursor.close();
  }


}
