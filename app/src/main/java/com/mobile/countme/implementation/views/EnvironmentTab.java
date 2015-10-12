package com.mobile.countme.implementation.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.countme.R;
import com.mobile.countme.custom_views.CustomTextView;

/**
 * Created by Kristian on 11/09/2015.
 */
public class EnvironmentTab extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.environment_activity,container,false);
        return v;
    }

    /**
     * Updates this view based on the model values sent by the controller.
     * @param co2_savedToday
     * @param co2_carDistance
     */
    public void setEnvironmentGain(int co2_savedToday, int co2_carDistance, int co2_busDistance, int co2_trainDistance, int co2_plainDistance){
        if(getView() == null) return;
        CustomTextView co2_display = (CustomTextView) getView().findViewById(R.id.co2_display);
        CustomTextView carDistance = (CustomTextView) getView().findViewById(R.id.car_km);
        CustomTextView busDistance = (CustomTextView) getView().findViewById(R.id.bus_km);
        CustomTextView trainDistance = (CustomTextView) getView().findViewById(R.id.train_km);
        CustomTextView plainDistance = (CustomTextView) getView().findViewById(R.id.plain_km);
        co2_display.setText(Integer.toString(co2_savedToday) + " g");
        carDistance.setText(Integer.toString(co2_carDistance) + " km med bil");
        busDistance.setText(Integer.toString(co2_busDistance) + " km med buss");
        trainDistance.setText(Integer.toString(co2_trainDistance) + " km med tog");
        plainDistance.setText(Integer.toString(co2_plainDistance) + " km med fly");
    }

}