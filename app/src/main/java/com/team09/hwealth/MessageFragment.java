package com.team09.hwealth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MessageFragment extends Fragment {

    String[] nameArray = {"Adrian","Alan","Marcus","Jeremy",
            "Beng","Jovan","Charles"};
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_message,container,false);

        ArrayAdapter adapter = new ArrayAdapter<String>(getContext(), R.layout.fragment_listview, nameArray);

        ListView messageListView = view.findViewById(R.id.message_list);
        messageListView.setAdapter(adapter);
        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MessageListFragment()).commit();
            }
        });

        return view;
    }
}
