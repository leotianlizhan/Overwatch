package something.overwatch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.Arrays;
import java.util.List;

public class MapsAdapter extends RecyclerView.Adapter<MapsAdapter.MyViewHolder>{
    private List<String> _list;
    private Context ctx;

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
            int position = getAdapterPosition();
//            Intent intent = new Intent(this.ctx, MapInfoActivity.class);
//            intent.putExtra("position", position);
//            this.ctx.startActivity(intent);
            String str = MainActivity.mapNames.get(position).toLowerCase().replace(" ", "").replace(":", "").replace("'", "");
            int resId = ctx.getResources().getIdentifier("map_" + str, "drawable", MainActivity.PACKAGE_NAME);
            List<String> list = Arrays.asList("res:///" + Integer.toString(resId));
            new ImageViewer.Builder<>(ctx, list)
                    .setStartPosition(0)
                    .show();
        }
    }

    public MapsAdapter(List<String> list, Context ctx){
        this._list = list;
        this.ctx = ctx;
    }

    @NonNull
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mapCardView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_map, parent, false);

        return new MyViewHolder(mapCardView, ctx);
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        final int cardPosition = holder.getAdapterPosition();
        String str = _list.get(position);
        holder.name.setText(str);
        str = MainActivity.mapTypes.get(position).toLowerCase();
        //holder.mapType.setImageResource(ctx.getResources().getIdentifier("ic_" + str, "drawable", MainActivity.PACKAGE_NAME));
        int resId = ctx.getResources().getIdentifier("ic_" + str, "drawable", MainActivity.PACKAGE_NAME);
        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(resId))
                .build();
        holder.mapType.setImageURI(uri);
    }

    public int getItemCount() {
        return _list.size();
    }
}
