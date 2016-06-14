package something.overwatch;


import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
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
