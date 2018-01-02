package com.github.binitabharati.kafkapoc.case10;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class Consumer implements Runnable{
	
	final static Logger logger = LoggerFactory.getLogger(Consumer.class);
	
	private String name;
	private String topic;
	private String group;
	private KafkaConsumer<String, String> consumer;
	private CountDownLatch cdl;
	
	public Consumer(String name, String topic, String group, CountDownLatch cdl) {
		this.name = name;
		this.topic = topic;
		this.group = group;
		this.cdl = cdl;
		
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.10.12:9092,192.168.10.13:9092,192.168.10.14:9092");
		props.put("group.id", group);//Not mandatory, but assume its mandatory, since we want to write a group consumer.
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");//Switch off auto-commit
		

		consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Collections.singletonList(topic));
	}
	
	public void consume() {
		int pollCount = 0;
		try {
			while (true) {
		    	  logger.info(name + " invoking poll, so that GroupCoordinator does not deem me dead!");
			      ConsumerRecords<String, String> records = consumer.poll(100); 			      
			      if (records.isEmpty()){
			    	  logger.info(name + " did not receive any records :(");
			      } else {
			    	  pollCount++;
			    	  logger.info(name + " started receiving records :) of size = "+records.count());
			    	  for (ConsumerRecord<String, String> record : records) 
				      {
				    	  logger.info(name + "-topic = " + record.topic() + ", partition = "+ record.partition()
				    	  			+ ", offset = "+record.offset() + ", key="+record.key() + "-END-KEY" + ", msg="+record.value()); 
				      }
			    	  
			    	  consumer.commitSync();//commit only the records obtained during first invocation of poll.
			    	  if (pollCount == 1) {
			    		  break; //Next time, when u restart only the Consumer, the records that will be received by poll is lastCommitedOffset + 1.
			    	  }
			    	  
			      }			      
			  
			} 
		}
		catch (Exception ex)
		{
			logger.error("Opps Exception :(( ", ex);
		}
		finally {
			  //No use of finally, as you have done System.exit, control will never come here.
			  logger.info(name + " entered finally :)");
			  consumer.close(); 
			  cdl.countDown();
			}
	}
	
	public static void main(String[] args) {
		CountDownLatch cdl = new CountDownLatch(1);
		Consumer consm1 = new Consumer("CONSUMER-1", args[0], args[1], cdl);
	
		Thread t1 = new Thread(consm1);t1.start();
		try {
			cdl.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		consume();
		
	}

}
