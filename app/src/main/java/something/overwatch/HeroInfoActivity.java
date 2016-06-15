package something.overwatch;


import android.app.ActionBar;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class HeroInfoActivity extends AppCompatActivity {

    private Toolbar toolbar = null;
    private TabLayout tabLayout = null;
    private ViewPager viewPager = null;
    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hero_info);
        //get position
        position = getIntent().getIntExtra("position", -1);
        TextView lblHeroName = (TextView)findViewById(R.id.lbl_hero_name);
        //set hero name
        if(position != -1)lblHeroName.setText(MainActivity.heroNames.get(position));
        else lblHeroName.setText("Error");
        //some toolbar stuff
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //set hero hp
        TextView hpTotal = (TextView)findViewById(R.id.lbl_hp_total_value);
        TextView hpNormal = (TextView)findViewById(R.id.lbl_hp_normal_value);
        TextView hpArmor = (TextView)findViewById(R.id.lbl_hp_armor_value);
        TextView hpShield = (TextView)findViewById(R.id.lbl_hp_shield_value);
        hpTotal.setText(getInfo(position,"Total HP"));
        hpNormal.setText(getInfo(position,"Normal HP"));
        hpArmor.setText(getInfo(position, "Armor HP"));
        hpShield.setText(getInfo(position, "Shield HP"));

        /****************abilities stats testing****************/
        LinearLayout abilitySection = (LinearLayout)findViewById(R.id.ability_section);
        abilitySection.setOrientation(LinearLayout.VERTICAL);
        //set stats
        AbilityStat damage = new AbilityStat(this);
        damage.setStatName("Damage");
        damage.setStatValue("420");
        AbilityStat fireRate = new AbilityStat(this);
        fireRate.setStatName("Fire Rate");
        fireRate.setStatValue("20 rps");
        AbilityStat ammo = new AbilityStat(this);
        ammo.setStatName("Ammo");
        ammo.setStatValue("20");
        //set ability
        Ability ability1 = new Ability(this);
        ability1.setName("Pulse Bomb");
        ability1.setDescription("Tracer throws a bomb");
        ability1.setKey("Key: Q");
        ability1.addStat(damage);
        ability1.addStat(fireRate);
        ability1.addStat(ammo);
        ammo = new AbilityStat(this);
        ammo.setStatName("Reload");
        ammo.setStatValue("3s");
        ability1.addStat(ammo);
        abilitySection.addView(ability1);
        //IMPORTANT
        //make unique ability, stat, etc every time. AKA don't reuse any objects, make new ones
        //if you don't, you're adding 2 things, with the same reference which crashes
        ability1 = new Ability(this);
        ability1.setName("Butt Clench");
        ability1.setKey("Key: 69");
        ability1.setDescription("Clench butt.");
        ammo = new AbilityStat(this);
        ammo.setStatName("Damage");
        ammo.setStatValue("70");
        ability1.addStat(ammo);
        ammo = new AbilityStat(this);
        ammo.setStatName("Fire Rate");
        ammo.setStatValue("10 rps");
        ability1.addStat(ammo);
        abilitySection.addView(ability1);
        ability1 = new Ability(this);
        ability1.setName("Fan The Hammer");
        ability1.setKey("Key: E");
        ability1.setDescription("Nerfed.");
        abilitySection.addView(ability1);
        /****************abilities stats testing****************/
    }
    private void handleOnBackPress()
    {
        finish();
    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OverviewFragment(), "OVERVIEW");
        viewPager.setAdapter(adapter);
    }
    private String getInfo(int position, String info)
    {
        try {
            //excel stuff
            AssetManager am = getAssets();
            InputStream is = am.open("data.xls");
            Workbook wb = Workbook.getWorkbook(is);
            Sheet s = wb.getSheet(0);
            int row = s.getRows(), col = s.getColumns();
            for (int i = 0; i < col; i++)
            {
                Cell tmp = s.getCell(i, 0);
                if(tmp.getContents().equals(info))
                {
                    String str = s.getCell(i, position+1).getContents();
                    if(str.equals(""))return "0";
                    else return str;
                }
            }
        }
        catch(Exception e)
        {}
        return "";
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
