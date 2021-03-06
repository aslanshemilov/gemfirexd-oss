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

package com.gemstone.gemfire.internal.cache.execute;

import java.io.Serializable;

import java.util.concurrent.TimeUnit;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;

/**
 * A Special ResultCollector implementation. Functions having
 * {@link Function#hasResult()} false, this ResultCollector will be returned.
 * <br>
 * Calling getResult on this NoResult will throw
 * {@link FunctionException}
 * 
 * 
 * @author Yogesh Mahajan
 * 
 * @since 5.8 Beta
 * 
 * @see Function#hasResult()
 * 
 */
public final class NoResult implements ResultCollector, Serializable {

  private static final long serialVersionUID = -4901369422864228848L;

  public void addResult(DistributedMember memberID,
      Object resultOfSingleExecution) {
    throw new UnsupportedOperationException(
        LocalizedStrings.ExecuteFunction_CANNOT_0_RESULTS_HASRESULT_FALSE
            .toLocalizedString("add"));
  }

  public void endResults() {
    throw new UnsupportedOperationException(
        LocalizedStrings.ExecuteFunction_CANNOT_0_RESULTS_HASRESULT_FALSE
            .toLocalizedString("close"));
  }

  public Object getResult() throws FunctionException {
    throw new FunctionException(
        LocalizedStrings.ExecuteFunction_CANNOT_0_RESULTS_HASRESULT_FALSE
            .toLocalizedString("return any"));
  }

  public Object getResult(long timeout, TimeUnit unit)
      throws FunctionException, InterruptedException {
    throw new FunctionException(
        LocalizedStrings.ExecuteFunction_CANNOT_0_RESULTS_HASRESULT_FALSE
            .toLocalizedString("return any"));
  }
  
  public void clearResults() {
    throw new UnsupportedOperationException(
        LocalizedStrings.ExecuteFunction_CANNOT_0_RESULTS_HASRESULT_FALSE
            .toLocalizedString("clear"));
  }
}
