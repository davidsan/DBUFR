package com.github.davidsan.dbufr;
/*
 * Copyright (C) 2014 David San
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.github.davidsan.dbufr.data.GradesContract.CourseEntry;
import com.github.davidsan.dbufr.data.GradesContract.GradesEntry;

public class TestProvider extends AndroidTestCase {

  public static final String LOG_TAG = TestProvider.class.getSimpleName();
  static final String TEST_DATE = "201402";


  public void deleteAllRecords() {
    mContext.getContentResolver().delete(
        GradesEntry.CONTENT_URI,
        null,
        null
    );
    mContext.getContentResolver().delete(
        CourseEntry.CONTENT_URI,
        null,
        null
    );

    Cursor cursor = mContext.getContentResolver().query(
        GradesEntry.CONTENT_URI,
        null,
        null,
        null,
        null
    );
    assertEquals(0, cursor.getCount());
    cursor.close();

    cursor = mContext.getContentResolver().query(
        CourseEntry.CONTENT_URI,
        null,
        null,
        null,
        null
    );
    assertEquals(0, cursor.getCount());
    cursor.close();
  }

  public void setUp() {
    deleteAllRecords();
  }


  public void testGetType() {
    String type = mContext.getContentResolver().getType(GradesEntry.CONTENT_URI);
    assertEquals(GradesEntry.CONTENT_TYPE, type);

    String testStudent = "3000211";
    type = mContext.getContentResolver().getType(
        GradesEntry.buildGradesStudent(testStudent));
    assertEquals(GradesEntry.CONTENT_TYPE, type);

    type = mContext.getContentResolver().getType(CourseEntry.CONTENT_URI);
    assertEquals(CourseEntry.CONTENT_TYPE, type);

    type = mContext.getContentResolver().getType(
        CourseEntry.buildCourseUri(1L));
    assertEquals(CourseEntry.CONTENT_ITEM_TYPE, type);
  }

  public void testInsertReadProvider() {

    ContentValues testCourseValues = TestDb.createMI123CourseValues();

    Uri
        courseInsertUri =
        mContext.getContentResolver().insert(CourseEntry.CONTENT_URI, testCourseValues);

    long courseRowId = ContentUris.parseId(courseInsertUri);
    Log.d(LOG_TAG, "New row id: " + courseRowId);

    Cursor courseCursor = mContext.getContentResolver().query(
        CourseEntry.CONTENT_URI,
        null,
        null,
        null,
        null
    );
    TestDb.validateCursor(courseCursor, testCourseValues);

    ContentValues testGradeValues = TestDb.createGradeValues(courseRowId);
    courseCursor = mContext.getContentResolver().query(
        CourseEntry.buildCourseUri(courseRowId),
        null,
        null,
        null,
        null
    );
    TestDb.validateCursor(courseCursor, testCourseValues);

    Uri
        gradeInsertUri =
        mContext.getContentResolver().insert(GradesEntry.CONTENT_URI, testGradeValues);

    long gradeRowId = ContentUris.parseId(gradeInsertUri);

    Cursor gradeCursor = mContext.getContentResolver().query(
        GradesEntry.CONTENT_URI,
        null,
        null,
        null,
        null
    );
    TestDb.validateCursor(gradeCursor, testGradeValues);
  }

  public void testUpdateLocation() {
    ContentValues values = TestDb.createMI123CourseValues();

    Uri locationUri = mContext.getContentResolver().
        insert(CourseEntry.CONTENT_URI, values);
    long courseRowId = ContentUris.parseId(locationUri);

    assertTrue(courseRowId != -1);

    ContentValues updatedValues = new ContentValues(values);
    updatedValues.put(CourseEntry._ID, courseRowId);
    updatedValues.put(CourseEntry.COLUMN_COURSE_NAME, "Best course ever");

    int count = mContext.getContentResolver().update(
        CourseEntry.CONTENT_URI, updatedValues, CourseEntry._ID + "= ?",
        new String[]{Long.toString(courseRowId)});

    assertEquals(count, 1);

    Cursor cursor = mContext.getContentResolver().query(
        CourseEntry.buildCourseUri(courseRowId),
        null,
        null,
        null,
        null
    );

    TestDb.validateCursor(cursor, updatedValues);
  }

  public void testDeleteRecordsAtEnd() {
    deleteAllRecords();
  }

}
