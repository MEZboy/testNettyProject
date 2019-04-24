package com.test.queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.test.queue.Basket.Consumer;
import com.test.queue.Basket.Producer;

public class LinkedBlockingQueueTest1 {
	public static void main(String[] args) {
		// 建立一个装苹果的篮子
		Basket basket = new Basket();
		ExecutorService service = Executors.newCachedThreadPool();
		Producer producer = basket.new Producer("生产者001", basket);
		Producer producer2 = basket.new Producer("生产者002", basket);
		Producer producer3 = basket.new Producer("生产者003", basket);
		Producer producer4 = basket.new Producer("生产者004", basket);
		Consumer consumer = basket.new Consumer("消费者001", basket);
		
		service.submit(producer);
		service.submit(producer2);
		service.submit(producer3);
		service.submit(producer4);
		service.submit(consumer);
		// 程序运行5s后，所有任务停止
		try {
			Thread.sleep(1000 * 5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		service.shutdownNow();
	}
}
