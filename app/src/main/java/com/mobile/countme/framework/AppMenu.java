package com.mobile.countme.framework;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mobile.countme.implementation.AndroidFileIO;
import com.mobile.countme.implementation.controllers.MainController;
import com.mobile.countme.storage_and_memory.Assets;

/**
 * Created by Kristian on 11/09/2015.
 */
public abstract class AppMenu extends AppCompatActivity {

    //These values persists through the application lifetime.
    private static Assets appAssets;
    private static AndroidFileIO fileIO;
    private static MainController mainController;


    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
    }


    public void goToNoAnimation(Class javaClass) {
        Intent intent = new Intent(this, javaClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Sends the user to the specified screen.
     * @param javaClass
     */
    public void goTo(Class javaClass) {
        Intent intent = new Intent(this, javaClass);
        startActivity(intent);
        finish();
    }

    private String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }



    public static MainController getMainController() {
        return mainController;
    }

    public static AndroidFileIO getFileIO() {
        return fileIO;
    }

    public static Assets getAppAssets(){
        return appAssets;
    }

    public static void setMainController(MainController mainController) {
        AppMenu.mainController = mainController;
    }

    public static void setAppAssets(Assets assets) {
        AppMenu.appAssets = assets;
    }
    public static void setFileIO(AndroidFileIO fileIO) {
        AppMenu.fileIO = fileIO;
    }

}
