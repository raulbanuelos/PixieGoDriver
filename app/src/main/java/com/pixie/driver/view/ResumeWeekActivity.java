package com.pixie.driver.view;

import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.service.autofill.Dataset;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.pixie.driver.R;

import java.util.ArrayList;

public class ResumeWeekActivity extends AppCompatActivity {

    private BarChart barChart;

    private String[]days = new String[]{"Lunes","Martes","Miercoles","Jueves","Viernes","Sabado","Domingo"};
    private int[]sales = new int[]{120,125,150,155,170,180,300};
    private int[]colors = new int[]{Color.BLUE,Color.GREEN,Color.BLACK,Color.GRAY,Color.CYAN,Color.DKGRAY,Color.MAGENTA};

    private String DriverId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_week);
        barChart = (BarChart)findViewById(R.id.barChart);

        Intent iin = getIntent();
        Bundle bundle = iin.getExtras();
        if (bundle != null){
            DriverId = (String)bundle.get("DriverId");
        }

        createChart();
    }

    private Chart getSameChart(Chart chart,String description, int textColor,int background,int animateY){
        chart.getDescription().setText(description);
        chart.getDescription().setTextColor(textColor);
        chart.getDescription().setTextSize(15);
        chart.setBackgroundColor(background);
        chart.animateY(animateY);
        legend(chart);
        return chart;
    }

    private void legend(Chart chart){
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        ArrayList<LegendEntry>entries = new ArrayList<>();
        for (int i=0; i<days.length;i++){
            LegendEntry entry = new LegendEntry();
            entry.formColor = colors[i];
            entry.label=days[i];
            entries.add(entry);
        }

        legend.setCustom(entries);
    }

    private ArrayList<BarEntry>getBarEntries(){
        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i=0; i<sales.length;i++){
            entries.add(new BarEntry(i,sales[i]));
        }

        return entries;
    }

    private void axisX(XAxis axis){
        axis.setGranularityEnabled(true);
        axis.setPosition(XAxis.XAxisPosition.BOTTOM);
        axis.setValueFormatter(new IndexAxisValueFormatter(days));
        axis.setEnabled(false);
    }

    private void axisLeft(YAxis axis){
        axis.setSpaceTop(30);
        axis.setAxisMinimum(0);
    }

    private void axisRight(YAxis axis){
        axis.setEnabled(false);
    }

    public void createChart(){
        barChart =(BarChart)getSameChart(barChart,"Series",Color.BLACK,Color.WHITE,3000);
        barChart.setDrawGridBackground(true);
        barChart.setDrawBarShadow(false);
        barChart.setData(getBarData());
        barChart.invalidate();
        axisX(barChart.getXAxis());
        axisLeft(barChart.getAxisLeft());
        axisRight(barChart.getAxisRight());

    }

    private DataSet getData(DataSet dataSet){
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10);
        return  dataSet;
    }

    private BarData getBarData(){
        BarDataSet barDataSet = (BarDataSet)getData(new BarDataSet(getBarEntries(),""));

        barDataSet.setBarShadowColor(Color.TRANSPARENT);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.45f);
        return  barData;
    }
}
