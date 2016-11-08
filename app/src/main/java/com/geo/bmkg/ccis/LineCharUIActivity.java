package com.geo.bmkg.ccis;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;



public class LineCharUIActivity extends Activity {


    static final int MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE = 0;
    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;


    CharSequence[] items = { "Temperature", "Warm Extremes", "Cold Extremes", "Wet Extremes",
                             "Precipitation", "Drought", "Seasonal", "Humadity", "Sunshine Duration" };

    boolean[] itemsChecked = new boolean [items.length];

    List<String> indikatorVariable = new ArrayList<String>();
    CharSequence[] indikatorIklimItems = null;
    boolean[] indikatorItemsChecked = null;
    String title ="";
    String stationcode;
    String nid;
    File storagepath;
    String[] rawData = null;
    ProgressDialog progressDialog = null;
    LineDataSet dataset = null;
    LineChart chart = null;
    LineData lineData = null;
    String tempIndikator = null;
    String tempBuffer = "";
    //HashMap<Integer, Integer> tempIndikatorIklim = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetStationName();
        stationcode = GetStationCode();
        storagepath = GetStoragePath();
        nid = GetNID();

        if (Build.VERSION.SDK_INT < 23) {
            if(CheckIsFileExist(stationcode+".csv"))
            {

                BuildDataIndikatorIklim(readIndikatorIklim());

            }
            else
            {
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Generate Critical Climate Indicator Chart");
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new DownloadTextTask().execute(GetIndikatorIklimData(stationcode,nid));
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        }else{
            if(CheckIsFileExist(stationcode+".csv"))
            {
                if (ContextCompat.checkSelfPermission(LineCharUIActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {

                    ActivityCompat.requestPermissions(LineCharUIActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                }

            }else{
                if (ContextCompat.checkSelfPermission(LineCharUIActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(LineCharUIActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(LineCharUIActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(LineCharUIActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
                {

                    ActivityCompat.requestPermissions(LineCharUIActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE}, MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE);

                }
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE:
                {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        progressDialog = new ProgressDialog(this);
                        progressDialog.setTitle("Generate Critical Climate Indicator Chart");
                        progressDialog.setMessage("Please wait...");
                        progressDialog.show();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new DownloadTextTask().execute(GetIndikatorIklimData(stationcode,nid));
                                }catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    } else {

                        LineCharUIActivity.this.finish();
                        System.exit(0);
                    }
                    return;
                }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        BuildDataIndikatorIklim(readIndikatorIklim());

                    } else {

                        LineCharUIActivity.this.finish();
                        System.exit(0);
                    }
                    return;
                }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    protected  String GetIndikatorIklimData(String stationcode, String nid)
    {
        String url = "http://ccis.klimat.bmkg.go.id/ccis/ccis/dataquery/"+stationcode+"_yearly/download/csv?nid="+nid+"&range=yearly";
        return url;
    }

    protected String GetStationName(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title = extras.getString("title");
            rawData = extras.getString("snippet").split(" ");
        }
        else
        {
            title = "Stasiun Anonim";
        }

        extras = null;
        return title;
    }

    protected String GetStationCode(){
        String stationcode="";
        if (rawData != null) {
            stationcode = rawData[2].trim();
        }
        else
        {
            stationcode = "97430";
        }
        return stationcode;
    }

    protected String GetNID(){
        String nid="";
        if (rawData != null) {
            nid = rawData[4].trim();
        }
        else
        {
            nid = "183";
        }
        return nid;
    }

    protected void AddChartDataSet(HashMap<Integer, Integer> data, String indikatorIklim)
    {
        ArrayList<Entry> entries = new ArrayList<>();
        int counter = 0;

        List keys = new ArrayList(data.keySet());
        Collections.sort(keys);
        Iterator<Integer> iterator = keys.iterator();
        int key;

        while (iterator.hasNext()) {
            key = iterator.next();
            entries.add(new Entry(data.get(key), counter));
            counter++;
        }


        LineDataSet newDataSet = new LineDataSet(entries, indikatorIklim);
        lineData.addDataSet(newDataSet);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }


    protected void BuildDataIndikatorIklim(HashMap<Integer, Integer> data)
    {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();
        int counter = 0;

        List keys = new ArrayList(data.keySet());
        Collections.sort(keys);
        Iterator<Integer> iterator = keys.iterator();
        int key;

        while (iterator.hasNext()) {
            key = iterator.next();
            entries.add(new Entry(data.get(key), counter));
            labels.add(String.valueOf(key));
            counter++;
        }


        dataset = new LineDataSet(entries, "#CDD");
        chart = new LineChart(this.getBaseContext());
        chart.setOnClickListener(mIndikatorIklimListener);
        setContentView(chart);

        this.setTitle(title+" Station Code: "+stationcode+" NID:"+nid);
        lineData = new LineData(labels, dataset);
        chart.setData(lineData);
        chart.setDescription("# Indikator Iklim Tropis [CDD]");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                return new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Indikator Iklim Tropis")
                        .setPositiveButton("OK", new
                                DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        //Toast.makeText(getBaseContext(), "OK clicked!", Toast.LENGTH_SHORT).show();
                                        if(!tempIndikator.equals(""))
                                        {
                                            AddChartDataSet(readIndikatorIklimData(tempIndikator), tempIndikator);
                                        }

                                    }
                                })
                        .setNegativeButton("Cancel", new
                                DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        //Toast.makeText(getBaseContext(), "Cancel clicked!", Toast.LENGTH_SHORT).show();
                                        tempIndikator="";
                                    }
                                })
                        .setMultiChoiceItems(items, itemsChecked, new
                                DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                                        if(isChecked == true)
                                        {
                                            if(!indikatorVariable.isEmpty())
                                            {
                                                indikatorVariable.clear();
                                            }

                                            if(items[which].toString() == "Temperature")
                                            {
                                                indikatorVariable.add("DTR");
                                                indikatorVariable.add("GSL");
                                                indikatorVariable.add("TMAX_ANOM");
                                                indikatorVariable.add("TMIN_ANOM");
                                                indikatorVariable.add("TMEAN_ANOM");
                                                indikatorVariable.add("AT_ANOM");
                                                indikatorVariable.add("TMAX");
                                                indikatorVariable.add("TMIN");
                                                indikatorVariable.add("TMEAN");
                                                indikatorVariable.add("AT");

                                            }
                                            else if(items[which].toString() == "Warm Extremes")
                                            {
                                                indikatorVariable.add("SU");
                                                indikatorVariable.add("TN90p");
                                                indikatorVariable.add("Tnx");
                                                indikatorVariable.add("TR");
                                                indikatorVariable.add("Txx");
                                                indikatorVariable.add("WSDI");
                                                indikatorVariable.add("TR25");
                                                indikatorVariable.add("HNHD");
                                                indikatorVariable.add("HHI");
                                                indikatorVariable.add("SU30");

                                            }
                                            else if(items[which].toString() == "Cold Extremes")
                                            {
                                                indikatorVariable.add("CSDI");
                                                indikatorVariable.add("FD");
                                                indikatorVariable.add("ID");
                                                indikatorVariable.add("TN10p");
                                                indikatorVariable.add("Tnn");
                                                indikatorVariable.add("TX10p");
                                                indikatorVariable.add("Txn");

                                            }
                                            else if(items[which].toString() == "Wet Extremes")
                                            {
                                                indikatorVariable.add("R10mm");
                                                indikatorVariable.add("R20mm");
                                                indikatorVariable.add("R95pTOT");
                                                indikatorVariable.add("R99pTOT");
                                                indikatorVariable.add("Rx1day");
                                                indikatorVariable.add("Rx5day");
                                                indikatorVariable.add("SDII");
                                                indikatorVariable.add("RQ95");

                                            }
                                            else if(items[which].toString() == "Precipitation")
                                            {
                                                indikatorVariable.add("CWD");
                                                indikatorVariable.add("PRCPTOT");
                                                indikatorVariable.add("Rnnmm");
                                                indikatorVariable.add("PRECIP_ANOM");
                                                indikatorVariable.add("PRECIP");

                                            }
                                            else if(items[which].toString() == "Drought")
                                            {
                                                indikatorVariable.add("CDD");
                                                indikatorVariable.add("SPI_ANOM");
                                                indikatorVariable.add("SPI");

                                            }
                                            else if(items[which].toString() == "Seasonal")
                                            {
                                                indikatorVariable.add("ONWS");
                                                indikatorVariable.add("ONDS");
                                                indikatorVariable.add("LDS");
                                                indikatorVariable.add("LWS");
                                            }
                                            else if(items[which].toString() == "Humadity")
                                            {
                                                indikatorVariable.add("RH_ANOM");
                                                indikatorVariable.add("RH");

                                            }
                                            else
                                            {
                                                indikatorVariable.add("SUN_ANOM");
                                                indikatorVariable.add("SUN");

                                            }

                                            indikatorIklimItems = indikatorVariable.toArray(new CharSequence[indikatorVariable.size()]);
                                            indikatorItemsChecked =  new boolean[indikatorIklimItems.length];
                                            CreateVariableIndiatorIklimUI(indikatorIklimItems, indikatorItemsChecked).show();

                                        }


                                    }
                                }
                        )
                        .create();

            case 1:

                CreateVariableIndiatorIklimUI(indikatorIklimItems, indikatorItemsChecked);


        }
        return null;
    }

    private View.OnClickListener mIndikatorIklimListener = new View.OnClickListener() {
        public void onClick(View v) {
            showDialog(0);
        }
    };


    private AlertDialog.Builder CreateVariableIndiatorIklimUI(CharSequence[] items, boolean[] itemsChecked)
    {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle("Indikator Iklim Tropis");
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getBaseContext(), "OK clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,
                                int whichButton) {
                //Toast.makeText(getBaseContext(), "Cancel clicked!", Toast.LENGTH_SHORT).show();
                tempIndikator = "";
            }
        });

        dialog.setMultiChoiceItems(items, itemsChecked, new
                DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        //Toast.makeText(getBaseContext(), indikatorIklimItems[which] + (isChecked ? " checked!" : " unchecked!"), Toast.LENGTH_SHORT).show();
                        tempIndikator = indikatorIklimItems[which].toString().trim();

                    }
                }
        );

        dialog.create();

        return dialog;
    }

    protected HashMap<Integer, Integer> readIndikatorIklim()
    {
        HashMap<Integer, Integer> indikatorIklim= new HashMap<Integer, Integer>();
        BufferedReader reader=null;
        FileReader fileReader=null;


            try {
                //File file = new File("/storage/extSdCard/indikatoriklim.csv");
                File file = new File(storagepath,stationcode+".csv");
                fileReader = new FileReader(file);
                reader = new BufferedReader(fileReader);

                String data;
                int counter = 0;
                while ((data = reader.readLine()) != null) {
                    String[] RowData = data.split(",");
                    if(!RowData[1].equals("NA") && counter !=0){
                        indikatorIklim.put(Integer.parseInt(RowData[0]),Integer.parseInt(RowData[1]));
                    }
                    counter++;
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            finally {
                try {
                    fileReader.close();
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return indikatorIklim;
    }

    protected HashMap<Integer, Integer> readIndikatorIklimData(String indikator)
    {
        HashMap<Integer, Integer> indikatorIklim= new HashMap<Integer, Integer>();
        BufferedReader reader=null;
        FileReader fileReader=null;

        try {
            //File file = new File("/storage/extSdCard/indikatoriklim.csv");
            File file = new File(storagepath,stationcode+".csv");
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);

            String data;
            int counter = 0;
            int index;

            if(indikator.equals("cdd"))
            {
                index = 1;
            }
            else if(indikator.equals("cwd"))
            {
                index = 2;
            }
            else if(indikator.equals("fd"))
            {
                index = 3;
            }
            else if(indikator.equals("id"))
            {
                index = 4;
            }
            else if(indikator.equals("r20mm"))
            {
                index = 5;
            }
            else if(indikator.equals("r99ptot"))
            {
                index = 6;
            }
            else if(indikator.equals("rx1day"))
            {
                index = 7;
            }
            else {
                index = 8;
            }

            while ((data = reader.readLine()) != null) {
                String[] RowData = data.split(",");
                if(!RowData[index].equals("NA") && counter !=0){
                    indikatorIklim.put(Integer.parseInt(RowData[0]),Integer.parseInt(RowData[1]));
                }
                counter++;
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                fileReader.close();
                reader.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return indikatorIklim;
    }

     private String DownloadText(String URL) {
        int BUFFER_SIZE = 200000;
        InputStream in = null;
        try {
            in = OpenHttpGETConnection(URL);
        } catch (Exception e) {
            Log.d("Networking", e.getLocalizedMessage());
            return "";
        }

        InputStreamReader isr = new InputStreamReader(in);
        int charRead;
        String str = "";
        char[] inputBuffer = new char[BUFFER_SIZE];
        try {
            while ((charRead = isr.read(inputBuffer)) > 0) {
                // ---convert the chars to a String---
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                str += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            in.close();
        } catch (IOException e) {
            Log.d("Networking", e.getLocalizedMessage());
            return "";
        }
        return str;
    }

    public static InputStream OpenHttpGETConnection(String url) {
        InputStream inputStream = null;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
        } catch (Exception e) {
            Log.d("", e.getLocalizedMessage());
        }
        return inputStream;
    }

    private class DownloadTextTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return DownloadText(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {


                File file = new File(storagepath,stationcode+".csv");
                FileOutputStream outStream = null;
                OutputStreamWriter streamWriter = null;

                try{
                    file.createNewFile();
                    outStream = new FileOutputStream(file);
                    streamWriter = new OutputStreamWriter(outStream);
                    streamWriter.write(result);

                }catch(IOException e)
                {
                    e.printStackTrace();
                }
                finally {
                    try {
                        outStream.close();
                        streamWriter.close();
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
                BuildDataIndikatorIklim(readIndikatorIklim());
                //BuildDataIndikatorIklim(readIndikatorIklimData("cdd"));
                Toast.makeText(getBaseContext(), "Download has finished successfully", Toast.LENGTH_LONG).show();
                Log.d("DownloadTextTask", result);

        }
    }


    protected boolean CheckIsFileExist(String filename)
    {
        File file = new File(storagepath,filename);

        if (file.exists())
        {
            return true;
        }

        return false;
    }

    protected File GetStoragePath()
    {
        return Environment.getExternalStorageDirectory();
    }
}
