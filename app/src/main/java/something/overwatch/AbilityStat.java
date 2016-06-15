package something.overwatch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.TextView;

public class AbilityStat extends GridLayout{

    public AbilityStat(Context context) {
        super(context);
        initializeViews(context);
    }
    public AbilityStat(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }
    public AbilityStat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }
    private void initializeViews(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.col_ability_stats, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setStatName(String name){
        TextView statName=(TextView)this.findViewById(R.id.lbl_ability_stat_name);
        statName.setText(name);
    }
    public void setStatValue(String value){
        TextView statValue=(TextView)this.findViewById(R.id.lbl_ability_stat_value);
        statValue.setText(value);
    }
}
