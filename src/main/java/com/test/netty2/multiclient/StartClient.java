package com.test.netty2.multiclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class StartClient {

    public static void main(String[] args) {
    	multiClient();
    }

    public static void multiClient(){
        MultiClient client = new MultiClient();
        client.init(3);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            try {
                System.out.println("请输入:");
                String msg = bufferedReader.readLine();
                client.nextChannel().writeAndFlush(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}