package something.overwatch;

import androidx.cardview.widget.CardView;
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


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements Filterable{
    private ArrayList<String> _list;
    // Hacky solution to filtered-onclick problem. TODO: should serialize JSON into my own classes in the future
    private ArrayList<Integer> _indices;
    private ArrayList<Integer> _indicesFiltered;
    private ArrayList<String> _classes;
    private final WeakReference<Fragment> fragmentRef;
    private final String PACKAGE_NAME;
    private RecyclerItemClickListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener { //implements View.OnClickListener
        public TextView name;
        public ImageView icon;
        private ImageView heroClass;
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
            icon = (ImageView) v.findViewById(R.id.pic_hero_card);
            heroClass = (ImageView) v.findViewById(R.id.pic_hero_class_card);
            card = (CardView)v.findViewById(R.id.card_hero);
        }

        @Override
        public void onClick(View view) {
            int realPos = _indicesFiltered.get(getAdapterPosition());
            if(mListener != null) mListener.onItemClick(view, realPos);
        }
    }

    RecyclerViewAdapter(ArrayList<String> list, ArrayList<String> classes, String packageName, Fragment fragment, RecyclerItemClickListener listener){
        this._list = list;
        this._classes = classes;
        this.PACKAGE_NAME = packageName;
        fragmentRef = new WeakReference<>(fragment);
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
                    for(int i=0; i<_list.size(); i++) {
                        if (_list.get(i).toLowerCase().contains(qString) || _classes.get(i).toLowerCase().contains(qString))
                            indices.add(_indices.get(i));
                        if (_list.get(i).toLowerCase().contains("cassidy") && "mccree".contains(qString))
                            indices.add(_indices.get(i));
                    }
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
        Fragment fragment = fragmentRef.get();
        if(fragment == null) return;

        final int realPos = _indicesFiltered.get(holder.getAdapterPosition());
        String str = _list.get(realPos);
        holder.name.setText(str);
        holder.heroClass.layout(0,0,0,0);
        holder.icon.layout(0,0,0,0);
        str = str.toLowerCase().replace(".", "").replace(" ", "");
        int resId = fragment.getResources().getIdentifier("pic_" + str, "drawable", PACKAGE_NAME);
        Log.d(str, Integer.toString(resId));
        if(resId!=0){
            Glide.with(fragment).load(resId).into(holder.icon);
        } else {
            Log.d("WARNING", str + " icon cannot be found");
        }
        str = _classes.get(realPos).toLowerCase();
        resId = fragment.getResources().getIdentifier("pic_class_" + str, "mipmap", PACKAGE_NAME);

        Glide.with(fragment).load(resId).into(holder.heroClass);
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
