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
/**
 * 
 */
package com.gemstone.gemfire.internal.cache.execute.data;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.internal.cache.execute.PRColocationDUnitTest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Customer implements DataSerializable {
  String name;

  String address;

  public Customer() {

  }

  public Customer(String name, String address) {
    this.name = name;
    this.address = address + PRColocationDUnitTest.getDefaultAddOnString();
  }

  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    this.name = DataSerializer.readString(in);
    this.address = DataSerializer.readString(in);

  }

  public void toData(DataOutput out) throws IOException {
    DataSerializer.writeString(this.name, out);
    DataSerializer.writeString(this.address, out);
  }

  @Override
  public String toString() {
    return "Customer { name=" + this.name + " address=" + this.address + "}";
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (!(o instanceof Customer))
      return false;

    Customer cust = (Customer)o;
    return (cust.name.equals(name) && cust.address.equals(address));
  }

  @Override
  public int hashCode() {
    return this.name.hashCode() + this.address.hashCode();
  }

}