package com.team09.hwealth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class MessageListFragment extends Fragment {


    public MessageListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view =  inflater.inflate(R.layout.fragment_message_list, container, false);

        List messageDataList = new ArrayList<MessageData>();
        messageDataList.add(new MessageData("Adrian", "Hello", "10:30"));
        messageDataList.add(new MessageData("Alan", "Hello", "10:31"));
        messageDataList.add(new MessageData("Adrian", "I FAT", "10:32"));
        messageDataList.add(new MessageData("Alan", "Go GYM", "10:33"));



        ArrayAdapter adapter = new MessageAdapter(getContext(), messageDataList);
        ListView listView = view.findViewById(R.id.listview_messages);
        listView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return view;
    }

}
