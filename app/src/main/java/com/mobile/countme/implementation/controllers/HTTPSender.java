package com.mobile.countme.implementation.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.util.Log;

import com.mobile.countme.framework.AppMenu;
import com.mobile.countme.framework.GPSFilter;
import com.mobile.countme.implementation.models.ErrorModel;
import com.mobile.countme.implementation.models.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static com.google.android.gms.internal.zzhu.runOnUiThread;

/**
 * Created by Torgeir on 14.10.2015.
 */
public class HTTPSender {

    private static final String SERVER_URL = "https://tf2.sintef.no:8084/smioTest/api/";

    static public LoginInfo info;
    static private UserModel userModel;
    private static AppMenu context;
    private static UUID tripID;
    private static JSONObject survey;


    public HTTPSender() {

    }


    //sendTrip method creates a json from an arraylist of locations, an arraylist of ints and a context
    //Then it uses delegation to send the json to the server via a specified url
    public static void sendTrip(ArrayList<Location> trip, ArrayList<Integer> connectionTypes, UserModel userModel, AppMenu context) {
        HTTPSender.context = context;


        Log.d("SendTrip", "SendTrip started");

        GPSFilter.filterTrip(trip, connectionTypes);
        if(trip.size() < 20){
            return;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("_userId", info.getUserID());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
            sdf.setTimeZone(TimeZone.getTimeZone("CET"));
            jsonObject.put("startTime", sdf.format(new Date(trip.get(0).getTime())));
            jsonObject.put("endTime", sdf.format(new Date(trip.get(trip.size() - 1).getTime())));
            if(!"".equals(userModel.getGender())) {
                jsonObject.put("gender", userModel.getGender());
            }
            if( userModel.getBirthYear() != 0) {
                jsonObject.put("age", userModel.getAge());
            }
            tripID = UUID.randomUUID();
            Log.e("huuheuhe", tripID.toString());
            jsonObject.put("tripID", tripID.toString());

            JSONArray tripData = new JSONArray();
            JSONObject dataPoint;
            Location location;
            for (int i = 0; i < trip.size(); i++) {
                location = trip.get(i);
                dataPoint = new JSONObject();


                dataPoint.put("lat", location.getLatitude());
                dataPoint.put("lon", location.getLongitude());
                dataPoint.put("time", sdf.format(new Date(location.getTime())));
                dataPoint.put("mode", "mobile");
                int connectionType = connectionTypes.get(i);
                switch (connectionType) {
                    case (ConnectivityManager.TYPE_BLUETOOTH):
                        dataPoint.put("mode", "bluethooth");
                        break;
                    case (ConnectivityManager.TYPE_DUMMY):
                        dataPoint.put("mode", "dummy");
                        break;
                    case (ConnectivityManager.TYPE_ETHERNET):
                        dataPoint.put("mode", "ethernet");
                        break;
                    case (ConnectivityManager.TYPE_MOBILE):
                        dataPoint.put("mode", "mobile");
                        break;
                    case (ConnectivityManager.TYPE_MOBILE_DUN):
                        dataPoint.put("mode", "mobile_dun");
                        break;
                    case (ConnectivityManager.TYPE_VPN):
                        dataPoint.put("mode", "vpn");
                        break;
                    case (ConnectivityManager.TYPE_WIFI):
                        dataPoint.put("mode", "wifi");
                        break;
                    case (ConnectivityManager.TYPE_WIMAX):
                        dataPoint.put("mode", "wimax");
                        break;
                    case (-1):
                        dataPoint.put("mode", "none");
                    default:
                        dataPoint.put("mode", "");
                        break;


                }
                dataPoint.put("altitude", trip.get(i).getAltitude());
                dataPoint.put("accuracy", trip.get(i).getAccuracy());
                dataPoint.put("altitudeAccuracy", "");
                dataPoint.put("heading", "");
                dataPoint.put("speed", trip.get(i).getSpeed());
                tripData.put(dataPoint);

            }
            jsonObject.put("tripData", tripData);
            jsonObject.put("purpose", "");
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            jsonObject.put("OS", versionName);
            Log.d("SendTrip", "JSON created successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (jsonObject != null) {
            String sendURL = SERVER_URL + "user/" + info.getUserID() + "/trips/?token=" + info.getToken();
            HttpSenderThread thread = new HttpSenderThread(jsonObject, sendURL, info, HttpPostKind.TRIP);
            thread.start();
        }
    }

    public static void getPotensialSurvey(){

        if(context.getMainController().getUserModel().isReceiveSurveys()) {
            JSONObject jsonObject = new JSONObject();
            if (jsonObject != null) {
                String sendURL = SERVER_URL + "user/" + info.getUserID() + "/trips/" + tripID + "/?token=" + info.getToken();
                HttpSenderThread thread = new HttpSenderThread(jsonObject, sendURL, info, HttpPostKind.SURVEY);
                thread.start();
            }
        }
    }

    //sendanswer method creates a jsonObject
    //Then it uses delegation to send the jsonObject to the server via a specified url
    public static void sendAnswer(String answer, String qid) {
        Log.d("SendAnswer", "SendAnswer started");

        JSONObject jsonanswer = null;
        try {
            jsonanswer = new JSONObject();
            jsonanswer.put("_userId", info.getUserID());
            synchronized (jsonanswer){
                jsonanswer.put("answer", answer);
                if (jsonanswer != null) {
                    String sendURL = SERVER_URL + "user/" + info.getUserID() + "/answers/" + qid + "/?token=" + info.getToken();
                    HttpSenderThread thread = new HttpSenderThread(jsonanswer, sendURL, info, HttpPostKind.ANSWER);
                    thread.start();
                    synchronized (thread) {
                        try {
                            thread.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            Log.d("SendErrors", "JSON created successfully");
        }catch(JSONException e){
            e.printStackTrace();
        }


    }


    //sendErrors method creates jsonObjects from an hashmap of trip errors
    //Then it uses delegation to send the jsonObejcts to the server via a specified url
    public static void sendErrors(Map<String, ErrorModel> tripErrors) {
        Log.d("SendErrors", "SendErrors started");

        JSONObject error = null;
        try {
            error = new JSONObject();
            error.put("_userId", info.getUserID());
            ErrorModel errorModel;
            for (String errorStr : tripErrors.keySet()) {
                errorModel = tripErrors.get(errorStr);
                synchronized (error) {
                    error.put("description", errorModel.getDescription());
                    error.put("lat", errorModel.getLatitude());
                    error.put("lon", errorModel.getLongitude());
                    error.put("image", errorModel.getPhotoTakenInBase64());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("CET"));
                    String timeStamp = sdf.format(new Date(errorModel.getTimeStamp()));
                    error.put("timestamp", timeStamp);

                    if (error != null) {
                        String sendURL = SERVER_URL + "user/" + info.getUserID() + "/errors/?token=" + info.getToken();
                        HttpSenderThread thread = new HttpSenderThread(error, sendURL, info, HttpPostKind.ERROR);
                        thread.start();
                        synchronized (thread) {
                            try {
                                thread.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


            }

                Log.d("SendErrors", "JSON created successfully");
            }catch(JSONException e){
                e.printStackTrace();
            }


        }

    /**
     * Logs the user into the server
     * @param model
     * @return
     */
    public static boolean logIn(UserModel model) {
        userModel = model;
        if (info == null) {
            info = new LoginInfo();
            info.setUsername(model.getUsername());
            info.setPassword(model.getPassword());
        }
        if (info.isLoggedIn()) {
            return true;
        }

        try {

            JSONObject obj = new JSONObject();
            obj.put("username", info.getUsername());
            obj.put("password", info.getPassword());
            HttpSenderThread thread = new HttpSenderThread(obj, SERVER_URL, info, HttpPostKind.LOGIN);
            thread.start();
            synchronized (thread) {
                thread.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("loginattempt done", "login returning " + info.isLoggedIn());
        return info.isLoggedIn();

    }

    /**
     * Creates the user in the database
     * @param model
     * @return
     */
    public static boolean createUser(UserModel model) {
        if (info == null) {
            info = new LoginInfo();
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("username", model.getUsername());
            obj.put("password", model.getPassword());
            info.setPassword(model.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (obj != null) {
            String sendURL = SERVER_URL + "user/";
            HttpSenderThread thread = new HttpSenderThread(obj, sendURL, info, HttpPostKind.CREATEUSER);
            thread.start();
            synchronized (thread) {
                try {
                    thread.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return info.hasInfo();

    }


    public static void setSurvey(JSONObject survey) {
        HTTPSender.survey = survey;
    }

    public static JSONObject getSurvey() {
        return survey;
    }
}





