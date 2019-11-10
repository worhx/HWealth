package com.team09.hwealth;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class MessageAdapter extends ArrayAdapter<MessageData> {

    private final Context context;
    List<MessageData> messageDataList;
    public MessageAdapter(Context context, List<MessageData> messageDataList){
        super(context, R.layout.fragment_message_listview, messageDataList);
        this.context = context;
        this.messageDataList = messageDataList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.fragment_message_listview, parent, false);
        TextView name = rowView.findViewById(R.id.textview_message_name);
        TextView message = rowView.findViewById(R.id.textview_message);
        TextView time = rowView.findViewById(R.id.textview_message_time);

        MessageData messageData;
        messageData = messageDataList.get(position);
        name.setText(messageData.getName());
        message.setText(messageData.getMessage());
        time.setText((messageData.getTime().toString()).substring(0,10));

        return rowView;
    }
}
