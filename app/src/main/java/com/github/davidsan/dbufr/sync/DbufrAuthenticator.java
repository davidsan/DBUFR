package com.github.davidsan.dbufr.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

/*
 * Copyright (C) 2014 David San
 */
public class DbufrAuthenticator extends AbstractAccountAuthenticator {

  public DbufrAuthenticator(Context context) {
    super(context);
  }

  @Override
  public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse,
                               String s) {
    return null;
  }

  @Override
  public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s,
                           String s2, String[] strings, Bundle bundle)
      throws NetworkErrorException {
    return null;
  }

  @Override
  public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                   Account account, Bundle bundle) throws NetworkErrorException {
    return null;
  }

  @Override
  public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse,
                             Account account, String s, Bundle bundle)
      throws NetworkErrorException {
    return null;
  }

  @Override
  public String getAuthTokenLabel(String s) {
    return null;
  }

  @Override
  public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                  Account account, String s, Bundle bundle)
      throws NetworkErrorException {
    return null;
  }

  @Override
  public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse,
                            Account account, String[] strings) throws NetworkErrorException {
    return null;
  }
}