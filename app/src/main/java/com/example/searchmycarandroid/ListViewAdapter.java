package com.example.searchmycarandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter{
    String [] textsAndRefs;
    Context context;
    Bitmap[] images;
    private static LayoutInflater inflater=null;
    public ListViewAdapter(ListOfCars mainActivity, String[] prgmList, Bitmap[] prgmImages) {
        // TODO Auto-generated constructor stub
        textsAndRefs=prgmList;
        context=mainActivity;
        images=prgmImages;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return images.length;
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
        holder.tv=(TextView) rowView.findViewById(R.id.textView1);
        holder.img=(ImageView) rowView.findViewById(R.id.imageView1);
        holder.tv.setText(Html.fromHtml(textsAndRefs[position+getCount()]));
        holder.tv.setLinksClickable(true);
        holder.img.setImageBitmap(images[position]);

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(context,CarPage.class);
                intent.putExtra("url",textsAndRefs[position]);
                context.startActivity(intent);
            }
        });
        return rowView;
    }



}