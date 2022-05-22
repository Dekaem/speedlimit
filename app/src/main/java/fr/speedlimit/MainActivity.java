package fr.speedlimit;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Math.round;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Formatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements /*View.OnClickListener,*/ LocationListener {

    SwitchCompat switchMetric;
    TextView speedText;
    TextView title;
    //Button buttonClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        switchMetric = findViewById(R.id.switchMetric);
        speedText = findViewById(R.id.speedText);
        title = findViewById(R.id.title);
        //buttonClose = findViewById(R.id.buttonClose);
        //this.buttonClose.setOnClickListener(this);

        // Demander permission GPS
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            // Lancer le programme quand la permission est validée
            doStuff();
        }

        this.updateSpeed(null);
        switchMetric.setOnCheckedChangeListener((compoundButton, isChecked) -> MainActivity.this.updateSpeed(null));
    }


    // PiP mode

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onUserLeaveHint() {
        PictureInPictureParams pip = new PictureInPictureParams.Builder().build();
        enterPictureInPictureMode(pip);
    }
    @Override
    public void onPictureInPictureModeChanged (boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            title.setVisibility(GONE);
            //buttonClose.setVisibility(GONE);
            switchMetric.setVisibility(GONE);
        } else {
            title.setVisibility(VISIBLE);
            //buttonClose.setVisibility(VISIBLE);
            switchMetric.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(location != null) {
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @SuppressLint("MissingPermission")
    private void doStuff() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        Toast.makeText(this, "Connexion GPS...", Toast.LENGTH_SHORT).show();
    }

    private void updateSpeed(CLocation location) {
        float nCurrentSpeed = 0;

        if(location != null) {
            location.setUseMetricUnits(this.useMetricUnits());
            nCurrentSpeed = round(location.getSpeed());
        }

        String strCurrentSpeed = String.valueOf(round(nCurrentSpeed));
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");

        if(this.useMetricUnits()) {
            speedText.setText(strCurrentSpeed);
            switchMetric.setText("km/h");
        } else {
            speedText.setText(strCurrentSpeed);
            switchMetric.setText("mph");
        }
    }

    private boolean useMetricUnits() {
        return switchMetric.isChecked();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStuff();
            } else {
                finish();
            }
        }
    }

    /*
    @Override
    public void onClick(View view) {

    if (buttonClose.isPressed()) {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }
    */
}