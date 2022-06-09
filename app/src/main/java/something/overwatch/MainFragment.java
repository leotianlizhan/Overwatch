package something.overwatch;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements RecyclerItemClickListener{
    private JSONArray heroesJson;
    private RecyclerViewAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    //private static Parcelable mState;


    /**
     * Memory leak fix. TODO: use view binding in Kotlin
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerAdapter = null;
        recyclerView = null;
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
        recyclerView.setHasFixedSize(true);
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
        MainActivity act = (MainActivity) getActivity();
        heroesJson = act.getHeroesJson();
        recyclerAdapter = new RecyclerViewAdapter(act.getHeroNames(), act.getHeroClasses(), act.getPackageName(), this, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onItemClick(View v, int position) {
        FirebaseCrashlytics.getInstance().log("MainFragment Clicked adapter position "+Integer.toString(position));
        String data = "";
        try {
            data = heroesJson.getJSONObject(position).toString();
        } catch (JSONException e){
            FirebaseCrashlytics.getInstance().log("MainFragment getJSONObject(position) failed");
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
