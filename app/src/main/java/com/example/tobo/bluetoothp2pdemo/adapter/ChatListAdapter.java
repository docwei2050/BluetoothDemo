package com.example.tobo.bluetoothp2pdemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tobo.bluetoothp2pdemo.model.Message;
import com.example.tobo.bluetoothp2pdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tobo on 17/1/17.
 */

public class ChatListAdapter extends BaseAdapter {
    private List<Message> mMessageList;
    private Context mContext;

    public ChatListAdapter(Context context) {
        mMessageList = new ArrayList<>();
        //aaaaaaaa
        mContext = context;
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message=mMessageList.get(position);
        ViewHolder holder;
        if(convertView==null){
            convertView=View.inflate(mContext, R.layout.item_chatlist,null);
            holder=new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }
        if(message.getType()==1){
            holder.ra_send.setVisibility(View.VISIBLE);
            holder.ll_receive.setVisibility(View.GONE);
            holder.tv_send.setText(message.getData());
        }else if(message.getType()==2){
            holder.ll_receive.setVisibility(View.VISIBLE);
            holder.ra_send.setVisibility(View.GONE);
            holder.tv_receive.setText(message.getData());
        }

        return convertView;
    }
    public void add(Message msg){
        mMessageList.add(msg);
        notifyDataSetChanged();
    }
    public void clear(){
        mMessageList.clear();
        notifyDataSetChanged();
    }
    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
   static class ViewHolder{
        TextView tv_send,tv_receive;
        LinearLayout ll_receive;
        RelativeLayout ra_send;
        public ViewHolder(View view){
            tv_send= (TextView) view.findViewById(R.id.tv_send);
            tv_receive= (TextView) view.findViewById(R.id.tv_receive);
            ll_receive= ( LinearLayout) view.findViewById(R.id.receive);
            ra_send= (RelativeLayout) view.findViewById(R.id.send);
            System.out.println("FFFFFFFFFFFFFFFFF");
            System.out.println("EEEEEEEEEEEEEEEEEEEEE");
            System.out.println("ttttttttttttttttttttt");
            System.out.println("ggggggggggggg");

            
        }
    }
}
