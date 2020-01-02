package com.yusufcakmak.utils;




import com.example.ijkplayerdemo.messagetype.ExoPlayerEventMessage;

import org.greenrobot.eventbus.EventBus;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
    //设置接收数据的超时时间
    private static final int TIMEOUT = 5000;

    public UDPClient() {

    }


    public void sendUDPData(String message, int port, String ip) {
        byte[] send_message = message.getBytes();
        try {
            DatagramSocket ds = new DatagramSocket(port);
            // 获取本地的网络地址对象
            InetAddress loc = InetAddress.getByName(ip);
            // 定义用来发送数据的DatagramPacket实例
            DatagramPacket dp_send = new DatagramPacket(send_message, send_message.length, loc, port);
            // 设置接收数据时阻塞的最长时间
            ds.setSoTimeout(TIMEOUT);
            // 发送数据
            ds.send(dp_send);
            ds.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void receiveUDPData(int port ) {
        try {
            byte[] buf = new byte[80];
            // 服务端在port端口监听接收到的数据
            DatagramSocket ds = new DatagramSocket(port);
            // 接收从客户端发送过来的数据
            DatagramPacket dp_receive = new DatagramPacket(buf, 80);
            System.out.println("server is on，waiting for client to send data......");
            boolean f = true;
            while (f) {
                // 服务器端接收来自客户端的数据
                ds.receive(dp_receive);
                String str_receive = new String (dp_receive.getData());
                EventBus.getDefault().post(new ExoPlayerEventMessage(1,"rtmp://58.200.131.2:1935/livetv/hunantv"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
