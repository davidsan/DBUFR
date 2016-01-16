package com.github.davidsan.dbufr.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/*
 * Copyright (C) 2014 David San
 */
public class GradesProvider extends ContentProvider {

  private static final int GRADE = 100;
  private static final int GRADE_WITH_STUDENT = 101;
  private static final int COURSE = 200;
  private static final int COURSE_ID = 201;

  private GradesDbHelper mOpenHelper;
  private static final UriMatcher sUriMatcher = buildUriMatcher();


  private static final String sStudentIdSelection =
      GradesContract.GradesEntry.TABLE_NAME + "."
      + GradesContract.GradesEntry.COLUMN_STUDENT_ID + " = ? ";
  private static final String sRowIdSelection =
      GradesContract.GradesEntry.TABLE_NAME + "."
      + GradesContract.GradesEntry._ID + " = ? ";


  @Override
  public boolean onCreate() {
    mOpenHelper = new GradesDbHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                      String sortOrder) {
    Cursor retCursor;
    switch (sUriMatcher.match(uri)) {
      case GRADE_WITH_STUDENT:
        retCursor = getGradesByStudentId(uri, projection, sortOrder);
        break;
      case GRADE:
        retCursor = getGradesByStudentId(uri, projection, sortOrder);
        break;
      case COURSE:
        retCursor = mOpenHelper.getReadableDatabase().query(
            GradesContract.CourseEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        );
        break;
      case COURSE_ID:
        retCursor = mOpenHelper.getReadableDatabase().query(
            GradesContract.CourseEntry.TABLE_NAME,
            projection,
            GradesContract.CourseEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
            null,
            null,
            null,
            sortOrder
        );
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    retCursor.setNotificationUri(getContext().getContentResolver(), uri);
    return retCursor;
  }

  private Cursor getGradesByStudentId(Uri uri, String[] projection, String sortOrder) {
    String selection = null;
    String[] selectionArgs = null;
    String studentId = GradesContract.GradesEntry.getStudentIdFromUri(uri);
    if(studentId!=null){
       selection = sStudentIdSelection;
       selectionArgs= new String[]{studentId};
    }
    String rowId = GradesContract.GradesEntry.getRowIdFromUri(uri);

    if(rowId!=null){
      selection = sRowIdSelection;
      selectionArgs= new String[]{rowId};
    }

    return mOpenHelper.getReadableDatabase().query(GradesContract.GradesEntry.TABLE_NAME,
                                                   projection,
                                                   selection,
                                                   selectionArgs,
                                                   null,
                                                   null,
                                                   sortOrder
    );
  }


  @Override
  public String getType(Uri uri) {
    final int match = sUriMatcher.match(uri);
    switch (match) {
      case GRADE:
        return GradesContract.GradesEntry.CONTENT_TYPE;
      case GRADE_WITH_STUDENT:
        return GradesContract.GradesEntry.CONTENT_TYPE;
      case COURSE:
        return GradesContract.CourseEntry.CONTENT_TYPE;
      case COURSE_ID:
        return GradesContract.CourseEntry.CONTENT_ITEM_TYPE;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    Uri returnUri;
    long _id;
    switch (match) {
      case GRADE:
        _id = db.insert(GradesContract.GradesEntry.TABLE_NAME, null, contentValues);
        if (_id > 0) {
          returnUri = GradesContract.GradesEntry.buildGradesUri(_id);
        } else {
          throw new SQLException("Failed to insert row into " + uri);
        }
        break;
      case COURSE:
        _id = db.insert(GradesContract.CourseEntry.TABLE_NAME, null, contentValues);
        if (_id > 0) {
          returnUri = GradesContract.CourseEntry.buildCourseUri(_id);
        } else {
          throw new SQLException("Failed to insert row into " + uri);
        }
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return returnUri;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int rowsDeleted;
    switch (match) {
      case GRADE:
        rowsDeleted = db.delete(GradesContract.GradesEntry.TABLE_NAME, selection, selectionArgs);
        break;
      case COURSE:
        rowsDeleted = db.delete(GradesContract.CourseEntry.TABLE_NAME, selection, selectionArgs);
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    if (selection == null || rowsDeleted != 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues contentValues, String selection,
                    String[] selectionArgs) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int rowsUpdated;
    switch (match) {
      case GRADE:
        rowsUpdated = db.update(GradesContract.GradesEntry.TABLE_NAME,
                                contentValues,
                                selection,
                                selectionArgs
        );
        break;
      case COURSE:
        rowsUpdated = db.update(GradesContract.CourseEntry.TABLE_NAME,
                                contentValues,
                                selection,
                                selectionArgs
        );
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    if (rowsUpdated != 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return rowsUpdated;
  }

  private static UriMatcher buildUriMatcher() {
    final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    final String authority = GradesContract.CONTENT_AUTHORITY;

    matcher.addURI(authority, GradesContract.PATH_GRADES, GRADE);
    matcher.addURI(authority, GradesContract.PATH_GRADES + "/*", GRADE_WITH_STUDENT);
    matcher.addURI(authority, GradesContract.PATH_COURSE, COURSE);
    matcher.addURI(authority, GradesContract.PATH_COURSE + "/#", COURSE_ID);

    return matcher;
  }
}
