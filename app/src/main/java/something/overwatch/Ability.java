package something.overwatch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

//A compound view for each hero ability
public class Ability extends TableLayout {
    private TextView name;
    private TextView description;
    private ImageView icon;
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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        name = (TextView)this.findViewById(R.id.ability_name);
        description = (TextView)this.findViewById(R.id.ability_description);
        icon = (ImageView)this.findViewById(R.id.ability_icon);
    }

    //set name of the ability
    public void setName(String name){
        TextView abilityName = (TextView)this.findViewById(R.id.ability_name);
        abilityName.setText(name);
    }

    //set the default key of the ability (1, 2, LMB, RMB, Shift, E, Q)
    public void setKey(String key){
        TextView abilityKey = (TextView)this.findViewById(R.id.ability_key);
        abilityKey.setText(key);
    }

    //set description of the ability
    public void setDescription(String description){
        TextView abilityDescription = (TextView)this.findViewById(R.id.ability_description);
        abilityDescription.setText(description);
    }

    //set image view??? i'm not sure how
    public void setIcon(int resId){
        icon = (ImageView)this.findViewById(R.id.ability_icon);
        this.icon.setImageResource(resId);
    }

    //add a set of stats
    public void addStat(AbilityStat stat){
        TableRow abilityStatRow=(TableRow)this.findViewById(R.id.row_ability_stats);
        abilityStatRow.addView(stat);
        TableLayout abilityStatSection = (TableLayout)this.findViewById(R.id.tbl_ability_stats);
        abilityStatSection.setStretchAllColumns(true);
    }
}
