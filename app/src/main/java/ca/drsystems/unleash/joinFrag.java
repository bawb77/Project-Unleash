package ca.drsystems.unleash;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by BBaxter3160 on 1/31/2015.
 */
public class joinFrag extends Fragment {

    ViewGroup container;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.container = container;
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_join_ready, container, false);
    }

    @Override
    public void onDestroyView(){

        Log.v("#############", "ChildCount: " + container.getChildCount());
        Log.v("#############", "ChildCount: " + container.getChildAt(0).getId());
        Log.v("#############", "ChildCount: " + container.getChildAt(1).getId());
        super.onDestroyView();
    }
}
