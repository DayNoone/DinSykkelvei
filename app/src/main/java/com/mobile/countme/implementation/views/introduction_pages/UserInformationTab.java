package com.mobile.countme.implementation.views.introduction_pages;

/**
 * Created by Kristian on 11/09/2015.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.countme.R;

public class UserInformationTab extends Fragment  {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.user_information_tab,container,false);
        return v;
    }
}
