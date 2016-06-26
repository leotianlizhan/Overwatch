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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.List;

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
        TextView lblHeroClass = (TextView)findViewById(R.id.lbl_hero_role);
        //set hero name
        if(position != -1) {
            lblHeroName.setText(MainActivity.heroNames.get(position));
            lblHeroClass.setText(MainActivity.heroClasses.get(position));
        } else lblHeroName.setText("Error");
        //some toolbar stuff
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //set hero picture
        ImageView pic = (ImageView)findViewById(R.id.pic_hero_info);
        String heroName = MainActivity.heroNames.get(position);
        heroName = heroName.toLowerCase().replace(".", "").replace(" ", "");
        int resId = getResources().getIdentifier("pic_" + heroName, "mipmap", MainActivity.PACKAGE_NAME);
        pic.setImageResource(resId);

        //set hero hp
        TextView hpTotal = (TextView)findViewById(R.id.lbl_hp_total_value);
        TextView hpNormal = (TextView)findViewById(R.id.lbl_hp_normal_value);
        TextView hpArmor = (TextView)findViewById(R.id.lbl_hp_armor_value);
        TextView hpShield = (TextView)findViewById(R.id.lbl_hp_shield_value);
        hpTotal.setText(getHpInfo(position,"Total HP"));
        hpNormal.setText(getHpInfo(position,"Normal HP"));
        hpArmor.setText(getHpInfo(position, "Armor HP"));
        hpShield.setText(getHpInfo(position, "Shield HP"));

        /****************abilities stats****************/
        LinearLayout abilitySection = (LinearLayout)findViewById(R.id.ability_section);
        abilitySection.setOrientation(LinearLayout.VERTICAL);
        Ability[] abilities = getAbilityInfo(position);
        for(int i=0; i<abilities.length; i++) abilitySection.addView(abilities[i]);
        /****************abilities stats****************/
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
    private String getHpInfo(int position, String info)
    {
        try
        {
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
        {
        }
        return "";
    }
    private Ability[] getAbilityInfo(int position)
    {
        //read each hero stat and add to the class
        try
        {
            //excel setup
            AssetManager am = getAssets();
            InputStream is = am.open("heroes.xls");
            Workbook wb = Workbook.getWorkbook(is);
            Sheet s = wb.getSheet(0);
            int idx = 1, row = s.getRows(), col = s.getColumns();
            //skip to hero row
            for (int i = 0; i < position; i++)
            {
                String str = s.getCell(0, idx).getContents();
                int tmp = str.charAt(str.length() - 1) - '0';
                System.out.println(tmp);
                idx += tmp + 2;
            }
            //number of abilities
            String str = s.getCell(0, idx).getContents();
            int abilities = str.charAt(str.length() - 1) - '0', cur = 0;
            //return array
            Ability[] ret = new Ability[abilities];
            System.out.println("current index " + idx);
            for(int i = idx+1; i <= idx + abilities; i++)
            {
                ret[cur] = new Ability(this, s.getCell(0, i).getContents(), s.getCell(8, i).getContents(), s.getCell(11, i).getContents());
                for(int j = 1; j < 11; j++)if(j != 8)
                {
                    String tmp = s.getCell(j, i).getContents();
                    if(!tmp.equals(""))ret[cur].addStat(new AbilityStat(this, s.getCell(j, 0).getContents(), s.getCell(j, i).getContents()));
                }
                cur++;
            }
            return ret;
        }
        catch(Exception e)
        {

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
