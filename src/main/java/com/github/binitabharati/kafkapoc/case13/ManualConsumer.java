package com.github.binitabharati.kafkapoc.case13;

import java.util.ArrayList;
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

/**
 * 
 * @author binita
 * Ref: https://kafka.apache.org/0110/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html
 *
 */

public class ManualConsumer implements Runnable {
	
	final static Logger logger = LoggerFactory.getLogger(ManualConsumer.class);
	
	private String topic;
	private String group;
	private KafkaConsumer<String, String> consumer;
	private CountDownLatch cdl;
	
	public ManualConsumer(String topic, String group, CountDownLatch cdl) {
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
		//consumer.subscribe(Collections.singletonList(topic) );
		
		Collection<TopicPartition> tpColl = new ArrayList<>();
		TopicPartition tp = new TopicPartition(topic, 0);
		tpColl.add(tp);
		consumer.assign(tpColl);
	}
		
	public KafkaConsumer<String, String> getConsumer() {
		return consumer;
	}

	public void setConsumer(KafkaConsumer<String, String> consumer) {
		this.consumer = consumer;
	}

	public void consume() {
		 long tId = Thread.currentThread().getId();
		try {			 
			  while (true) { 
				    	  logger.info(tId + "-ManualConsumer" + " invoking poll, so that GroupCoordinator does not deem me dead!");
					      ConsumerRecords<String, String> records = consumer.poll(100); 
					      Map<TopicPartition, OffsetAndMetadata> currentPolledOffSets = new HashMap<>();
					      if (records.isEmpty()){
					    	  logger.info(tId + "-ManualConsumer" + " did not receive any records :(");
					      } else {
					    	  logger.info(tId + "-ManualConsumer" + " started receiving records :) of size = "+records.count());
					    	  for (ConsumerRecord<String, String> record : records) 
						      {
						    	  logger.info(tId + "-ManualConsumer" + "-topic = " + record.topic() + ", partition = "+ record.partition()
						    	  			+ ", offset = "+record.offset() + ", key="+record.key() + "-END-KEY" + ", msg="+record.value()); 
						    	  
						      }
					    	 			    	  
					      }			      
			  }
			} 
		catch (Exception ex)
		{
			logger.error(tId + "-ManualConsumer" + "Opps Exception :(( ", ex);
		}
		finally {
	    		logger.info(tId + "-ManualConsumer" + "-topic = " + " in finally");
				consumer.close(); 
				consumer.commitSync();
			  
			}
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		consume();
		cdl.countDown();
		
	}


}

