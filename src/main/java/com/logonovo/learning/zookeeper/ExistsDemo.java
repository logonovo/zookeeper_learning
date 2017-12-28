package com.logonovo.learning.zookeeper;

import com.logonov.learning.utils.PropertiesUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Id;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2017/12/26 23:03
 */
public class ExistsDemo implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        String path ="/zk-exists";
        zk = new ZooKeeper(PropertiesUtil.getProperty("zk.server"), 5000, new ExistsDemo());
        connectedSemaphore.await();

        //检查节点是否存在，并且注册watcher
        zk.exists(path, true);

        zk.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.setData(path, "123".getBytes(), -1);

        zk.create(path +"/c1", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        zk.delete(path+"/c1",-1);
        zk.delete(path, -1);
        Thread.sleep(Integer.MAX_VALUE);
    }
    @Override
    public void process(WatchedEvent event) {
        try {
        if(Event.KeeperState.SyncConnected == event.getState()){
            if(Event.EventType.None == event.getType() && null == event.getType()){
                connectedSemaphore.await();
            }else if(Event.EventType.NodeCreated == event.getType()){
                System.out.println("Node("+event.getPath()+")Created");
            }else if(Event.EventType.NodeDeleted == event.getType()){
                System.out.println("Node("+event.getPath()+")Deleted");
            }else if(Event.EventType.NodeDataChanged == event.getType()){
                System.out.println("Node("+event.getPath()+")DataChanged");
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
