package com.github.binitabharati.kafkapoc.case9;

import java.util.Collections;
import java.util.Properties;

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
	
	public Consumer(String name, String topic, String group) {
		this.name = name;
		this.topic = topic;
		this.group = group;
		
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.10.12:9092,192.168.10.13:9092,192.168.10.14:9092");
		props.put("group.id", group);//Not mandatory, but assume its mandatory, since we want to write a group consumer.
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

		consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Collections.singletonList(topic));
	}
	
	public void consume() {
		try {
			  while (true) { 
		    	  logger.info(name + " invoking poll, so that GroupCoordinator does not deem me dead!");
			      ConsumerRecords<String, String> records = consumer.poll(100); 
			      if (records.isEmpty()){
			    	  logger.info(name + " did not receive any records :(");
			      } else {
			    	  logger.info(name + " started receiving records :)");
			    	  for (ConsumerRecord<String, String> record : records) 
				      {
				    	  logger.info(name + "-topic = " + record.topic() + ", partition = "+ record.partition()
				    	  			+ ", offset = "+record.offset() + ", key="+record.key() + "-END-KEY" + ", msg="+record.value()); 
				      }
			    	  
			    	  //Do some long standing operation, so that poll() does not get invoked by this consumer thread.
			    	  try {
						Thread.sleep(30*60*1000);
						logger.info(name + " awoke from sleep! Will start polling again.");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						logger.error("Interrupted while sleep ", e);
					}
			    	  
			      }
			      
			      
			  }
			} finally {
			  consumer.close(); 
			}
	}
	
	public static void main(String[] args) {
		Consumer consm1 = new Consumer("CONSUMER-1", args[0], args[1]);
		
		Consumer consm2 = new Consumer("CONSUMER-2", args[0], args[1]);
		
		Consumer consm3 = new Consumer("CONSUMER-3", args[0], args[1]);
		
		Thread t1 = new Thread(consm1);t1.start();
		Thread t2 = new Thread(consm2);t2.start();
		Thread t3 = new Thread(consm3);t3.start();
		
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		consume();
		
	}

}
