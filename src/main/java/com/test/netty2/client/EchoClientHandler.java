package com.test.netty2.client;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

	private String host;
	private int port;
	private EchoClient echoClinet;

	public EchoClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
		echoClinet = new EchoClient(host, port);
	}

	/**
	 * 向服务端发送数据
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("通道已连接！！");
		System.out.println("客户端与服务端通道-开启：" + ctx.channel().localAddress() + "channelActive");

		String sendInfo = "Hello 这里是客户端  你好啊！$_";
		System.out.println("客户端准备发送的数据包：" + sendInfo);
		ctx.writeAndFlush(Unpooled.copiedBuffer(sendInfo, CharsetUtil.UTF_8)); // 必须有flush

	}

	/**
	 * channelInactive channel 通道 Inactive 不活跃的
	 * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("客户端与服务端通道-关闭：" + ctx.channel().localAddress() + "channelInactive");

		// 断线重连的功能，以防止在运行过程中突然断线
		final EventLoop eventLoop = ctx.channel().eventLoop();
		eventLoop.schedule(new Runnable() {
			@Override
			public void run() {
				echoClinet.start();
			}
		}, 1, TimeUnit.SECONDS);

		ctx.fireChannelInactive();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		System.out.println("读取客户端通道信息..");
		System.out.println("Server Response:" + msg.toString());
		
		//ByteBuf buf = msg.readBytes(msg.readableBytes());
		//System.out.println("客户端接收到的服务端信息:" + ByteBufUtil.hexDump(buf) + "; 数据包为:" + buf.toString(Charset.forName("gbk")));
	}

	/**
	 * 如果30s内客户端没有向服务端写入任何消息，该方法就会触发向服务端发送心跳信息，从而保持客户端与服务端的长连接
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state().equals(IdleState.READER_IDLE)) {
				System.out.println("READER_IDLE");

			} else if (event.state().equals(IdleState.WRITER_IDLE)) {
				/** 发送心跳,保持长连接 */
				String s = "ping$_";
				ctx.channel().writeAndFlush(s);
				System.out.println("心跳发送成功!");
			} else if (event.state().equals(IdleState.ALL_IDLE)) {
				System.out.println("ALL_IDLE");
			}
		}
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		System.out.println("异常退出:" + cause.getMessage());
	}
}