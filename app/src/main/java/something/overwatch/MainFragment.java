package something.overwatch;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private List<String> heroNames = MainActivity.heroNames;
    private RecyclerViewAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private static Parcelable mState;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView=(RecyclerView)v.findViewById(R.id.hero_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
        if(mState!=null){
            recyclerView.getLayoutManager().onRestoreInstanceState(mState);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerAdapter = new RecyclerViewAdapter(heroNames, getActivity());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save list state
        mState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mState = recyclerView.getLayoutManager().onSaveInstanceState();
    }



}
