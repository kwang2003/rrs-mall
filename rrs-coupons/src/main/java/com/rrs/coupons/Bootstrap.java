package com.rrs.coupons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;

public class Bootstrap {
	private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

	public static void main(String[] args) throws Exception {
		final ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(
				"spring/coupons-dubbo-provider.xml",
				"spring/coupons-dubbo-consumer.xml");
		ac.start();
		log.info("rrs coupons service started successfully");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				log.debug("Shutdown hook was invoked. Shutting down Shop Service.");
				ac.close();
			}
		});
		// prevent main thread from exit
		CountDownLatch countDownLatch = new CountDownLatch(1);
		countDownLatch.await();
	}
}
