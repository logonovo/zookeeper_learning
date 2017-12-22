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
 * @Date 2017/12/20 20:39
 */
public class ReUseSessionWithPassword implements Watcher{
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public void process(WatchedEvent event) {
        System.out.println("Receive Watched event:" + event);
        connectedSemaphore.countDown();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ZooKeeper zooKeeper = new ZooKeeper(PropertiesUtil.getProperty("zk.server"), 5000, new ReUseSessionWithPassword());
        connectedSemaphore.await();
        long sessionId = zooKeeper.getSessionId();
        byte[] passwd = zooKeeper.getSessionPasswd();
        //wrong sesssionid and password
        connectedSemaphore = new CountDownLatch(1);
        zooKeeper = new ZooKeeper("192.168.31.200:2181", 5000, new ReUseSessionWithPassword(),1l,"test".getBytes());

        connectedSemaphore.await();
        connectedSemaphore = new CountDownLatch(1);
        //right sessionid and passwd
        zooKeeper = new ZooKeeper("192.168.31.200:2181", 5000, new ReUseSessionWithPassword(),sessionId,passwd);

        connectedSemaphore.await();
    }
}
