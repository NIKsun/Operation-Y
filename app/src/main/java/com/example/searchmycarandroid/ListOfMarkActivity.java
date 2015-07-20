package com.example.searchmycarandroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import static com.example.searchmycarandroid.CreateRequestActivity.*;


public class ListOfMarkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_mark);

        String[] marks_arr = getIntent().getStringArrayExtra("Marks");
        String[] models_arr = getIntent().getStringArrayExtra("Models");

        ListView listMark = (ListView) findViewById(R.id.listViewMark);
        // ������� �������
        ArrayAdapter<String> adapter;

        if(getIntent().hasExtra("Marks"))
        adapter = new ArrayAdapter<String>(this,
                R.layout.list_of_marks,marks_arr);
        else
        adapter= new ArrayAdapter<String>(this,
               R.layout.list_of_marks,models_arr);

        //������������� �������������� �� ����� ������ � ��������� ���������� ������,
        //������� ��� ���� �     ������� intent ������� id ������, ������� �� ��������� �
        //���(��. ����), � �������� � ������ � ������������ ������� �� ����.
        listMark.setClickable(true);
        listMark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                if(getIntent().hasExtra("Marks"))
                    ed.putInt("SelectedMark",pos);
                else
                    ed.putInt("SelectedModel",pos);
                ed.commit();
                finish();
            }
        });
        // ����������� ������� ������
        listMark.setAdapter(adapter);

    }

}
