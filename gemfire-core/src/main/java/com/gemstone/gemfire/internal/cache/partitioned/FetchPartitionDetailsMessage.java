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
package com.gemstone.gemfire.internal.cache.partitioned;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.distributed.internal.DM;
import com.gemstone.gemfire.distributed.internal.DistributionManager;
import com.gemstone.gemfire.distributed.internal.DistributionMessage;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.distributed.internal.ReplyException;
import com.gemstone.gemfire.distributed.internal.ReplyMessage;
import com.gemstone.gemfire.distributed.internal.ReplyProcessor21;
import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.Assert;
import com.gemstone.gemfire.internal.InternalDataSerializer;
import com.gemstone.gemfire.internal.NanoTimer;
import com.gemstone.gemfire.internal.cache.ForceReattemptException;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;

public class FetchPartitionDetailsMessage extends PartitionMessage {

  private volatile boolean internal;
  private LoadProbe loadProbe;
  private boolean fetchOfflineMembers;
  
  /**
   * Empty constructor to satisfy {@link DataSerializer} requirements
   */
  public FetchPartitionDetailsMessage() {
  }

  private FetchPartitionDetailsMessage(
      Set<InternalDistributedMember> recipients, 
      int regionId, 
      ReplyProcessor21 processor,
      boolean internal, boolean fetchOfflineMembers, LoadProbe probe) {
    super(recipients, regionId, processor, null);
    this.internal = internal;
    this.fetchOfflineMembers = fetchOfflineMembers;
    this.loadProbe = probe;
  }

  /**
   * Sends a message to fetch {@link 
   * com.gemstone.gemfire.cache.partition.PartitionMemberInfo
   * PartitionMemberDetails} for the specified <code>PartitionedRegion</code>.
   * 
   * @param recipients the members to fetch PartitionMemberDetails from
   * @param region the PartitionedRegion to fetch member details for
   * @param fetchOfflineMembers 
   * @return the processor used to fetch the PartitionMemberDetails
   */
  public static FetchPartitionDetailsResponse send(
      Set<InternalDistributedMember> recipients, 
      PartitionedRegion region,
      boolean internal, boolean fetchOfflineMembers, LoadProbe probe) {
    
    Assert.assertTrue(recipients != null && !recipients.isEmpty(),
        "FetchPartitionDetailsMessage NULL recipient");
    
    FetchPartitionDetailsResponse response = new FetchPartitionDetailsResponse(
        region.getSystem(), recipients, region);
    FetchPartitionDetailsMessage msg = new FetchPartitionDetailsMessage(
        recipients, region.getPRId(), response, internal, fetchOfflineMembers, probe);

    /*Set<InternalDistributedMember> failures =*/ 
    region.getDistributionManager().putOutgoing(msg);

    region.getPrStats().incPartitionMessagesSent();
    return response;
  }

  public FetchPartitionDetailsMessage(DataInput in) 
  throws IOException, ClassNotFoundException {
    fromData(in);
  }

  @Override
  public boolean isSevereAlertCompatible() {
    // allow forced-disconnect processing for all cache op messages
    return true;
  }

  @Override
  protected final boolean operateOnPartitionedRegion(DistributionManager dm,
                                                     PartitionedRegion region, 
                                                     long startTime) 
                                              throws ForceReattemptException {
    
    PartitionMemberInfoImpl details = (PartitionMemberInfoImpl)
        region.getRedundancyProvider().buildPartitionMemberDetails(
            this.internal, this.loadProbe);
    OfflineMemberDetails offlineDetails; 
    if(this.internal && this.fetchOfflineMembers) {
      offlineDetails = region.getRedundancyProvider().fetchOfflineMembers();
    } else {
      offlineDetails = new OfflineMemberDetailsImpl(new Set[0]);
    }
    region.getPrStats().endPartitionMessagesProcessing(startTime);
    FetchPartitionDetailsReplyMessage.send(
        getSender(), getProcessorId(), details, dm, offlineDetails, null);
    
    // Unless there was an exception thrown, this message handles sending the
    // response
    return false;
  }

  @Override
  protected void appendFields(StringBuilder buff) {
    super.appendFields(buff);
    buff.append("; internal=").append(this.internal);
  }

  public int getDSFID() {
    return PR_FETCH_PARTITION_DETAILS_MESSAGE;
  }

  @Override
  public void fromData(DataInput in)
      throws IOException, ClassNotFoundException {
    super.fromData(in);
    this.internal = in.readBoolean();
    this.fetchOfflineMembers = in.readBoolean();
    this.loadProbe = (LoadProbe) DataSerializer.readObject(in);
  }

  @Override
  public void toData(DataOutput out)
      throws IOException {
    super.toData(out);
    out.writeBoolean(this.internal);
    out.writeBoolean(this.fetchOfflineMembers);
    DataSerializer.writeObject(loadProbe, out);
  }

  public static final class FetchPartitionDetailsReplyMessage 
  extends ReplyMessage {
    
    static final byte NO_PARTITION = 0;
    static final byte OK = 1;
    static final byte OK_INTERNAL = 2;
    
    private long configuredMaxMemory;
    private long size;
    private int bucketCount;
    private int primaryCount;
    private PRLoad prLoad;
    private long[] bucketSizes;
    private OfflineMemberDetails offlineDetails;
    
    /**
     * Empty constructor to conform to DataSerializable interface
     */
    public FetchPartitionDetailsReplyMessage() {
    }

    public FetchPartitionDetailsReplyMessage(DataInput in)
        throws IOException, ClassNotFoundException {
      fromData(in);
    }

    private FetchPartitionDetailsReplyMessage(int processorId,
        PartitionMemberInfoImpl details, OfflineMemberDetails offlineDetails, ReplyException re) {
      this.processorId = processorId;
      
      this.configuredMaxMemory = details.getConfiguredMaxMemory();
      this.size = details.getSize();
      this.bucketCount = details.getBucketCount();
      this.primaryCount = details.getPrimaryCount();
      this.prLoad = details.getPRLoad();
      this.bucketSizes = details.getBucketSizes();
      this.offlineDetails = offlineDetails;
      
      setException(re);
    }

    /** Send an ack 
     * @param offlineDetails */
    public static void send(InternalDistributedMember recipient,
                            int processorId, 
                            PartitionMemberInfoImpl details, 
                            DM dm, 
                            OfflineMemberDetails offlineDetails, 
                            ReplyException re) {
      Assert.assertTrue(recipient != null,
          "FetchPartitionDetailsReplyMessage NULL recipient");
      FetchPartitionDetailsReplyMessage m = 
          new FetchPartitionDetailsReplyMessage(processorId, details, offlineDetails, re);
      m.setRecipient(recipient);
      dm.putOutgoing(m);
    }

    @Override
    public void process(final DM dm, final ReplyProcessor21 processor) {
      final long startTime = getTimestamp();
      LogWriterI18n logger = dm.getLoggerI18n();
      if (DistributionManager.VERBOSE) {
        logger.fine("FetchPartitionDetailsReplyMessage process invoking reply processor with processorId:"
                + this.processorId);
      }

      if (processor == null) {
        if (DistributionManager.VERBOSE) {
          logger.fine("FetchPartitionDetailsReplyMessage processor not found");
        }
        return;
      }
      processor.process(this);

      if (DistributionManager.VERBOSE) {
        logger.info(LocalizedStrings.DEBUG, processor + " processed " + this);
      }
      dm.getStats().incReplyMessageTime(NanoTimer.getTime() - startTime);
    }

    InternalPartitionDetails unmarshalPartitionMemberDetails() {
      if (this.configuredMaxMemory == 0) {
        return null;
      }
      else {
        if (this.prLoad == null) {
          return new PartitionMemberInfoImpl(
              getSender(), this.configuredMaxMemory, this.size, 
              this.bucketCount, this.primaryCount);
        }
        else {
          return new PartitionMemberInfoImpl(
              getSender(), this.configuredMaxMemory, this.size, 
              this.bucketCount, this.primaryCount,
              this.prLoad, this.bucketSizes);
        }
      }
    }

    @Override
    public void toData(DataOutput out) throws IOException {
      super.toData(out);
      if (this.configuredMaxMemory == 0) {
        out.writeByte(NO_PARTITION);
      }
      else {
        if (this.prLoad == null) {
          out.writeByte(OK);
        }
        else {
          out.writeByte(OK_INTERNAL);
        }
        
        out.writeLong(this.configuredMaxMemory);
        out.writeLong(this.size);
        out.writeInt(this.bucketCount);
        out.writeInt(this.primaryCount);
        if (this.prLoad != null) {
          InternalDataSerializer.invokeToData(this.prLoad, out);
          DataSerializer.writeLongArray(this.bucketSizes, out);
          InternalDataSerializer.invokeToData(offlineDetails, out);
        }
      }
    }

    @Override
    public int getDSFID() {
      return PR_FETCH_PARTITION_DETAILS_REPLY;
    }

    @Override
    public void fromData(DataInput in) throws IOException,
        ClassNotFoundException {
      super.fromData(in);
      byte flag = in.readByte();
      if (flag != NO_PARTITION) {
        this.configuredMaxMemory = in.readLong();
        this.size = in.readLong();
        this.bucketCount = in.readInt();
        this.primaryCount = in.readInt();
        if (flag == OK_INTERNAL) {
          this.prLoad = PRLoad.createFromDataInput(in);
          this.bucketSizes = DataSerializer.readLongArray(in);
          this.offlineDetails = new OfflineMemberDetailsImpl();
          InternalDataSerializer.invokeFromData(this.offlineDetails, in);
        }
      }
    }

    @Override
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("FetchPartitionDetailsReplyMessage ")
        .append("processorid=").append(this.processorId)
        .append(" reply to sender ").append(this.getSender())
        .append(" returning configuredMaxMemory=").append(this.configuredMaxMemory)
        .append(" size=").append(this.size)
        .append(" bucketCount=").append(this.bucketCount)
        .append(" primaryCount=").append(this.primaryCount)
        .append(" prLoad=").append(this.prLoad)
        .append(" bucketSizes=").append(Arrays.toString(this.bucketSizes));
      return sb.toString();
    }
  }

  /**
   * A processor to capture the value returned by {@link 
   * com.gemstone.gemfire.internal.cache.partitioned.FetchPartitionDetailsMessage.FetchPartitionDetailsReplyMessage}
   * 
   */
  public static class FetchPartitionDetailsResponse extends
      PartitionResponse {
    
    private final Set<InternalPartitionDetails> allDetails = 
        new HashSet<InternalPartitionDetails>();
    private OfflineMemberDetails offlineDetails; 

    final PartitionedRegion partitionedRegion;

    public FetchPartitionDetailsResponse(
        InternalDistributedSystem ds,
        Set<InternalDistributedMember> recipients, 
        PartitionedRegion theRegion) {
      super(ds, recipients, null);
      partitionedRegion = theRegion;
    }

    @Override
    public void process(DistributionMessage msg) {
      try {
        if (msg instanceof FetchPartitionDetailsReplyMessage) {
          FetchPartitionDetailsReplyMessage reply = 
              (FetchPartitionDetailsReplyMessage)msg;
          InternalPartitionDetails details = reply.unmarshalPartitionMemberDetails();
          if (details != null) {
            synchronized(allDetails) {
              this.allDetails.add(details);
              //This just picks the offline details from the last member to return
              this.offlineDetails = reply.offlineDetails;
            }
            if (DistributionManager.VERBOSE) {
              getDistributionManager().getLoggerI18n().fine(
                  "FetchPartitionDetailsResponse return details is "
                      + details);
            }
          }
          else if (DistributionManager.VERBOSE) {
            getDistributionManager().getLoggerI18n().fine(
                "FetchPartitionDetailsResponse ignoring null details");
          }
        }
      }
      finally {
        super.process(msg);
      }
    }

    /**
     * Ignore any incoming exception from other VMs, we just want an
     * acknowledgement that the message was processed.
     */
    @Override
    protected void processException(ReplyException ex) {
      getDistributionManager().getLoggerI18n().fine(
          "FetchPartitionDetailsResponse ignoring exception", ex);
    }
    
    /**
     * @return set of all PartitionMemberDetails
     */
    public Set<InternalPartitionDetails> waitForResponse() {
      waitForRepliesUninterruptibly();
      synchronized(allDetails) {
        return this.allDetails;
      }
    }

    public OfflineMemberDetails getOfflineMembers() {
      return offlineDetails;
    }
  }

}