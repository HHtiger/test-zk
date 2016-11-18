
package com.tiger;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Package: PACKAGE_NAME
 * ClassName: JavaApiSampleTest
 * Author: Tiger
 * Description:
 * CreateDate: 2016-11-16
 * Version: 1.0
 */
public class JavaApiSampleTest {

    private static Logger log = LoggerFactory.getLogger(JavaApiSampleTest.class);
    final static String CONNECTION_STRING = "172.29.214.33:2181";
    private static JavaApiSample sample;
    private final String ZK_PATH = "/tiger";

    @BeforeSuite
    public static void before() {
        log.warn("init zk.");
        sample = new JavaApiSample(CONNECTION_STRING);
    }


    @AfterSuite
    public static void after() {
        log.warn("release zk.");
        sample.releaseConnection();
    }


    @Test
    public void testCreatePath() {
        log.warn("testCreatePath");
        sample.createPath(ZK_PATH, "0");
        sample.createPath(ZK_PATH + "/a", "0");
        sample.createPath(ZK_PATH + "/a/1", "0");

    }

    @Test
    public void testGetData() throws KeeperException, InterruptedException {
        log.warn("testGetData");
//        sample.createPath(ZK_PATH + "/a/2","200");
        String dataPath = ZK_PATH + "/a";
        List<String> paths = sample.getChild(dataPath);
        for (String path : paths) {

            String fullPath = dataPath + "/" + path;
            String data = sample.readData(fullPath);
            log.debug(fullPath + "=" + data);

        }

    }

    @Test
    public void testWriteDate() {
        log.warn("testWriteDate");
        sample.writeData(ZK_PATH + "/a/1", "200");
        String data = sample.readData(ZK_PATH + "/a/1");
    }

    @Test(threadPoolSize = 10, invocationCount = 10)
    public void inTransaction() {
        log.warn("inTransaction");
        for (int i = 0; i < 10; i++) {
            sample.addOne();
        }
        String data = sample.readData(ZK_PATH + "/a/1");
        log.debug(data);

    }


    @Test
    public void testDelete() {
        log.warn("testDelete");
        sample.deleteNode(ZK_PATH + "/a/1");
        sample.deleteNode(ZK_PATH + "/a");
        sample.deleteNode(ZK_PATH);
    }

}