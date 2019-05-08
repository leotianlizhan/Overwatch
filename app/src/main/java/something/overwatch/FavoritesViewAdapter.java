package something.overwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


public class FavoritesViewAdapter extends RecyclerView.Adapter<FavoritesViewAdapter.MyViewHolder>{
    private List<String> _list;
    private Context ctx;

    public class MyViewHolder extends RecyclerView.ViewHolder { //implements View.OnClickListener
        public TextView name;
        public TextView platform;
        public ImageView icon;
        public CardView card;

        public MyViewHolder(View v, final Context ctx) {
            super(v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String queries[] = _list.get(position).split(";");
                    String id = queries[0];
                    String region = queries[1];
                    Intent intent = new Intent(ctx, InfoPlayerActivity.class);
                    intent.putExtra("query", id);
                    intent.putExtra("region", region);
                    intent.putExtra("favoriteslist", new ArrayList<String>(_list));
                    ctx.startActivity(intent);
                }
            });
            v.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    showRemoveDialog(position);
                    return false;
                }
            });
            name = (TextView) v.findViewById(R.id.lbl_player_name_card);
            //icon = (ImageView) v.findViewById(R.id.pic_hero_card);
            card = (CardView)v.findViewById(R.id.card_player);
            platform = (TextView) v.findViewById(R.id.lbl_player_platform_card);
        }
    }
    //create yes-no dialog for deleting players
    private void showRemoveDialog(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.MyAlertDialogStyle);
        builder.setTitle("Remove this favorite?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                _list.remove(position);
                saveInfo(_list);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void saveInfo(List<String> list){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        String result = "";
        for(int i=0; i<list.size(); i++){
            result = result + "," + list.get(i);
        }
        if(!result.equals(""))
            result = result.substring(1);
        editor.putString("favorites", result);
        editor.commit();
        //update the recyclerview
        this.notifyDataSetChanged();
    }

    public FavoritesViewAdapter(List<String> list, Context ctx){
        this._list = list;
        this.ctx = ctx;
    }


    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View favView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_player, parent, false);
        return new MyViewHolder(favView, ctx);
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        final int cardPosition = holder.getAdapterPosition();
        String str = _list.get(position);
        String name = str.split(";")[0];
        String platform = str.split(";")[1];
        holder.name.setText(URLDecoder.decode(name));
        holder.platform.setText(platform.toUpperCase());
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

