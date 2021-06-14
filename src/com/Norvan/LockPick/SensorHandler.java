package com.Norvan.LockPick;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/4/12
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class SensorHandler {
    Context context;
    int lastTiltReading = -1;
    float lastAngularVelocity = -1;
    SensorHandlerInterface sensorHandlerInterface;
    long lastTimestampGyro = 0;
    long lastTimestampAccel = 0;
    SensorManager sensorManager;
    boolean gyroExists;

    int initialSideFacingUp = 0;
    private static final int LEFT_FACING_UP = -1;
    private static final int RIGHT_FACING_UP = 1;

    public SensorHandler(Context context, SensorHandlerInterface sensorHandlerInterface) {
        this.context = context;
        this.sensorHandlerInterface = sensorHandlerInterface;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        PackageManager paM = context.getPackageManager();
        gyroExists = paM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);


    }

    public void startPolling() {
        if (gyroExists) {
            Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            Sensor sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(sensorEventListenerWithGyro, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(sensorEventListenerWithGyro, sensorGyroscope, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            sensorManager.registerListener(sensorEventListenerNoGyro, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stopPolling() {
        try {
            sensorManager.unregisterListener(sensorEventListenerNoGyro);
        } catch (Exception e) {

        }
        try {
            sensorManager.unregisterListener(sensorEventListenerWithGyro);
        } catch (Exception e) {

        }
    }

    SensorEventListener sensorEventListenerWithGyro = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (this) {
                long timestamp = sensorEvent.timestamp;

                if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    if (timestamp - lastTimestampAccel > 50000000) {

                        boolean isFacingDown = (Math.abs(sensorEvent.values[1]) > 90);
                        float phoneRightSideHeading = sensorEvent.values[2] + 90;
                        if (isFacingDown) {
                            phoneRightSideHeading = 360 - phoneRightSideHeading;
                        }
                        lastTiltReading = (int) ((phoneRightSideHeading * 1000) / 360);

                        if (initialSideFacingUp == 0) {
                            if (lastTiltReading > 350 && lastTiltReading < 650) {
                                initialSideFacingUp = RIGHT_FACING_UP;
                            } else if (lastTiltReading > 850 || lastTiltReading < 150) {
                                initialSideFacingUp = LEFT_FACING_UP;
                            }
                        }
                        if (initialSideFacingUp == LEFT_FACING_UP) {
                            if (lastTiltReading > 500) {
                                lastTiltReading = lastTiltReading - 500;
                            }   else {
                                lastTiltReading = lastTiltReading + 500;
                            }
                        }
                        lastTimestampAccel = timestamp;
                    }
                } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if (timestamp - lastTimestampGyro > 50000000) {
                        if (initialSideFacingUp != 0) {
                            lastAngularVelocity = Math.abs(sensorEvent.values[1]);
                            sensorHandlerInterface.newValues(lastAngularVelocity, lastTiltReading);
                        }   else{
                            sensorHandlerInterface.notOnSide();
                        }
                        lastTimestampGyro = timestamp;
                    }


                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    SensorEventListener sensorEventListenerNoGyro = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (this) {
                long timestamp = sensorEvent.timestamp;

                if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    if (timestamp - lastTimestampAccel > 50000000) {
                        int currentTiltReading;
                        boolean isFacingDown = (Math.abs(sensorEvent.values[1]) > 90);
                        float phoneRightSideHeading = sensorEvent.values[2] + 90;
                        if (isFacingDown) {
                            phoneRightSideHeading = 360 - phoneRightSideHeading;
                        }
                        currentTiltReading = (int) ((phoneRightSideHeading * 1000) / 360);
                        if (initialSideFacingUp == 0) {
                            if (currentTiltReading > 350 && currentTiltReading < 650) {
                                initialSideFacingUp = RIGHT_FACING_UP;
                            } else if (currentTiltReading > 850 || currentTiltReading < 150) {
                                initialSideFacingUp = LEFT_FACING_UP;
                            }
                        }
                        if (initialSideFacingUp == LEFT_FACING_UP) {
                            if (currentTiltReading > 500) {
                                currentTiltReading = currentTiltReading - 500;
                            }   else {
                                currentTiltReading = currentTiltReading + 500;
                            }
                        }
                        int delta = Math.abs(lastTiltReading - currentTiltReading);
                        lastTiltReading = currentTiltReading;
                        lastTimestampAccel = timestamp;
                        if (initialSideFacingUp != 0) {
                        sensorHandlerInterface.newValues(delta, currentTiltReading);
                        }else{
                            sensorHandlerInterface.notOnSide();
                        }
                        
                    }
                } 
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    public interface SensorHandlerInterface {
        public void newValues(float angularVelocity, int tilt);
        public void notOnSide();

    }


}
