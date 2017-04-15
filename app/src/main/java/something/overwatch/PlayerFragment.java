package something.overwatch;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.PopupMenuCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment {

    private final int PC = 0, CONSOLE = 1;
    private String region = "PC";
    private MenuItem menuItem = null;
    private ArrayList<String> favorites = null;
    private FavoritesViewAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    ImageView img;
    TextView lbl_empty, lbl_fav;
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

        final FloatingSearchView searchView = (FloatingSearchView) v.findViewById(R.id.searchBar);
        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
            }

            @Override
            public void onSearchAction(String currentQuery) {
                Intent intent = new Intent(getContext(), InfoPlayerActivity.class);
                intent.putExtra("query", currentQuery);
                intent.putExtra("region", region);
                intent.putExtra("favoriteslist", favorites);
                getContext().startActivity(intent);
            }
        });
        getRegion(searchView);
        final CharSequence[] regions = {"PC","Console"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setTitle("Select Platform");
        builder.setItems(regions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == PC)
                    searchView.setSearchHint("username#1337");
                else
                    searchView.setSearchHint("username");
                region=regions[which].toString();
                saveRegion(region);
            }
        });
        final AlertDialog selectRegions = builder.create();
        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                selectRegions.show();
            }
        });
        return v;
    }

    private void saveRegion(String region){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("region", region);
        editor.commit();
    }
    private void getRegion(FloatingSearchView searchView){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String raw = sharedPref.getString("region", "-1");
        if(!raw.equals("-1")) {
            region = raw;
            if(raw.equals("PC"))
                searchView.setSearchHint("username#1337");
            else
                searchView.setSearchHint("username");
        } else
            region = "PC";
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void getFavorites(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String raw = sharedPref.getString("favorites", "-1;-1");
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
}
