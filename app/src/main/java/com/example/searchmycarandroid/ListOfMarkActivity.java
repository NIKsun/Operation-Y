package com.example.searchmycarandroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.searchmycarandroid.CreateRequestActivity.*;


public class ListOfMarkActivity extends Activity {

    LayoutInflater inflater;
    ArrayList<String>  searchResults;

    ArrayList<String>  originalValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("size", "56789");
        setContentView(R.layout.activity_list_of_mark);
        TextView tvHeader = (TextView)findViewById(R.id.text_view_title);

        //String[] models_arra = getIntent().getStringArrayExtra("Models");

        ListView listMark = (ListView) findViewById(R.id.listViewMark);

        //get the LayoutInflater for inflating the customomView
        //this will be used in the custom adapter
        inflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //create the adapter
        //first param-the context
        //second param-the id of the layout file
        //you will be using to fill a row
        //third param-the set of values that
        //will populate the ListView
        final CustomAdapter adapter;

        if(getIntent().hasExtra("Marks")) {
            tvHeader.setText("Выберите марку авто:");
            originalValues = new ArrayList<String>() ;
            for(int i = 0; i < getIntent().getStringArrayExtra("Marks").length; i++) {
                originalValues.add(getIntent().getStringArrayExtra("Marks")[i]);
            }
            searchResults=new ArrayList<String>(originalValues);
            adapter = new CustomAdapter(ListOfMarkActivity.this, R.layout.list_of_marks, searchResults);

        }
        else {
            tvHeader.setText("Выберите модель авто:");
            originalValues  = new ArrayList<String>();
            getIntent().getStringArrayExtra("Models");
            for(int i = 0; i < getIntent().getStringArrayExtra("Models").length; i++) {
                originalValues.add(getIntent().getStringArrayExtra("Models")[i]);
            }
            searchResults=new ArrayList<String>(originalValues);
            adapter = new CustomAdapter(ListOfMarkActivity.this, R.layout.list_of_marks, searchResults);

        }

        // присваиваем адаптер списку
        listMark.setAdapter(adapter);

        final EditText searchBox = (EditText) findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //get the text in the EditText

                String searchString = searchBox.getText().toString();
                int textLength = searchString.length();

                //clear the initial data set
                searchResults.clear();

                for (int i = 0; i < originalValues.size(); i++) {
                    String marks_or_model_name = originalValues.get(i).toString();
                    if (textLength <= marks_or_model_name.length()) {
                        //compare the String in EditText with Names in the ArrayList
                        if (searchString.equalsIgnoreCase(marks_or_model_name.substring(0, textLength)))

                            searchResults.add(originalValues.get(i));
                    }
                }

                adapter.notifyDataSetChanged();
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        //устанавливаем кликаельность на нашем списке и указываем обработчик кликов,
        //который при тапе с     помощью intent передаёт id записи, который мы поместили в
        //тэг(см. выше), в активити с картой и осуществляет переход на него.
        listMark.setClickable(true);
        listMark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                TextView tv =  (TextView)v.findViewById(R.id.textViewW);
                if (getIntent().hasExtra("Marks")) {

                    //Pair<Integer, ViewHolder> pair = (Pair<Integer, ViewHolder>) v.getTag();
                    //int id = pair.first;
                    //ViewHolder holder = pair.second;
                    ed.putString("SelectedMark", String.valueOf(tv.getText()));
                    Log.d("Tag", String.valueOf(tv.getText()));
                } else {
                    ed.putString("SelectedModel", String.valueOf(tv.getText()));
                }
                ed.commit();
                finish();
            }
        });


    }

    class CustomAdapter extends ArrayAdapter<String>
    {

        public CustomAdapter(Context context, int textViewResourceId,
                             ArrayList<String> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }


        //class for caching the views in a row
        private class ViewHolder
        {
            TextView name;

        }

        ViewHolder viewHolder;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView==null)
            {
                convertView=inflater.inflate(R.layout.list_of_marks, null);
                viewHolder=new ViewHolder();

                //cache the views
                viewHolder.name=(TextView) convertView.findViewById(R.id.textViewW);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);
                //Pair <String, ViewHolder> pair = Pair.create(searchResults.get(position).toString(), viewHolder);
                //convertView.setTag(pair);
            }
            else {
                Log.d("size", String.valueOf(position));
                viewHolder = (ViewHolder) convertView.getTag();
            }


            //set the data to be displayed
           // if(searchResults.size()>position)
                viewHolder.name.setText(searchResults.get(position).toString());
            /*else {
                viewHolder.name.setText("");

            }*/


            //return the view to be displayed
            return convertView;
        }

    }

}
