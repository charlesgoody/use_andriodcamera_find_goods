package com.example.product_detector;

import android.app.LauncherActivity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ItemStruct implements Serializable {
    public transient String productName;
    public transient Integer productPrice;
    public transient Bitmap productImage;
    public transient Uri productURL;
    public transient String productLabel;
    public ItemStruct(String iName, Integer iPrice, Bitmap iImage, Uri iURL, String iLabel){
        productName = iName;
        productPrice = iPrice;
        productImage = iImage;
        productURL = iURL;
        productLabel = iLabel;
    }
}
public class ResultActivity extends AppCompatActivity {
    private ItemStruct[] ListItem = new ItemStruct[30];
    private File OriImagePath;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        try {
            InitViewData();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InitListView();
    }




    private void InitListView() {
        final ListView listView = (ListView) findViewById(R.id.resultListView);
        ListAdapter listAdapter = new ArrayAdapter<ItemStruct>(this,R.layout.list_view_item,R.id.productNameText,ListItem){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if(v == null){
                    LayoutInflater inflater = getLayoutInflater();
                    v = inflater.inflate(R.layout.list_view_item, parent, false);
                }
                ImageView imageView = (ImageView) v.findViewById(R.id.productImage);
                TextView textView1 = (TextView) v.findViewById(R.id.productNameText);
                TextView textView2 = (TextView) v.findViewById(R.id.productPriceText);
                //Bitmap bitmap = BitmapFactory.decodeByteArray(ListItem[position].productImage,0,ListItem[position].productImage.length);
                //imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap,85,85,true));
                imageView.setImageBitmap(ListItem[position].productImage);
                Log.e("ImageID",String.valueOf(position));
                textView1.setText(ListItem[position].productName);
                textView2.setText(Integer.toString(ListItem[position].productPrice));
                return v;
            }

            @Override
            public int getCount() {
                return 30;
            }

            @Override
            public ItemStruct getItem(int position) {
                return super.getItem(position);
            }

            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }
        };
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent Int = new Intent(Intent.ACTION_VIEW,ListItem[i].productURL);
                startActivity(Int);
            }
        });
    }

    private void InitViewData() throws JSONException, IOException {
        OriImagePath = (File) getIntent().getExtras().get("imagepath");
        ImageView imageView = findViewById(R.id.originImageView);
        imageView.setImageBitmap(BitmapFactory.decodeFile(OriImagePath.getAbsolutePath()));
        FileInputStream fileInputStream = openFileInput("JSONArray");
        byte[] bytes = new byte[1024];
        StringBuffer stringBuffer = new StringBuffer();
        while (fileInputStream.read(bytes) != -1){
            stringBuffer.append(new String(bytes));
        }
        JSONArray jsonArray = new JSONArray(stringBuffer.toString());
        String strlabel = new String();
        for (Integer i = 0; i < 30; i++) {
            //Log.d("length",String.valueOf(jsonArray.length()));
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Log.e("Base64",jsonObject.getString("Imagepath"));
            ListItem[i] = new ItemStruct(
                    jsonObject.getString("Product"),
                    jsonObject.getInt("Price"),
                    BitmapFactory.decodeByteArray(Base64.decode(jsonObject.getString("Imagepath"),Base64.DEFAULT),0,Base64.decode(jsonObject.getString("Imagepath"),Base64.DEFAULT).length),
                    Uri.parse(jsonObject.getString("Dataurl")),
                    jsonObject.getString("Label"));
            //Log.d("NO.",String.valueOf(i+1));
            //Log.d("Name",jsonObject.getString("Product"));
            //Log.d("Label",jsonObject.getString("Label"));
            //Log.d("Price",String.valueOf(jsonObject.getInt("Price")));
            if (i < 1) {
                strlabel = jsonObject.getString("Label");
            }
        }
        TextView labelTextView1 = findViewById(R.id.textViewLabel1);
        TextView labelTextView2 = findViewById(R.id.textViewLabel2);
        switch (strlabel){
            case "jacket":
                labelTextView1.setText("Jacket");
                labelTextView2.setText("");
                break;
            case "jacket_long":
                labelTextView1.setText("Jacket");
                labelTextView2.setText("Long");
                break;
            case "polo":
                labelTextView1.setText("Polo");
                labelTextView2.setText("");
                break;
            case "polo_long":
                labelTextView1.setText("Polo");
                labelTextView2.setText("Long");
                break;
            case "polo_simplecolor":
                labelTextView1.setText("Polo");
                labelTextView2.setText("Simple Color");
                break;
            case "polo_stripe":
                labelTextView1.setText("Polo");
                labelTextView2.setText("Stripe");
                break;
            case "sleeveless":
                labelTextView1.setText("Sleeveless");
                labelTextView2.setText("");
                break;
            case "stripe":
                labelTextView1.setText("Stripe");
                labelTextView2.setText("");
                break;
            case "stripe_long":
                labelTextView1.setText("Stripe");
                labelTextView2.setText("Long");
                break;
            case "tshirt":
                labelTextView1.setText("T-Shirt");
                labelTextView2.setText("");
                break;
            case "tshirt_mark":
                labelTextView1.setText("T-Shirt");
                labelTextView2.setText("Mark");
                break;
            case "tshirt_nike":
                labelTextView1.setText("T-Shirt");
                labelTextView2.setText("Nike");
                break;
            case "tshirt_simplecolor":
                labelTextView1.setText("T-Shirt");
                labelTextView2.setText("Simple Color");
                break;
        }
    }
}
