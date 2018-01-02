package com.github.binitabharati.kafkapoc.case11;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;

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
		try {
			  while (true) { 
		    	  logger.info(name + " invoking poll, so that GroupCoordinator does not deem me dead!");
			      ConsumerRecords<String, String> records = consumer.poll(100); 
			      Map<TopicPartition, OffsetAndMetadata> currentPolledOffSets = new HashMap<>();
			      if (records.isEmpty()){
			    	  logger.info(name + " did not receive any records :(");
			      } else {
			    	  logger.info(name + " started receiving records :) of size = "+records.count());
			    	  for (ConsumerRecord<String, String> record : records) 
				      {
				    	  logger.info(name + "-topic = " + record.topic() + ", partition = "+ record.partition()
				    	  			+ ", offset = "+record.offset() + ", key="+record.key() + "-END-KEY" + ", msg="+record.value()); 
				    	  currentPolledOffSets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
				      }
			    	  logger.info(name + "-topic = " + " about to commit async:)");
			    	  consumer.commitAsync(currentPolledOffSets, new AsyncOffSetCommitCallBack(name, consumer));
			    	  //Now, do some long standing operation, so that poll() does not get invoked by this consumer thread.
			    	  try {
				    	logger.info(name + "-topic = " + " going to snooze");
						Thread.sleep(30*60*1000);
						logger.info(name + " awoke from sleep! Will start polling again.");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						logger.error("Interrupted while sleep ", e);
					}
			    	  
			      }
			      
			      
			  }
			} 
		catch (Exception ex)
		{
			logger.error("Opps Exception :(( ", ex);
		}
		finally {
	    		logger.info(name + "-topic = " + " in finally");
				consumer.commitSync();
				consumer.close(); 
				cdl.countDown();
			  
			}
	}
	
	public static void main(String[] args) {
		CountDownLatch cdl = new CountDownLatch(3);
		Consumer consm1 = new Consumer("CONSUMER-1", args[0], args[1], cdl);
		
		Consumer consm2 = new Consumer("CONSUMER-2", args[0], args[1], cdl);
		
		Consumer consm3 = new Consumer("CONSUMER-3", args[0], args[1], cdl);
		
		Thread t1 = new Thread(consm1);t1.start();
		Thread t2 = new Thread(consm2);t2.start();
		Thread t3 = new Thread(consm3);t3.start();
		
		try {
			cdl.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error("CountDown await interrupted - "+e);
		}
		
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		consume();
		
	}

}

class AsyncOffSetCommitCallBack implements OffsetCommitCallback {

	private KafkaConsumer<String, String> consumer;
	private String name;
	
	static Logger logger = LoggerFactory.getLogger(AsyncOffSetCommitCallBack.class);
	
	public AsyncOffSetCommitCallBack(String name, KafkaConsumer<String, String> consumer){
		this.consumer = consumer;
		this.name = name;
	}
	@Override
	public void onComplete(Map<TopicPartition, OffsetAndMetadata> commitedOffSets,
			Exception exception) {
		// TODO Auto-generated method stub
		logger.info(name+ " onComplete: entered with commitedOffSets = "+commitedOffSets);
		logger.info(name+ " onComplete: entered with exception = "+exception);
		if (exception != null && exception.getMessage() != null) {
			logger.info(name+ " onComplete: entered with exception = "+exception.getMessage());
			//Try commitSync now, as this will also retry till it successfully commits.
			consumer.commitSync(commitedOffSets);
		} else {
			commitedOffSets.entrySet().forEach(eachEntry -> {
				TopicPartition tp = eachEntry.getKey();
				OffsetAndMetadata meta = eachEntry.getValue();
				logger.info(name+ " onComplete: TP = topic = "+ tp.topic() + ", partition = "+tp.partition() + ", offset = "+meta.offset());

			});
		}
		
		
	}
	
}
