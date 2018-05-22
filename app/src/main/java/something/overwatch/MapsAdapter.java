package something.overwatch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsAdapter extends RecyclerView.Adapter<MapsAdapter.MyViewHolder> implements Filterable{
    private ArrayList<String> _list;
    private ArrayList<Integer> _indices;
    private ArrayList<Integer> _indicesFiltered;
    private ArrayList<String> _classes;
    private Context ctx;
    private final String PACKAGE_NAME;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name;
        public SimpleDraweeView mapType;
        Context ctx;

        public MyViewHolder(View v, Context ctx) {
            super(v);
            this.ctx = ctx;
            v.setOnClickListener(this);
            name = (TextView) v.findViewById(R.id.lbl_map_name);
            mapType = (SimpleDraweeView) v.findViewById(R.id.pic_map_type_card);
        }

        @Override
        public void onClick(View v) {
            int realPos = _indicesFiltered.get(getAdapterPosition());
//            Intent intent = new Intent(this.ctx, MapInfoActivity.class);
//            intent.putExtra("position", position);
//            this.ctx.startActivity(intent);
            String str = MainActivity.mapNames.get(realPos).toLowerCase().replace(" ", "").replace(":", "").replace("'", "");
            int resId = ctx.getResources().getIdentifier("map_" + str, "drawable", PACKAGE_NAME);
            List<String> list = Arrays.asList("res:///" + Integer.toString(resId));
            new ImageViewer.Builder<>(ctx, list)
                    .setStartPosition(0)
                    .show();
        }
    }

    public MapsAdapter(ArrayList<String> list, ArrayList<String> classes, Context ctx, String pName){
        this._list = list;
        this._classes = classes;
        this.ctx = ctx;
        this.PACKAGE_NAME = pName;
        // Hacky solution to filtered-onclick problem
        this._indices = new ArrayList<>();
        for(int i=0; i<list.size(); i++) _indices.add(i);
        this._indicesFiltered = _indices;
    }

    @NonNull
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mapCardView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_map, parent, false);

        return new MyViewHolder(mapCardView, ctx);
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        final int realPosition = _indicesFiltered.get(holder.getAdapterPosition());
        String str = _list.get(realPosition);
        holder.name.setText(str);
        str = _classes.get(realPosition).toLowerCase();
        //holder.mapType.setImageResource(ctx.getResources().getIdentifier("ic_" + str, "drawable", MainActivity.PACKAGE_NAME));
        int resId = ctx.getResources().getIdentifier("ic_" + str, "drawable", PACKAGE_NAME);
        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(resId))
                .build();
        holder.mapType.setImageURI(uri);
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
                    for(int i=0; i<_list.size(); i++)
                        if (_list.get(i).toLowerCase().contains(qString) || _classes.get(i).toLowerCase().contains(qString))
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
