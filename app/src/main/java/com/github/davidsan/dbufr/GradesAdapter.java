package com.github.davidsan.dbufr;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.davidsan.dbufr.data.GradesContract;

/*
 * Copyright (C) 2014 David San
 */
public class GradesAdapter extends CursorAdapter {

  private final int VIEW_TYPE_FIRST = 0;
  private final int VIEW_TYPE_OTHER = 1;


  public GradesAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

  @Override
  public int getItemViewType(int position) {
    return (position == 0) ? VIEW_TYPE_FIRST : VIEW_TYPE_OTHER;
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    int viewType = getItemViewType(cursor.getPosition());
    int layoutId = -1;
    if(viewType == VIEW_TYPE_FIRST){
      layoutId = R.layout.list_item_grade_first;
    }else{
      layoutId = R.layout.list_item_grade;
    }
    View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
    ViewHolder viewHolder = new ViewHolder(view);
    view.setTag(viewHolder);
    return view;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {

    ViewHolder viewHolder = (ViewHolder) view.getTag();

    // Read grade icon ID from cursor
    int gradeId = cursor.getInt(GradesFragment.COL_GRADE_ID);

    // Read casper code from cursor
    String casperString = cursor.getString(GradesFragment.COL_GRADE_CASPER);
    viewHolder.casperView.setText(casperString);

    // Read date from cursor
    String dateString = GradesContract.getPrettyDateString(
        cursor.getString(GradesFragment.COL_GRADE_DATE));
    viewHolder.dateView.setText(dateString);

    // Read grade description from cursor
    String description = Utility.capitalize(cursor.getString(GradesFragment.COL_GRADE_DESCRIPTION));
    viewHolder.descriptionView.setText(description);

    // Read grade type from cursor
    String type = Utility.capitalize(cursor.getString(GradesFragment.COL_GRADE_TYPE));
    viewHolder.typeView.setText(type);

    // Read grade from cursor
    String grade = cursor.getString(GradesFragment.COL_GRADE_GRADE);
    viewHolder.gradeView.setText(grade);

    // Read max grade from cursor
    String maxGrade = context.getString(R.string.dbufr_grade_separator) + cursor.getString(GradesFragment.COL_GRADE_MAX_GRADE);
    viewHolder.maxGradeView.setText(maxGrade);
  }

  public static class ViewHolder {
    public final TextView casperView;
    public final TextView dateView;
    public final TextView descriptionView;
    public final TextView typeView;
    public final TextView gradeView;
    public final TextView maxGradeView;

    public ViewHolder(View view) {
      casperView = (TextView) view.findViewById(R.id.list_item_grade_casper_textview);
      dateView = (TextView) view.findViewById(R.id.list_item_grade_date_textview);
      descriptionView = (TextView) view.findViewById(R.id.list_item_grade_description_textview);
      typeView = (TextView) view.findViewById(R.id.list_item_grade_type_textview);
      gradeView = (TextView) view.findViewById(R.id.list_item_grade_textview);
      maxGradeView = (TextView) view.findViewById(R.id.list_item_max_grade_textview);
    }
  }

}
