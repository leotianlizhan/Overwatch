package something.overwatch;

import android.content.Context;
import android.net.Uri;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements Filterable{
    private ArrayList<String> _list;
    // Hacky solution to filtered-onclick problem. TODO: should serialize JSON into my own classes in the future
    private ArrayList<Integer> _indices;
    private ArrayList<Integer> _indicesFiltered;
    private ArrayList<String> _classes;
    private final WeakReference<Context> ctxRef;
    private final String PACKAGE_NAME;
    private RecyclerItemClickListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener { //implements View.OnClickListener
        public TextView name;
        public SimpleDraweeView icon;
        private SimpleDraweeView heroClass;
        private CardView card;
        private RecyclerItemClickListener mListener;

        MyViewHolder(View v, final RecyclerItemClickListener listener) {
            super(v);
            mListener = listener;
            v.setOnClickListener(this);
//            v.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = getAdapterPosition();
//                    Intent intent = new Intent(ctx, HeroInfoActivity.class);
//                    intent.putExtra("position", position);
//                    ctx.startActivity(intent);
//                }
//            });
            name = (TextView) v.findViewById(R.id.lbl_hero_name_card);
            icon = (SimpleDraweeView) v.findViewById(R.id.pic_hero_card);
            heroClass = (SimpleDraweeView) v.findViewById(R.id.pic_hero_class_card);
            card = (CardView)v.findViewById(R.id.card_hero);
        }

        @Override
        public void onClick(View view) {
            int realPos = _indicesFiltered.get(getAdapterPosition());
            if(mListener != null) mListener.onItemClick(view, realPos);
        }
    }

    RecyclerViewAdapter(ArrayList<String> list, ArrayList<String> classes, String packageName, Context ctx, RecyclerItemClickListener listener){
        this._list = list;
        this._classes = classes;
        this.PACKAGE_NAME = packageName;
        ctxRef = new WeakReference<>(ctx);
        this.listener = listener;
        // Hacky solution to filtered-onclick problem
        this._indices = new ArrayList<>();
        for(int i=0; i<list.size(); i++) _indices.add(i);
        this._indicesFiltered = _indices;
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

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View heroCardView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_hero, parent, false);
        // calls the MyViewHolder constructor above
        return new MyViewHolder(heroCardView, listener);
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        Context ctx = ctxRef.get();
        if(ctx == null) return;

        final int realPos = _indicesFiltered.get(holder.getAdapterPosition());
        String str = _list.get(realPos);
        holder.name.setText(str);
        str = str.toLowerCase().replace(".", "").replace(" ", "");
        int resId = ctx.getResources().getIdentifier("pic_" + str, "drawable", PACKAGE_NAME);
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
        str = _classes.get(realPos).toLowerCase();
        resId = ctx.getResources().getIdentifier("pic_class_" + str, "mipmap", PACKAGE_NAME);
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
        return _indicesFiltered.size();
    }
}
