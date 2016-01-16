package com.github.davidsan.dbufr;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.davidsan.dbufr.data.GradesContract;
import com.github.davidsan.dbufr.sync.DbufrSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class GradesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = GradesFragment.class.getSimpleName();
  private static final String[] GRADES_COLUMNS = {
      GradesContract.GradesEntry.TABLE_NAME + "." + GradesContract.GradesEntry._ID,
      GradesContract.GradesEntry.COLUMN_DATE,
      GradesContract.GradesEntry.COLUMN_TYPE,
      GradesContract.GradesEntry.COLUMN_DESCRIPTION,
      GradesContract.GradesEntry.COLUMN_GRADE,
      GradesContract.GradesEntry.COLUMN_MAX_GRADE,
      GradesContract.GradesEntry.COLUMN_CASPER,
//      GradesContract.GradesEntry.COLUMN_STUDENT_ID
  };
  public static final int COL_GRADE_ID = 0;
  public static final int COL_GRADE_DATE = 1;
  public static final int COL_GRADE_TYPE = 2;
  public static final int COL_GRADE_DESCRIPTION = 3;
  public static final int COL_GRADE_GRADE = 4;
  public static final int COL_GRADE_MAX_GRADE = 5;
  public static final int COL_GRADE_CASPER = 6;
//  public static final int COL_GRADE_STUDENT_ID = 7;

  private static final int GRADE_LOADER = 0;

  private CursorAdapter mGradesAdapter;

  private ListView mGradesListView;
  private String mStudentId;
  private String mStudentPassword;

  public GradesFragment() {
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    getLoaderManager().initLoader(GRADE_LOADER, null, this);
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.gradesfragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_refresh) {
      updateGrades();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mGradesAdapter = new SimpleCursorAdapter(
        getActivity(),
        R.layout.list_item_grade,
        null,
        new String[]{
            GradesContract.GradesEntry.COLUMN_CASPER,
            GradesContract.GradesEntry.COLUMN_DATE,
            GradesContract.GradesEntry.COLUMN_TYPE,
            GradesContract.GradesEntry.COLUMN_DESCRIPTION,
            GradesContract.GradesEntry.COLUMN_GRADE,
            GradesContract.GradesEntry.COLUMN_MAX_GRADE,
//            GradesContract.GradesEntry.COLUMN_STUDENT_ID
        },
        new int[]{
            R.id.list_item_grade_casper_textview,
            R.id.list_item_grade_date_textview,
            R.id.list_item_grade_type_textview,
            R.id.list_item_grade_description_textview,
            R.id.list_item_grade_textview,
            R.id.list_item_max_grade_textview,
//            R.id.list_item_student_id
        },
        0
    );
    mGradesAdapter = new GradesAdapter(getActivity(), null, 0);

    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    mGradesListView = (ListView) rootView.findViewById(R.id.listview_grades);
    mGradesListView.setAdapter(mGradesAdapter);
    mGradesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Cursor cursor = mGradesAdapter.getCursor();
        Log.v(LOG_TAG, "clicked");
        if (cursor != null && cursor.moveToPosition(position)) {
          Intent intent = new Intent(getActivity(), DetailActivity.class)
              .putExtra(DetailFragment.GRADE_KEY, cursor.getString(COL_GRADE_ID));
          startActivity(intent);
        }
      }
    });

    return rootView;
  }


  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    if ((mStudentId != null && !mStudentId.equals(Utility.getPreferredStudentId(getActivity())))
        || (mStudentPassword != null && !mStudentPassword
        .equals(Utility.getPreferredStudentPassword(getActivity())))) {
      getLoaderManager().restartLoader(GRADE_LOADER, null, this);
      Log.d(LOG_TAG, "Restart loader");
    }
  }

  private void updateGrades() {

//    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//    String studentId = Utility.getPreferredStudentId(getActivity());
//    String studentPassword = Utility.getPreferredStudentPassword(getActivity());
//
//    Intent alarmIntent = new Intent(getActivity(), DbufrService.AlarmReceiver.class);
//    alarmIntent.putExtra(DbufrService.STUDENT_QUERY_EXTRA, studentId);
//    alarmIntent.putExtra(DbufrService.PASSWORD_QUERY_EXTRA, studentPassword);
//
//    PendingIntent
//        pi =
//        PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
//    AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//
//    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
//
//    Intent intent = new Intent(getActivity(), DbufrService.class);
//    intent.putExtra(DbufrService.STUDENT_QUERY_EXTRA, studentId);
//    intent.putExtra(DbufrService.PASSWORD_QUERY_EXTRA, studentPassword);
//    getActivity().startService(intent);

    DbufrSyncAdapter.syncImmediately(getActivity());

  }


  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    mStudentId = Utility.getPreferredStudentId(getActivity());
    mStudentPassword = Utility.getPreferredStudentPassword(getActivity());
    Uri gradeForStudentUri = GradesContract.GradesEntry.buildGradesStudentId(mStudentId);
    Log.v(LOG_TAG, gradeForStudentUri.toString());

    return new CursorLoader(
        getActivity(),
        gradeForStudentUri,
        GRADES_COLUMNS,
        null,
        null,
        GRADES_COLUMNS[COL_GRADE_ID] + " DESC"
    );
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    mGradesAdapter.swapCursor(cursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    mGradesAdapter.swapCursor(null);
  }

}
