package something.overwatch;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MapsAdapter extends RecyclerView.Adapter<MapsAdapter.MyViewHolder>{
    private List<String> _list;
    private Context ctx;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name;
        public ImageView mapType;
        Context ctx;

        public MyViewHolder(View v, Context ctx) {
            super(v);
            this.ctx = ctx;
            v.setOnClickListener(this);
            name = (TextView) v.findViewById(R.id.lbl_map_name);
            mapType = (ImageView) v.findViewById(R.id.pic_map_type_card);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Intent intent = new Intent(this.ctx, MapInfoActivity.class);
            intent.putExtra("position", position);
            this.ctx.startActivity(intent);

        }
    }

    public MapsAdapter(List<String> list, Context ctx){
        this._list = list;
        this.ctx = ctx;
    }


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
        if(resId!=0) Picasso.with(ctx).load(resId).into(holder.mapType);
    }

    public int getItemCount() {
        return _list.size();
    }
}
