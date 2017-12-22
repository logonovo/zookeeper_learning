package com.logonovo.learning.zookeeper;

import com.logonov.learning.utils.PropertiesUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2017/12/22 23:02
 */
public class GetDataSyncDemo implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk = null;
    private static Stat stat = new Stat();

    public void process(WatchedEvent event) {
        if(Event.KeeperState.SyncConnected == event.getState()){
            if(Event.EventType.None == event.getType() && null == event.getPath()){
                connectedSemaphore.countDown();
            }else if(event.getType() == Event.EventType.NodeDataChanged){//处理节点数据变更事件
                try {
                    System.out.println(new String(zk.getData(event.getPath(), true, stat)));
                    System.out.println(stat.getCzxid()+","+stat.getMzxid()+","+stat.getVersion());
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String path = "/zk-getdata";
        zk = new ZooKeeper(PropertiesUtil.getProperty("zk.server"),5000, new GetDataSyncDemo());
        connectedSemaphore.await();
        zk.create(path, "123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        //getData会将stat内容替换为最新的数据
        System.out.println(new String(zk.getData(path,true, stat)));
        System.out.println(stat.getCzxid()+","+stat.getMzxid()+","+stat.getVersion());
        //节点数据变更
        zk.setData(path, "123".getBytes(),-1);

        Thread.sleep(Integer.MAX_VALUE);
    }
}
