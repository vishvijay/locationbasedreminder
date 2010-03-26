package tol.misa.reminder;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Reminder extends MapActivity {
	
	
	LinearLayout linearLayout;
	MapView mapView;
	ZoomControls mZoom;
	
	List<Overlay> mapOverlays;
	Drawable drawable;
	ItemizedOverlay itemizedOverlay;
	
	private static final int DIALOG_ADD = 0;
	private static final int DIALOG_ADD_OK = 1;

	private LocationManager locationmanager;
	private LocationListener locationListener;

	private static final String TAG = "Reminder"; 


	private ReminderDatabase mDbHelper;
	
	protected Dialog onCreateDialog(int id){

		//final AlertDialog addDialog;
		switch(id) {
		case DIALOG_ADD:
			LayoutInflater factory = LayoutInflater.from(this);
			final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
			AlertDialog.Builder addDialog= new AlertDialog.Builder(Reminder.this);
			addDialog.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.alert_dialog_text_entry)
			.setView(textEntryView)
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					/* User clicked OK */
					Double lat;
					Double lon;
					String desc;

					removeDialog(DIALOG_ADD);
					Location loc = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					EditText myText = (EditText) textEntryView.findViewById(R.id.description_edit);
					desc = myText.getText().toString();
					Log.v(TAG, "Text from box: " + desc);

					lat = loc.getLatitude() * 1e6;
					lon = loc.getLongitude() * 1e6;
					Log.v(TAG, "Lat and long: " + lat + lon);
					long newID;
					newID = mDbHelper.createReminder(desc, lat.intValue(), lon.intValue());
					if(newID>-1){
						Log.v(TAG, "New reminder: " + newID);
						showDialog(DIALOG_ADD_OK);
					}

					populate();
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					/* User clicked cancel */
				}
			});
			return addDialog.create();            	
		case DIALOG_ADD_OK:
			return new AlertDialog.Builder(Reminder.this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.alert_dialog_add_ok)
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					/* User clicked OK so do some stuff */
				}
			})
			.create();
		}
		return null;
	}

	/**
	 * Invoked during init to give the Activity a chance to set up its Menu.
	 * 
	 * @param menu the Menu to which entries may be added
	 * @return true
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, 0, 0, R.string.menu_add);

		return true;
	}
	
	/**
	 * Invoked when the user selects an item from the Menu.
	 * 
	 * @param item the Menu entry which was selected
	 * @return true if the Menu item was legit (and we consumed it), false
	 *         otherwise
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			showDialog(DIALOG_ADD);
			return true;
		}

		return false;
	}
	
	public void populate(){
		
		Cursor cLocations = mDbHelper.fetchAllReminders();
        startManagingCursor(cLocations);
        
		int cCount=cLocations.getCount();
        cLocations.moveToFirst();
        while(cCount>0){
        	Log.v(TAG, "Cursor counter: " + cCount);
        	int lat = cLocations.getInt(2);
        	int lon = cLocations.getInt(3);
        	//lat = lat * 1e6;
        	Log.v(TAG, "Latitude : " + lat);
        	GeoPoint point = new GeoPoint(lat,lon);
        	OverlayItem overlayitem = new OverlayItem(point, "", "");
        	itemizedOverlay.addOverlay(overlayitem);
        	mapOverlays.add(itemizedOverlay);
        	cLocations.moveToNext();
        	cCount--;        	
        }
        cLocations.close();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //---use the LocationManager class to obtain GPS locations---
        locationmanager = (LocationManager) 
        getSystemService(Context.LOCATION_SERVICE);    

        locationListener = new MyLocationListener();

        locationmanager.requestLocationUpdates(
        		LocationManager.GPS_PROVIDER, 
        		0, 
        		0, 
        		locationListener);
        
      //Instantiate database
		mDbHelper = new ReminderDatabase(this);
		mDbHelper.open();

        linearLayout = (LinearLayout) findViewById(R.id.zoomview);
        mapView = (MapView) findViewById(R.id.mapview);
        mZoom = (ZoomControls) mapView.getZoomControls();

        linearLayout.addView(mZoom);
        
        mapOverlays = mapView.getOverlays();
        drawable = this.getResources().getDrawable(R.drawable.blu_stars);
        itemizedOverlay = new ItemizedOverlay(drawable);
        
        populate();            
        
        
    }
    
    @Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

    private class MyLocationListener implements LocationListener 
    {
    	@Override
    	public void onLocationChanged(Location loc) {
    		if (loc != null) {
    			//               Toast.makeText(getBaseContext(), 
    			//                  "Location changed : Lat: " + loc.getLatitude() + 
    			//                   " Lng: " + loc.getLongitude(), 
    			//                    Toast.LENGTH_SHORT).show();
    		}
    	}


    	


    	@Override
    	public void onProviderDisabled(String provider) {
    		// TODO Auto-generated method stub

    	}


    	@Override
    	public void onProviderEnabled(String provider) {
    		// TODO Auto-generated method stub

    	}


    	@Override
    	public void onStatusChanged(String provider, int status, Bundle extras) {
    		// TODO Auto-generated method stub

    	}
    }
    
}