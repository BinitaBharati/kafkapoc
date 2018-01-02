package com.github.binitabharati.kafkapoc.case14;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		CountDownLatch cdl = new CountDownLatch(2);
		CountDownLatch cdl2 = new CountDownLatch(10);
		
		AutoConsumer1 ac = new AutoConsumer1(args[0], args[1], cdl, cdl2);
		AutoConsumer2 ac2 = new AutoConsumer2(args[0], args[1], cdl);
		
		
		Thread t1 = new Thread(ac); t1.start();
		try {
			cdl2.await();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		logger.info("main: after awaiting for latch - starting auto consumer2");
		Thread t2 = new Thread(ac2); t2.start();
		try {
			cdl.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
