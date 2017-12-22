package com.logonovo.learning.zookeeper;

import com.logonov.learning.utils.PropertiesUtil;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2017/12/21 22:02
 */
public class CreateAsyncDemo implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    public void process(WatchedEvent event) {
        System.out.println("Receive Watched event: "+event);
        if(Event.KeeperState.SyncConnected == event.getState()){
            connectedSemaphore.countDown();
        }
    }

    public static void main(String[] args) throws Exception {
        ZooKeeper zooKeeper = new ZooKeeper(PropertiesUtil.getProperty("zk.server"),5000,new CreateAsyncDemo());
        connectedSemaphore.await();
        //异步创建接口，实现AsyncCallback.xxx接口
        zooKeeper.create("/zk-test-ephemeral-","".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                new IStringCallBack(),"some context");

        zooKeeper.create("/zk-test-ephemeral-","".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,
                new IStringCallBack(),"some context");
        Thread.sleep(Integer.MAX_VALUE);
    }
}
class IStringCallBack implements AsyncCallback.StringCallback{

    public void processResult(int rc, String path, Object ctx, String name) {
        System.out.println("Create Path result: ["+rc+","+path+", "+ctx+", path name:" +name);
    }
}
