package com.github.binitabharati.kafkapoc.case15;

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
		
		Consumer ac = new Consumer(args[0], args[1], cdl, "consumer1");
		Consumer ac2 = new Consumer(args[0], args[1], cdl, "consumer2");
		
		
		Thread t1 = new Thread(ac); t1.start();		
		Thread t2 = new Thread(ac2); t2.start();
		
		try {
			cdl.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
