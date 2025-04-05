package com.example.coolweather.android;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.R;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.MyDatabaseHelper;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.MyApplication;
import com.example.coolweather.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    // 数据列表
    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<County> countyList = new ArrayList<>();

    // 当前选中项
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListeners();
        queryProvinces();
    }

    private void setupListeners() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provinceList.get(position);
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList.get(position);
                queryCounties();
            } else if (currentLevel==LEVEL_COUNTY) {
                String weatherId=countyList.get(position).getWeatherId();
                if(getActivity() instanceof MainActivity){
                   Intent intent=new Intent(getActivity(),WeatherActivity.class);
                   intent.putExtra("weather_id",weatherId);
                   startActivity(intent);
                   getActivity().finish();
                }else if(getActivity() instanceof WeatherActivity){
                    WeatherActivity activity=(WeatherActivity) getActivity();
                    activity.drawerLayout.closeDrawers();
                    activity.swipeRefresh.setRefreshing(true);
                    activity.requestWeather(weatherId);
                }

            }
        });

        backButton.setOnClickListener(v -> {
            if (currentLevel == LEVEL_COUNTY) {
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces();
            }
        });
    }

    /**
     * 查询省份数据
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList.clear();

        try (MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext());
             SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query("Province", null, null, null, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                dataList.clear();
                do {
                    Province province = new Province();
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("provinceName"));
                    @SuppressLint("Range") int code = cursor.getInt(cursor.getColumnIndex("provinceCode"));

                    province.setId(id);
                    province.setProvinceName(name);
                    province.setProvinceCode(code);

                    dataList.add(name);
                    provinceList.add(province);
                } while (cursor.moveToNext());

                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_PROVINCE;
            } else {
                queryFromServer("http://guolin.tech/api/china", "province");
            }
        } catch (Exception e) {
            e.printStackTrace();
            queryFromServer("http://guolin.tech/api/china", "province");
        }
    }

    /**
     * 查询城市数据
     */
    private void queryCities() {
        if (selectedProvince == null) return;

        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList.clear();

        try (MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext());
             SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query("City", null, "provinceId=?",
                     new String[]{String.valueOf(selectedProvince.getId())}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                dataList.clear();
                do {
                    City city = new City();
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("cityName"));
                    @SuppressLint("Range") int code = cursor.getInt(cursor.getColumnIndex("cityCode"));

                    city.setId(id);
                    city.setCityName(name);
                    city.setCityCode(code);
                    city.setProvinceId(selectedProvince.getId());

                    dataList.add(name);
                    cityList.add(city);
                } while (cursor.moveToNext());

                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_CITY;
            } else {
                String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
                queryFromServer(url, "city");
            }
        } catch (Exception e) {
           e.printStackTrace();
            String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
            queryFromServer(url, "city");
        }
    }

    /**
     * 查询区县数据
     */
    private void queryCounties() {
        if (selectedCity == null || selectedProvince == null) return;

        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList.clear();

        try (MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext());
             SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query("County", null, "cityId=?",
                     new String[]{String.valueOf(selectedCity.getId())}, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                dataList.clear();
                do {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("countyName"));
                    @SuppressLint("Range") String weatherId = cursor.getString(cursor.getColumnIndex("weatherId"));

                    County county = new County();
                    county.setCountyName(name);
                    county.setWeatherId(weatherId);
                    county.setCityId(selectedCity.getId());

                    dataList.add(name);
                    countyList.add(county);
                } while (cursor.moveToNext());

                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_COUNTY;
            } else {
                String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode()
                        + "/" + selectedCity.getCityCode();
                queryFromServer(url, "county");
            }
        } catch (Exception e) {
            Log.e(TAG, "Database error", e);
            String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode()
                    + "/" + selectedCity.getCityCode();
            queryFromServer(url, "county");
        }
    }

    /**
     * 从服务器查询数据
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    Toast.makeText(getContext(), "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;

                try {
                    if ("province".equals(type)) {
                        result = Utility.handleProvinceResponse(responseText);
                    } else if ("city".equals(type)) {
                        result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                    } else if ("county".equals(type)) {
                        result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final boolean finalResult = result;
                requireActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    if (finalResult) {
                        switch (type) {
                            case "province":
                                queryProvinces();
                                break;
                            case "city":
                                queryCities();
                                break;
                            case "county":
                                queryCounties();
                                break;
                        }
                    } else {
                        Toast.makeText(getContext(), "数据解析失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(requireActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeProgressDialog();
    }
}