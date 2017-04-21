package com.github.davidsan.dbufr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.davidsan.dbufr.data.GradesContract;

import java.util.HashSet;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = DetailFragment.class.getSimpleName();

  private static final String DBUFR_SHARE_HASHTAG = " #DBUFR";

  private String mGradeStr;

  private ShareActionProvider mShareActionProvider;

  public static final String GRADE_KEY = "grade_key";

  private static final int DETAIL_LOADER = 0;

  private static final String[] GRADES_COLUMNS = {
      GradesContract.GradesEntry.TABLE_NAME + "." + GradesContract.GradesEntry._ID,
      GradesContract.GradesEntry.COLUMN_DATE,
      GradesContract.GradesEntry.COLUMN_TYPE,
      GradesContract.GradesEntry.COLUMN_DESCRIPTION,
      GradesContract.GradesEntry.COLUMN_GRADE,
      GradesContract.GradesEntry.COLUMN_MAX_GRADE,
      GradesContract.GradesEntry.COLUMN_CASPER,
  };
  private TextView mCasperView;
  private TextView mGradeView;
  private TextView mMaxGradeView;
  private TextView mDateView;
  private TextView mTypeView;
  private TextView mDescriptionView;


  public DetailFragment() {
    setHasOptionsMenu(true);
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
    mCasperView = (TextView) rootView.findViewById(R.id.detail_item_grade_casper_textview);
    mGradeView = (TextView) rootView.findViewById(R.id.detail_item_grade_textview);
    mMaxGradeView = (TextView) rootView.findViewById(R.id.detail_item_max_grade_textview);
    mDateView = (TextView) rootView.findViewById(R.id.detail_item_grade_date_textview);
    mTypeView = (TextView) rootView.findViewById(R.id.detail_item_grade_type_textview);
    mDescriptionView =
        (TextView) rootView.findViewById(R.id.detail_item_grade_description_textview);
    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
//    getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.detailfragment, menu);
    MenuItem menuItem = menu.findItem(R.id.action_share);
      Drawable c = menuItem.getIcon();
    mShareActionProvider =
        (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    if (mShareActionProvider != null) {
      mShareActionProvider.setShareIntent(createShareGradeIntent());
    }
  }


  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    Intent intent = getActivity().getIntent();
    if (intent == null || !intent.hasExtra(GRADE_KEY)) {
      return null;
    }
    String gradeId = intent.getStringExtra(GRADE_KEY);
    Log.v(LOG_TAG, "Grade id is " + gradeId);
    Uri gradeUri = GradesContract.GradesEntry.buildGradesRowId(gradeId);

    Log.v(LOG_TAG, gradeUri.toString());
    return new CursorLoader(
        getActivity(),
        gradeUri,
        GRADES_COLUMNS,
        null,
        null,
        null
    );
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.v(LOG_TAG, "In onLoadFinished");
    if (data != null && data.moveToFirst()) {

      Log.v(LOG_TAG, "In onLoadFinished 2");
      String
          dateString =
          data.getString(data.getColumnIndex(GradesContract.GradesEntry.COLUMN_DATE));
      String prettyDateString = GradesContract.getPrettyDateString(dateString);
      mDateView.setText(prettyDateString);

      String
          casperString =
          data.getString(data.getColumnIndex(GradesContract.GradesEntry.COLUMN_CASPER));
      mCasperView.setText(casperString);

      String
          gradeString =
          data.getString(data.getColumnIndex(GradesContract.GradesEntry.COLUMN_GRADE));
      mGradeView.setText(gradeString);

      String
          maxGradeString =
          getActivity().getString(R.string.dbufr_grade_separator) + data
              .getString(data.getColumnIndex(GradesContract.GradesEntry.COLUMN_MAX_GRADE));
      mMaxGradeView.setText(maxGradeString);

      String
          typeString = Utility.capitalize(
          data.getString(data.getColumnIndex(GradesContract.GradesEntry.COLUMN_TYPE)));
      mTypeView.setText(typeString);

      String
          descriptionString = Utility.capitalize(
          data.getString(data.getColumnIndex(GradesContract.GradesEntry.COLUMN_DESCRIPTION)));
      mDescriptionView.setText(descriptionString);

      mGradeStr =
          String.format("[%s] %s - %s : %s%s", casperString, typeString, descriptionString,
                        gradeString, maxGradeString);

      Log.v(LOG_TAG, mGradeStr);
      if (mShareActionProvider != null) {
        mShareActionProvider.setShareIntent(createShareGradeIntent());
      }

    }
  }

  private Intent createShareGradeIntent() {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, mGradeStr + DBUFR_SHARE_HASHTAG);
    return shareIntent;
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {

  }

}
