package com.example.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    // 数据库名称和版本
    private static final String DATABASE_NAME = "CoolWeather.db";
    private static final int DATABASE_VERSION = 2; // 版本号升级（新增外键后需递增）

    // 表名常量
    public static final String TABLE_PROVINCE = "Province";
    public static final String TABLE_CITY = "City";
    public static final String TABLE_COUNTY = "County";

    // Province 表结构
    public static final String CREATE_PROVINCE = "CREATE TABLE " + TABLE_PROVINCE + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "provinceName TEXT NOT NULL, "  // 非空约束
            + "provinceCode INTEGER UNIQUE)"; // 唯一约束

    // City 表结构（添加外键关联 Province）
    public static final String CREATE_CITY = "CREATE TABLE " + TABLE_CITY + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "cityName TEXT NOT NULL, "
            + "cityCode INTEGER UNIQUE, "
            + "provinceId INTEGER NOT NULL, "
            + "FOREIGN KEY (provinceId) REFERENCES " + TABLE_PROVINCE + "(id) ON DELETE CASCADE)";

    // County 表结构（添加外键关联 City）
    public static final String CREATE_COUNTY = "CREATE TABLE " + TABLE_COUNTY + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "countyName TEXT NOT NULL, "
            + "weatherId TEXT UNIQUE, "  // 天气ID唯一
            + "cityId INTEGER NOT NULL, "
            + "FOREIGN KEY (cityId) REFERENCES " + TABLE_CITY + "(id) ON DELETE CASCADE)";

    // 索引优化（加速省、市、县的查询）
    private static final String CREATE_INDEX_PROVINCE_CODE =
            "CREATE INDEX idx_province_code ON " + TABLE_PROVINCE + "(provinceCode)";
    private static final String CREATE_INDEX_CITY_CODE =
            "CREATE INDEX idx_city_code ON " + TABLE_CITY + "(cityCode)";
    private static final String CREATE_INDEX_COUNTY_WEATHER_ID =
            "CREATE INDEX idx_county_weather_id ON " + TABLE_COUNTY + "(weatherId)";

    private Context mContext;

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 启用外键支持（默认关闭）
        db.execSQL("PRAGMA foreign_keys = ON");

        // 创建表
        db.execSQL(CREATE_PROVINCE);
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_COUNTY);

        // 创建索引
        db.execSQL(CREATE_INDEX_PROVINCE_CODE);
        db.execSQL(CREATE_INDEX_CITY_CODE);
        db.execSQL(CREATE_INDEX_COUNTY_WEATHER_ID);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 安全升级：保留数据（实际项目需更复杂的迁移逻辑）
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROVINCE);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // 强制启用外键（兼容某些旧设备）
        db.setForeignKeyConstraintsEnabled(true);
    }
}