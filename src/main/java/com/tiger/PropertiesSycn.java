package com.tiger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package: com.tiger
 * ClassName: PropertiesSycn
 * Author: Tiger
 * Description:
 * CreateDate: 2016-11-18
 * Version: 1.0
 */
public class PropertiesSycn {

    private static Logger log = LoggerFactory.getLogger(PropertiesSycn.class);

    private CuratorFramework client = null;

    private static String path = "/tiger/properties/1";

    private int index = 0;

    public String getProperties() {
        try {
            return new String(client.getData().forPath(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setProperties(String properties) {
        try {
            client.setData().forPath(path,properties.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PropertiesSycn(String connectionString,int index) {
        this.index = index;
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        client.start();
        try {
            Stat stat = client.checkExists().forPath(path);

            if(stat == null){
                log.warn("properties is not created");
                client.create().creatingParentsIfNeeded().forPath(path,new byte[]{0});
            }

            client.getData().usingWatcher((Watcher) event -> System.out.println(index + " get notice:" + event)).forPath(path);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 关闭ZK连接
     */
    public void releaseConnection() {
        client.close();
    }

}
