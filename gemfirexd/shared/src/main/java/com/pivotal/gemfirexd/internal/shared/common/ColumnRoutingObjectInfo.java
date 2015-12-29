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
package com.pivotal.gemfirexd.internal.shared.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 
 * @author kneeraj
 * 
 */
public abstract class ColumnRoutingObjectInfo extends AbstractRoutingObjectInfo {

  public ColumnRoutingObjectInfo() {
    
  }

  public ColumnRoutingObjectInfo(int isParameter, Object val,
      Object resolver) {
    super(isParameter, val, resolver);
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
  }

  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    super.readExternal(in);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ColumnRoutingObjectInfo: ");
    sb.append(super.toString());
    return sb.toString();
  }
}