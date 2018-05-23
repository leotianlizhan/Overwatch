package something.overwatch;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;


import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.leakcanary.RefWatcher;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements RecyclerItemClickListener{

    private String packageName;
    private ArrayList<String> heroNames;
    private ArrayList<String> heroClasses;
    private JSONArray heroesJson;
    private RecyclerViewAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    //private static Parcelable mState;


    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = App.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_main, container, false);
//        TODO: uncomment this when finished implementing search
        setHasOptionsMenu(true);
        recyclerView = v.findViewById(R.id.hero_list_recycler_view);
        Bundle args = getArguments();
        if(args == null) Crashlytics.log("getArguments() returned null in MainFragment");
        heroNames = args.getStringArrayList("heroNames");
        heroClasses = args.getStringArrayList("heroClasses");
        packageName = args.getString("packageName");
        try {
            heroesJson = new JSONArray(args.getString("heroesJson"));
        } catch (JSONException e){
            Crashlytics.log("converting argument into JSON failed in MainFragment");
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_heroes, menu);
        // Associate searchable configuration with the SearchView
        FragmentActivity ctx = getActivity();
        if(ctx != null) {
//            SearchManager searchManager = (SearchManager) ctx.getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setQueryHint("Search heroes");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    recyclerAdapter.getFilter().filter(s);
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String s) {
                    recyclerAdapter.getFilter().filter(s);
                    return false;
                }
            });
//            if(searchManager != null) searchView.setSearchableInfo(searchManager.getSearchableInfo(ctx.getComponentName()));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    //    @Override
//    public void onStart() {
//        super.onStart();
//        if(mState!=null){
//            recyclerView.getLayoutManager().onRestoreInstanceState(mState);
//        }
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerAdapter = new RecyclerViewAdapter(heroNames, heroClasses, packageName, getActivity(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onItemClick(View v, int position) {
        Crashlytics.log(2,"MainFragment","Clicked adapter position "+Integer.toString(position));
        String data = "";
        try {
            data = heroesJson.getJSONObject(position).toString();
        } catch (JSONException e){
            Crashlytics.log(2,"MainFragment","getJSONObject(position) failed");
        }
        FragmentActivity activity = getActivity();
        if(activity == null) return;
        Intent intent = new Intent(activity, HeroInfoActivity.class);
        intent.putExtra("json", data);
        activity.startActivity(intent);
    }

    //    @Override
//    public void onSaveInstanceState(Bundle state) {
//        super.onSaveInstanceState(state);
//
//        // Save list state
//        mState = recyclerView.getLayoutManager().onSaveInstanceState();
//    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mState = recyclerView.getLayoutManager().onSaveInstanceState();
//    }


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
