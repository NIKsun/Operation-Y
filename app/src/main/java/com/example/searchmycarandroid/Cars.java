package com.example.searchmycarandroid;

import android.annotation.TargetApi;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jsoup.nodes.Element;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Никита on 22.07.2015.
 */


public class Cars {
    class Car {
        String id;
        String href;
        String img;
        String message;
        String price;
        String mileage;
        String year;
        String city;
        Date timeOfCreate;
    }

    Car[] cars;
    int capacity;
    int lastCar;

    public Cars(int len)
    {
        cars = new Car[len];
        capacity = len;
        lastCar = 0;
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public boolean addFromAutoRu(Element elem)
    {
        if(lastCar >= capacity)
            return false;
        Car currentCar = new Car();
        if(elem == null){
            return false;
        }
        Pattern pattern = Pattern.compile("card_id\":\"([0-9]+).+updated\":\"([^\"]+)");
        Matcher matcher = pattern.matcher(elem.attr("data-stat_params"));
        if(matcher.find()){
            currentCar.id = matcher.group(1);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            try {
                currentCar.timeOfCreate = format.parse(matcher.group(2));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else
            return false;


        currentCar.href = elem.select("td.sales-list-cell.sales-list-cell_images > a").first().attr("href");
        currentCar.img = elem.select("td.sales-list-cell.sales-list-cell_images > a > img").first().attr("data-original");
        if(currentCar.img.isEmpty() == true)
            currentCar.img = elem.select("td.sales-list-cell.sales-list-cell_images > a > img").first().attr("src");

        if (currentCar.img.indexOf("/i/all7/img/no-photo-thumb.png") != -1)
            currentCar.img = "http://auto.ru/i/all7/img/no-photo-thumb.png";

        currentCar.message = elem.select("td.sales-list-cell.sales-list-cell_mark_id").first().text();
        currentCar.price =  elem.select("td.sales-list-cell.sales-list-cell_price").first().text();
        currentCar.year =  elem.select("td.sales-list-cell.sales-list-cell_year").first().text();
        currentCar.mileage=  elem.select("td.sales-list-cell.sales-list-cell_run").first().text();
        currentCar.city =  elem.select("td.sales-list-cell.sales-list-cell_poi_id > div.sales-list-region.ico-appear").first().text();
        cars[lastCar] = currentCar;
        lastCar++;
        return true;
    }
    public String getMessage(int pos)
    {
        String message = "";
        message  += "<font face=fantasy><b>" + cars[pos].message + "</b></font>";
        message += "<br>Год: " + cars[pos].year;
        message += "<br>Цена: " + cars[pos].price;
        message += "<br>Пробег: " + cars[pos].mileage;
        message += "<br>Город: " + cars[pos].city;
        return message;
    }
    public String getHref(int pos)
    {
        return cars[pos].href;
    }
    public String getImg(int pos)
    {
        return cars[pos].img;
    }
    public int getLenth()
    {
        return lastCar;
    }
}
