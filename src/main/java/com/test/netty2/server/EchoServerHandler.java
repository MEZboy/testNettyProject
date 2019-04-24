package com.test.netty2.server;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {
	
	/**
     * 心跳丢失次数
     */
    private int counter = 0;
 
	/*
	 * channelAction
	 *
	 * channel 通道 action 活跃的
	 *
	 * 当客户端主动链接服务端的链接后，这个通道就是活跃的了。也就是客户端与服务端建立了通信通道并且可以传输数据
	 *
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().localAddress().toString() + " 通道已激活！");
	}

	/*
	 * channelInactive
	 *
	 * channel 通道 Inactive 不活跃的
	 *
	 * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
	 *
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().localAddress().toString() + " 通道不活跃！");
		// 关闭流
	}

	/**
	 * 
	 * @author Taowd TODO 此处用来处理收到的数据中含有中文的时 出现乱码的问题 2017年8月31日 下午7:57:28
	 * @param buf
	 * @return
	 */
	private String getMessage(ByteBuf buf) {
		byte[] con = new byte[buf.readableBytes()];
		buf.readBytes(con);
		try {
			return new String(con, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 功能：读取服务器发送过来的信息
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("Client say : " + msg.toString());
        //重置心跳丢失次数
        counter = 0;
        
		// 第一种：接收字符串时的处理
		//ByteBuf buf = (ByteBuf) msg;
		//String rev = getMessage(buf);
		//System.out.println("客户端收到服务器数据:" + rev);
	}
	
	/**
	 * 如果服务端40s内没有接收到客户端发来的消息，就将丢失次数累加，如果累加超过3次也就是120s内都没有接收到客户端传来的消息，服务端将断开此客户端的连接
	 */
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)){
                // 空闲40s之后触发 (心跳包丢失)
                if (counter >= 3) {
                    // 连续丢失3个心跳包 (断开连接)
                    ctx.channel().close().sync();
                    System.out.println("已与"+ctx.channel().remoteAddress()+"断开连接");
                } else {
                    counter++;
                    System.out.println(ctx.channel().remoteAddress() + "丢失了第 " + counter + " 个心跳包");
                }
            }
 
        }
    }

	/**
	 * 功能：读取完毕客户端发送过来的数据之后的操作
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("服务端接收数据完毕..");
		// 第一种方法：写一个空的buf，并刷新写出区域。完成后关闭sock channel连接。
		//ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		// ctx.flush();
		// ctx.flush(); //
		// 第二种方法：在client端关闭channel连接，这样的话，会触发两次channelReadComplete方法。
		// ctx.flush().close().sync(); // 第三种：改成这种写法也可以，但是这中写法，没有第一种方法的好。
	}

	/**
	 * 功能：服务端发生异常的操作
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		System.out.println("异常信息：\r\n" + cause.getMessage());
	}
}