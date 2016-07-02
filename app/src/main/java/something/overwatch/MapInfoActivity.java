package something.overwatch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MapInfoActivity extends AppCompatActivity {

    private int position;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_info);
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        position = getIntent().getIntExtra("position", -1);
        String str = MainActivity.mapNames.get(position).toLowerCase().replace(" ", "").replace(":", "").replace("'", "");
        TouchImageView img = (TouchImageView)findViewById(R.id.img_map);
        int resId = getResources().getIdentifier("map_" + str, "drawable", MainActivity.PACKAGE_NAME);
        img.setImageResource(resId);
    }
}
