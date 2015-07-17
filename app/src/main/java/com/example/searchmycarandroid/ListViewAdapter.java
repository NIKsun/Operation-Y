package com.example.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

class Container
{
    public ImageView image;
    public Integer position;
    public Container(ImageView iv, int pos)
    {
        image = iv;
        position = pos;
    }
}

public class ListViewAdapter extends BaseAdapter{
    String [] result;
    Context context;
    String[] images;
    private static LayoutInflater inflater=null;
    public ListViewAdapter(ListOfCars mainActivity, String[] prgmNameList, String[] prgmImages) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        context=mainActivity;
        images=prgmImages;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.length;
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
        holder.tv.setText(Html.fromHtml(result[position]));

        Resources myResources = context.getResources();
        Drawable myIcon = myResources.getDrawable(R.drawable.res);
        ImageTask task = new ImageTask();

        task.execute(new Container(holder.img,position));
        holder.img.setImageDrawable(myIcon);
        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(context, "You Clicked " + result[position], Toast.LENGTH_LONG).show();
            }
        });
        return rowView;
    }

    class ImageTask extends AsyncTask<Container, Void, Container> {
        Bitmap bm;

        @Override
        protected void onPreExecute() {
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected Container doInBackground(Container... params) {
            try {
                bm = BitmapFactory.decodeStream((InputStream) new URL(images[params[0].position]).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Container result) {
            result.image.setImageBitmap(bm);
            Log.d("hello", String.valueOf(result.position));
        }
    }

}