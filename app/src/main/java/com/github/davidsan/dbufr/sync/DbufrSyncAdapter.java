package com.github.davidsan.dbufr.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.github.davidsan.dbufr.MainActivity;
import com.github.davidsan.dbufr.R;
import com.github.davidsan.dbufr.Utility;
import com.github.davidsan.dbufr.data.GradesContract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* 
 * Copyright (C) 2014 David San
 */
public class DbufrSyncAdapter extends AbstractThreadedSyncAdapter {

  private static final int DBUFR_NOTIFICATION_ID = 1111;
  private final Context mContext;
  public final String LOG_TAG = DbufrSyncAdapter.class.getSimpleName();

  public static final int SYNC_INTERVAL = 60 * 180;
  public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

  private static final String[] NOTIFY_GRADE_PROJECTION = new String[]{
      GradesContract.GradesEntry.COLUMN_CASPER,
      GradesContract.GradesEntry.COLUMN_TYPE,
      GradesContract.GradesEntry.COLUMN_DESCRIPTION,
      GradesContract.GradesEntry.COLUMN_GRADE,
      GradesContract.GradesEntry.COLUMN_MAX_GRADE
  };

  private static final int INDEX_CASPER = 0;
  private static final int INDEX_TYPE = 1;
  private static final int INDEX_DESCRIPTION = 2;
  private static final int INDEX_GRADE = 3;
  private static final int INDEX_MAX_GRADE = 4;

  public DbufrSyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    mContext = context;
  }

  @Override
  public void onPerformSync(Account account, Bundle bundle, String authority,
                            ContentProviderClient provider, SyncResult syncResult) {

    final String studentId = Utility.getPreferredStudentId(getContext());
    final String studentPassword = Utility.getPreferredStudentPassword(getContext());
    Log.d(LOG_TAG, "Starting sync");

    // These two need to be declared outside the try/catch
    // so that they can be closed in the finally block.
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;

    // Will contain the raw response as a string.
    String dbufrHtmlStr = null;

    try {
      final String DBUFR_BASE_URL =
          "https://www-dbufr.ufr-info-p6.jussieu.fr/lmd/2004/master/auths/seeStudentMarks.php";
      //   "http://192.168.0.1:8000";
      URL url = new URL(DBUFR_BASE_URL);

      // Set the authenticator with student id and password
      Authenticator.setDefault(new Authenticator() {
        boolean alreadyTriedAuthenticating = false;

        protected PasswordAuthentication getPasswordAuthentication() {
          if (!alreadyTriedAuthenticating) {
            alreadyTriedAuthenticating = true;
            return new PasswordAuthentication(studentId, studentPassword.toCharArray());
          } else {
            return null;
          }
        }
      });

      Log.d(LOG_TAG, "Trying to connect");
      // Create the request to dbufr, and open the connection
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setUseCaches(false);
      urlConnection.setConnectTimeout(3000);
      urlConnection.connect();
      Log.d(LOG_TAG, "Connect");
      int responseCode = urlConnection.getResponseCode();

      Log.d(LOG_TAG, "Response code : " + responseCode);
      if (responseCode != HttpURLConnection.HTTP_OK) {
        Log.d(LOG_TAG, "Error in connection");
        return;
      }

      Log.v(LOG_TAG, "Connected");

      // Read the input stream into a String
      InputStream inputStream = urlConnection.getInputStream();
      StringBuffer buffer = new StringBuffer();
      if (inputStream == null) {
        // Nothing to do.
        return;
      }
      reader =
          new BufferedReader(
              new InputStreamReader(inputStream, getContext().getString(R.string.dbufr_charset)));

      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line + "\n");
      }

      if (buffer.length() == 0) {
        return;
      }

      dbufrHtmlStr = buffer.toString();
    } catch (IOException e) {
      Log.e(LOG_TAG, "Error ", e);
      return;
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
          Log.e(LOG_TAG, "Error closing stream ", e);
        }
      }
    }
    Document doc = Jsoup.parse(dbufrHtmlStr);
    Element body = doc.body();
    Elements tables = body.getElementsByClass("Table");

    if (tables.size() < 2) {
      Log.e(LOG_TAG, "Error parsing data");
      return;
    }
    getGradesDataFromHtml(tables.get(1), studentId);
    return;
  }


  /**
   * Helper method to handle insertion of a new course
   *
   * @param casperCode The casper's code of the course
   * @param courseName The course's name
   * @return the row ID of the added course.
   */
  private long addCourse(String casperCode, String courseName) {
    Cursor cursor = getContext().getContentResolver().query(
        GradesContract.CourseEntry.CONTENT_URI,
        new String[]{GradesContract.CourseEntry._ID},
        GradesContract.CourseEntry.COLUMN_CASPER_CODE + " = ?",
        new String[]{casperCode},
        null);

    if (cursor.moveToFirst()) {
      int locationIdIndex = cursor.getColumnIndex(GradesContract.CourseEntry._ID);
      long returnCursor = cursor.getLong(locationIdIndex);
      if (!cursor.isClosed()) {
        cursor.close();
      }
      return returnCursor;
    } else {
      ContentValues locationValues = new ContentValues();
      locationValues.put(GradesContract.CourseEntry.COLUMN_CASPER_CODE, casperCode);
      locationValues.put(GradesContract.CourseEntry.COLUMN_COURSE_NAME, courseName);

      Uri locationInsertUri = getContext().getContentResolver()
          .insert(GradesContract.CourseEntry.CONTENT_URI, locationValues);

      return ContentUris.parseId(locationInsertUri);
    }
  }


  /**
   * Take the Element representing the grades table in HTML Format and pull out the data we need to
   * construct the Strings needed for the wireframes.
   */
  private String[] getGradesDataFromHtml(Element table, String studentId) {
    Log.d(LOG_TAG, "get data from html");
    // Indexes of the data in the html table
    final int DBUFR_GRADES_CASPER_DATE_INDEX = 0;
    final int DBUFR_GRADES_TYPE_DESCRIPTION = 1;
    final int DBUFR_GRADES_GRADE_INDEX = 2;

    // Pattern for splitting the html string
    final String CASPER_DATE_SPLIT = "-";
    final Pattern
        TYPE_DESCRIPTION_PATTERN =
        Pattern.compile(" *\\[([^\\]]*)\\] *(.*)", Pattern.DOTALL);
    final String GRADE_SPLIT = "/";

    Elements rows = table.getElementsByTag("tr");
    Vector<ContentValues> cVVector = new Vector<ContentValues>(rows.size());

    for (Element row : rows) {

      // Ignore th tags, only focus on td tags
      Elements cols = row.getElementsByTag("td");
      if (cols.size() == 0) {
        continue;
      }
      String casperDateText = cols.get(DBUFR_GRADES_CASPER_DATE_INDEX).text();
      String typeDescriptionText = cols.get(DBUFR_GRADES_TYPE_DESCRIPTION).text();
      String gradeText = cols.get(DBUFR_GRADES_GRADE_INDEX).text();

      String casperCode;
      String date;
      String[] casperDateArray = casperDateText.split(CASPER_DATE_SPLIT);
      casperCode = casperDateArray[0];
      date = casperDateArray[casperDateArray.length - 1];

      String type = "";
      String description = "";
      Matcher matcher = TYPE_DESCRIPTION_PATTERN.matcher(typeDescriptionText);

      if (matcher.matches()) {
        type = matcher.group(1);
        description = matcher.group(2);
      }

      String grade;
      String maxGrade;
      String[] gradeArray = gradeText.split(GRADE_SPLIT);
      grade = gradeArray[0];
      maxGrade = gradeArray[1];

      // Get the course ID.
      long courseId = addCourse(casperCode, "");

      ContentValues gradeValues = new ContentValues();
      gradeValues.put(GradesContract.GradesEntry.COLUMN_COURSE_KEY, courseId);
      gradeValues.put(GradesContract.GradesEntry.COLUMN_CASPER, casperCode);
      gradeValues.put(GradesContract.GradesEntry.COLUMN_DATE, date);
      gradeValues.put(GradesContract.GradesEntry.COLUMN_TYPE, type);
      gradeValues.put(GradesContract.GradesEntry.COLUMN_DESCRIPTION, description);
      gradeValues.put(GradesContract.GradesEntry.COLUMN_GRADE, grade);
      gradeValues.put(GradesContract.GradesEntry.COLUMN_MAX_GRADE, maxGrade);
      gradeValues.put(GradesContract.GradesEntry.COLUMN_STUDENT_ID, studentId);
      cVVector.add(gradeValues);
    }
    if (cVVector.size() > 0) {
      ContentValues[] cvArray = new ContentValues[cVVector.size()];
      cVVector.toArray(cvArray);
      getContext().getContentResolver().bulkInsert(GradesContract.GradesEntry.CONTENT_URI, cvArray);
    }
    notifyGrade();
    return null;
  }


  /**
   * Helper method to get the fake account to be used with SyncAdapter, or make a new one if the
   * fake account doesn't exist yet.
   *
   * @param context The context used to access the account service
   * @return a fake account.
   */
  public static Account getSyncAccount(Context context) {
    // Get an instance of the Android account manager
    AccountManager accountManager =
        (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

    // Create the account type and default account
    Account newAccount = new Account(
        context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

    // If the password doesn't exist, the account doesn't exist
    if (null == accountManager.getPassword(newAccount)) {
      // Add the account and account type, no password or user data
      // If successful, return the Account object, otherwise report an error.
      if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
        return null;
      }

      onAccountCreated(newAccount, context);
    }
    return newAccount;
  }

  private static void onAccountCreated(Account newAccount, Context context) {
    DbufrSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
    ContentResolver
        .setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
    syncImmediately(context);
  }

  private static void configurePeriodicSync(Context context, int syncInterval, int syncFlextime) {
    Account account = getSyncAccount(context);
    String authority = context.getString(R.string.content_authority);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // we can enable inexact timers in our periodic sync
      SyncRequest request = new SyncRequest.Builder().
          syncPeriodic(syncInterval, syncFlextime).
          setSyncAdapter(account, authority).build();
      ContentResolver.requestSync(request);
    } else {
      ContentResolver.addPeriodicSync(account,
                                      authority, new Bundle(), syncInterval);
    }
  }

  /**
   * Helper method to have the sync adapter sync immediately
   *
   * @param context The context used to access the account service
   */
  public static void syncImmediately(Context context) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    ContentResolver.requestSync(getSyncAccount(context),
                                context.getString(R.string.content_authority), bundle);
  }

  public static void initializeSyncAdapter(Context context) {
    getSyncAccount(context);
  }


  private void notifyGrade() {
    Context context = getContext();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String lastCountNotifiedKey = context.getString(R.string.pref_last_id_notification);
    long lastCountNotified = prefs.getLong(lastCountNotifiedKey, 0);

    // compute the number of rows
    String studentId = Utility.getPreferredStudentId(context);
    Uri gradeUri = GradesContract.GradesEntry.buildGradesStudentId(studentId);
    Cursor
        cursor =
        context.getContentResolver().query(gradeUri,
                                           NOTIFY_GRADE_PROJECTION,
                                           null,
                                           null,
                                           null);
    Log.d(LOG_TAG, "Number of rows : " + cursor.getCount());
    if (cursor.getCount() > lastCountNotified) {
      if (cursor.moveToLast()) {
        String casper = cursor.getString(INDEX_CASPER);
        String type = Utility.capitalize(cursor.getString(INDEX_TYPE));
        String description = Utility.capitalize(cursor.getString(INDEX_DESCRIPTION));
        String grade = cursor.getString(INDEX_GRADE);
        String maxGrade = cursor.getString(INDEX_MAX_GRADE);

        String title = context.getString(R.string.app_name);

        // Define the text of the forecast.
        String contentText = String.format(context.getString(R.string.format_notification),
                                           casper,
                                           type,
                                           description,
                                           grade,
                                           maxGrade);

        Intent
            resultIntent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
            );

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_actionbar)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                              .bigText(contentText))
                .setContentIntent(resultPendingIntent);

        Notification notification = mBuilder.build();

        notification.defaults |= Notification.DEFAULT_VIBRATE;

        // cancel notification after click
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // show scrolling text on status bar when notification arrives
        notification.tickerText = contentText;

        NotificationManager mNotificationManager =
            (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(DBUFR_NOTIFICATION_ID, notification);

        // Refreshing number of grades
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(lastCountNotifiedKey, cursor.getCount());
        editor.commit();
      }
    }
  }
}
