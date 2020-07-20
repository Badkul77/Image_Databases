package com.example.image_database;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.InvalidMarkException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
Button btnSelectImage,btnUploadImage;
ImageView imageView;
Bitmap bitmap;
String encodedImage=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSelectImage=findViewById(R.id.btnSelectImage);
        btnUploadImage=findViewById(R.id.btnUploadImage);
        imageView=findViewById(R.id.imView);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(MainActivity.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                           .withListener(new PermissionListener() {
                               @Override
                               public void onPermissionGranted(PermissionGrantedResponse response) {
                                   //we are picking the image from gallery that is why action is pick
                                   Intent intent=new Intent(Intent.ACTION_PICK);
                                   //what type of data in intent
                                   intent.setType("image/*");
                                   startActivityForResult(Intent.createChooser(intent,"Select Image"),11);
                               }

                               @Override
                               public void onPermissionDenied(PermissionDeniedResponse response) {

                               }

                               @Override
                               public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                               token.continuePermissionRequest();
                               }
                           }).check();
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog pd=new ProgressDialog(MainActivity.this);
                pd.setTitle("Uploading....");
                pd.show();
                String url="https://simplyfied.co.in/Test/uploadimages.php";
                StringRequest request=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pd.dismiss();
                        Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Toast.makeText(MainActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params=new HashMap<>();
                        params.put("image",encodedImage);
                        return params;
                    }
                };
                RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
                requestQueue.add(request);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==11 && resultCode==RESULT_OK && data!=null)
        {
            //in this your image file path will come
            Uri filepath=data.getData();
            //Now we need to convert this image into bitmap and encode it
            try {
                InputStream inputStream=getContentResolver().openInputStream(filepath);
              //decoding the inputstream into bitmap
                bitmap= BitmapFactory.decodeStream(inputStream);
                bitmap=getResizedBitmap(bitmap,1024);
                imageView.setImageBitmap(bitmap);

                //this defined function is used to encode the bitmap to store in mysql
                imageStore(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

        private void imageStore(Bitmap bitmap) {
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

        //to convert image into byte array
        byte[] imageBytes=stream.toByteArray();
    encodedImage=android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);

    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
