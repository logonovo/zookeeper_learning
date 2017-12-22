package com.logonovo.learning.zookeeper;

import com.logonov.learning.utils.PropertiesUtil;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2017/12/19 22:56
 */
public class SimpleWatcher  implements Watcher{
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    public void process(WatchedEvent watchedEvent) {
        System.out.println("Receive watched event:" +watchedEvent);
        if(Event.KeeperState.SyncConnected == watchedEvent.getState()) {
            connectedSemaphore.countDown();
        }
    }

    public static void main(String[] args) throws IOException {
        ZooKeeper zooKeeper = new ZooKeeper(PropertiesUtil.getProperty("zk.server"), 5000, new SimpleWatcher());
        System.out.println(zooKeeper.getState());
        try {
            connectedSemaphore.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("zookeeper session established");
    }
}
