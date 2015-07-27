package com.example.searchmycarandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

public class ListViewAdapter extends BaseAdapter{
    Context context;
    Cars cars;
    Bitmap[] images;
    String lastCarDate;
    Boolean isFromMonitor;

    private static LayoutInflater inflater=null;
    public ListViewAdapter(NotificationActivity mainActivity, Cars c, Bitmap[] imgs, String lastCarDate) {
        // TODO Auto-generated constructor stub
        context=mainActivity;
        this.lastCarDate=lastCarDate;
        images = imgs;
        cars = c;
        isFromMonitor = true;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public ListViewAdapter(ListOfCars mainActivity, Cars c, Bitmap[] imgs) {
        // TODO Auto-generated constructor stub
        context=mainActivity;
        images = imgs;
        cars = c;
        isFromMonitor = false;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return cars.getLenth();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.listview, null);

        if(isFromMonitor){
            if(Date.parse(lastCarDate) < Date.parse(cars.getCarDate(position)))    //New cars
                rowView.setBackgroundColor(0xFFC1E1FF);
        }

        holder.tv=(TextView) rowView.findViewById(R.id.textView1);
        holder.img=(ImageView) rowView.findViewById(R.id.imageView1);
        holder.tv.setText(Html.fromHtml(cars.getMessage(position)));
        holder.tv.setLinksClickable(true);
        holder.img.setImageBitmap(images[position]);

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(context,CarPage.class);
                intent.putExtra("url",cars.getHref(position));
                context.startActivity(intent);
            }
        });
        return rowView;
    }



}