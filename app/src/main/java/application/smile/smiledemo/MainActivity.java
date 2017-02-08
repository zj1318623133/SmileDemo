package application.smile.smiledemo;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    MapView mMapView = null;
    private BaiduMap mMapViewMap;
    private EditText et_city;
    private EditText et_thing;
    private PoiSearch poiSearch;
    private OnGetPoiSearchResultListener getPoiSearchResultListener;
    private LatLng point;
    private GeoCoder geoCoder;
    private LatLng location;
    private EditText et_address;
    private String city;
    private String address;
    private String thing;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数

        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        //获取地图
        mMapViewMap = mMapView.getMap();
        //开启交通图
        mMapViewMap.setTrafficEnabled(true);
        initSearch();
        initSearchOne();
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //普通地图
            case R.id.normal_view:
                BaiduMap map = mMapView.getMap();
                map.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                map.setBaiduHeatMapEnabled(false);
                break;
            //卫星地图
            case R.id.satellite_view:
                map = mMapView.getMap();
                map.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                map.setBaiduHeatMapEnabled(false);
                break;
            //热力地图
            case R.id.heat_show:
                map = mMapView.getMap();
                map.setBaiduHeatMapEnabled(true);
                break;
            //定位
            case R.id.sou_show:
                //定义Maker坐标点
                point = new LatLng(42, 122.83);
                //构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.mipmap.qq_leba_list_seek_neighbour);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap);
                //在地图上添加Marker，并显示
                mMapViewMap.addOverlay(option);
                //设置地图的缩放级别和中心点
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(point, 15);
                //以动画方式更新地图状态，动画耗时300ms
                mMapViewMap.animateMapStatus(mapStatusUpdate);
                break;
            //精准搜索
            case R.id.find_show:
                createDialogFind1();
                break;
            //周边搜索
            case R.id.find2_show:
                createDialogFind2();
                break;
            case R.id.sou_myself_show:
                souMyself();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void souMyself() {
        initLocation();
        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    //精准查找
    private void createDialogFind1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.qq_leba_list_seek_neighbour);
        builder.setTitle("精准查找");
        View view = getLayoutInflater().inflate(R.layout.dialog_item_layout, null);
        et_city = (EditText) view.findViewById(R.id.et_city);
        et_thing = (EditText) view.findViewById(R.id.et_thing);
        builder.setView(view);
        builder.setPositiveButton("查找", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMapViewMap.clear();
                String city = et_city.getText().toString().trim();
                String thing = et_thing.getText().toString().trim();
                if (city.equals("") || city == null) {
                    Toast.makeText(getApplicationContext(), "城市不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (thing.equals("") || thing == null) {
                        Toast.makeText(getApplicationContext(), "内容不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        poiSearch.searchInCity(new PoiCitySearchOption().city(city).keyword(thing).pageNum(1));
                    }
                }
            }
        });

        builder.show();
    }

    //周边查找（使用地理编码）
    private void createDialogFind2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.qq_leba_list_seek_neighbour);
        builder.setTitle("周边查找");
        View view = getLayoutInflater().inflate(R.layout.dialog_item_layout_one, null);
        et_city = (EditText) view.findViewById(R.id.et_city);
        et_address = (EditText) view.findViewById(R.id.et_address);
        et_thing = (EditText) view.findViewById(R.id.et_thing);
        builder.setView(view);
        builder.setPositiveButton("查找", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMapViewMap.clear();
                city = et_city.getText().toString().trim();
                address = et_address.getText().toString().trim();
                thing = et_thing.getText().toString().trim();
                if (city.equals("") || city == null) {
                    Toast.makeText(getApplicationContext(), "城市不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (address.equals("") || address == null) {
                        Toast.makeText(getApplicationContext(), "城市不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        if (thing.equals("") || thing == null) {
                            Toast.makeText(getApplicationContext(), "内容不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            geoCoder.geocode(new GeoCodeOption()
                                    .city(city)
                                    .address(address));
                        }
                    }
                }
            }
        });
        builder.show();
    }

    //精确检索（输入类型检索）
    private void initSearch() {
        poiSearch = PoiSearch.newInstance();
        getPoiSearchResultListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult == null) {
                    Toast.makeText(MainActivity.this, "未查询到响应结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                //获取POI检索结果
                List<PoiInfo> allAddr = poiResult.getAllPoi();
                for (PoiInfo info : allAddr) {
                    System.out.println(info.city + "--" + info.name + "--" + info.address + "--" + info.phoneNum);
                    //把检索出来的地点在地图上显示标注
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.qq_leba_list_seek_neighbour);
                    //创建一个标注图层
                    OverlayOptions overlay = new MarkerOptions().icon(bitmap).position(info.location).title(info.name);
                    //把图层显示到地图上
                    mMapViewMap.addOverlay(overlay);
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                //获取Place详情页检索结果
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                // poi 室内检索结果回调
            }
        };
        poiSearch.setOnGetPoiSearchResultListener(getPoiSearchResultListener);
    }

    private void initSearchOne() {
        geoCoder = GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有检索到结果
                    Toast.makeText(getApplicationContext(), "没有查询到结果", Toast.LENGTH_SHORT).show();
                }
                //获取地理编码结果
                location = geoCodeResult.getLocation();
                if (location == null){
                    Toast.makeText(MainActivity.this, "没有找到您想要的结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                poiSearch.searchNearby(new PoiNearbySearchOption()
                        .location(location)
                        .keyword(thing)
                        .pageNum(1)
                        .radius(2000));

                System.out.println(geoCodeResult.getAddress() + "--" + geoCodeResult.getLocation());
                //把检索出来的地点在地图上显示标注
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.qq_leba_list_seek_neighbour);
                //创建一个标注图层
                OverlayOptions overlay = new MarkerOptions().icon(bitmap).position(location).title(geoCodeResult.getAddress());
                //把图层显示到地图上
                mMapViewMap.addOverlay(overlay);
                //设置地图的缩放级别和中心点
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(location, 12);
                //以动画方式更新地图状态，动画耗时300ms
                mMapViewMap.animateMapStatus(mapStatusUpdate);
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有找到检索结果
                }
                //获取反向地理编码结果
            }
        };
        geoCoder.setOnGetGeoCodeResultListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        poiSearch.destroy();
        mMapView.onDestroy();
        geoCoder.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(bdLocation.getTime());
            sb.append("\nerror code : ");
            sb.append(bdLocation.getLocType());
            sb.append("\nlatitude : ");
            sb.append(bdLocation.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(bdLocation.getLongitude());
            sb.append("\nradius : ");
            sb.append(bdLocation.getRadius());
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(bdLocation.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(bdLocation.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(bdLocation.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(bdLocation.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(bdLocation.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                sb.append("\naddr : ");
                sb.append(bdLocation.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(bdLocation.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(bdLocation.getLocationDescribe());// 位置语义化信息
            List<Poi> list = bdLocation.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            double latitude = bdLocation.getLatitude();
            double longitude = bdLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            //把检索出来的地点在地图上显示标注
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.qq_leba_list_seek_neighbour);
            //创建一个标注图层
            OverlayOptions overlay = new MarkerOptions().icon(bitmap).position(latLng);
            //把图层显示到地图上
            mMapViewMap.addOverlay(overlay);
            //设置地图的缩放级别和中心点
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, 20);
            //以动画方式更新地图状态，动画耗时300ms
            mMapViewMap.animateMapStatus(mapStatusUpdate);
            Log.i("BaiduLocationApiDem", sb.toString());
            mLocationClient.stop();
        }
    }
}
