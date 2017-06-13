package com.example.lenovo.enclosure;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Region;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

public class MainActivity extends AppCompatActivity implements PathView.OnFinishListener{

    private PathView pview;
    private MapView bmapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        bmapView = (MapView) findViewById(R.id.bmapView);
        pview = (PathView) findViewById(R.id.pview);
        pview.setOnFinishListener(this);
    }

    @Override
    public void onFinish(Region p) {
//        p.contains(x,y)//闭合区域  判断用是否在区域里面
        pview.setVisibility(View.GONE);
        Point point = new Point(0, 0);
        Point point1 = new Point(pview.getWidth(), pview.getHeight());
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(getViewBitmap(pview));
        LatLng southwest = bmapView.getMap().getProjection().fromScreenLocation(point);
        LatLng northeast = bmapView.getMap().getProjection().fromScreenLocation(point1);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();
        OverlayOptions path = new GroundOverlayOptions().positionFromBounds(bounds).image(bitmapDescriptor).zIndex(9);
        bmapView.getMap().addOverlay(path);
    }

    /**
     * 将View转换为图片
     * @param v
     * @return
     */
    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);
        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e("Folder", "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }
}
