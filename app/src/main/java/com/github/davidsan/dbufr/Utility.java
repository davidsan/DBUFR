package com.github.davidsan.dbufr;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {


  private static final String LOG_TAG = Utility.class.getSimpleName();

  public static String getPreferredStudentId(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(
        context.getString(R.string.pref_student_id_key),
        context.getString(R.string.pref_student_id_default));

  }

  public static String getPreferredStudentPassword(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(
        context.getString(R.string.pref_student_password_key),
        context.getString(R.string.pref_student_password_default));
  }

  public static String capitalize(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return str;
    }
    return new StringBuffer(strLen)
        .append(Character.toTitleCase(str.charAt(0)))
        .append(str.substring(1))
        .toString();
  }

}
