package com.logonovo.learning.zookeeper.loadbalance;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.ZkClient;
import com.logonov.learning.utils.PropertiesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2017/12/23 10:47
 */
public class ServiceConsumer {
    private List<String> serverList = new ArrayList<String>();

    public void init(){
        String serviceName = "service-B";
        String PATH = "/serverCenter";
        String SERVICE_PATH = PATH+"/"+serviceName;
        ZkClient zkClient = new ZkClient(PropertiesUtil.getProperty("zk.server"));

        boolean serviceExists = zkClient.exists(SERVICE_PATH);
        if(serviceExists){
            serverList = zkClient.getChildren(SERVICE_PATH);
        }else{
            throw new RuntimeException("service not exist!");
        }

        zkClient.subscribeChildChanges(SERVICE_PATH, new IZkChildListener() {
            public void handleChildChange(String parentPath, List<String> list) throws Exception {
                System.out.println(parentPath+",subscribeChildChanges："+list);
                serverList = list;
            }
        });

    }

    //消费服务
    public void consume(){
        //通过负载均衡算法，找到一台服务器进行调用
        if(serverList == null || serverList.size() == 0){
            throw new RuntimeException("找不到服务");
        }

    }

    public static void main(String[] args) throws InterruptedException {

        ServiceConsumer consumer = new ServiceConsumer();
        consumer.init();

        Thread.sleep(Integer.MAX_VALUE);
    }
}
