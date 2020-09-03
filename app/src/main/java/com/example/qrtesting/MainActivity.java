package com.example.qrtesting;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CameraPreview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int PICKFILE_RESULT_CODE  = 1;
    Uri fileUri;
    String filePath;
    ImageView DisplayImage;
    String currentPhotoPath;
    Button btn_scan;
    EditText etPhoneName;
    EditText etFileDirectory;
    TextView tvFiledirectory;
    Bitmap mBitmap;
    int position;
    OutputStream outputStream;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_scan = findViewById(R.id.btnScan);
        etPhoneName = findViewById(R.id.etPhoneName);
        etFileDirectory = findViewById(R.id.etFileDirectory);
        tvFiledirectory = findViewById(R.id.tvFileDirectory);


        etPhoneName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor prefEdit = prefs.edit();
                prefEdit.putString("phonename",etPhoneName.getText().toString() );
                prefEdit.commit();;
            }
        });
        etFileDirectory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor prefEdit = prefs.edit();
                prefEdit.putString("filedirectory", etFileDirectory.getText().toString() );
                prefEdit.commit();;
            }
        });
        tvFiledirectory.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);  // optional
                integrator.setOrientationLocked(false);// allow barcode scanner in potrait mode
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();



            }
        });






    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        DisplayImage = findViewById(R.id.ImageView);


        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == -1) {
                    fileUri = data.getData();
                    filePath = fileUri.getPath();
                    etFileDirectory.setText(filePath);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor prefEdit = prefs.edit();
                    prefEdit.putString("phonename",etPhoneName.getText().toString() );
                    prefEdit.putString("filedirectory",filePath );
                    prefEdit.commit();
                }

                break;
        }



        if (result != null) {
            String value = result.getContents();
            if (value != null) {
                Log.i("test123", result.getBarcodeImagePath());

                Toast.makeText(getApplicationContext(),value,Toast.LENGTH_LONG).show();
//                dispatchTakePictureIntent();
                Bitmap myBitmap = BitmapFactory.decodeFile(result.getBarcodeImagePath());
                DisplayImage.setImageBitmap(myBitmap);
                String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                BitmapDrawable drawable = (BitmapDrawable)DisplayImage.getDrawable();


                File fp = Environment.getExternalStorageDirectory();
                File dir = new File(fp.getAbsolutePath()+"/Test/");
                dir.mkdir();
                File file  = new File(dir,System.currentTimeMillis()+".jpg");
                try {
                    outputStream = new FileOutputStream(file);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                myBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                Log.i("test123","Image Saved To Internal"+file);
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor prefEdit2 = prefs2.edit();
                prefEdit2.putString("qrvalue",value);
            } else {
                Log.i("test123","QR code capture canceled or failed");
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);  // optional
                integrator.setOrientationLocked(false);// allow barcode scanner in potrait mode
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }


    }









    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String phonename = prefs.getString("phonename", "");
        String filedirectory = prefs.getString("filedirectory","");
        etFileDirectory.setText(filedirectory);
        etPhoneName.setText(phonename);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor prefEdit = prefs.edit();
        prefEdit.putInt("position" , position);
        prefEdit.commit();
    }


//        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());


}