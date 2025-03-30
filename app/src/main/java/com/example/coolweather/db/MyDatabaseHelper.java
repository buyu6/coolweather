package com.example.coolweather.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_PROVINCE="create table Province("
            +"id integer primary key autoincrement, "
            +"provinceName text, "
            +"provinceCode integer)";
    public static final String CREATE_CITY="create table City("
            +"id integer primary key autoincrement, "
            +"cityName text, "
            +"cityCode integer, "
            +"provinceId integer)";
    public static final String CREATE_COUNTY="create table County("
            +"id integer primary key autoincrement, "
            +"countyName text, "
            +"weatherId text, "
            +"cityId integer)";

private Context mContext;
    public MyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext=context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_COUNTY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
