package com.github.binitabharati.kafkapoc.case6;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
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

public class Producer implements Runnable{
	
	final static Logger logger = LoggerFactory.getLogger(Producer.class);

	
	private KafkaProducer<String, String> kafkaProd;
	private int startIdx;
	private int endIdx;
	private String topic;
	private CountDownLatch cdl;
	
	public Producer(int startIdx, int endIdx, String topic, CountDownLatch cdl) {
		this.startIdx = startIdx;
		this.endIdx = endIdx;
		this.topic = topic;
		this.cdl = cdl;
		//Below are the basic 3 props that always needs to be set.
		Properties kafkaProp = new Properties();
		kafkaProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "192.168.10.12:9092,192.168.10.13:9092,192.168.10.14:9092");
		kafkaProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
	        		"org.apache.kafka.common.serialization.StringSerializer");//Even if you do not produce key based records, u need to specify this.
		kafkaProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
	                                    StringSerializer.class.getName());
		//kafkaProp.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                //"60000");
		
		//use above properties to create KafkaProducer
		kafkaProd = new KafkaProducer<String, String>(kafkaProp);
		
	}
	
	private void sendMsg() throws Exception {
		//create a ProducerRecord
		long startTime1 = System.nanoTime();
		for (int i = startIdx ; i < endIdx ; i++) {		
			String msg = produceOneMbString();
			ProducerRecord pr = new ProducerRecord<String, String>(topic, Integer.toString(i), msg);
			long startTime2 = -1;
			try {
				
				/**
				 * This thread does not send the messages. Actual message sending is done by another thread.
				 * the send API returns a Future object through which user of the API can inspect the status 
				 * of the send. The actual sending thread can also throw InterruptedException, which is being
				 * caught here.
				 */
				startTime2 = System.nanoTime();
				logger.info("Sending message with key="+ i + "-END-KEY");				
				RecordMetadata metadata = (RecordMetadata)kafkaProd.send(pr).get();
				 if (metadata != null) {
					 	long tt2 = System.nanoTime() - startTime2;
	                    logger.info("sent record key=" + pr.key() + "-END-KEY" +
	                    		" meta(partition = " + metadata.partition() + ", offset = " + metadata.offset() +
                             ") time = "+ TimeUnit.SECONDS.convert(tt2, TimeUnit.NANOSECONDS));//Wondering why this always prints 0 secs of time taken, whereas overall tt is a lot.
	                } 
				
				
			} catch (Exception ex) {
				long tt2 = System.nanoTime() - startTime2;
				logger.info("Sending message with key="+ i + "-END-KEY caused Exception after waiting for response for time = "+TimeUnit.SECONDS.convert(tt2, TimeUnit.NANOSECONDS));		
				logger.error("Opps", ex);
				//I do not care if Exception happens. I wont retry or anything!
				//Exceptions like Serialization failure, Batch buffer full or Interrupted exception may come
				//here, if the actual sending thread got interrupted
			}
		}
		long tt1 = System.nanoTime() - startTime1;
  		logger.info("Sent message, tt =  "+ TimeUnit.SECONDS.convert(tt1, TimeUnit.NANOSECONDS));
  		cdl.countDown();
	}
	
	private static String produceOneMbString() {
		char[] chars = new char[600000]; //this is slightly less than message.max.bytes which is equal to 1000000 bytes
		Arrays.fill(chars, 'f');
		return new String(chars);
	}
	
	
	public static void main(String[] args) throws Exception {
		int totalMsges = Integer.parseInt(args[0]);
		int splitMsgBy4 = totalMsges/4;
		Thread t1 = null, t2 = null, t3 = null, t4 = null;
		CountDownLatch cdl = new CountDownLatch(4);
		for (int i = 0 ; i < 4 ; i++){
			Producer prod1 = new Producer(0, splitMsgBy4, args[1], cdl);
			t1 = new Thread(prod1);
			Producer prod2 = new Producer(splitMsgBy4, splitMsgBy4 + splitMsgBy4, args[1], cdl);
			t2 = new Thread(prod2);
			Producer prod3 = new Producer(splitMsgBy4 + splitMsgBy4, splitMsgBy4 + splitMsgBy4 + splitMsgBy4, args[1], cdl);
			t3 = new Thread(prod3);
			Producer prod4 = new Producer(splitMsgBy4 + splitMsgBy4 + splitMsgBy4, splitMsgBy4 + splitMsgBy4 + splitMsgBy4 + splitMsgBy4, args[1], cdl);
			t4 = new Thread(prod4);
		}
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		long startTime2 = System.nanoTime();
		cdl.await();
		long tt2 = System.nanoTime() - startTime2;
		logger.info("Main thread : finished awaiting, tt =  "+ TimeUnit.SECONDS.convert(tt2, TimeUnit.NANOSECONDS));
				
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			sendMsg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

}
