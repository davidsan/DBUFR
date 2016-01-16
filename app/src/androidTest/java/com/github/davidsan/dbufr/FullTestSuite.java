package com.github.davidsan.dbufr;
/*
 * Copyright (C) 2014 David San
 */

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FullTestSuite extends TestSuite {
  public static Test suite() {
    return new TestSuiteBuilder(FullTestSuite.class)
        .includeAllPackagesUnderHere().build();
  }

  public FullTestSuite() {
    super();
  }
}
