package com.github.binitabharati.kafkapoc.case18;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class Consumer implements Runnable{
	
	final static Logger logger = LoggerFactory.getLogger(Consumer.class);
	
	private String topic;
	private String group;
	private String name;
	private KafkaConsumer<String, String> consumer;
	
	public Consumer(String topic, String group, String name) {
		this.topic = topic;
		this.group = group;
		this.name = name;
		
		
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.10.12:9092,192.168.10.13:9092,192.168.10.14:9092");
		props.put("group.id", group);//Not mandatory, but assume its mandatory, since we want to write a group consumer.
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		//props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");//Switch off auto-commit
		

		consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Collections.singletonList(topic));//this is also a instance of ConsumerRebalanceListener
	}
	
	public void consume() {
		long overallRecvdMsgCount = 0;
		long tId = Thread.currentThread().getId();
		try {			 
			  while (true) { 
				    	  logger.info(tId + "-" + name + "-" + " invoking poll, so that GroupCoordinator does not deem me dead!");
					      ConsumerRecords<String, String> records = consumer.poll(100); 
					      Map<TopicPartition, OffsetAndMetadata> currentPolledOffSets = new HashMap<>();
					      if (records.isEmpty()){
					    	  logger.info(tId + "-" + name + "-" + " did not receive any records at this moment :(");
					    	  logger.info(tId + "-" + name + "-" + " overallRecvdMsgCount = "+overallRecvdMsgCount);
					      } else {
					    	  logger.info(tId + "-" + name + "-" + " started receiving records :) of size = "+records.count());
					    	  overallRecvdMsgCount = overallRecvdMsgCount + records.count();
					    	  for (ConsumerRecord<String, String> record : records) 
						      {
						    	  logger.info(tId + "-" + name + "-" + "-topic = " + record.topic() + ", partition = "+ record.partition()
						    	  			+ ", offset = "+record.offset() + ", key="+record.key() + "-END-KEY" + ", msg="+record.value()); 
						    	  
						      }
					    	  			    	  
					      }			      
			  }
			} 
		catch (Exception ex)
		{
			logger.error(tId + "-" + name + "-" + "Opps Exception :(( ", ex);
		}
		finally {
	    		logger.info(tId + "-" + name + "-" + "-topic = " + " in finally");
				consumer.close(); 
			  
			}
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		consume();
		
		
	}

}

