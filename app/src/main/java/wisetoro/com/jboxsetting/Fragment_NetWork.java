package wisetoro.com.jboxsetting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by guhh on 2017/9/25.
 */

public class Fragment_NetWork extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<HashMap<String,String>> wifi_data = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scroll,container,false);
//        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.setAdapter(new MyAdapter());
//        wifi_data.add(null);wifi_data.add(null);wifi_data.add(null);wifi_data.add(null);wifi_data.add(null);wifi_data.add(null);
        return view;
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(Fragment_NetWork.this.getContext()).inflate(R.layout.item_wifiinfo,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return wifi_data.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            private ImageView imageView;
            private TextView wifiName_tv;
            private TextView wifiStatus_tv;
            public MyViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.icon);
                wifiName_tv = (TextView) itemView.findViewById(R.id.wifi_name_tv);
                wifiStatus_tv = (TextView) itemView.findViewById(R.id.wifi_status_tv);
            }
        }
    }

}
