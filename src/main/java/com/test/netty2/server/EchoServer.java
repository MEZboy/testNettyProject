package com.test.netty2.server;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class EchoServer {
	private final int port;

	public EchoServer(int port) {
		this.port = port;
	}

	public void start() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();

		EventLoopGroup group = new NioEventLoopGroup();
		try {
			ServerBootstrap sb = new ServerBootstrap();
			sb.option(ChannelOption.SO_BACKLOG, 1024);
			sb.group(group, bossGroup) // 绑定线程池
					.channel(NioServerSocketChannel.class) // 指定使用的channel
					.localAddress(this.port)// 绑定监听端口
					.childHandler(new ChannelInitializer<SocketChannel>() { // 绑定客户端连接时候触发操作

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							System.out.println("报告");
							System.out.println("信息：有一客户端链接到本服务端");
							System.out.println("IP:" + ch.localAddress().getHostName());
							System.out.println("Port:" + ch.localAddress().getPort());
							System.out.println("报告完毕");

							// 解决TCP粘包拆包的问题，以特定的字符结尾（$_），只收收到的消息以$_ 符号结尾是该消息才算接收完毕
							ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Unpooled.copiedBuffer("$_".getBytes())));
							
							// 字符串解码和编码
							ch.pipeline().addLast("decoder", new StringDecoder());
							ch.pipeline().addLast("encoder", new StringEncoder());
							
							// 每隔40s检测一次是否要读事件，如果超过40s你没有读事件的发生，则执行相应的操作（在handler中实现）
							ch.pipeline().addLast(new IdleStateHandler(40,0,0,TimeUnit.SECONDS));
							ch.pipeline().addLast(new EchoServerHandler()); // 客户端触发操作
						}
					});
			ChannelFuture cf = sb.bind().sync(); // 服务器异步创建绑定
			System.out.println(EchoServer.class + " 启动正在监听： " + cf.channel().localAddress());
			cf.channel().closeFuture().sync(); // 关闭服务器通道
		} finally {
			group.shutdownGracefully().sync(); // 释放线程池资源
			bossGroup.shutdownGracefully().sync();
		}
	}

	public static void main(String[] args) throws Exception {

		new EchoServer(9000).start(); // 启动
	}
}