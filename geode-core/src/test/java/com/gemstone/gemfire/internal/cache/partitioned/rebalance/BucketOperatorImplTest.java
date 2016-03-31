package com.gemstone.gemfire.internal.cache.partitioned.rebalance;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.control.InternalResourceManager;
import com.gemstone.gemfire.internal.cache.partitioned.PartitionedRegionRebalanceOp;
import com.gemstone.gemfire.internal.cache.partitioned.rebalance.BucketOperator.Completion;
import com.gemstone.gemfire.test.junit.categories.UnitTest;

@Category(UnitTest.class)
public class BucketOperatorImplTest {

  private InternalResourceManager.ResourceObserver resourceObserver;

  private BucketOperatorImpl operator;

  private PartitionedRegion region;
  private PartitionedRegionRebalanceOp rebalanceOp;
  private Completion completion;

  private Map<String, Long> colocatedRegionBytes = new HashMap<String, Long>();
  private int bucketId = 1;
  private InternalDistributedMember sourceMember, targetMember;

  @Before
  public void setup() throws UnknownHostException {
    region = mock(PartitionedRegion.class);
    rebalanceOp = mock(PartitionedRegionRebalanceOp.class);
    completion = mock(Completion.class);
    
    resourceObserver = spy(new InternalResourceManager.ResourceObserverAdapter());
    InternalResourceManager.setResourceObserver(resourceObserver);
    
    doReturn(region).when(rebalanceOp).getLeaderRegion();

    operator = new BucketOperatorImpl(rebalanceOp);

    sourceMember = new InternalDistributedMember(InetAddress.getByName("127.0.0.1"), 1);
    targetMember = new InternalDistributedMember(InetAddress.getByName("127.0.0.2"), 1);
  }

  @After
  public void after() {
    reset(resourceObserver);
  }

  @Test
  public void moveBucketShouldDelegateToParRegRebalanceOpMoveBucketForRegion() throws UnknownHostException {
    doReturn(true).when(rebalanceOp).moveBucketForRegion(sourceMember, targetMember, bucketId);

    operator.moveBucket(sourceMember, targetMember, bucketId, colocatedRegionBytes);

    verify(resourceObserver, times(1)).movingBucket(region, bucketId, sourceMember, targetMember);
    verify(rebalanceOp, times(1)).moveBucketForRegion(sourceMember, targetMember, bucketId);
  }

  @Test
  public void movePrimaryShouldDelegateToParRegRebalanceOpMovePrimaryBucketForRegion() throws UnknownHostException {
    doReturn(true).when(rebalanceOp).movePrimaryBucketForRegion(targetMember, bucketId);

    operator.movePrimary(sourceMember, targetMember, bucketId);

    verify(resourceObserver, times(1)).movingPrimary(region, bucketId, sourceMember, targetMember);
    verify(rebalanceOp, times(1)).movePrimaryBucketForRegion(targetMember, bucketId);
  }

  @Test
  public void createBucketShouldDelegateToParRegRebalanceOpCreateRedundantBucketForRegion() throws UnknownHostException {
    doReturn(true).when(rebalanceOp).createRedundantBucketForRegion(targetMember, bucketId);

    operator.createRedundantBucket(targetMember, bucketId, colocatedRegionBytes, completion);

    verify(rebalanceOp, times(1)).createRedundantBucketForRegion(targetMember, bucketId);
  }

  @Test
  public void createBucketShouldInvokeOnSuccessIfCreateBucketSucceeds() {
    doReturn(true).when(rebalanceOp).createRedundantBucketForRegion(targetMember, bucketId);

    operator.createRedundantBucket(targetMember, bucketId, colocatedRegionBytes, completion);

    verify(rebalanceOp, times(1)).createRedundantBucketForRegion(targetMember, bucketId);
    verify(completion, times(1)).onSuccess();
  }

  @Test
  public void createBucketShouldInvokeOnFailureIfCreateBucketFails() {
    doReturn(false).when(rebalanceOp).createRedundantBucketForRegion(targetMember, bucketId); //return false for create fail

    operator.createRedundantBucket(targetMember, bucketId, colocatedRegionBytes, completion);

    verify(rebalanceOp, times(1)).createRedundantBucketForRegion(targetMember, bucketId);
    verify(completion, times(1)).onFailure();
  }

  @Test
  public void removeBucketShouldDelegateToParRegRebalanceOpRemoveRedundantBucketForRegion() {
    doReturn(true).when(rebalanceOp).removeRedundantBucketForRegion(targetMember, bucketId);

    operator.removeBucket(targetMember, bucketId, colocatedRegionBytes);

    verify(rebalanceOp, times(1)).removeRedundantBucketForRegion(targetMember, bucketId);
  }

}
