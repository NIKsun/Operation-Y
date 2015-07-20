package com.example.searchmycarandroid;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


public class CreateRequestActivity extends Activity implements View.OnClickListener {
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_request);


        dbHelper = new DBHelper(this);
        // подключаемся к БД
        //SQLiteDatabase db = dbHelper.getWritableDatabase();

       /* ContentValues cv = new ContentValues();
        ContentValues cv2 = new ContentValues();
        ContentValues cv3 = new ContentValues();
        ContentValues cv4 = new ContentValues();
        //cv.put("marka_id", "3");
        cv.put("model", "X5");
        cv.put("marka", "BMW");
        cv.put("marka_id", "2");
        // вставляем запись и получаем ее ID
        db.insert("carsTable", null, cv);
        cv2.put("model", "X6");
        cv2.put("marka", "BMW");
        cv2.put("marka_id", "2");
        // вставляем запись и получаем ее ID
        db.insert("carsTable", null, cv2);
        cv3.put("model", "A6");
        cv3.put("marka", "Audi");
        cv3.put("marka_id", "1");
        // вставляем запись и получаем ее ID
        db.insert("carsTable", null, cv3);
        cv4.put("model", "A4");
        cv4.put("marka", "Audi");
        cv4.put("marka_id", "1");
        // вставляем запись и получаем ее ID
        db.insert("carsTable", null, cv4);*/
    }

    @Override
    public void onClick(View v) {
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();



        switch (v.getId()) {
            case R.id.buttonSearch:
                Intent intent = new Intent(this, ListOfCars.class);
                TextView request = (TextView) findViewById(R.id.editTextRequest);

                SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                //тестирую
                //ed.putString("SearchMyCarRequest", request.getText().toString());

                SharedPreferences sPref3 = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                Integer posMark = sPref3.getInt("SelectedMark", 0)+1;
                Integer posModel = sPref3.getInt("SelectedModel",0)+1;
                String begin = "http://auto.ru/cars/";
                String end = "/all/?sort%5Bcreate_date%5D=desc";

                Cursor cursorMark = db.query("marksTable", null, "id=?", new String[]{posMark.toString()}, null, null, null);
                cursorMark.moveToFirst();
                String marka = cursorMark.getString(cursorMark.getColumnIndex("marka"));

                Cursor cursorModel = db.query("modelsTable", null, "marka_id=?", new String[]{posMark.toString()}, null, null, null);
                cursorModel.moveToFirst();
                int i = 1;
                while(i<posModel){
                    cursorModel.moveToNext();
                    ++i;
                }
                String model = cursorModel.getString(cursorModel.getColumnIndex("model"));

                //ed.putString("SearchMyCarRequest", request.getText().toString());
                String re = begin+marka+"/"+model+end;
                ed.putString("SearchMyCarRequest", begin+marka+"/"+model+end);
                ed.putBoolean("SearchMyCarIsFromService", false);
                ed.commit();
                startActivity(intent);

                break;
            case R.id.marka_button:

                Cursor cursor = db.query("marksTable", null, null, null, null, null, null);
                String strToParse = "";

                if (cursor.moveToFirst()) {
                    int MarkColIndex = cursor.getColumnIndex("marka");
                    do {

                        strToParse += cursor.getString(MarkColIndex) + "@@@";
                    } while (cursor.moveToNext());
                }
                String[] marks_arr = strToParse.split("@@@");
                Intent intent2 = new Intent(this, ListOfMarkActivity.class);
                intent2.putExtra("Marks",marks_arr);
                startActivity(intent2);

                break;
            case R.id.model_button:

                SharedPreferences sPref2 = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                Integer pos = sPref2.getInt("SelectedMark",0);
                pos+=1;
                Cursor cursor2 = db.query("modelsTable", null, "marka_id=?", new String[]{pos.toString()}, null, null, null);
                String strToParse2 = "";

                if (cursor2.moveToFirst()) {
                    int ModelColIndex = cursor2.getColumnIndex("model");
                    do {

                        strToParse2 += cursor2.getString(ModelColIndex) + "@@@";
                    } while (cursor2.moveToNext());
                }
                String[] models_arr = strToParse2.split("@@@");
                Intent intent3 = new Intent(this, ListOfMarkActivity.class);
                intent3.putExtra("Models",models_arr);
                startActivity(intent3);

                break;
            default:
                break;
        }
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "mycars4DB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("11111111111111111111", "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table modelsTable ("
                    + "id integer primary key autoincrement,"
                    + "model text,"
                    + "marka_id integer" + ");");

            db.execSQL("create table marksTable ("
                    + "id integer primary key autoincrement,"
                    + "marka text"
                    + ");");
            String[] models_arr = new String[]{"a6","a4","x6","x5"};
            String[] index_arr = new String[]{"1","1","2","2"};
            String[] marks_arr = new String[]{"audi","bmw"};
            for(int i=0; i<models_arr.length;++i){
                ContentValues cv = new ContentValues();
                //cv.put("marka_id", "3");
                cv.put("model", models_arr[i]);
                //cv.put("marka", "BMW");
                cv.put("marka_id", index_arr[i]);
                // вставляем запись и получаем ее ID
                db.insert("modelsTable", null, cv);
            }
            for(int i=0; i<marks_arr.length;++i){
                ContentValues cv = new ContentValues();
                cv.put("marka", marks_arr[i]);
                db.insert("marksTable", null, cv);
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
