package no.mesan.handterminator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.PercentFormatter;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import no.mesan.handterminator.model.db.RouteStatistics;
import no.mesan.handterminator.model.db.Statistics;
import no.mesan.handterminator.model.db.User;
import no.mesan.handterminator.util.PrintUtil;

@EActivity
public class UserProfileActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener{

    private Toolbar cardToolbar;
    private String name;

    private User user;
    private Statistics userStats;

    @ViewById(R.id.pieChart_wage)
    PieChart mChart;

    @ViewById(R.id.lineChart_wage)
    LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        user = User.findById(User.class, 1L);

        name = user.getFirstname() + " " + user.getLastname();
        userStats = user.getStatistics(); //must be called before init-values


        setupGUI();
    }

    private void setupGUI() {
        cardToolbar =(Toolbar)findViewById(R.id.profile_toolbar);
        modifyToolbar(cardToolbar);

        fillViews(user);

        //Create rounded profile picture
        ImageView profilePic = (ImageView)findViewById(R.id.ic_profile);
        Bitmap img = ((BitmapDrawable)profilePic.getDrawable()).getBitmap();
        profilePic.setImageBitmap(getRoundedImage(img));

        createRouteChart();     //create the chart of route-income
        createPieChart(3, 100); //create the pie-chart that displays salary-info

    }
    

    //Onclick for clicking outside the card. Closes the card.
    public void clickBack(View view){
        onBackPressed();
    }

    public void fillViews(User user){
        //task fields
        TextView number = (TextView)findViewById(R.id.profile_phone_field);
        TextView type = (TextView)findViewById(R.id.profile_position_field);
        TextView address = (TextView)findViewById(R.id.profile_address_field);
        TextView extra = (TextView)findViewById(R.id.profile_username_field);
        TextView licence = (TextView)findViewById(R.id.profile_licence_field);
        TextView totalSalary = (TextView)findViewById(R.id.profile_total_salary_stat);
        TextView totalDriven = (TextView)findViewById(R.id.profile_total_driven_stat);
        TextView totalTime = (TextView)findViewById(R.id.profile_total_time_stat);


        number.setText("95153437");
        type.setText("Sjåfør");
        address.setText(R.string.placeholder_adress);
        extra.setText(user.getLogin());
        licence.setText("Klasse C, T, C1, D1");
        totalSalary.setText(PrintUtil.displayMoney(userStats.getMoneyEarned()));
        totalDriven.setText(PrintUtil.displayDistance((int)userStats.getMetersDriven()));
        totalTime.setText(PrintUtil.displayTime((int)userStats.getTimeSpent()));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
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

    /**
     * manually defines our toolbar's items and listener
     * @param toolbar the toolbar to be modified.
     */
    void modifyToolbar(Toolbar toolbar){
        toolbar.setTitle(name);
        toolbar.inflateMenu(R.menu.menu_toolbar_profile);

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.action_exit_fragment:
                                onBackPressed();
                                break;
                            default:break;
                        }
                        return true;
                    }
                });

    }


    private void createRouteChart(){
        //get statistics for routes from db
        List<RouteStatistics> routeStatisticsFromDb = RouteStatistics.find(RouteStatistics.class, "userstatistics = ?", String.valueOf(userStats.getId()));

        int size = routeStatisticsFromDb.size();
        if(size <= 0) return;

        lineChart.setDescription("");
        lineChart.setNoDataTextDescription("N/A");
        lineChart.setHighlightEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setBackgroundColor(getResources().getColor(R.color.superWhite));
        lineChart.setHighlightIndicatorEnabled(false);

        Legend l = lineChart.getLegend();
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);

        //x-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setSpaceBetweenLabels(1);
        xAxis.setTextSize(10f);

        //y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setDrawGridLines(false);

        //values
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();
        ArrayList<Entry> yVals3 = new ArrayList<Entry>();
        for (int i = 0; i < size; i++) {
            RouteStatistics routeStatistics = routeStatisticsFromDb.get(i);
            xVals.add(routeStatisticsFromDb.get(i).getName());
            yVals1.add(new BarEntry(routeStatistics.getTotalEarned(), i));
            yVals2.add(new BarEntry(routeStatistics.getTotalMeters()/1000, i));
            yVals3.add(new BarEntry(routeStatistics.getTotalSeconds()/60, i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals1, "Inntekt per rute(kr)");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(2f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));

        // create a dataset and give it a type
        LineDataSet set2 = new LineDataSet(yVals2, "Distanse kjørt(km)");
        set2.setColor(getResources().getColor(R.color.pieChartGreen));
        set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set2.setFillAlpha(65);
        set2.setLineWidth(2f);
        set2.setFillColor(getResources().getColor(R.color.pieChartGreen));
        set2.setHighLightColor(Color.rgb(244, 117, 117));

        // create a dataset and give it a type
        LineDataSet set3 = new LineDataSet(yVals3, "Tid brukt(min)");
        set3.setColor(getResources().getColor(R.color.pieChartYellow));
        set3.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set3.setFillAlpha(65);
        set3.setLineWidth(2f);
        set3.setFillColor(getResources().getColor(R.color.pieChartYellow));
        set3.setHighLightColor(Color.rgb(244, 117, 117));

        //combine linedata-sets
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1);
        dataSets.add(set2);
        dataSets.add(set3);

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
       lineChart.setData(data);

    }

    private void createPieChart(int count, float range) {

        //configure piechart
        mChart.setUsePercentValues(true);
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);
        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);
        mChart.setDrawCenterText(true);
        mChart.setRotationAngle(40f);
        mChart.setRotationEnabled(true); // enable rotation of the chart by touch
        mChart.setOnChartValueSelectedListener(this); // add a selection listener
        mChart.setCenterText("Lønnskilder");
        mChart.setNoDataTextDescription("N/A");
        mChart.setCenterTextSize(15f);
        mChart.setDescription("");


        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        yVals1.add(new Entry((float)userStats.getTotalDrivingReimbursed(), 0));
        yVals1.add(new Entry((float)userStats.getTotalHourlySalaray(), 1));
        yVals1.add(new Entry((float)userStats.getTotalPackageSalary(), 2));

        ArrayList<String> xVals = new ArrayList<String>();

        xVals.add("Tillegg");
        xVals.add("Timeslønn");
        xVals.add("Pakkelønn");

        PieDataSet dataSet = new PieDataSet(yVals1, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        //add colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        //color for "Drivstoff" - pos 0
        int fuelColor = getResources().getColor(R.color.pieChartYellow);
        colors.add(fuelColor);
        //color for "Timer" - pos 1
        int hoursColor = getResources().getColor(R.color.pieChartGreen);
        colors.add(hoursColor);
        //color for "Pakker" - pos 2
        int packagesColor = getResources().getColor(R.color.pieChartBlue);
        colors.add(packagesColor);

        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();

        mChart.animateY(1500);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * Create a rounded image of the provided profile picture, for displaying in drawer
     * @param bmp Bitmap to be rounded
     * @return
     */
    public static Bitmap getRoundedImage(Bitmap bmp){
        Bitmap output = Bitmap.createBitmap(bmp.getWidth(),bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

        canvas.drawCircle(bmp.getWidth()/2, bmp.getHeight()/2, bmp.getWidth()/2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bmp, rect, rect, paint);
        return output;
    }
}
