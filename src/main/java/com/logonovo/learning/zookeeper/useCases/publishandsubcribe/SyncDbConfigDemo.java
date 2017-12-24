package com.logonovo.learning.zookeeper.useCases.publishandsubcribe;

import com.alibaba.fastjson.JSON;
import com.github.zkclient.IZkDataListener;
import com.github.zkclient.ZkClient;
import com.logonov.learning.utils.PropertiesUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

/**
 * 同步数据库配置demo
 * @Author 小凡
 * Email: logonovo@gmail.com
 * @Date 2017/12/24 8:13
 */
public class SyncDbConfigDemo{
    private static ZkClient zkClient= null;

    public static void main(String[] args) throws Exception {
        ZkClient zkClient = new ZkClient("192.168.31.200:2181");
        String PATH = "/configServer";
        String service = "server";
        boolean rootExists = zkClient.exists(PATH);
        if(!rootExists){
            zkClient.createPersistent(PATH);
        }
        boolean serviceExists = zkClient.exists(PATH+"/"+service);
        if(!serviceExists){
            zkClient.createPersistent(PATH+"/"+service);
        }
        String dbConfigPath = PATH+"/"+service+"/dbconfig";
        boolean dbConfigExists = zkClient.exists(dbConfigPath);
        DbConfig config = null;
        if(!dbConfigExists){
            config = new DbConfig();
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setUrl("jdbc:mysql://localhost:3306/mmall?characterEncoding=utf-8");
            config.setUserName("root");
            config.setPassword("123456");
            config.setInitialSize(10);
            config.setMaxActive(50);
            config.setMaxIdle(20);
            config.setMaxIdle(10);
            config.setMaxWait(10);
            config.setDefaultAutoCommit(true);
            String configInfo = JSON.toJSONString(config);
            zkClient.createEphemeral(dbConfigPath,configInfo.getBytes());
            System.out.println("创建dbconfig节点");
            byte[] data = zkClient.readData(dbConfigPath);
            System.out.println("初始化配置"+new String(data));

        }else{
            byte[] data = zkClient.readData(dbConfigPath);
            System.out.println("dbconfig已经存在:"+new String(data));
            config = JSON.parseObject(new String(data), DbConfig.class);
        }
        //监听节点数据变更
        zkClient.subscribeDataChanges(dbConfigPath,new IZkDataListener(){

            public void handleDataChange(String dataPath, byte[] data) throws Exception {
                String configStr = new String(data);
                System.out.println("数据更新后配置:"+configStr);
                DbConfig newConfig = JSON.parseObject(configStr, DbConfig.class);
                System.out.println(newConfig);
            }

            public void handleDataDeleted(String dataPath) throws Exception {
                System.out.println("删除配置:"+dataPath);
            }
        });

        //更新配置
        config.setPassword("654321");
        zkClient.writeData(dbConfigPath,JSON.toJSONString(config).getBytes());

        Thread.sleep(Integer.MAX_VALUE);
    }
}
