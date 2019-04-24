package com.test.netty4.server;

import com.test.netty4.util.ProtoStuffServerHandler;
import com.test.netty4.util.ProtostuffDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer {
	public static void main(String[] args) throws Exception {  
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();  
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)// 对应的是ServerSocketChannel类  
                .option(ChannelOption.SO_BACKLOG, 128)//  
                .handler(new LoggingHandler(LogLevel.TRACE))//  
                .childHandler(new ChannelInitializer<SocketChannel>() {  
                    @Override  
                    protected void initChannel(SocketChannel ch) throws Exception {
                    	// 注意解码器的顺序：
                    	// 必须先LengthFieldBasedFrameDecoder,然后ProtostuffDecoder，再ProtoStuffServerHandler
                    	
                    	// 10240表示如果此次读取的字节长度比这个大说明可能是别人伪造socket攻击，将会抛出异常，第一个4表示读取四个字节表示此次 消息的长度，后面一个4表示丢弃四个字节，然后读取业务数据。
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(10240, 0, 4, 0, 4));  
                        ch.pipeline().addLast(new ProtostuffDecoder());  
                        ch.pipeline().addLast(new ProtoStuffServerHandler());  
                    }  
                });  
        ChannelFuture future = bootstrap.bind(9000).sync();  
        log.info("server start in port:[{}]", 9000);  
        future.channel().closeFuture().sync();  
        boss.shutdownGracefully();  
        worker.shutdownGracefully();  
    }
}