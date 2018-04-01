package something.overwatch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{
    private List<String> _list;
    private Context ctx;

    public class MyViewHolder extends RecyclerView.ViewHolder { //implements View.OnClickListener
        public TextView name;
        public SimpleDraweeView icon;
        public SimpleDraweeView heroClass;
        public CardView card;

        public MyViewHolder(View v, final Context ctx) {
            super(v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent intent = new Intent(ctx, HeroInfoActivity.class);
                    intent.putExtra("position", position);
                    ctx.startActivity(intent);
                }
            });
            name = (TextView) v.findViewById(R.id.lbl_hero_name_card);
            icon = (SimpleDraweeView) v.findViewById(R.id.pic_hero_card);
            heroClass = (SimpleDraweeView) v.findViewById(R.id.pic_hero_class_card);
            card = (CardView)v.findViewById(R.id.card_hero);
        }
    }

    public RecyclerViewAdapter(List<String> list, Context ctx){
        this._list = list;
        this.ctx = ctx;
    }


    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View heroCardView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_hero, parent, false);
        return new MyViewHolder(heroCardView, ctx);
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        final int cardPosition = holder.getAdapterPosition();
        String str = _list.get(position);
        holder.name.setText(str);
        str = str.toLowerCase().replace(".", "").replace(" ", "");
        int resId = ctx.getResources().getIdentifier("pic_" + str, "mipmap", MainActivity.PACKAGE_NAME);
        Log.d(str, Integer.toString(resId));
        if(resId!=0){
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(resId))
                    .build();
            holder.icon.setImageURI(uri);
        } else {
            Log.d("WARNING", str + " icon cannot be found");
        }
        str = MainActivity.heroClasses.get(position).toLowerCase();
        resId = ctx.getResources().getIdentifier("pic_class_" + str, "mipmap", MainActivity.PACKAGE_NAME);
        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(resId))
                .build();
        holder.heroClass.setImageURI(uri);
//        holder.card.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ctx, HeroInfoActivity.class);
//                intent.putExtra("position", cardPosition);
//                ctx.startActivity(intent);
//            }
//        });
    }

    public int getItemCount() {
        return _list.size();
    }

}
