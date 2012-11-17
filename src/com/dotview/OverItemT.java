package com.dotview;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.amap.mapapi.core.GeoPoint;
import com.amap.mapapi.core.OverlayItem;
import com.amap.mapapi.map.ItemizedOverlay;
import com.amap.mapapi.map.MapView;
import com.amap.mapapi.map.Projection;

public class OverItemT extends ItemizedOverlay<OverlayItem> {
	private List<OverlayItem> GeoList = new ArrayList<OverlayItem>();
	private Drawable marker;
	private Context mContext;

	private double mLat1 = 39.9022; // point1γ��
	private double mLon1 = 116.3922; // point1����

	private double mLat2 = 39.607723;
	private double mLon2 = 116.397741;

	private double mLat3 = 39.917723;
	private double mLon3 = 116.6552;

	public OverItemT(Drawable marker, Context context) {
		super(boundCenterBottom(marker));
        this.marker = marker;
		this.mContext = context;
        // �ø����ľ�γ�ȹ���GeoPoint����λ��΢�� (�� * 1E6)
		GeoPoint p1 = new GeoPoint((int) (mLat1 * 1E6), (int) (mLon1 * 1E6));
		GeoPoint p2 = new GeoPoint((int) (mLat2 * 1E6), (int) (mLon2 * 1E6));
		GeoPoint p3 = new GeoPoint((int) (mLat3 * 1E6), (int) (mLon3 * 1E6));
        // ����OverlayItem��������������Ϊ��item��λ�ã������ı�������Ƭ��
		GeoList.add(new OverlayItem(p1, "P1", "point1"));
		GeoList.add(new OverlayItem(p2, "P2", "point2"));
		GeoList.add(new OverlayItem(p3, "P3", "point3"));		
		populate();  //createItem(int)��������item��һ���������ݣ��ڵ�����������ǰ�����ȵ����������
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        // Projection�ӿ�������Ļ���ص�����ϵͳ�͵�����澭γ�ȵ�����ϵͳ֮��ı任
		Projection projection = mapView.getProjection(); 
		for (int index = size() - 1; index >= 0; index--) { // ����GeoList
			OverlayItem overLayItem = getItem(index); // �õ�����������item
            String title = overLayItem.getTitle();
			// �Ѿ�γ�ȱ任�������MapView���Ͻǵ���Ļ��������
			Point point = projection.toPixels(overLayItem.getPoint(), null); 
			// ���ڴ˴�������Ļ��ƴ���
			Paint paintText = new Paint();
			paintText.setColor(Color.BLACK);
			paintText.setTextSize(15);
			canvas.drawText(title, point.x-30, point.y - 25, paintText); // �����ı�
		}
		drawPolygon(canvas,mapView.getProjection());
        super.draw(canvas, mapView, shadow);
		//����һ��drawable�߽磬ʹ�ã�0��0�������drawable�ײ����һ�����ĵ�һ������
		boundCenterBottom(marker);
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return GeoList.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return GeoList.size();
	}

	@Override
	// ��������¼�
	protected boolean onTap(int i) {
		setFocus(GeoList.get(i));
		Toast.makeText(this.mContext, GeoList.get(i).getSnippet(),
				Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public boolean onTap(GeoPoint point, MapView mapView) {
		// TODO Auto-generated method stub
		return super.onTap(point, mapView);
	}
	/** 
	 * ���ƶ����
	 * @param canvas
	 * @param projection
	 */
	private void drawPolygon(Canvas canvas, Projection projection) {
		
		 Paint p = new Paint();  
	     p.setColor(Color.RED);//���ñ���Ϊ��ɫ
	     Path path = new Path();
	    for(int index=0; index < size(); index++) {//����GeoList
			OverlayItem overLayItem = getItem(index); // �õ�����������item
			// �Ѿ�γ�ȱ任�������MapView���Ͻǵ���Ļ��������
			Point point = projection.toPixels(overLayItem.getPoint(), null); 
			
			 if(index==0) {
				 path.moveTo(point.x, point.y);// �˵�Ϊ����ε����  
			 } else {
				 path.lineTo(point.x, point.y);
			 }
		}
		path.close(); // ʹ��Щ�㹹�ɷ�յĶ����  
        canvas.drawPath(path, p); 
	}
}