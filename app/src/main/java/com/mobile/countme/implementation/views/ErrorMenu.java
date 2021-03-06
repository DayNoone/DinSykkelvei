package com.mobile.countme.implementation.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mobile.countme.R;
import com.mobile.countme.framework.AppMenu;
import com.mobile.countme.implementation.controllers.HTTPSender;
import com.mobile.countme.implementation.controllers.MainMenu;
import com.mobile.countme.implementation.models.ErrorModel;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Kristian on 16/09/2015.
 */
public class ErrorMenu extends AppMenu {

    private ImageView photoTaken;
    private String description = "";

    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.error_activity);

        photoTaken = (ImageView)findViewById(R.id.pictureTaken);
        if(getMainController().getErrorModel().getPhotoTaken() != null) {
            photoTaken.setImageBitmap(getMainController().getErrorModel().getPhotoTaken());
        }
        description = getMainController().getErrorModel().getDescription();
    }

    /**
     * When the user if finished editing an error.
     * @param view
     */
    public void finishEditing(View view){
        getMainController().getErrorModel().setEditedWhenReported(false);
        Toast.makeText(getApplicationContext(), getString(R.string.error_saved), Toast.LENGTH_SHORT).show();
        if(getMainController().getErrorModel().isCreatedInIdle()){
            Map<String,ErrorModel> errorSend = new TreeMap<String, ErrorModel>();
            errorSend.put("Feilmelding1",getMainController().getErrorModel());
            HTTPSender.sendErrors(errorSend);
            goTo(MainMenu.class);
        }
        else if(getMainController().isTripInitialized()){
            goTo(BikingActive.class);
        }else {
            goTo(ResultMenu.class);
        }
    }

    public void createDescription(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.add_description));

// Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(description);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do something with value!
                Editable editable = input.getText();
                description = editable.toString();
                getMainController().addDescription(description);
                Toast.makeText(getApplicationContext(), getString(R.string.description_saved), Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void startCamera(View view){
        if(getMainController().getErrorModel().isEditedWhenReported()) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 0);
        }else {
            Toast.makeText(getApplicationContext(),"Du kan ikke laste opp bilde i ettertid", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            Bitmap bp = (Bitmap) data.getExtras().get("data");
            getMainController().addPhoto(bp);
            photoTaken.setImageBitmap(bp);
            Toast.makeText(getApplicationContext(), getString(R.string.photo_saved), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getMainController().getErrorModel().setEditedWhenReported(false);
        Toast.makeText(getApplicationContext(),getString(R.string.error_saved),Toast.LENGTH_SHORT).show();
        if(getMainController().getErrorModel().isCreatedInIdle()){
            Map<String,ErrorModel> errorSend = new TreeMap<String, ErrorModel>();
            errorSend.put("Feilmelding1",getMainController().getErrorModel());
            HTTPSender.sendErrors(errorSend);
            goTo(MainMenu.class);
        }
        else if(getMainController().isTripInitialized()){
            goTo(BikingActive.class);
        }else {
            goTo(ResultMenu.class);
        }
    }

}
