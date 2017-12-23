package com.logonovo.learning.zookeeper.loadbalance;

import com.github.zkclient.ZkClient;
import com.logonov.learning.utils.PropertiesUtil;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2017/12/23 10:47
 */
public class ServiceBProvider{
    public static final String PATH = "/serverCenter";//根节点路径
    public  String service = "service-B";

    public  void init() throws IOException{
        String serverList = PropertiesUtil.getProperty("zk.server");
        ZkClient zkClient = new ZkClient(serverList);
        boolean rootExists = zkClient.exists(PATH);
        if(!rootExists){
            zkClient.createPersistent(PATH);
        }
        boolean serviceExists = zkClient.exists(PATH+"/"+service);
        if(!serviceExists){
            zkClient.createPersistent(PATH+"/"+service);
        }
        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress().toString();
        zkClient.createEphemeral(PATH+"/"+service+"/"+ip);
    };
    //提供服务
    public void provide() {

    }

    public static void main(String[] args) throws Exception {
        ServiceBProvider provider = new ServiceBProvider();
        provider.init();

        Thread.sleep(Integer.MAX_VALUE);
    }
}
