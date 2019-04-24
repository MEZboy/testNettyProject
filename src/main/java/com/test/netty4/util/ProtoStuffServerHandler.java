package com.test.netty4.util;

import com.test.netty4.model.Person;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * ProtoStuffServerHandler用于将接收到的数据输出到控制台
 * 
 * @author mezbo
 */
@Slf4j
public class ProtoStuffServerHandler extends ChannelInboundHandlerAdapter {
	private int counter = 0;

	/**
	 * 接收到数据的时候调用
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Person person = (Person) msg;
		System.out.println("person.getName()):::" + person.getName() + ",time is :::" + person.getCurrDate());
		log.info("当前是第[{}]次获取到客户端发送过来的person对象[{}].", ++counter, person);
	}

	/**
	 * 当发生了异常时，次方法调用
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("error:", cause);
		ctx.close();
	}
}
