package com.example.truongkyle.lab1_3;

import android.os.Build;

public class BoardDefaults {
    private static final String DEVICE_RPI3 = "rpi3";

    public static String getLedRPin(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM17";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getLedGPin(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM27";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getLedBPin(){
        switch (Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM22";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getLedPin(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "PWM0";

            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}
