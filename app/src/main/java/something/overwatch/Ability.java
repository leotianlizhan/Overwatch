package something.overwatch;

//IMPORTANT
//make unique ability, stat, etc every time. AKA don't reuse any objects, make new ones
//if you don't, you're adding 2 things, with the same reference which crashes

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bumptech.glide.Glide;

//A compound view for each hero ability
public class Ability extends TableLayout {
    private String iconUrl = null;

    public Ability(Context context, String name, String key, String description) {
        super(context);
        if(context == null) return;
        initializeViews(context);
        this.setName(name);
        this.setKey(key);
        this.setDescription(description);
        ImageView v = (ImageView) this.findViewById(R.id.ability_icon);
        v.setVisibility(View.GONE);
    }
    public Ability(Context context, String name, String key, String description, String iconUrl) {
        super(context);
        initializeViews(context);
        this.setName(name);
        this.setKey(key);
        this.setDescription(description);
        this.iconUrl = Constants.REMOTE_URL + "images/" + iconUrl;
        ImageView v = (ImageView) this.findViewById(R.id.ability_icon);
        v.setVisibility(View.VISIBLE);
    }
    public Ability(Context context) {
        super(context);
        initializeViews(context);
    }
    public Ability(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    private void initializeViews(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.ability, this);
    }

    //set name of the ability
    public void setName(String name){
        TextView abilityName = (TextView)this.findViewById(R.id.ability_name);
        abilityName.setText(name);
    }

    //set the default key of the ability (1, 2, LMB, RMB, Shift, E, Q)
    public void setKey(String key){
        TextView abilityKey = (TextView)this.findViewById(R.id.ability_key);
        if(!key.equals("")) abilityKey.setText(key);
        else abilityKey.setText("Passive");
    }

    //set description of the ability
    public void setDescription(String description){
        TextView abilityDescription = (TextView)this.findViewById(R.id.ability_description);
        abilityDescription.setText(description);
    }

    //set ability icon
    public void setIcon(Activity activity){
        if (iconUrl != null) {
            ImageView iconView = (ImageView) this.findViewById(R.id.ability_icon);
            Glide.with(activity).load(iconUrl).into(iconView);
        }
    }

    //add a set of stats
    public void addStat(AbilityStat stat){
        TableRow abilityStatRow=(TableRow)this.findViewById(R.id.row_ability_stats);
        abilityStatRow.addView(stat);
        TableLayout abilityStatSection = (TableLayout)this.findViewById(R.id.tbl_ability_stats);
        abilityStatSection.setStretchAllColumns(true);
    }
}
