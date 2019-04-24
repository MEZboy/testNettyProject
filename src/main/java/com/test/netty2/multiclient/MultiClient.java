package com.test.netty2.multiclient;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 多连接客户端
 */
public class MultiClient {

    /**服务类*/
    private Bootstrap bootstrap = new Bootstrap();

    /**会话集合*/
    private List<Channel>  channels = new ArrayList<Channel>();

    /**引用计数*/
    private final AtomicInteger index = new AtomicInteger();

    /**初始化*/
    public void init(int count){
        //worker
        EventLoopGroup worker = new NioEventLoopGroup();

        //设置工作线程
        this.bootstrap.group(worker);

        //初始化channel
        bootstrap.channel(NioSocketChannel.class);

        //设置handler管道
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new StringDecoder());
                channel.pipeline().addLast(new StringEncoder());
                channel.pipeline().addLast(new ClientHandler());
            }
        });

        //根据连接数建立连接
        for(int i = 0;i < count;i++){
            ChannelFuture channelFuture = bootstrap.connect("192.168.1.233",9000);
            channels.add(channelFuture.channel());
        }

    }

    /**获取channel（会话）*/
    public Channel nextChannel(){
        return getFirstActiveChannel(0);
    }

    private Channel getFirstActiveChannel(int count) {
        Channel channel = channels.get(Math.abs(index.getAndIncrement() % channels.size()));
        if(!channel.isActive()){
            //重连
            reconect(channel);
            if(count > channels.size()){
                throw new RuntimeException("no Idle channel!");
            }

            return getFirstActiveChannel(count + 1);
        }
        return channel;
    }

    /**重连*/
    private void reconect(Channel channel) {
        //此处可改为原子操作
        synchronized(channel){
            if(channels.indexOf(channel) == -1){
                return ;
            }

            Channel newChannel = bootstrap.connect("192.168.1.233", 9000).channel();
            channels.set(channels.indexOf(channel), newChannel);

            System.out.println(channels.indexOf(channel) + "位置的channel成功进行重连!");
        }
    }

}