package com.logonovo.learning.zookeeper;

import com.logonov.learning.utils.PropertiesUtil;
import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

/**
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2017/12/21 21:44
 */
public class CreateSyncDemo implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    public void process(WatchedEvent event) {
        System.out.println("Receive Watched event: "+event);
        if(Event.KeeperState.SyncConnected == event.getState()){
            connectedSemaphore.countDown();
        }
    }

    public static void main(String[] args) throws Exception{
        ZooKeeper zooKeeper = new ZooKeeper(PropertiesUtil.getProperty("zk.server"),5000,new CreateSyncDemo());
        System.out.println(zooKeeper);
        connectedSemaphore.await();
        //创建临时节点
        String path1 = zooKeeper.create("/zk-test-ephemeral-","".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println("Success create znode:" + path1);
        //创建临时顺序节点
        String path2 = zooKeeper.create("/zk-test-ephemeral-","".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Success create znode:" + path2);
    }
}
