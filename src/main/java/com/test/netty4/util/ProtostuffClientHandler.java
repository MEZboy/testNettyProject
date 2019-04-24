package com.test.netty4.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.test.netty4.model.Person;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端的handler处理器，用于向服务器端发送Person对象
 * 
 * @author mezbo
 */
@Slf4j
public class ProtostuffClientHandler extends ChannelInboundHandlerAdapter {
	/**
	 * 客户端和服务器端TCP链路建立成功后，此方法被调用
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Person person;
		for (int i = 0; i < 1; i++) {
			person = new Person();
			person.setId(i);
			person.setName("张三awefawefawefawefawefawe" + i);
			person.setCurrDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			ctx.writeAndFlush(person);
		}
	}

	/**
	 * 发生异常时调用
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("client error:", cause);
		ctx.close();
	}
}
