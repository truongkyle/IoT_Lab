package com.example.ritodoji.voiceregconition_pocketsphinx_ver1;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TempActivity extends Activity implements MqttCommand.MqttControl {
    private static final String topicTemp = "smarthome/temp/value";
    private MqttCommand mqttCommand;
    private TextView textView, textTime, textview2, textTime2;
    private LineChart mChart;
    private static String timeDay;
    String message;
    Handler mhanderl;
    private ExecutorService pool = Executors.newFixedThreadPool(10);
    int pay ;
    int pay2 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_temp);
        init();
        try {
            mqttCommand = new MqttCommand(this, this,topicTemp);
            Log.d("SUB","SubCribed Temp");
        } catch (MqttException e) {
            e.printStackTrace();
        }
        setupChart();
        setupAxes();
        setupData();
        setLegend();
    }
    private void init(){
        textView = findViewById(R.id.textTemp);
        textview2 = findViewById(R.id.textTemp2);
        textTime2 = findViewById(R.id.textDaytime2);
        textTime = findViewById(R.id.textDaytime);
        mChart = findViewById(R.id.linechart);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss a", Locale.US);
        timeDay = dateFormat.format(new Date());
    }
    private void setupChart() {
        // disable description text
        mChart.getDescription().setEnabled(false);
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // enable scaling
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        // set an alternative background color
        mChart.setBackgroundColor(Color.DKGRAY);
    }
    private void setupAxes() {
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(50.0f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Add a limit line
        LimitLine ll = new LimitLine(40.0f, "Hot hot !! ");
        ll.setLineWidth(2f);
        ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll.setTextSize(10f);
        ll.setTextColor(Color.WHITE);
        // Add limit line
        LimitLine l2 = new LimitLine(20.0f, "Cold cold !! ");
        l2.setLineWidth(2f);
        l2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        l2.setTextSize(10f);
        l2.setTextColor(Color.WHITE);
        // reset all limit lines to avoid overlapping lines
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll);
        leftAxis.addLimitLine(l2);
        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);
    }
    private void setupData() {
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
    }
    private void setLegend() {
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setTextColor(Color.WHITE);
    }
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, " Living room Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColors(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);
        // To show values of each point
        set.setDrawValues(true);

        return set;
    }   private LineDataSet createSet2() {
        LineDataSet set = new LineDataSet(null, "Bed room Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColors(ColorTemplate.VORDIPLOM_COLORS[1]);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(10f);
        // To show values of each point
        set.setDrawValues(true);

        return set;
    }
    private void addEntry(int a, int b) {
        LineData data = mChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set1 = data.getDataSetByIndex(1);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            if(set1 == null){
                set1 = createSet2();
                data.addDataSet(set1);
            }

            data.addEntry(new Entry(set.getEntryCount(),a), 0);
            data.addEntry(new Entry(set1.getEntryCount(),b), 1);

            // let the chart know it's data has changed
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(15);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());
        }
    }
    @Override
    public void getMessage(String payload) {
        message = payload;
       pool.execute(runnable);
        /*Log.d("mess","You got a value : " + payload);
        if(payload.trim().substring(0,10).equals("LivingRoom")) {
            Log.d("temp","temp : " + payload.trim().substring(23)+ "time: " + payload.trim().substring(11,22));
            setText(textView,payload.trim().substring(23));
            setText(textTime,payload.trim().substring(11,22));
            pay = Integer.valueOf(payload.trim().substring(23));
        }
        else if (payload.trim().substring(0,7).equals("BedRoom")){
            Log.d("temp","temp : " + payload.trim().substring(20)+ "time: " + payload.trim().substring(8,19));
            setText(textview2,payload.trim().substring(20));
            setText(textTime2,payload.trim().substring(8,19));
            pay2 = Integer.valueOf(payload.trim().substring(20));
        }
        else {
            pay = 0;
            pay2 = 0;
        }
        addEntry(pay,pay2);*/
    }

    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(message.trim().substring(0,10).equals("LivingRoom")) {
                Log.d("temp","temp : " + message.trim().substring(23)+ "time: " + message.trim().substring(11,22));
                setText(textView,message.trim().substring(23));
                setText(textTime,message.trim().substring(11,22));
                pay = Integer.valueOf(message.trim().substring(23));
            }
            else if (message.trim().substring(0,7).equals("BedRoom")){
                Log.d("temp","temp : " + message.trim().substring(20)+ "time: " + message.trim().substring(8,19));
                setText(textview2,message.trim().substring(20));
                setText(textTime2,message.trim().substring(8,19));
                pay2 = Integer.valueOf(message.trim().substring(20));
            }
            else {
                pay = 0;
                pay2 = 0;
            }
            addEntry(pay,pay2);
        }
    };
    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            mqttCommand.close();
            Log.d("unsub","unsub Temp");
            Toast.makeText(TempActivity.this,"client closed",Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
