package com.tiger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.tiger
 * ClassName: WatcherTest
 * Author: Tiger
 * Description:
 * CreateDate: 2016-11-18
 * Version: 1.0
 */
public class PropertiesSycnTest {

    private static Logger log = LoggerFactory.getLogger(PropertiesSycnTest.class);
    final static String CONNECTION_STRING = "172.29.214.33:2181";
    private static List<PropertiesSycn> propertiesSycnList;
    private final String ZK_PATH = "/tiger";


    @BeforeSuite
    public static void before() {
        log.warn("init zk.");
        propertiesSycnList = new ArrayList<>();
        for(int i = 0; i< 5;i++){
            PropertiesSycn propertiesSycn = new PropertiesSycn(CONNECTION_STRING , i);
            propertiesSycnList.add(propertiesSycn);
        }
    }


    @AfterSuite
    public static void after() {
        log.warn("release zk.");
        for(PropertiesSycn propertiesSycn : propertiesSycnList) {
            propertiesSycn.releaseConnection();
        }
    }

    public void print(){
        for( PropertiesSycn propertiesSycn : propertiesSycnList){
            System.out.println(propertiesSycn.getProperties());
        }
    }

    @Test
    public void changeProperties() throws IOException {

        /*change data by using shell*/
        System.in.read();

        log.warn("changeProperties");
        print();
        propertiesSycnList.get(0).setProperties("4");
        print();
    }
}
