package com.mobile.countme.implementation.models;

import android.util.Log;

/**
 * Created by Robin on 27.09.2015.
 * This class contains all the information the EnvironmentTab will display.
 */
public class EnvironmentModel {

    private int co2_savedToday;
    private int co2_carDistance;
    private int co2_busDistance;
    private int co2_trainDistance;
    private int co2_plainDistance;
    private final int avgBicycle_co2_per_kilometer = 21;
    private final int avgCar_co2_per_kilometer = 160;
    private final int avgBus_co2_per_kilometer = 101;
    private final int avgTrain_co2_per_kilometer = 56;
    private final int avgPlain_co2_per_kilometer = 259;

    public EnvironmentModel(){
    }

    /**
     * Adds the CO2 saved during the trip to the CO2 saved today so far.
     */
    public int addCo2_savedTrip(double distance){
        co2_savedToday += (distance/1000) * avgCar_co2_per_kilometer - (distance/1000) * avgBicycle_co2_per_kilometer;
        return (int)((distance/1000) * avgCar_co2_per_kilometer - (distance/1000) * avgBicycle_co2_per_kilometer);

    }

    public int getCo2_savedToday() {
        return co2_savedToday;
    }

    /**
     * This value is derived from the co2 saved today value and is given in kilometers.
     * It uses the calculator from here: http://www.ecf.com/resources/co2-calculator/
     * @return
     */
    public int getCo2_carDistance() {
        co2_carDistance = co2_savedToday/avgCar_co2_per_kilometer;
        return co2_carDistance;
    }

    /**
     * This value is derived from the co2 saved today value and is given in kilometers.
     * It uses the calculator from here: http://www.ecf.com/resources/co2-calculator/
     * @return
     */
    public int getCo2_busDistance() {
        co2_busDistance = co2_savedToday/avgBus_co2_per_kilometer;
        return co2_busDistance;
    }

    /**
     * This value is derived from the co2 saved today value and is given in kilometers.
     * It uses the calculator from here: http://www.ecf.com/resources/co2-calculator/
     * @return
     */
    public int getCo2_trainDistance() {
        co2_trainDistance = co2_savedToday/avgTrain_co2_per_kilometer;
        return co2_trainDistance;
    }

    /**
     * This value is derived from the co2 saved today value and is given in kilometers.
     * It uses the calculator from here: http://www.ecf.com/resources/co2-calculator/
     * @return
     */
    public int getCo2_plainDistance() {
        co2_plainDistance = co2_savedToday/avgPlain_co2_per_kilometer;
        return co2_plainDistance;
    }

    public void setCo2_savedToday(int co2_savedToday) {
        this.co2_savedToday = co2_savedToday;
    }

}
