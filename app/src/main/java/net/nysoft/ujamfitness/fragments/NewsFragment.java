package net.nysoft.ujamfitness.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nysoft.ujamfitness.R;

/**
 * TODO: Write Javadoc for NewsFragment.
 *
 * @author pngai
 */
public class NewsFragment extends Fragment {

    public NewsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_news, container, false);
        return rootView;
    }

}
