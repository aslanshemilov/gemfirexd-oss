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

package com.gemstone.gemfire.cache.execute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.gemstone.gemfire.GemFireException;
import com.gemstone.gemfire.internal.Assert;

/**
 * Thrown to indicate incorrect execution of {@linkplain Function}s in GemFire.
 *
 * <p>The exception string provides details on the cause of failure.
 * </p>
 * 
 * @author Yogesh Mahajan
 * @author Mitch Thomas
 * 
 * @since 6.0
 * @see FunctionService
 */
public class FunctionException extends GemFireException {

  private static final long serialVersionUID = 4893171227542647452L;

  private transient ArrayList<Throwable> exceptions;

  /**
   * Creates new function exception with given error message.
   * 
   * @since 6.5
   */
  public FunctionException() {
  }

  /**
   * Creates new function exception with given error message.
   * 
   * @param msg
   * @since 6.0
   */
  public FunctionException(String msg) {
    super(msg);
  }

  /**
   * Creates new function exception with given error message and optional nested
   * exception.
   * 
   * @param msg
   * @param cause
   * @since 6.0 
   */
  public FunctionException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Creates new function exception given throwable as a cause and source of
   * error message.
   * 
   * @param cause
   * @since 6.0 
   */
  public FunctionException(Throwable cause) {
    super(cause);
  }

  /**
   * Adds exceptions thrown from different nodes to a ds
   * 
   * @param cause
   * @since 6.5
   */
  public final void addException(Throwable cause) {
    Assert.assertTrue(cause != null,
        "unexpected null exception to add to FunctionException");
    getExceptions().add(cause);
  }

  /**
   * Returns the list of exceptions thrown from different nodes
   * 
   * @since 6.5
   */
  public final List<Throwable> getExceptions() {
    if (this.exceptions == null) {
      this.exceptions = new ArrayList<Throwable>();
    }
    return this.exceptions;
  }

  /**
   * Adds the list of exceptions provided
   * 
   * @since 6.5
   */
  public final void addExceptions(Collection<? extends Throwable> ex) {
    getExceptions().addAll(ex);
  }
}
