package com.example.product_detector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
/*
class ItemStruct implements Serializable {
    public transient String productName;
    public transient Integer productPrice;
    public transient byte[] productImage;
    public transient Uri productURL;
    public transient String productLabel;
    public ItemStruct(String iName,Integer iPrice,byte[] iImage,Uri iURL,String iLabel){
        productName = iName;
        productPrice = iPrice;
        productImage = iImage;
        productURL = iURL;
        productLabel = iLabel;
    }
}
*/
public class LoadingActivity extends AppCompatActivity {
    private byte[] imagebytes = {};
    private ImageView imageView;
    private File imageFile;
    private String ServerIP = "122.116.104.96";
    private Integer ServerPort = 8787;
    private Bitmap imagebitmap;
    private String JSONString = new  String();
    //public ItemStruct[] ListItem = new ItemStruct[300];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        GetIntent();
        try {
            imagebytes = FileToByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread socket = new Thread(ClientSocket);
        socket.start();
    }


    private byte[] FileToByteArray() throws IOException {
        byte [] bytes = new byte[(int)imageFile.length()];
        bytes = IOUtil.readFile(imageFile);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        Bitmap resizeBMP = Bitmap.createBitmap(bitmap,0,350,900,900);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizeBMP.compress(Bitmap.CompressFormat.JPEG,100,stream);
        bytes = stream.toByteArray();
        resizeBMP.recycle();
        return bytes;
    }
    public static class IOUtil {

        public static byte[] readFile(String file) throws IOException {
            return readFile(new File(file));
        }

        public static byte[] readFile(File file) throws IOException {
            // Open file
            RandomAccessFile f = new RandomAccessFile(file, "r");
            try {
                // Get and check length
                long longlength = f.length();
                int length = (int) longlength;
                if (length != longlength)
                    throw new IOException("File size >= 2 GB");
                // Read file and return data
                byte[] data = new byte[length];
                f.readFully(data);
                return data;
            } finally {
                f.close();
            }
        }
    }

    Runnable ClientSocket = new Runnable() {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(ServerIP,ServerPort);
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                outputStream.write(imagebytes);
                outputStream.flush();
                socket.shutdownOutput();
                //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int len = 0;
                JSONString = new String();
                byte[] buffer = new byte[1024];
                while((len = inputStream.read(buffer)) != -1){
                    JSONString += new String(buffer,0,len) ;
                    //Log.d("JSON",JSONString);
                }
                JSONArray jsonArray = new JSONArray(JSONString);
                /*
                for(Integer i=0; i < 30; i++){
                    Log.d("length",String.valueOf(jsonArray.length()));
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ListItem[i] = new ItemStruct(
                            jsonObject.getString("Product"),
                            jsonObject.getInt("Price"),
                            Base64.decode(jsonObject.getString("Imagepath"),Base64.DEFAULT),
                            Uri.parse(jsonObject.getString("Dataurl")),
                            jsonObject.getString("Label"));
                    Log.d("NO.",String.valueOf(i+1));
                    Log.d("Name",jsonObject.getString("Product"));
                    Log.d("Label",jsonObject.getString("Label"));
                    Log.d("Price",String.valueOf(jsonObject.getInt("Price")));
                }
                */
                socket.close();
                Log.d("socket", "socket closed");
                GOResult(jsonArray);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void GOResult(JSONArray jsonArray) throws IOException {
        Intent intent = new Intent(this,ResultActivity.class);
        intent.putExtra("imagepath",imageFile);
        FileOutputStream fileOutputStream = openFileOutput("JSONArray", Context.MODE_PRIVATE);
        fileOutputStream.write(jsonArray.toString().getBytes());
        fileOutputStream.close();
        startActivity(intent);
        finish();
    }


    private void GetIntent() {
        Intent intent = getIntent();
        imageFile = (File) getIntent().getExtras().get("imagepath");
        imagebitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        imageView = findViewById(R.id.MLimageView);
        imageView.setImageBitmap(imagebitmap);
    }

}
