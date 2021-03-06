package com.mobile.countme.implementation.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.mobile.countme.R;
import com.mobile.countme.framework.AppMenu;
import com.mobile.countme.implementation.AndroidFileIO;
import com.mobile.countme.implementation.models.EnvironmentModel;
import com.mobile.countme.implementation.models.ErrorModel;
import com.mobile.countme.implementation.models.GPSTracker;
import com.mobile.countme.implementation.models.TripModel;
import com.mobile.countme.implementation.models.StatisticsModel;
import com.mobile.countme.implementation.models.UserModel;
import com.mobile.countme.implementation.views.BikingActive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.UUID;

import static com.google.android.gms.internal.zzhu.runOnUiThread;

/**
 * Created by Robin on 27.09.2015.
 * This is the main controller in the application.
 */
public class MainController {

    private AndroidFileIO fileIO;
    private AppMenu context;

    //Format for timestamp
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-M-yyyy");
    private Calendar calendar = new GregorianCalendar();

    //Used in the result menu
    private boolean errorClicked;
    //Current time
    private long time;

    //Errors reported during trip
    private Map<String, ErrorModel> tripErrors;

    //Timer during trip with a Timertask that will update the bikingactive view
    private Timer timer;
    private TimerTask timerTask;
    private String lastTime;

    //Biking active fields
    private BikingActive bikingActive;
    //Set to true when starting tracker
    private boolean start_using_tracker;
    private boolean tripInitialized;
    //The count of the errors in one trip
    private int errorCount = 1;

    /**
     * The models of the MVC structure.
     */
    private EnvironmentModel environmentModel;
    private StatisticsModel statisticsModel;
    private TripModel tripModel;
    private ErrorModel errorModel;
    public UserModel userModel;
    private GPSTracker tracker;

    public MainController(AndroidFileIO io, AppMenu context) {
        this.fileIO = io;
        this.context = context;

        //Instantiate the models and load the internal statistics
        environmentModel = new EnvironmentModel();
        statisticsModel = new StatisticsModel();
        tripModel = new TripModel();
        userModel = new UserModel();
        tracker = new GPSTracker(context);

        tripErrors = new TreeMap<>();


    }

    /**
     * Loads the environmental statistics of the user from phones internal storage
     */
    public void loadEnvironmentalStatistics() {
        JSONArray environmentStatistics = fileIO.readStatisticsSaveFile();
        Log.w("MainController", "loadEnvironmentalStatistics: " + environmentStatistics);
        try {
            JSONObject todaysTrips = environmentStatistics.getJSONObject(environmentStatistics.length() - 1);
            if (todaysTrips.getString("TimeStamp").equals(simpleDateFormat.format(calendar.getTime()))) {
                environmentModel.setCo2_savedToday(Integer.parseInt(todaysTrips.getString("co2Saved")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns a JSONObject that contains the summed values of the specified period.
     *
     * @param numberOfDays
     * @return
     */
    public JSONObject getLastPeriodTrips(int numberOfDays) {
        JSONArray allDaysTrips = fileIO.readStatisticsSaveFile();
        calendar.add(Calendar.DAY_OF_MONTH, -numberOfDays);
        JSONObject lastPeriodTrips = new JSONObject();
        try {
            lastPeriodTrips.put("co2Saved", 0);
            lastPeriodTrips.put("distance", 0);
            lastPeriodTrips.put("avgSpeed", 0);
            lastPeriodTrips.put("calories", 0);
            int numOfTrips = 0;
            for (int i = allDaysTrips.length() - 1; i >= 0; i--) {

                JSONObject dayTrips = allDaysTrips.getJSONObject(i);

                Date date = simpleDateFormat.parse((dayTrips.getString("TimeStamp")));
                if (date.after(calendar.getTime())) {
                    numOfTrips++;
                    lastPeriodTrips.put("co2Saved", Integer.toString(Integer.parseInt(lastPeriodTrips.getString("co2Saved")) + Integer.parseInt(dayTrips.getString("co2Saved"))));
                    lastPeriodTrips.put("distance", Double.toString(Double.parseDouble(lastPeriodTrips.getString("distance")) + Double.parseDouble(dayTrips.getString("distance"))));
                    lastPeriodTrips.put("avgSpeed", Double.toString(Double.parseDouble(lastPeriodTrips.getString("avgSpeed")) + Double.parseDouble(dayTrips.getString("avgSpeed"))));
                    lastPeriodTrips.put("calories", Integer.toString(Integer.parseInt(lastPeriodTrips.getString("calories")) + Integer.parseInt(dayTrips.getString("calories"))));
                } else {
                    break;
                }
            }
            if (numOfTrips > 0) {
                Log.e("MainController", "numoftrips: " + numOfTrips + "avgSpeed: " + lastPeriodTrips.getString("avgSpeed"));
                lastPeriodTrips.put("avgSpeed", Double.toString(Double.parseDouble(lastPeriodTrips.getString("avgSpeed")) / numOfTrips));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        calendar = Calendar.getInstance();
        return lastPeriodTrips;
    }

    /**
     * Loads the trips statistics of the user from phones internal storage
     */
    public void loadTripsStatistics() {
        JSONArray tripsStatistics = fileIO.readStatisticsSaveFile();
        Log.w("MainController", "loadTripsStatistics: " + tripsStatistics);
        try {
            JSONObject todaysTrips = tripsStatistics.getJSONObject(tripsStatistics.length() - 1);
            if (todaysTrips.getString("TimeStamp").equals(simpleDateFormat.format(calendar.getTime()))) {
                statisticsModel.setCo2_saved(Integer.parseInt(todaysTrips.getString("co2Saved")));
                statisticsModel.setDistance(Double.parseDouble(todaysTrips.getString("distance")));
                statisticsModel.setAvg_speed(Double.parseDouble(todaysTrips.getString("avgSpeed")));
                statisticsModel.addKcal(Integer.parseInt(todaysTrips.getString("calories")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Loads the user information from phones internal storage
     */
    public void loadUserInformation() {
        JSONObject userInformation = fileIO.readUserInformation();
        Log.w("MainController", "user information: " + userInformation);
        if(!userInformation.has("Username") || !userInformation.has("Password")) {
            Log.e("MainController", "Username: " + userInformation.has("Username"));
            try {
                boolean created = false;
                while (!created) {
                    String username = new UUID(System.currentTimeMillis(), System.nanoTime()).toString();
                    userModel.setUsername(username);
                    String password = new UUID(System.currentTimeMillis(), System.nanoTime()).toString();
                    userModel.setPassword(password);


                    created = HTTPSender.createUser(userModel);
                    if (!created) {
                        clicked = false;
                        new Thread() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                                  public void run() {
                                                      AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                      builder.setMessage(context.getString(R.string.require_network_on_startup))
                                                              .setCancelable(false)
                                                              .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                  public void onClick(DialogInterface dialog, int id) {
                                                                      synchronized (userModel) {
                                                                          MainController.clicked = true;
                                                                          userModel.notify();
                                                                      }
                                                                  }
                                                              });
                                                      AlertDialog alert = builder.create();
                                                      alert.show();
                                                  }
                                              }
                                );
                            }
                        }.start();

                        synchronized (userModel) {
                            try {
                                if (!clicked) {
                                    Log.d("Wait for click", "no connection, user has not clicked button, waiting");
                                    userModel.wait();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


                userInformation.put("Username", userModel.getUsername());
                userInformation.put("Password", userModel.getPassword());
            } catch (
                    JSONException e
                    )

            {
                e.printStackTrace();
            }

            fileIO.writeUserInformationSaveFile(userInformation);
        }

        try {
            userModel.setGender(userInformation.getString("Gender"));
            userModel.setBirthYear(Integer.parseInt(userInformation.getString("BirthDate")));
            userModel.setWeight(Float.parseFloat(userInformation.getString("Weight")));
            userModel.setReceiveSurveys(Boolean.parseBoolean(userInformation.getString("ReceiveSurveys")));
            userModel.setUsername(userInformation.getString("Username"));
            userModel.setPassword(userInformation.getString("Password"));
            while (!HTTPSender.logIn(userModel)) {
                clicked = false;


                new Thread() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                                          public void run() {
                                              AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                              builder.setMessage(context.getString(R.string.require_network_on_startup))
                                                      .setCancelable(false)
                                                      .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                          public void onClick(DialogInterface dialog, int id) {
                                                              synchronized (userModel) {
                                                                  MainController.clicked = true;
                                                                  userModel.notify();
                                                              }
                                                          }
                                                      });
                                              AlertDialog alert = builder.create();
                                              alert.show();
                                          }
                                      }
                        );
                    }
                }.start();

                synchronized (userModel) {
                    try {
                        if (!clicked) {
                            Log.d("Wait for click", "no connection, user has not clicked button, waiting");
                            userModel.wait();
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        catch (JSONException e){
            e.printStackTrace();
        }

    }

    public static boolean clicked;

    //Saves all trips statistics to internal storage
    public void saveTripsStatistics() {
        JSONObject tripsToday = new JSONObject();
        try {
            tripsToday.put("TimeStamp", simpleDateFormat.format(calendar.getTime()));
            tripsToday.put("co2Saved", statisticsModel.getCo2_saved());
            tripsToday.put("distance", statisticsModel.getDistance());
            tripsToday.put("avgSpeed", statisticsModel.getAvg_speed());
            tripsToday.put("calories", statisticsModel.getKcal());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fileIO.writeStatisticSaveFile(tripsToday);
    }

    //Saves the user information to internal storage
    public void saveUserInformationToStorage() {
        JSONObject userInfo = new JSONObject();
        try {
            userInfo.put("BirthDate", userModel.getBirthYear());
            userInfo.put("Gender", userModel.getGender());
            userInfo.put("Weight", userModel.getWeight());
            userInfo.put("ReceiveSurveys", userModel.isReceiveSurveys());
            userInfo.put("Username", userModel.getUsername());
            userInfo.put("Password", userModel.getPassword());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        fileIO.writeUserInformationSaveFile(userInfo);
    }

    /**
     * Creates a reset trip statistics
     */
    public void createTripsStatistics() {
        JSONArray trips = new JSONArray();
            JSONObject trip = new JSONObject();
            try {
                trip.put("TimeStamp", simpleDateFormat.format(calendar.getTime()));
                trip.put("co2Saved", 0);
                trip.put("distance", 0);
                trip.put("avgSpeed", 0);
                trip.put("calories", 0);
                trips.put(trip);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        fileIO.writeInitialStatisticsSaveFile(trips);
    }



    /**
     * Creates a reset user information
     */
    public void createUserInformation() {
        JSONObject userInformation = new JSONObject();
        try {
            boolean created = false;
            while (!created) {
                userInformation.put("BirthDate", 0);
                userInformation.put("Gender", 0);
                userInformation.put("Weight", 0.0);
                userInformation.put("ReceiveSurveys", false);
                String username = new UUID(System.currentTimeMillis(), System.nanoTime()).toString();
                userModel.setUsername(username);
                String password = new UUID(System.currentTimeMillis(), System.nanoTime()).toString();
                userModel.setPassword(password);


                created = HTTPSender.createUser(userModel);
                if (!created) {
                    clicked = false;
                    new Thread() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                              public void run() {
                                                  AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                  builder.setMessage(context.getString(R.string.require_network_on_startup))
                                                          .setCancelable(false)
                                                          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                              public void onClick(DialogInterface dialog, int id) {
                                                                  synchronized (userModel) {
                                                                      MainController.clicked = true;
                                                                      userModel.notify();
                                                                  }
                                                              }
                                                          });
                                                  AlertDialog alert = builder.create();
                                                  alert.show();
                                              }
                                          }
                            );
                        }
                    }.start();

                    synchronized (userModel) {
                        try {
                            if (!clicked) {
                                Log.d("Wait for click", "no connection, user has not clicked button, waiting");
                                userModel.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


            userInformation.put("Username", userModel.getUsername());
            userInformation.put("Password", userModel.getPassword());
        } catch (
                JSONException e
                )

        {
            e.printStackTrace();
        }

        fileIO.writeUserInformationSaveFile(userInformation);
    }

    /**
     * Recreates the user information if it failed to get created earlier.
     */
    public void reCreateUserInformation() {
        JSONObject userInformation = fileIO.readUserInformation();
        try {
            boolean created = false;
            while (!created) {
                String username = new UUID(System.currentTimeMillis(), System.nanoTime()).toString();
                userModel.setUsername(username);
                String password = new UUID(System.currentTimeMillis(), System.nanoTime()).toString();
                userModel.setPassword(password);


                created = HTTPSender.createUser(userModel);
                if (!created) {
                    clicked = false;
                    new Thread() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                              public void run() {
                                                  AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                  builder.setMessage(context.getString(R.string.require_network_on_startup))
                                                          .setCancelable(false)
                                                          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                              public void onClick(DialogInterface dialog, int id) {
                                                                  synchronized (userModel) {
                                                                      MainController.clicked = true;
                                                                      userModel.notify();
                                                                  }
                                                              }
                                                          });
                                                  AlertDialog alert = builder.create();
                                                  alert.show();
                                              }
                                          }
                            );
                        }
                    }.start();

                    synchronized (userModel) {
                        try {
                            if (!clicked) {
                                Log.d("Wait for click", "no connection, user has not clicked button, waiting");
                                userModel.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }


            userInformation.put("Username", userModel.getUsername());
            userInformation.put("Password", userModel.getPassword());
        } catch (
                JSONException e
                )

        {
            e.printStackTrace();
        }

        fileIO.writeUserInformationSaveFile(userInformation);

        try {
            userModel.setGender(userInformation.getString("Gender"));
            userModel.setBirthYear(Integer.parseInt(userInformation.getString("BirthDate")));
            userModel.setWeight(Float.parseFloat(userInformation.getString("Weight")));
            userModel.setReceiveSurveys(Boolean.parseBoolean(userInformation.getString("ReceiveSurveys")));
            userModel.setUsername(userInformation.getString("Username"));
            userModel.setPassword(userInformation.getString("Password"));
            while (!HTTPSender.logIn(userModel)) {
                clicked = false;


                new Thread() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                                          public void run() {
                                              AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                              builder.setMessage(context.getString(R.string.require_network_on_startup))
                                                      .setCancelable(false)
                                                      .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                          public void onClick(DialogInterface dialog, int id) {
                                                              synchronized (userModel) {
                                                                  MainController.clicked = true;
                                                                  userModel.notify();
                                                              }
                                                          }
                                                      });
                                              AlertDialog alert = builder.create();
                                              alert.show();
                                          }
                                      }
                        );
                    }
                }.start();

                synchronized (userModel) {
                    try {
                        if (!clicked) {
                            Log.d("Wait for click", "no connection, user has not clicked button, waiting");
                            userModel.wait();
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    public EnvironmentModel getEnvironmentModel() {
        return environmentModel;
    }

    public StatisticsModel getStatisticsModel() {
        return statisticsModel;
    }

    public ErrorModel getErrorModel() {
        return errorModel;
    }

    public TripModel getTripModel() {
        return tripModel;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public Map<String, ErrorModel> getTripErrors() {
        return tripErrors;
    }

    public void setBikingActive(BikingActive bikingActive) {
        this.bikingActive = bikingActive;
    }

    public AppMenu getContext() {
        return context;
    }

    public boolean isTripInitialized() {
        return tripInitialized;
    }

    public void setTripInitialized(boolean tripInitialized) {
        this.tripInitialized = tripInitialized;
    }

    /**
     * Adds a description to the error.
     *
     * @param description
     */
    public void addDescription(String description) {
        errorModel.setDescription(description);
    }

    /**
     * Adds a photo to the error
     *
     * @param photo
     */
    public void addPhoto(Bitmap photo) {
        errorModel.setPhotoTaken(photo);
    }

    /**
     * Adds an errorModel to the list of errors
     *
     * @param errorModel
     */
    public void addError(ErrorModel errorModel) {
        tripErrors.put(errorModel.toString(), errorModel);
    }

    public void setErrorModel(ErrorModel error) {
        errorModel = error;
    }

    /**
     * Resets the error list after a trip is finished, also resets the error counter
     */
    public void resetErrors() {
        tripErrors = new TreeMap<>();
        errorCount = 1;
    }

    public void setTime() {
        this.time = System.currentTimeMillis();
    }

    public void setStart_using_tracker(boolean start_using_tracker) {
        this.start_using_tracker = start_using_tracker;
    }

    public int getErrorCount() {
        return errorCount++;
    }

    public GPSTracker getTracker() {
        return tracker;
    }

    public String getLastTime() {
        return lastTime;
    }

    /**
     * Adds and calculates the new statistics after a trip is finished
     *
     * @param distance
     */
    public void addStatistics(double distance) {
        statisticsModel.addDistance(distance);
        tripModel.setDistance(distance);
        double avgSpeed = distance / getTimeUsedInSeconds() * 3.6;
        statisticsModel.calc_new_avgSpeed(avgSpeed);
        //http://www.health.harvard.edu/diet-and-weight-loss/calories-burned-in-30-minutes-of-leisure-and-routine-activities - Using the average speed from 13-19 mph as a basis for calorie calculation.
        int kcal = (int) (((userModel.getWeight() * 2.2046) / 1800) * 286.696 * (avgSpeed * 0.621371192) * (getTimeUsedInSeconds() / 1800));
        statisticsModel.addKcal(kcal);
        tripModel.setKcal(kcal);
        tripModel.setAvg_speed(avgSpeed);
        int co2 = environmentModel.addCo2_savedTrip(distance);
        statisticsModel.addCo2_saved(co2);
        tripModel.setCo2_saved(co2);
    }

    /**
     * Returns the time used in the format: HH:MM:SS
     *
     * @param time_used
     * @return
     */
    public String getTimeInFormat(Integer time_used) {
        String seconds = "";
        String minutes = "";
        String hours = "";
        long difference = System.currentTimeMillis() - time;
        Integer numSeconds = (int) (difference / 1000);
        if (numSeconds > 1) {
            start_using_tracker = true;
        }
        if (time_used > 0) {
            numSeconds = time_used;
        }
        Integer numMinutes = numSeconds / 60;
        Integer numHours = numMinutes / 60;
        numSeconds = numSeconds - numMinutes * 60;
        numMinutes = numMinutes - numHours * 60;
        seconds = numSeconds.toString();
        minutes = numMinutes.toString();
        hours = numHours.toString();
        if (numSeconds < 10) {
            seconds = "0" + seconds;
        }
        if (numMinutes < 10) {
            minutes = "0" + minutes;
        }
        if (numHours < 10) {
            hours = "0" + hours;
        }
        lastTime = "" + hours + ":"  + minutes + ":"+ seconds;
        return "" + hours + ":"  + minutes + ":"+ seconds;

    }

    /**
     * Returns the time used in seconds
     *
     * @return
     */
    public double getTimeUsedInSeconds() {
        long difference = System.currentTimeMillis() - time;
        return (double) (difference / 1000);
    }

    /**
     * Initializes the timer
     */
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * Stops the timer
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Initializes the timer task after X milliseconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bikingActive != null) {
                            bikingActive.updateView(getTimeInFormat(-1), start_using_tracker);
                        }
                    }
                });

            }
        };
    }

    /**
     * Stops the GPS tracker
     */
    public void stopTracker() {
        tracker.stopUsingGPS(userModel);
    }

    public void resetTracker() {
        tracker = new GPSTracker(context);
    }

}
