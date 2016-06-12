package something.overwatch;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{
    private List<String> _list;
    private Context ctx;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name;
        Context ctx;

        public MyViewHolder(View v, Context ctx) {
            super(v);
            this.ctx = ctx;
            v.setOnClickListener(this);
            name = (TextView) v.findViewById(R.id.hero_name);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Intent intent = new Intent(this.ctx, HeroInfoActivity.class);
            intent.putExtra("position", position);
            this.ctx.startActivity(intent);
        }
    }

    public RecyclerViewAdapter(List<String> list, Context ctx){
        this._list = list;
        this.ctx = ctx;
    }


    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View heroNameView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hero_name, parent, false);
        return new MyViewHolder(heroNameView, ctx);
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        String item = _list.get(position);
        holder.name.setText(item);
    }

    public int getItemCount() {
        return _list.size();
    }

}
