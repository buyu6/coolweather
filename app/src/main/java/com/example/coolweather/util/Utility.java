package com.example.coolweather.util;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.example.coolweather.db.MyDatabaseHelper;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        if (TextUtils.isEmpty(response)) {
            return false;
        }

        MyDatabaseHelper dbHelper = null;
        SQLiteDatabase db = null;
        try {
            dbHelper = new MyDatabaseHelper(MyApplication.getContext());
            db = dbHelper.getWritableDatabase();

            // 开启事务（批量插入性能优化）
            db.beginTransaction();
            JSONArray allProvinces = new JSONArray(response);
            for (int i = 0; i < allProvinces.length(); i++) {
                JSONObject provinceObject = allProvinces.getJSONObject(i);
                String provinceName = provinceObject.getString("name");
                int provinceCode = provinceObject.getInt("id");

                // 数据校验
                if (TextUtils.isEmpty(provinceName) ){
                    continue;
                }

                ContentValues values = new ContentValues();
                values.put("provinceName", provinceName);
                values.put("provinceCode", provinceCode);
                db.insert(MyDatabaseHelper.TABLE_PROVINCE, null, values);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
           e.printStackTrace();
            return false;
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (TextUtils.isEmpty(response)) {
            return false;
        }

        MyDatabaseHelper dbHelper = null;
        SQLiteDatabase db = null;
        try {
            dbHelper = new MyDatabaseHelper(MyApplication.getContext());
            db = dbHelper.getWritableDatabase();

            db.beginTransaction();
            JSONArray allCities = new JSONArray(response);
            for (int i = 0; i < allCities.length(); i++) {
                JSONObject cityObject = allCities.getJSONObject(i);
                String cityName = cityObject.getString("name");
                int cityCode = cityObject.getInt("id");

                if (TextUtils.isEmpty(cityName)) {
                    continue;
                }

                ContentValues values = new ContentValues();
                values.put("cityName", cityName);
                values.put("cityCode", cityCode);
                values.put("provinceId", provinceId);
                db.insert(MyDatabaseHelper.TABLE_CITY, null, values);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
           e.printStackTrace();
            return false;
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (TextUtils.isEmpty(response)) {
            return false;
        }

        MyDatabaseHelper dbHelper = null;
        SQLiteDatabase db = null;
        try {
            dbHelper = new MyDatabaseHelper(MyApplication.getContext());
            db = dbHelper.getWritableDatabase();

            db.beginTransaction();
            JSONArray allCounties = new JSONArray(response);
            for (int i = 0; i < allCounties.length(); i++) {
                JSONObject countyObject = allCounties.getJSONObject(i);
                String countyName = countyObject.getString("name");
                String weatherId = countyObject.getString("weather_id");

                if (TextUtils.isEmpty(countyName) || TextUtils.isEmpty(weatherId)) {
                    continue;
                }

                ContentValues values = new ContentValues();
                values.put("countyName", countyName);
                values.put("weatherId", weatherId);
                values.put("cityId", cityId);
                db.insert(MyDatabaseHelper.TABLE_COUNTY, null, values);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (JSONException e) {
          e.printStackTrace();
            return false;
        } catch (Exception e) {
           e.printStackTrace();
            return false;
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                  e.printStackTrace();
                }
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }
    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }
}