package something.overwatch;

import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MapsAdapter extends RecyclerView.Adapter<MapsAdapter.MyViewHolder> implements Filterable{
    private Map[] _list;
    private ArrayList<Integer> _indices;
    private ArrayList<Integer> _indicesFiltered;
    private RecyclerItemClickListener listener;
    private final Fragment fragment;
    private final String PACKAGE_NAME;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name;
        public ImageView mapType;

        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            name = (TextView) v.findViewById(R.id.lbl_map_name);
            mapType = (ImageView) v.findViewById(R.id.pic_map_type_card);
        }

        @Override
        public void onClick(View v) {
            int realPos = _indicesFiltered.get(getAdapterPosition());
            if (listener != null) listener.onItemClick(v, realPos);
        }
    }

    public MapsAdapter(Map[] list, Fragment f, String pName, RecyclerItemClickListener listener){
        this._list = list;
        this.fragment = f;
        this.PACKAGE_NAME = pName;
        this.listener = listener;
        // Hacky solution to filtered-onclick problem
        this._indices = new ArrayList<>();
        for(int i=0; i<list.length; i++) _indices.add(i);
        this._indicesFiltered = _indices;
    }

    @NonNull
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mapCardView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_map, parent, false);

        return new MyViewHolder(mapCardView);
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        final int realPosition = _indicesFiltered.get(holder.getAdapterPosition());
        holder.name.setText(_list[realPosition].getName());
        String type = _list[realPosition].getType().getValue();
        int resId = fragment.getResources().getIdentifier("maptype_" + type, "drawable", PACKAGE_NAME);
        // 2CP maps are removed in OW2
        if (type.equals("assault")) {
            Glide.with(fragment).load(resId).into(holder.mapType);
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // Aside from 2cp/assault, the rest of the map-type icons are updated on official site.
            holder.mapType.setImageResource(resId);
            holder.name.setPaintFlags(holder.name.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String qString = charSequence.toString();
                if(qString.isEmpty()){
                    _indicesFiltered = _indices;
                } else {
                    qString = qString.toLowerCase();
                    ArrayList<Integer> indices = new ArrayList<>();
                    for(int i=0; i<_list.length; i++)
                        if (_list[i].getName().toLowerCase().contains(qString) || _list[i].getType().toString().toLowerCase().contains(qString))
                            indices.add(_indices.get(i));
                    _indicesFiltered = indices;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = _indicesFiltered;
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                _indicesFiltered = (ArrayList<Integer>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public int getItemCount() {
        return _indicesFiltered.size();
    }
}
