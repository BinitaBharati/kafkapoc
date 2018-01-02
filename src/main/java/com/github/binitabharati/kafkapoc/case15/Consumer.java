package com.github.binitabharati.kafkapoc.case15;

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

public class Consumer implements ConsumerRebalanceListener, Runnable{
	
	final static Logger logger = LoggerFactory.getLogger(Consumer.class);
	
	private String topic;
	private String group;
	private String name;
	private KafkaConsumer<String, String> consumer;
	private CountDownLatch cdl;
	
	public Consumer(String topic, String group, CountDownLatch cdl, String name) {
		this.topic = topic;
		this.group = group;
		this.cdl = cdl;
		this.name = name;
		
		
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.10.12:9092,192.168.10.13:9092,192.168.10.14:9092");
		props.put("group.id", group);//Not mandatory, but assume its mandatory, since we want to write a group consumer.
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");//Switch off auto-commit
		

		consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Collections.singletonList(topic), this );//this is also a instance of ConsumerRebalanceListener
	}
	
	public void consume() {
		long tId = Thread.currentThread().getId();
		try {			 
			  while (true) { 
				    	  logger.info(tId + "-" + name + "-" + " invoking poll, so that GroupCoordinator does not deem me dead!");
					      ConsumerRecords<String, String> records = consumer.poll(100); 
					      Map<TopicPartition, OffsetAndMetadata> currentPolledOffSets = new HashMap<>();
					      if (records.isEmpty()){
					    	  logger.info(tId + "-" + name + "-" + " did not receive any records :(");
					      } else {
					    	  logger.info(tId + "-" + name + "-" + " started receiving records :) of size = "+records.count());
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
	public void onPartitionsAssigned(Collection<TopicPartition> arg0) {
		// TODO Auto-generated method stub
		//Partitions get assigned, when a consumer is deemed dead by the GroupCoordinator, and another running Consumer 
		//(ie one which is invoking poll() ) is picked by the GroupCoordinator to reassign the dead Consumer's partitions.
		long tId = Thread.currentThread().getId();
		logger.info(tId + "-" + name + " onPartitionsAssigned:  is triggered ");
		logger.info(tId + "-" + name + " onPartitionsAssigned:  Printing details of assigned topic partition started");
		for (TopicPartition topicPartition : arg0) {
			logger.info(tId + "-" + name + " onPartitionsAssigned: tp details; topic =  "+topicPartition.topic() + ", partition = "+topicPartition.partition());
		}
		logger.info(tId + "-" + name + " onPartitionsAssigned:  Printing details of assigned topic partition ended");
		
	}

	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> arg0) {
		// TODO Auto-generated method stub
		//Partitions are revoking means this consumer is going down according to the GroupCoordinator
		long tId = Thread.currentThread().getId();
		logger.info(tId + "-" + name + " onPartitionsRevoked:  is triggered ");
		logger.info(tId + "-" + name + " onPartitionsRevoked:  Printing details of revoked topic partition started");
		for (TopicPartition topicPartition : arg0) {
			logger.info(tId + "-" + name + " onPartitionsRevoked: tp details; topic =  "+topicPartition.topic() + ", partition = "+topicPartition.partition());
		}
		logger.info(tId + "-" + name + " onPartitionsRevoked:  Printing details of revoked topic partition ended");
		//Good place to do a blocking call for commitSync
		consumer.commitSync();
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		consume();
		cdl.countDown();
		
	}

}

