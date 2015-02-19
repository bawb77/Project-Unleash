package ca.drsystems.unleash;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

/**
 * Created by BBaxter3160 on 1/31/2015.
 */
public class joinFrag extends Fragment {
    ToggleButton tb;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_ready, container, false);
    }

    public void setVisible() {

    }
}
