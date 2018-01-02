package com.github.binitabharati.kafkapoc.case2;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author binita.bharati@gmail.com
 * 
 * 
 * 
 *  
 */

public class Producer {
	
	final static Logger logger = LoggerFactory.getLogger(Producer.class);

	
	private KafkaProducer<String, String> kafkaProd;
	
	public Producer() {
		//Below are the basic 3 props that always needs to be set.
		Properties kafkaProp = new Properties();
		kafkaProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "192.168.10.12:9092,192.168.10.13:9092,192.168.10.14:9092");
		kafkaProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
	        		"org.apache.kafka.common.serialization.StringSerializer");//Even if you do not produce key based records, u need to specify this.
		kafkaProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
	                                    StringSerializer.class.getName());
		
		//use above properties to create KafkaProducer
		kafkaProd = new KafkaProducer<String, String>(kafkaProp);
		
	}
	
	private void sendMsg() throws Exception {
		//create a ProducerRecord
		long startTime1 = System.nanoTime();
		for (int i = 0 ; i < 1000000 ; i++) {		
			String msg = produceOneMbString();
			ProducerRecord pr = new ProducerRecord<String, String>("case2", Integer.toString(i), msg);
			try {
				
				/**
				 * This thread does not send the messages. Actual message sending is done by another thread.
				 * the send API returns a Future object through which user of the API can inspect the status 
				 * of the send. The actual sending thread can also throw InterruptedException, which is being
				 * caught here.
				 */
				long startTime2 = System.nanoTime();
				logger.info("Sending message with key  =  "+ i);				
				RecordMetadata metadata = (RecordMetadata)kafkaProd.send(pr).get();
				 if (metadata != null) {
					 	long tt2 = System.nanoTime() - startTime2;
	                    logger.info("sent record key = " + pr.key() +
	                    		" meta(partition = " + metadata.partition() + ", offset = " + metadata.offset() +
                             ") time = "+ TimeUnit.SECONDS.convert(tt2, TimeUnit.NANOSECONDS));//Wondering why this always prints 0 secs of time taken, whereas overall tt is a lot.
	                } 
				
				
			} catch (Exception ex) {
				logger.info("Sending message with key  =  "+ i + " caused Exception");		
				ex.printStackTrace();
				//I do not care if Exception happens. I wont retry or anything!
				//Exceptions like Serialization failure, Batch buffer full or Interrupted exception may come
				//here, if the actual sending thread got interrupted
			}
		}
		long tt1 = System.nanoTime() - startTime1;
  		logger.info("Sent message, tt =  "+ TimeUnit.SECONDS.convert(tt1, TimeUnit.NANOSECONDS));
	}
	
	private static String produceOneMbString() {
		char[] chars = new char[1024*1024]; //1 MB = 1024 KB, 1KB = 1024 bytes.
		// Optional step - unnecessary if you're happy with the array being full of \0
		Arrays.fill(chars, 'f');
		return new String(chars);
	}
	
	public static void main(String[] args) throws Exception {
		Producer prod = new Producer();
		prod.sendMsg();
	}

}
