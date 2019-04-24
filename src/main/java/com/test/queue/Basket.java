package com.test.queue;

import java.util.concurrent.LinkedBlockingQueue;

public class Basket {
	// 容纳3个苹果的篮子
	LinkedBlockingQueue<String> basket = new LinkedBlockingQueue<String>(3);

	// 生产苹果，放入篮子
	public void produce() throws InterruptedException {
		basket.put("一个苹果" + basket.size());
	}

	// 消费苹果，从篮子中取走
	public String consume() throws InterruptedException {
		// take方法取出一个苹果，若basket为空，等到basket有苹果为止(获取并移除此队列的头部)
		return basket.take();
	}

	// 定义苹果生产者
	class Producer implements Runnable {
		private String instance;
		private Basket basket;

		public Producer(String instance, Basket basket) {
			this.instance = instance;
			this.basket = basket;
		}

		public void run() {
			try {
				while (true) {
					// 生产苹果
					System.out.println(instance + "生产苹果");
					basket.produce();

					// 休眠300ms
					Thread.sleep(300);
				}
			} catch (InterruptedException ex) {
				System.out.println("Producer Interrupted");
			}
		}
	}

	// 定义苹果消费者
	class Consumer implements Runnable {
		private String instance;
		private Basket basket;

		public Consumer(String instance, Basket basket) {
			this.instance = instance;
			this.basket = basket;
		}

		public void run() {
			try {
				while (true) {
					// 消费苹果
					System.out.println(instance + "消费苹果" + basket.consume());
					// 休眠1000ms
					Thread.sleep(150);
				}
			} catch (InterruptedException ex) {
				System.out.println("Consumer Interrupted");
			}
		}
	}
}
