/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.pivotal.gemfirexd.jdbc.offheap;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.internal.cache.CacheObserverHolder;
import com.gemstone.gemfire.internal.cache.LocalRegion;
import com.pivotal.gemfirexd.internal.engine.GemFireXDQueryObserverHolder;
import com.pivotal.gemfirexd.internal.engine.jdbc.GemFireXDRuntimeException;
import com.pivotal.gemfirexd.internal.engine.management.GfxdManagementService;
import com.pivotal.gemfirexd.jdbc.ForeignKeyTest;
import com.pivotal.gemfirexd.jdbc.JdbcTestBase;
import com.pivotal.gemfirexd.jdbc.JdbcTestBase.RegionMapClearDetector;

public class OffheapForeignKeyTest extends ForeignKeyTest {
  private RegionMapClearDetector rmcd = null;	
  
  public static void main(String[] args) {
    TestRunner.run(new TestSuite(OffheapForeignKeyTest.class));
  }
  
  
  public OffheapForeignKeyTest(String name) {
    super(name); 
  }
  
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    System.setProperty("gemfire.OFF_HEAP_TOTAL_SIZE", "100M");
    System.setProperty("gemfire."+DistributionConfig.OFF_HEAP_MEMORY_SIZE_NAME, "50M");
    System.setProperty(GfxdManagementService.DISABLE_MANAGEMENT_PROPERTY,"true");
    LocalRegion.ISSUE_CALLBACKS_TO_CACHE_OBSERVER = true;
    rmcd = new JdbcTestBase.RegionMapClearDetector();
    CacheObserverHolder.setInstance(rmcd);
    GemFireXDQueryObserverHolder.putInstance(rmcd);
  }
  
  @Override
  public void tearDown() throws Exception {
	LocalRegion.ISSUE_CALLBACKS_TO_CACHE_OBSERVER = false;
	CacheObserverHolder.setInstance(null);
	GemFireXDQueryObserverHolder.clearInstance();    
    super.tearDown();
    System.setProperty("gemfire.OFF_HEAP_TOTAL_SIZE", "");
    System.setProperty("gemfire."+DistributionConfig.OFF_HEAP_MEMORY_SIZE_NAME, "");
    System.setProperty(GfxdManagementService.DISABLE_MANAGEMENT_PROPERTY,"");
  }
  
  @Override
  protected String getSuffix() {
    return " offheap ";
  }

  @Override
  public void waitTillAllClear() {
	try {  
    rmcd.waitTillAllClear();
	}catch(InterruptedException ie) {
	  Thread.currentThread().interrupt();
	  throw new GemFireXDRuntimeException(ie);
	}
  }
}