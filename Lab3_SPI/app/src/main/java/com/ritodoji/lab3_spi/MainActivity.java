package com.ritodoji.lab3_spi;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.galarzaa.androidthings.Rc522;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;



/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private Rc522 mRc522;
    RfidTask mRfidTask;
    private TextView mTagDetectedView;
    private TextView mTagUidView;
    private TextView mTagResultsView;
    private Button button,button1;
    private EditText name,dob,mssv;
    private Gpio gpio,gpio1,gpio2;
    private SpiDevice spiDevice;
    private Gpio gpioReset;
    private String nam, birth, maso;

    private static final String SPI_PORT = "SPI0.0";
    private static final String PIN_RESET = "BCM25";
    private static final String LED_G = "BCM22";
    private static final String LED_R = "BCM27";
    private static final String LED_B = "BCM17";
    private static final int INTERVAL_BETWEEN_TRIGGER = 400;
    private String NAME2;

    private static final String UID_CODE = "UID: 147-44-150-29-52";
    String resultsText = "";
    boolean check = false;

    private Handler blinkled  = new Handler();
    private Handler ledsx = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.name);
        dob = findViewById(R.id.dob);
        mssv = findViewById(R.id.mssv);
        mTagDetectedView = (TextView)findViewById(R.id.tag_read);
        mTagUidView = (TextView)findViewById(R.id.tag_uid);
        mTagResultsView = (TextView) findViewById(R.id.tag_results);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    gpio2.setValue(true);
                    gpio1.setValue(false);
                    gpio.setValue(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mRfidTask = new RfidTask(mRc522);
                mRfidTask.execute();
                ((Button)v).setText(R.string.reading);

            }
        });

        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nam = String.valueOf(name.getText());
                birth = String.valueOf(dob.getText());
                if(birth.length()!=4){
                    birth ="0000";
                }
                maso = String.valueOf(mssv.getText());
                if(maso.length() == 7){
                    maso = "0"+maso;
                }else{
                    maso = "00000000";
                }
                check = true;
                Log.d("RFIDxxx","asd" + nam);
            }
        });
        PeripheralManager pioService = PeripheralManager.getInstance();
        //mRfidTask = new RfidTask(mRc522);
       // mRfidTask.execute();
        Log.d("RFIDxxx","Reading card");
        try {
            gpio = pioService.openGpio(LED_G);
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            gpio.setValue(true);

            gpio1 = pioService.openGpio(LED_B);
            gpio1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            gpio1.setValue(false);

            gpio2 = pioService.openGpio(LED_R);
            gpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            gpio2.setValue(true);

            spiDevice = pioService.openSpiDevice(SPI_PORT);
            gpioReset = pioService.openGpio(PIN_RESET);
            mRc522 = new Rc522(spiDevice, gpioReset);
            mRc522.setDebugging(true);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.e("RFIDxxx",e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            if(spiDevice != null){
                spiDevice.close();
            }
            if(gpioReset != null){
                gpioReset.close();
            } if(gpio != null){
                gpio.close();
            }
            if(gpio1 != null){
                gpio1.close();
            }
            if(gpio2 != null){
                gpio2.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class RfidTask extends AsyncTask<Object, Object, Boolean> {
        private static final String TAG = "RfidTask";
        private Rc522 rc522;

        RfidTask(Rc522 rc522){
            this.rc522 = rc522;
        }

        @Override
        protected void onPreExecute() {
            button.setEnabled(false);
            mTagResultsView.setVisibility(View.GONE);
            mTagDetectedView.setVisibility(View.GONE);
            mTagUidView.setVisibility(View.GONE);
            resultsText = "";
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            rc522.stopCrypto();
            while(true){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
                //Check if a RFID tag has been found
                if(!rc522.request()){
                    continue;
                }
                //Check for collision errors
                if(!rc522.antiCollisionDetect()){
                    continue;
                }
                byte[] uuid = rc522.getUid();
                return rc522.selectTag(uuid);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(!success){
                mTagResultsView.setText(R.string.unknown_error);
                Log.e("RFIDxxx","Loi cmnr");
                return;
            }
            byte[] sum = new byte[6];
            byte[] datax = new byte[16];

            // Try to avoid doing any non RC522 operations until you're done communicating with it.
            byte address = Rc522.getBlockAddress(2,1);
            // Mifare's card default key A and key B, the key may have been changed previously
            byte[] key = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
            // Each sector holds 16 bytes
            // Data that will be written to sector 2, block 1
            byte[] newData = {0x68,0x75,0x6e,0x67,0x0B,0x0A,0x68,0x75,0x6e,0x67,0x28,0x06,0x01,0x51,0x13,0x43};
            // In this case, Rc522.AUTH_A or Rc522.AUTH_B can be used
            try {
                //We need to authenticate the card, each sector can have a different key
                boolean result = rc522.authenticateCard(Rc522.AUTH_A, address, key);
                if (!result) {
                    mTagResultsView.setText(R.string.authetication_error);
                    Log.e("RFIDxxx","deo thay ket qua dm");
                    return;
                }
                if(check ) {
                    /////////
                    byte[] n = null;
                    n = nam.getBytes();
                    String m = bytesToHexString(n);
                    byte[] nex = hexStringToByteArray(m);
                    for ( int i = 0; i < nex.length; i++){
                        sum[i] = nex[i];
                    }
                    if(sum.length - nex.length > 0){
                        for ( int i = nex.length; i < sum.length; i++){
                            sum[i] = 0x20;
                        }
                    }
                    ////////
                    byte[] nex1 = hexStringToByteArray(birth.trim());
                    byte[] nex2 = hexStringToByteArray(maso.trim());
                    ///////
                    for(int i = 0; i <nex.length;i ++){
                        datax[i] = nex[i];
                    }
                    for(int i = 0; i <nex2.length;i ++){
                        datax[i+12] = nex2[i];
                    }
                    for(int i = 0; i <nex1.length;i ++){
                        datax[i+10] = nex1[i];
                    }
                    result = rc522.writeBlock(address, datax);
                    if (!result) {
                        mTagResultsView.setText(R.string.write_error);
                        Log.e("RFIDxxx", "ghi sai cmnr");
                        return;
                    }

                }
                check = false;
                resultsText += "Sector written successfully";
                byte[] buffer = new byte[16];
                //Since we're still using the same block, we don't need to authenticate again
                result = rc522.readBlock(address, buffer);
                Log.d("RFIDxxx","abcxyz : " + result + Arrays.toString(buffer));
                if(!result){
                    mTagResultsView.setText(R.string.read_error);
                    Log.e("RFIDxxx","doc loi roi");
                    return;
                }
                resultsText += "\nSector read successfully: "+ Rc522.dataToHexString(buffer);
               // String sub1 = resultsText.substring()
                rc522.stopCrypto();
                Log.d("RFIDxxx","ket qua ne: " + resultsText );

                String hex = resultsText.substring(54,56) +resultsText.substring(57,59) +resultsText.substring(60,62) +resultsText.substring(63,65)
                        +resultsText.substring(66,68)+resultsText.substring(69,71);
                byte[] ne = hexStringToByteArray(hex);
                NAME2 = new String(ne, StandardCharsets.UTF_8);
                String DOB =  resultsText.substring(84,86) +"/"+resultsText.substring(87,89) ;
                String MSSV = resultsText.substring(91,92) + resultsText.substring(93,95) + resultsText.substring(96,98)
                        +resultsText.substring(99,101);
                Log.d("RFIDxxx","aaaaaaaaaa: "+ "+" +NAME2.trim()+"+");
                ledsx.post(triggerRunnable);
                mTagResultsView.setText( MSSV + " " + DOB + " " + "+"+NAME2+"+" );

            }
             finally{
                button.setEnabled(true);
                button.setText(R.string.start);
                Log.d("RFIDxxx", getString(R.string.tag_uid,rc522.getUidString()));
                mTagUidView.setText(getString(R.string.tag_uid,rc522.getUidString()));
                mTagResultsView.setVisibility(View.VISIBLE);
                mTagDetectedView.setVisibility(View.VISIBLE);
                mTagUidView.setVisibility(View.VISIBLE);
                name.setText("");
                dob.setText("");
                mssv.setText("");


            }
        }
    }
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    private static final Map<Integer, String> myMap = new HashMap<>();
    static {
        myMap.put(1, "hung");
        myMap.put(2, "nhan");
        myMap.put(3, "minh");
        myMap.put(4, "ky");
    }
    private    Runnable triggerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                String tet;
                if (myMap.containsValue(NAME2.trim())){
                    tet = "dung";
                }else {
                    tet = "sai";
                }
                switch (tet){
                    case "dung":
                        gpio.setValue(false);
                        gpio1.setValue(true);
                        gpio2.setValue(true);
                        break;
                    case "sai":
                        for(int i =0; i < 5; i++){
                            gpio2.setValue(false);
                            gpio1.setValue(true);
                            gpio.setValue(true);
                             Thread.sleep(200);
                            gpio2.setValue(true);
                            gpio1.setValue(true);
                            gpio.setValue(true);
                            Thread.sleep(200);
                        }
                        //tet ="default";
                    default:
                        gpio2.setValue(true);
                        gpio1.setValue(false);
                        gpio.setValue(true);
                        break;

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
             catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    };


}

