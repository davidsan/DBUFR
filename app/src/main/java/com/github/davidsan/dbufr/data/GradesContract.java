package com.github.davidsan.dbufr.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Defines the table and columns name for the grades database
 */
public class GradesContract {

  public static final String CONTENT_AUTHORITY = "com.github.davidsan.dbufr.provider";
  public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
  public static final String PATH_COURSE = "course";
  public static final String PATH_GRADES = "grades";
  public static final String DATE_FORMAT = "MMMM yyyy";
  public static final String DATE_FORMAT_YEAR = "''yy";

  /**
   * Empty constructor
   */
  public GradesContract() {
  }

  /**
   * Converts DB String into pretty string
   *
   * @param date The input date string
   * @return string representation of the date
   */
  public static String getPrettyDateString(String date) {
    String yearStr = date.substring(0, 4);
    String monthStr = date.substring(4);
    int month;
    // Manually parsing the month string
    // DBUFR website doesn't use Locale.FRENCH, nor Locale.FRANCE
    // Only fev and oct are useful for DBUFR
    if (monthStr.compareTo("jan") == 0) {
      month = 1;
    } else if (monthStr.compareTo("fev") == 0) {
      month = 2;
    } else if (monthStr.compareTo("mar") == 0) {
      month = 3;
    } else if (monthStr.compareTo("avr") == 0) {
      month = 4;
    } else if (monthStr.compareTo("mai") == 0) {
      month = 5;
    } else if (monthStr.compareTo("jun") == 0) {
      month = 6;
    } else if (monthStr.compareTo("jul") == 0) {
      month = 7;
    } else if (monthStr.compareTo("aou") == 0) {
      month = 8;
    } else if (monthStr.compareTo("sep") == 0) {
      month = 9;
    } else if (monthStr.compareTo("oct") == 0) {
      month = 10;
    } else if (monthStr.compareTo("nov") == 0) {
      month = 11;
    } else {
      month = 12;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
    try {
      Date d = sdf.parse(String.format("%02d", month) + "/" + yearStr);
      return getSemester(month) + " " + new SimpleDateFormat(DATE_FORMAT_YEAR).format(d);

    } catch (ParseException e) {
      e.printStackTrace();
    }
    return "";
  }

  private static String getSemester(int month) {
    if (month > 1 && month < 9) {
      return "S2";
    } else {
      return "S1";
    }
  }


  /* Inner class that defines the table contents of the course table */
  public static final class CourseEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSE).build();

    public static final String CONTENT_TYPE =
        "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_COURSE;
    public static final String CONTENT_ITEM_TYPE =
        "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_COURSE;

    // Table name
    public static final String TABLE_NAME = "course";

    // The CASPER code of the course (eg. MI123)
    public static final String COLUMN_CASPER_CODE = "casper_code";
    // The course's name
    public static final String COLUMN_COURSE_NAME = "course_name";

    public static Uri buildCourseUri(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }

  }

  /* Inner class that defines the table contents of the grades table */
  public static final class GradesEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_GRADES).build();

    public static final String CONTENT_TYPE =
        "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_GRADES;
    public static final String CONTENT_ITEM_TYPE =
        "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_GRADES;

    // Table name
    public static final String TABLE_NAME = "grades";

    // Column with the foreign key into the course table
    public static final String COLUMN_COURSE_KEY = "course_id";

    public static final String COLUMN_CASPER = "casper";

    // Date, stored as Text with format yyyy-MM
    public static final String COLUMN_DATE = "date";
    // Type of the grade
    public static final String COLUMN_TYPE = "type";
    // Description of the grade
    public static final String COLUMN_DESCRIPTION = "description";
    // Grade
    public static final String COLUMN_GRADE = "grade";
    // Maximum grade
    public static final String COLUMN_MAX_GRADE = "max_grade";
    public static final String COLUMN_STUDENT_ID = "student_id";


    public static Uri buildGradesUri(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }

//    public static Uri buildGradesStudent(String studentId) {
//      return CONTENT_URI.buildUpon().appendPath(studentId).build();
//    }

    public static Uri buildGradesStudentId(String studentId) {
      return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_STUDENT_ID, studentId).build();
    }

    public static String getStudentIdFromUri(Uri uri) {
      return uri.getQueryParameter(COLUMN_STUDENT_ID);
    }


    public static Uri buildGradesRowId(String studentId) {
      return CONTENT_URI.buildUpon().appendQueryParameter(_ID, studentId).build();
    }

    public static String getRowIdFromUri(Uri uri) {
      return uri.getQueryParameter(_ID);
    }


//    public static String getStudentIdFromUri(Uri uri) {
//      return uri.getPathSegments().get(1);
//    }
  }
}
