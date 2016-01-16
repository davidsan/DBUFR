package com.github.davidsan.dbufr.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/*
 * Copyright (C) 2014 David San
 */
public class DbufrAuthenticatorService extends Service {

  private DbufrAuthenticator mAuthenticator;

  @Override
  public IBinder onBind(Intent intent) {
    return mAuthenticator.getIBinder();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mAuthenticator = new DbufrAuthenticator(this);
  }
}
