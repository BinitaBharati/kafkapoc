package com.github.binitabharati.kafkapoc.case5;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class Consumer {
	
	final static Logger logger = LoggerFactory.getLogger(Consumer.class);
	
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.10.12:9092,192.168.10.13:9092,192.168.10.14:9092");
		logger.info("Subscribing to group named - "+args[1]);
		props.put("group.id", args[1]);//Not mandatory, but assume its mandatory, since we want to write a group consumer.
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		logger.info("Subscribing to topic named - "+args[0]);
		consumer.subscribe(Collections.singletonList(args[0]));
		
		try {
			  while (true) { 
			      ConsumerRecords<String, String> records = consumer.poll(100); 
			      for (ConsumerRecord<String, String> record : records) 
			      {
			    	  logger.info("topic = " + record.topic() + ", partition = "+ record.partition()
			    	  			+ ", offset = "+record.offset() + ", key="+record.key() + "-END-KEY"); 
			      }
			  }
			} finally {
			  consumer.close(); 
			}
	}

}
