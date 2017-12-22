package com.logonovo.learning.zookeeper;

import com.logonov.learning.utils.PropertiesUtil;
import org.apache.zookeeper.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2017/12/21 22:19
 */
public class GetChildrenSyncDemo implements Watcher {
    private static ZooKeeper zooKeeper;
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    public void process(WatchedEvent event) {
        System.out.println("Receive Watched event:" + event);
        if(Event.KeeperState.SyncConnected == event.getState()){
            if(Event.EventType.None == event.getType() && null == event.getPath()){
                connectedSemaphore.countDown();
            }else if(event.getType() == Event.EventType.NodeChildrenChanged){//子节点变更事件，只通知事件，子节点需要客户端再去获取
                try {
                    //ReGet Child:[c1, c2] 显示的相对路径
                    System.out.println("ReGet Child:" + zooKeeper.getChildren(event.getPath(), true));
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        zooKeeper = new ZooKeeper(PropertiesUtil.getProperty("zk.server"),5000,new GetChildrenSyncDemo());
        connectedSemaphore.await();

        String path = "/zk-test";
        zooKeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zooKeeper.create(path+"/c1", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);

        List<String> childrenList = zooKeeper.getChildren(path, true);
        //[c1] 显示的相对路径
        System.out.println(childrenList);

        zooKeeper.create(path+"/c2", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        Thread.sleep(Integer.MAX_VALUE);
    }
}
