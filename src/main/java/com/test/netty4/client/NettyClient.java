package com.test.netty4.client;

import com.test.netty4.util.ProtostuffClientHandler;
import com.test.netty4.util.ProtostuffEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClient {  
	  
    public static void main(String[] args) throws InterruptedException {  
        EventLoopGroup group = new NioEventLoopGroup();  
        Bootstrap bootstrap = new Bootstrap();  
        bootstrap.group(group)//  
                .channel(NioSocketChannel.class)//  
                .option(ChannelOption.TCP_NODELAY, true)//  
                .handler(new ChannelInitializer<SocketChannel>() {  
                    @Override  
                    protected void initChannel(SocketChannel ch) throws Exception {
                    	
                    	// 会在发送的数据前增加4个字节表示消息的长度
                        ch.pipeline().addLast(new LengthFieldPrepender(4));  
                        ch.pipeline().addLast(new ProtostuffEncoder());  
                        ch.pipeline().addLast(new ProtostuffClientHandler());  
                    }  
                });  
        ChannelFuture future = bootstrap.connect("192.168.1.233", 9000).sync();  
        log.info("client connect server.");  
        future.channel().closeFuture().sync();  
        group.shutdownGracefully();  
    }  
}