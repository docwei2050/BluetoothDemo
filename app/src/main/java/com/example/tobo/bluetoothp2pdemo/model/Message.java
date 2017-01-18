package com.example.tobo.bluetoothp2pdemo.model;

/**
 * Created by tobo on 17/1/17.
 */

public class Message {
    //发送的message类型时1，接收的message类型是2
    private static final int send_type=1;
    private static final int receive_type=2;
    private int type;
    private String data;

    public Message(String data, int type) {
        this.data = data;
        this.type = type;
    }

    public static int getReceive_type() {
        return receive_type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public static int getSend_type() {
        return send_type;
    }
}
