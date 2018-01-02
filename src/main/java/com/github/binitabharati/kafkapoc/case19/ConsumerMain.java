package com.github.binitabharati.kafkapoc.case19;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerMain {
	
	final static Logger logger = LoggerFactory.getLogger(ConsumerMain.class);
	
	public static void main(String[] args) {
		CountDownLatch cdl = new CountDownLatch(2);
		
		Consumer ac = new Consumer(args[0], args[1],"consumer1");
		Consumer ac2 = new Consumer(args[0], args[1],  "consumer2");
		Consumer ac3 = new Consumer(args[0], args[1],  "consumer3");
		
		
		Thread t1 = new Thread(ac); t1.start();		
		Thread t2 = new Thread(ac2); t2.start();
		Thread t3 = new Thread(ac3); t3.start();
				
	}

}
