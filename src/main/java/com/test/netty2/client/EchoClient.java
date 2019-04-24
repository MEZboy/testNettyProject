package com.test.netty2.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoClient {
	private final String host;
	private final int port;

	private static Channel channel;

	public EchoClient() {
		this(0);
	}

	public EchoClient(int port) {
		this("localhost", port);
	}

	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group) // 注册线程池
					.channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
					.remoteAddress(new InetSocketAddress(this.host, this.port)) // 绑定连接端口和host信息
					.handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							System.out.println("正在连接中...");

							// 解决TCP粘包拆包的问题，以特定的字符结尾（$_）
							ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Unpooled.copiedBuffer("$_".getBytes())));

							// 字符串解码和编码
							ch.pipeline().addLast("decoder", new StringDecoder());
							ch.pipeline().addLast("encoder", new StringEncoder());

							// 心跳检测
							ch.pipeline().addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));
							ch.pipeline().addLast(new EchoClientHandler(host, port));

						}
					});
			// System.out.println("服务端连接成功..");

			ChannelFuture cf = b.connect().sync(); // 异步连接服务器
			System.out.println("服务端连接成功..."); // 连接完成

			// 断线重连
			cf.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception {
					if (!channelFuture.isSuccess()) {
						final EventLoop loop = channelFuture.channel().eventLoop();
						loop.schedule(new Runnable() {
							@Override
							public void run() {
								log.error("服务端链接不上，开始重连操作...");
								System.err.println("服务端链接不上，开始重连操作...");
								start();
							}
						}, 1L, TimeUnit.SECONDS);
					} else {
						channel = channelFuture.channel();
						log.info("服务端链接成功...");
						System.err.println("服务端链接成功...");
					}
				}
			});

			cf.channel().closeFuture().sync(); // 异步等待关闭连接channel
			System.out.println("连接已关闭.."); // 关闭完成

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// group.shutdownGracefully().sync(); // 释放线程池资源
		}
	}

	public static void main(String[] args) throws Exception {
		new EchoClient("192.168.1.233", 9000).start(); // 连接127.0.0.1/65535，并启动

	}
}