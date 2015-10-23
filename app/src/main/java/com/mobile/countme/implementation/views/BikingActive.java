package com.mobile.countme.implementation.views;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.mobile.countme.R;
import com.mobile.countme.custom_views.CustomTextView;
import com.mobile.countme.framework.AppMenu;
import com.mobile.countme.framework.MapsActivity;
import com.mobile.countme.implementation.models.ErrorModel;

import java.math.BigDecimal;

import static com.google.android.gms.internal.zzhu.runOnUiThread;

/**
 * Created by Kristian on 16/09/2015.
 */
public class BikingActive extends AppMenu {

    public static BikingActive activeObject;

    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.biking_active);
        getMainController().setTripInitialized(true);
        getMainController().setBikingActive(this);
    }

    public static boolean clicked;

    public void stopBiking(View view) {
        activeObject = this;

        new AlertDialog.Builder(this)
                .setMessage(R.string.stop_biking)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        if(!BikingActive.activeObject.isConnected(BikingActive.activeObject)){
                            return;
                        }

                        getMainController().stopTracker();
                        getMainController().addStatistics(getMainController().getTracker().getDistance());
                        getMainController().stoptimertask();
                        goTo(ResultMenu.class);

                    }
                }).create().show();
    }

    public void goToMaps(View view) {
        goTo(MapsActivity.class);
    }

    /**
     * Error reporting
     *
     * @param view
     */
    public void sendError(View view) {
        final ErrorModel newErrorModel = new ErrorModel(getMainController());
        newErrorModel.setName("Feilmelding " + getMainController().getErrorCount());
        newErrorModel.setLatitude(getMainController().getTracker().getLatitude().toString());
        newErrorModel.setLongitude(getMainController().getTracker().getLongitude().toString());
        newErrorModel.setTimeStamp(getMainController().getTracker().getLocation().getTime());
        new AlertDialog.Builder(this)
                .setMessage(R.string.report_error)
                .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Her må det sendes inn koordinater eller noe slikt, sånn at brukeren kan identifisere problemet etter turen, hvis han/hun vil legge til beskrivelse i ettertid.
                        getMainController().addError(newErrorModel);
                    }
                })
                .setPositiveButton(R.string.now, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        //Add description and/or take picture.
                        newErrorModel.setEditedWhenReported(true);
                        getMainController().addError(newErrorModel);
                        getMainController().setErrorModel(newErrorModel);
                        goTo(ErrorMenu.class);

                    }
                }).create().show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.stop_biking)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        getMainController().stopTracker();
                        getMainController().addStatistics(getMainController().getTracker().getDistance());
                        getMainController().stoptimertask();
                        goTo(ResultMenu.class);
                    }
                }).create().show();
    }


    /**
     * Updates the view of this menu with new values for the user to see real time statistics
     *
     * @param time_used
     * @param start_using_tracker
     */
    public void updateView(String time_used, boolean start_using_tracker) {
        CustomTextView time = (CustomTextView) findViewById(R.id.tracking_time);
        CustomTextView speed = (CustomTextView) findViewById(R.id.current_speed);
        CustomTextView distance = (CustomTextView) findViewById(R.id.tripDistance);
        if (time != null) {
            time.setText(time_used);
        }
        if (getMainController().getTracker() != null && start_using_tracker) {
            Double currentSpeedInKmH = new BigDecimal(getMainController().getTracker().getCurrentSpeed() * 3.6).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            speed.setText(Double.toString(currentSpeedInKmH) + " km/h");
            Double transformedDistance = new BigDecimal((getMainController().getTracker().getDistance() / 1000)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            distance.setText(Double.toString(transformedDistance) + "km");
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
