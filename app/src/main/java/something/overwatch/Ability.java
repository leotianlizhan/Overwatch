package something.overwatch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TableLayout;
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
        inflater.inflate(R.layout.row_ability, this);
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
        this.name.setText(name);
    }

    //set description of the ability
    public void setDescription(String description){
        this.description.setText(description);
    }

    //set image view??? i'm not sure how
    public void setIcon(int resId){
        this.icon.setImageResource(resId);
    }
}
