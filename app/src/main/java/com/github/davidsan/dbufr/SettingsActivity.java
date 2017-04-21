package com.github.davidsan.dbufr;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import com.github.davidsan.dbufr.data.GradesContract;
import com.github.davidsan.dbufr.data.GradesDbHelper;
import com.github.davidsan.dbufr.sync.DbufrSyncAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.os.Build.*;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices,
 * settings are presented as a single list. On tablets, settings are split by category, with
 * category headers shown to the left of the list of settings. <p> See <a
 * href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a>
 * for design guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

  /**
   * Determines whether to always show the simplified settings UI, where settings are presented in a
   * single list. When false, settings are shown as a master/detail two-pane view on tablets. When
   * true, a single pane is shown on tablets.
   */
  private static final boolean ALWAYS_SIMPLE_PREFS = false;
  private static boolean sBindingPref = false;
  private Context context;

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    context = this;
    setupSimplePreferencesScreen();
  }

  /**
   * Shows the simplified settings UI if the device configuration if the device configuration
   * dictates that a simplified, single-pane UI should be shown.
   */
  private void setupSimplePreferencesScreen() {
    if (!isSimplePreferences(this)) {
      return;
    }

    // In the simplified UI, fragments are not used at all and we instead
    // use the older PreferenceActivity APIs.

    // Add 'general' preferences.
    addPreferencesFromResource(R.xml.pref_general);

    PreferenceCategory fakeHeader = new PreferenceCategory(this);

    // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
    // their values. When their values change, their summaries are updated
    // to reflect the new value, per the Android Design guidelines.
    bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_student_id_key)));
    bindPreferenceSummaryToValue(findPreference("pref_student_password"));


    final CheckBoxPreference check_pref_filtre = (CheckBoxPreference) findPreference(getString(R.string.pref_filtre_key));
    check_pref_filtre.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object o) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        if(prefs.getString("hasLoad",null)==null){
          Toast.makeText(context, "Veuillez charger au moins une fois l'ensemble de vos notes", Toast.LENGTH_LONG).show();
          return false;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("filtre", (Boolean) o);
        editor.apply();
        return true;
      }
    });

    final MultiSelectListPreference check_pref_filtre_periodes = (MultiSelectListPreference) findPreference(getString(R.string.pref_filtre_periode_key));
    GradesDbHelper db = new GradesDbHelper(this);
    List<String> years = new ArrayList<String>(context.getSharedPreferences("prefs", MODE_PRIVATE)
                            .getStringSet("list_years", new HashSet<String>(db.getAllYears()) ));
    Collections.sort(years);
    List<String> periodes = new ArrayList<String>(years.size()*2);
    String tmp;
    for(int i=0; i<years.size(); i++){
      tmp = years.get(i);                               // récupère l'année (exemple : 2015)
      int int_tmp = Integer.parseInt(tmp.substring(2)); // récupère l'année sous forme réduite en int (exemple : 15)
      int_tmp++;                                        // (exemple : 16)
      periodes.add("S1 "+tmp+"/"+int_tmp);
      periodes.add("S2 " + tmp + "/" + int_tmp);
    }
    check_pref_filtre_periodes.setEntries(periodes.toArray(new CharSequence[periodes.size()]));
    check_pref_filtre_periodes.setEntryValues(periodes.toArray(new CharSequence[periodes.size()]));

    check_pref_filtre_periodes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object o) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("periodes", (Set<String>)o);
        editor.apply();
        return true;
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onIsMultiPane() {
    return isXLargeTablet(this) && !isSimplePreferences(this);
  }

  /**
   * Helper method to determine if the device has an extra-large screen. For example, 10" tablets
   * are extra-large.
   */
  private static boolean isXLargeTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
  }

  /**
   * Determines whether the simplified settings UI should be shown. This is true if this is forced
   * via {@link #ALWAYS_SIMPLE_PREFS}, or the device doesn't have newer APIs like {@link
   * PreferenceFragment}, or the device doesn't have an extra-large screen. In these cases, a
   * single-pane "simplified" settings UI should be shown.
   */
  private static boolean isSimplePreferences(Context context) {
    return (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) || !isXLargeTablet(context);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @TargetApi(VERSION_CODES.HONEYCOMB)
  public void onBuildHeaders(List<Header> target) {
    if (!isSimplePreferences(this)) {
      loadHeadersFromResource(R.xml.pref_headers, target);
    }
  }

  private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
  /**
   * A preference value change listener that updates the preference's summary to reflect its new
   * value.
   */
  private static Preference.OnPreferenceChangeListener
      sBindPreferenceSummaryToValueListener =
      new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
          String stringValue = value.toString();
          if (!sBindingPref) {
            if ((preference.getKey()
                     .equals(preference.getContext().getString(R.string.pref_student_id_key)))
                || (preference.getKey()
                        .equals(preference.getContext()
                                    .getString(R.string.pref_student_password_key)))) {
              DbufrSyncAdapter.syncImmediately(preference.getContext());
            } else {
              preference.getContext().getContentResolver()
                  .notifyChange(GradesContract.GradesEntry.CONTENT_URI, null);
            }
          }
          if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                index >= 0
                ? listPreference.getEntries()[index]
                : null);

          } else {
            // For all other preferences, set the summary to the value's
            // simple string representation. (exception for password)
            if (!preference.getKey()
                .equals(preference.getContext().getString(R.string.pref_student_password_key))) {
              preference.setSummary(stringValue);
            }
          }
          return true;
        }
      };

  /**
   * Binds a preference's summary to its value. More specifically, when the preference's value is
   * changed, its summary (line of text below the preference title) is updated to reflect the value.
   * The summary is also immediately updated upon calling this method. The exact display format is
   * dependent on the type of preference.
   *
   * @see #sBindPreferenceSummaryToValueListener
   */
  private static void bindPreferenceSummaryToValue(Preference preference) {
    sBindingPref = true;
    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

    // Trigger the listener immediately with the preference's
    // current value.
    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                                                             PreferenceManager
                                                                 .getDefaultSharedPreferences(
                                                                     preference.getContext())
                                                                 .getString(preference.getKey(),
                                                                            ""));

    sBindingPref = false;
  }

  /**
   * This fragment shows general preferences only. It is used when the activity is showing a
   * two-pane settings UI.
   */
  @TargetApi(VERSION_CODES.HONEYCOMB)
  public static class GeneralPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_general);

      // Bind the summaries of EditText/List/Dialog/Ringtone preferences
      // to their values. When their values change, their summaries are
      // updated to reflect the new value, per the Android Design
      // guidelines.
      bindPreferenceSummaryToValue(findPreference("pref_student_id"));
      bindPreferenceSummaryToValue(findPreference("pref_student_password"));
    }
  }

  /**
   * This fragment shows data and sync preferences only. It is used when the activity is showing a
   * two-pane settings UI.
   */
  @TargetApi(VERSION_CODES.HONEYCOMB)
  public static class DataSyncPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
    }
  }
}
