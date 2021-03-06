package com.mobile.countme.implementation.views;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mobile.countme.R;
import com.mobile.countme.custom_classes.CustomTextView;
import com.mobile.countme.framework.AppMenu;
import com.mobile.countme.implementation.models.ErrorModel;

import java.math.BigDecimal;

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

    /**
     * Stops the trip
     * @param view
     */
    public void stopBiking(View view) {
        activeObject = this;

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.stop_biking))
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        if(!BikingActive.activeObject.isConnected(BikingActive.activeObject)){
                            Toast.makeText(getApplicationContext(),getString(R.string.require_network_stop_trip),Toast.LENGTH_SHORT).show();
                            return;
                        }
                        endTripAndReturn();

                    }
                }).create().show();
    }

    public void endTripAndReturn() {
        getMainController().stopTracker();
        getMainController().addStatistics(getMainController().getTracker().getDistance());
        getMainController().stoptimertask();
        goTo(ResultMenu.class);
    }

    /**
     * Notification to the user if their trip have been automatically stopped.
     */
    public void endTripNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), ResultMenu.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notifyPIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification)
                        .setVibrate(new long[] { 1000, 1000 })
                        .setAutoCancel(true)
                        .setContentIntent(notifyPIntent)
                        .setContentTitle(getString(R.string.stopped_automatically))
                        .setContentText(getString(R.string.stopped_automatically_long));
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    /**
     * Error reporting
     *
     * @param view
     */
    public void sendError(View view) {
        final ErrorModel newErrorModel = new ErrorModel(getMainController());
        newErrorModel.setName(getString(R.string.roadFault) + " " + getMainController().getErrorCount());
        newErrorModel.setLatitude(getMainController().getTracker().getLatitude().toString());
        newErrorModel.setLongitude(getMainController().getTracker().getLongitude().toString());
        newErrorModel.setTimeStamp(getMainController().getTracker().getLocation() != null ? System.currentTimeMillis() : -1);
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.report_error))
                .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getMainController().addError(newErrorModel);
                        Toast.makeText(getApplicationContext(), getString(R.string.error_saved), Toast.LENGTH_SHORT).show();
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
        getMainController().getTracker().checkIfNoLocationsReceived();
        /* if (getMainController().getTracker().isAutomaticallyStopped()) {
            endTripNotification();
            endTripAndReturn();
        } */
        CustomTextView time = (CustomTextView) findViewById(R.id.tracking_time);
        CustomTextView speed = (CustomTextView) findViewById(R.id.current_speed);
        CustomTextView distance = (CustomTextView) findViewById(R.id.tripDistance);
        if (time != null) {
            time.setText(time_used);
        }
        if (getMainController().getTracker() != null && start_using_tracker) {
            Double currentSpeedInKmH = new BigDecimal(getMainController().getTracker().getCurrentSpeed() * 3.6).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            speed.setText(Double.toString(currentSpeedInKmH) + " " + getString(R.string.kmph));

            Double transformedDistance = new BigDecimal((getMainController().getTracker().getDistance() / 1000)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            distance.setText(Double.toString(transformedDistance) + " km");

        }
    }

    /**
     * Checks of the user has internet access.
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
