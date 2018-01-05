package com.logonovo.learning.zookeeper.clientDemo;

import com.logonov.learning.utils.PropertiesUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2018/1/5 22:27
 */
public class CuratorDemo {

    public static void main(String[] args) throws Exception {
        CuratorFramework client = create();

        //master select
        //masterSelect(client);

        //recipes nolock
        final CountDownLatch down = new CountDownLatch(1);
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        down.await();
                    }catch (Exception e){}
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");
                    String orderNo = sdf.format(new Date());
                    System.out.println("生产订单号:"+orderNo);
                }
            }).start();
            down.countDown();
        }

        String lock_path = "/curator_recipes_lock_path";
        final InterProcessMutex lock = new InterProcessMutex(client, lock_path);
        final CountDownLatch down2 = new CountDownLatch(1);
        for (int i = 0; i < 30; i++) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        down.await();
                        lock.acquire();
                    }catch (Exception e){}
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");
                    String orderNo = sdf.format(new Date());
                    System.out.println("生产订单号:"+orderNo);
                    try {
                        lock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            down2.countDown();
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    private static CuratorFramework create() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(PropertiesUtil.getProperty("zk.server"), 5000, 3000, retryPolicy);
        client.start();

        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/curator/create", "init".getBytes());

        byte[] node = client.getData().forPath("/curator/create");
        System.out.println(new String(node));
        return client;
    }

    private static void masterSelect(CuratorFramework client) {
        String master_path = "/curator_master_path";
        LeaderSelector selector = new LeaderSelector(client, master_path, new LeaderSelectorListenerAdapter(){

            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                System.out.println("成为master");
                Thread.sleep(2000);
                System.out.println("完成master操作，释放");
            }
        });
        //selector.autoRequeue();
        selector.start();
    }
}
