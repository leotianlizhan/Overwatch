package something.overwatch;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment implements RecyclerItemClickListener{

    private MapsAdapter recyclerAdapter;
    private RecyclerView recyclerView;

    /**
     * Memory leak fix. TODO: use view binding in Kotlin
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerAdapter = null;
        recyclerView = null;
    }

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        recyclerView=(RecyclerView)v.findViewById(R.id.maps_recycler_view);
        recyclerView.setHasFixedSize(true);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerAdapter = new MapsAdapter(Constants.MAPS, this, getActivity().getPackageName(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(getActivity(), MapInfoActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_heroes, menu);
        // Associate searchable configuration with the SearchView
        FragmentActivity ctx = getActivity();
        if(ctx != null) {
            SearchManager searchManager = (SearchManager) ctx.getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setQueryHint("Search maps");
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
            if(searchManager != null) searchView.setSearchableInfo(searchManager.getSearchableInfo(ctx.getComponentName()));
        }
        super.onCreateOptionsMenu(menu, inflater);
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
