package com.github.davidsan.dbufr.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/*
 * Copyright (C) 2014 David San
 */
public class DbufrSyncService extends Service {

  private static final Object sSyncAdapterLock = new Object();
  private static DbufrSyncAdapter sDbufrSyncAdapter = null;

  @Override
  public void onCreate() {
    super.onCreate();
    synchronized (sSyncAdapterLock) {
      if (sDbufrSyncAdapter == null) {
        sDbufrSyncAdapter = new DbufrSyncAdapter(getApplicationContext(), true);
      }
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return sDbufrSyncAdapter.getSyncAdapterBinder();
  }
}
