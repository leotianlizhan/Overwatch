package something.overwatch;


import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HeroInfoActivity extends AppCompatActivity {

    private Toolbar toolbar = null;
    private TabLayout tabLayout = null;
    private ViewPager viewPager = null;
    private int position = -1;
    private JSONObject hero = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hero_info);
        //some toolbar stuff
        toolbar = (Toolbar) findViewById(R.id.toolbar_hero);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //get position
        position = getIntent().getIntExtra("position", -1);
        TextView lblHeroName = (TextView)findViewById(R.id.lbl_hero_name);
        TextView lblHeroClass = (TextView)findViewById(R.id.lbl_hero_role);
        //set hero name
        if(position != -1) {
            lblHeroName.setText(MainActivity.heroNames.get(position));
            lblHeroClass.setText(MainActivity.heroClasses.get(position));
        } else lblHeroName.setText("Error");
        //set hero picture
        ImageView pic = (ImageView)findViewById(R.id.pic_hero_info);
        String heroName = MainActivity.heroNames.get(position);
        heroName = heroName.toLowerCase().replace(".", "").replace(" ", "");
        int resId = getResources().getIdentifier("pic_" + heroName, "mipmap", MainActivity.PACKAGE_NAME);
        if(resId!=0) Picasso.with(this).load(resId).into(pic);
        // pic.setImageResource(resId);

        // get hero JSON object
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String heroesList = sharedPref.getString("heroesList", "-1");
        try {
            hero = new JSONArray(heroesList).getJSONObject(position);

            //set hero hp
            TextView hpTotal = (TextView) findViewById(R.id.lbl_hp_total_value);
            TextView hpNormal = (TextView) findViewById(R.id.lbl_hp_normal_value);
            TextView hpArmor = (TextView) findViewById(R.id.lbl_hp_armor_value);
            TextView hpShield = (TextView) findViewById(R.id.lbl_hp_shield_value);
            hpTotal.setText(hero.getString("hpTotal"));
            hpNormal.setText(hero.getString("hpNormal"));
            hpArmor.setText(hero.getString("hpArmor"));
            hpShield.setText(hero.getString("hpShield"));
//            hpTotal.setText(getHpInfo(position, "Total HP"));
//            hpNormal.setText(getHpInfo(position, "Normal HP"));
//            hpArmor.setText(getHpInfo(position, "Armor HP"));
//            hpShield.setText(getHpInfo(position, "Shield HP"));
        } catch (JSONException e){
            Log.e("******JSON", e.toString());
        }

        /****************abilities stats****************/
        LinearLayout abilitySection = (LinearLayout)findViewById(R.id.ability_section);
        abilitySection.setOrientation(LinearLayout.VERTICAL);
        Ability[] abilities = getAbilityInfo();
        //if(abilities==null) Log.d("ERROR", "abilities is null. "+heroName);
        for(int i=0; i<abilities.length; i++) abilitySection.addView(abilities[i]);
        /****************abilities stats****************/
    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OverviewFragment(), "OVERVIEW");
        viewPager.setAdapter(adapter);
    }
    private Ability[] getAbilityInfo()
    {
        try {
            JSONArray abilities = hero.getJSONArray("abilities");
            int len = abilities.length();
            Ability[] res = new Ability[len];
            for(int i=0; i<len; i++){
                JSONObject ability = abilities.getJSONObject(i);
                res[i] = new Ability(this, ability.getString("name"), ability.getString("key"), ability.getString("description"));
                JSONArray statsArray = ability.getJSONArray("stats");
                for(int j=0; j<statsArray.length(); j++){
                    JSONObject stat = statsArray.getJSONObject(j);
                    res[i].addStat(new AbilityStat(this, stat.getString("name"), stat.getString("value")));
                }
            }
            return res;
        }catch (Exception e) {
            Log.e("LOAD HERO", e.getMessage());
            Toast.makeText(getApplicationContext(), "Cannot find hero saved in data", Toast.LENGTH_LONG).show();
        }
        return null;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
