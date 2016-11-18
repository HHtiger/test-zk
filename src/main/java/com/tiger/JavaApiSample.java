package com.tiger;
/**
 * A simple example program to use DataMonitor to start and
 * stop executables based on a znode. The program watches the
 * specified znode and saves the data that corresponds to the
 * znode in the filesystem. It also starts the specified program
 * with the specified arguments when the znode exists and kills
 * the program if the znode goes away.
 */

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class JavaApiSample {

    private static Logger log = LoggerFactory.getLogger(JavaApiSample.class);

    private CuratorFramework client = null;

    private final InterProcessMutex lock ;

    private Properties properties;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public JavaApiSample(String connectionString) {
        // these are reasonable arguments for the ExponentialBackoffRetry.
        // The first retry will wait 1 second - the second will wait up to 2 seconds - the
        // third will wait up to 4 seconds.
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // The simplest way to get a CuratorFramework instance. This will use default values.
        // The only required arguments are the connection string and the retry policy
        client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        client.start();
        lock = new InterProcessMutex(client, "/tiger/a/1");
    }


    /**
     * 关闭ZK连接
     */
    public void releaseConnection() {
        client.close();
    }

    /**
     * 创建节点
     *
     * @param path 节点path
     * @param data 初始数据内容
     * @return path
     */
    public String createPath(String path, String data) {

        try {
            return client.create().withMode(CreateMode.PERSISTENT).forPath(path, data.getBytes());
        } catch (Exception e) {
            log.debug("节点创建失败");
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getChild(String path) {

        try {
            return client.getChildren().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 读取指定节点数据内容
     *
     * @param path 节点path
     * @return op
     */
    public String readData(String path) {

        try {
            return new String(client.getData().forPath(path));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 更新指定节点数据内容
     *
     * @param path 节点path
     * @param data 数据内容
     * @return op
     */
    public void writeData(String path, String data) {
        try {
            client.setData().forPath(path,data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定节点
     *
     * @param path 节点path
     */
    public void deleteNode(String path) {

        try {
            client.delete().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addOne() {
        // this example shows how to use ZooKeeper's new transactions
        try {
            String path = "/tiger/a/1";
            CuratorTransaction transaction = client.inTransaction();
            if (!lock.acquire(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException( client + " could not acquire the lock");
            }
            try {
                log.debug( client + " has the lock");
                byte[] data = client.getData().forPath(path);
                int i = Integer.parseInt(new String(data)) + 1;
                transaction.setData().forPath(path, String.valueOf(i).getBytes())
                        .and().commit();
            } finally {
                log.debug( client + " releasing the lock");
                lock.release(); // always release the lock in a finally block
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void toListener(String path){

        PathChildrenCacheListener listener = (client1, event) -> {
            switch ( event.getType() )
            {
                case CHILD_ADDED:
                {
                    System.out.println("Node added: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                    break;
                }

                case CHILD_UPDATED:
                {
                    System.out.println("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                    break;
                }

                case CHILD_REMOVED:
                {
                    System.out.println("Node removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                    break;
                }
            }
        };
//        String path = "/tiger/a/1";
        PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.getListenable().addListener(listener);

    }

    public void watchedGetChildren(String path, Watcher watcher) throws Exception
    {
        /**
         * Get children and set the given watcher on the node.
         */
        client.getData().usingWatcher(watcher).forPath(path);
    }

}