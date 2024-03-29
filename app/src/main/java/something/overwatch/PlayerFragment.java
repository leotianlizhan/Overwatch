package something.overwatch;


import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment {

    private final int PC = 0, CONSOLE = 1;
    private final String hintPC = "PC/Xbox";
    private final String hintConsole = "Xbox/PSN";
    private String region = "PC";
    private MenuItem menuItem = null;
    private ArrayList<String> favorites = null;
    private FavoritesViewAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private AppCompatImageButton searchButton;
    ImageView img;
    TextView lbl_empty, lbl_fav;

    /**
     * Memory leak fix. TODO: use view binding in Kotlin
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerAdapter = null;
        recyclerView = null;
        img = null;
        lbl_empty = null;
        lbl_fav = null;
    }

    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_player, container, false);
        recyclerView=(RecyclerView)v.findViewById(R.id.recycler_view_players);
        img = (ImageView)v.findViewById(R.id.img_player_search);
        lbl_empty = (TextView)v.findViewById(R.id.lbl_player_search);
        lbl_fav = (TextView)v.findViewById(R.id.lbl_favorites);
//        searchButton = v.findViewById(R.id.search_player_button);
        RadioGroup radioGroup = v.findViewById(R.id.radio_group_platform);
//        getRegion();
        if(region.equals("PC")) radioGroup.check(R.id.radio_button_pc);
        else radioGroup.check(R.id.radio_button_console);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.radio_button_pc:
                        region = "PC";
                        saveRegion(region);
                        break;
                    case R.id.radio_button_console:
                        region = "Console";
                        saveRegion("Console");
                        break;
                }
            }
        });

//        final FloatingSearchView searchView = (FloatingSearchView) v.findViewById(R.id.searchBar);
//        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
//            @Override
//            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
//            }
//
//            @Override
//            public void onSearchAction(String currentQuery) {
//                Intent intent = new Intent(getContext(), InfoPlayerActivity.class);
//                intent.putExtra("query", currentQuery);
//                intent.putExtra("region", region);
//                intent.putExtra("favoriteslist", favorites);
//                getContext().startActivity(intent);
//            }
//        });
//        getRegion(searchView);
//        final CharSequence[] regions = {"PC","Console"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
//        builder.setTitle("Select Platform");
//        builder.setItems(regions, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if(which == PC)
//                    searchView.setSearchHint(hintPC);
//                else
//                    searchView.setSearchHint(hintConsole);
//                region=regions[which].toString();
//                saveRegion(region);
//            }
//        });
//        final AlertDialog selectRegions = builder.create();
//        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
//            @Override
//            public void onActionMenuItemSelected(MenuItem item) {
//                selectRegions.show();
//            }
//        });
        return v;
    }

    private void saveRegion(String region){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("region", region);
        editor.commit();
    }
    private void getRegion(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String raw = sharedPref.getString("region", "-1");
        if(!raw.equals("-1")) {
            if(raw.equals("PC"))
                region = "PC";
            else
                region = "Console";
        } else
            region = "PC";
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = getActivity().findViewById(R.id.search_player);
        // TODO: fix X button and icon color to black
        searchView.setQueryHint("Search players");
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getActivity(), InfoPlayerActivity.class)));
        // Make searchView into a button
        EditText editText = searchView.findViewById(R.id.search_src_text);
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), InfoPlayerActivity.class);
            intent.setAction(Intent.ACTION_SEARCH);
            intent.putExtra(SearchManager.QUERY, "");
            intent.putExtra("region", region);
            intent.putStringArrayListExtra("favoriteslist", favorites);
            startActivity(intent);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getActivity(), InfoPlayerActivity.class);
                intent.setAction(Intent.ACTION_SEARCH);
                intent.putExtra(SearchManager.QUERY, query);
                intent.putExtra("region", region);
                intent.putStringArrayListExtra("favoriteslist", favorites);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
//        searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                searchView.setQuery(searchView.getQuery(), true);
//            }
//        });
        super.onActivityCreated(savedInstanceState);
    }



    private void getFavorites(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String raw = sharedPref.getString("favorites", "-1;-1");
        // Clear incompatible old format
        if (raw.contains(";")) {
            raw = "";
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("favorites", raw);
            editor.commit();
        }
        if(raw.equals("")||raw.equals("-1;-1"))
            favorites = null;
        else
            favorites = new ArrayList<String>(Arrays.asList(raw.split(",")));
        checkEmpty();
    }
    private void checkEmpty(){
        if (favorites == null) {
            recyclerView.setVisibility(View.GONE);
            img.setVisibility(View.VISIBLE);
            lbl_empty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            img.setVisibility(View.GONE);
            lbl_empty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        getFavorites();
        if (favorites!=null) {
            recyclerAdapter = new FavoritesViewAdapter(favorites, getActivity());
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(recyclerAdapter);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentActivity act = getActivity();
        if(act != null){
            FirebaseAnalytics mFirebase = FirebaseAnalytics.getInstance(act);
            mFirebase.setCurrentScreen(act, this.getClass().getSimpleName(), this.getClass().getSimpleName());
        }
    }
}
