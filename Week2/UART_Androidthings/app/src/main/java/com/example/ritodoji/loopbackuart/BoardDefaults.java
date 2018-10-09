package com.example.ritodoji.loopbackuart;

import android.os.Build;

public class BoardDefaults {
    private static final String DEVICE_RPI3 = "rpi3";

    public static String getGPIOForLedR(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM17";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForLedG(){
        switch (Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM27";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForLedB(){
        switch (Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM22";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForPwm(){
        switch (Build.DEVICE){
            case DEVICE_RPI3:
                return "PWM0";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }

    public static String getGPIOForButton(){
        switch(Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM5";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}
