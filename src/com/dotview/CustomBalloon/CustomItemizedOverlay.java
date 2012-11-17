/***
 * Copyright (c) 2011 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.dotview.CustomBalloon;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.amap.mapapi.core.OverlayItem;
import com.amap.mapapi.map.MapView;

public class CustomItemizedOverlay<Item extends OverlayItem> extends BalloonItemizedOverlay<CustomOverlayItem>   {

	private ArrayList<CustomOverlayItem> m_overlays = new ArrayList<CustomOverlayItem>();
	private Context c;
	
	public CustomItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		c = mapView.getContext();
	}

	public void addOverlay(CustomOverlayItem overlay) {
	    m_overlays.add(overlay);
	    populate();
	}
	 
	@Override
	protected CustomOverlayItem createItem(int i) {
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		return m_overlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index, CustomOverlayItem item) {
		 //Toast.makeText(c, "onBalloonTap for overlay index " + item.getTitle(),
		 //		Toast.LENGTH_LONG).show();
		
		 /*new AlertDialog.Builder(c).setTitle(item.getTitle())
		.setMessage(item.getTitle()+"\r\n"+item.getSnippet())
		.setPositiveButton("Close", new DialogInterface.OnClickListener() {
		 
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
		
		
		Intent intent = new Intent(c, ViewReport.class); 
		intent.putExtra("id", item.getID());
		intent.putExtra("type", item.getType());
		intent.putExtra("title", item.getTitle());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		((Activity) c).startActivity(intent); 
		*/
		return true;
	}

	@Override
	protected BalloonOverlayView<CustomOverlayItem> createBalloonOverlayView() {
		// use our custom balloon view with our custom overlay item type:
		return new CustomBalloonOverlayView<CustomOverlayItem>(getMapView().getContext(), getBalloonBottomOffset());
	}

}
