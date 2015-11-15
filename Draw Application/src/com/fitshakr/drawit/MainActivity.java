package com.fitshakr.drawit;


import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.fitshakr.drawit.R;
import com.google.ads.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

//TODO
//Fix alerdialog issue. Show user text number. and save from last.
//Write description[Done]
//Fix eraser icon?
//copy this project into Paid workspace and remove ads
//Design Phase- UI and LOGO[Done]
//Add Google Analytics[done]
//implement "Ask user to save before exiting application"[Done]
//Saving State
//Undo Button


public class MainActivity extends Activity{
	private static final String TAG = "BU/" + MainActivity.class.getName();
	//for ads
	private AdView adView;
	 
	//custom drawing view
	private DrawingView drawView;
	//buttons
	private ImageButton currPaint;
	//sizes
	private float smallBrush, mediumBrush, largeBrush;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 //Load whatever we have saved.
		//adview
		adView = new AdView(this, AdSize.BANNER, "ca-app-pub-1917391693301853/9466326886");
		LinearLayout layout = (LinearLayout)findViewById(R.id.adsLayout);
		layout.addView(adView); //add adView to layout
		adView.loadAd(new AdRequest()); //instantiate request with ad

		//get drawing view
		drawView = (DrawingView)findViewById(R.id.drawing);

		//get the palette and first color button
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

		//sizes from dimensions
		smallBrush = getResources().getInteger(R.integer.small_size);
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		largeBrush = getResources().getInteger(R.integer.large_size);
		drawView.setBrushSize(smallBrush); //init value, brush starts off small.
		//time how long it takes user to load app

		
		

	}
	public void saveThisDrawing()
	{
		String path = Environment.getExternalStorageDirectory().toString();
		path = path + "/Finger Draw/";
		File dir = new File(path);

		if (!dir.isDirectory()) {
			dir.mkdirs();
		}

		//save drawing
		drawView.setDrawingCacheEnabled(true);
		//attempt to save
		String imgSaved = MediaStore.Images.Media.insertImage(
				getContentResolver(), drawView.getDrawingCache(),
				UUID.randomUUID().toString().substring(0, 11)+".png", "a drawing");
		//feedback
		//UUID.randomUUID()

		try {
			drawView.setDrawingCacheEnabled(true);
			File file = new File(dir, "Drawing"+UUID.randomUUID().toString().substring(0, 11)+".png");
			FileOutputStream fOut = new FileOutputStream(file);
			Bitmap bm =  drawView.getDrawingCache(); //current Bitmap
			bm.compress(Bitmap.CompressFormat.PNG, 90, fOut);

			//fOut.flush();
			fOut.close();
			showSavedInGallery(file);
			//add to Notification Tray
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW); //we intend to view
			intent.setDataAndType(Uri.fromFile(file), "image/*"); //get full path
			//
			PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
			//Add screenshotted Bitmap to Notification Bar
			NotificationManager notifyManager = 
					(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(MainActivity.this);
			mBuilder.setLargeIcon(bm);
			mBuilder.setSmallIcon(R.drawable.ic_launcher);
			mBuilder.setContentText("Tap to open");
			mBuilder.setContentIntent(pIntent); //Onl
			//mBuilder.addAction(R.drawable.ic_launcher, "", pIntent);
			notifyManager.notify(1, mBuilder.build());

			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			Toast unsavedToast = Toast.makeText(getApplicationContext(), 
					"Oops! Image could not be saved and shared.", Toast.LENGTH_SHORT);
			unsavedToast.show();
			e.printStackTrace();
		}



		if(imgSaved!=null){
			Toast savedToast = Toast.makeText(getApplicationContext(), 
					"Drawing saved to Gallery!", Toast.LENGTH_SHORT);
			savedToast.show();
		}

		drawView.destroyDrawingCache();
}
	//On Methods 
		public void onLoad(long loadTime) {

		  // May return null if EasyTracker has not been initialized with a property
		  // ID.
		  EasyTracker easyTracker = EasyTracker.getInstance(this);

		  easyTracker.send(MapBuilder
		      .createTiming("resources",    // Timing category (required)
		                    loadTime,       // Timing interval in milliseconds (required)
		                    "Load Time for Main",  // Timing name
		                    null)           // Timing label
		      .build()
		  );
		}
		
	  @Override
	  public void onStart() {
	    super.onStart();
	    
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }
	  
	@Override
	  public void onDestroy() {
	    if (adView != null) {
	      adView.destroy();
	    }
	    super.onDestroy();
	  }
	
	  @Override
	  public void onStop() {
	    super.onStop();
	    
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }
	  
	@Override
		public void onBackPressed(){
		//when user hits back button, ask them if they want to save
		
		
		AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
		saveDialog.setTitle("Exit and Save");
		saveDialog.setMessage("Would you like to save your Drawing before leaving?");
		saveDialog.setPositiveButton("Yes", new	 DialogInterface.OnClickListener() {
			
			//Listen for the Yes Click
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//Create an Album
				saveThisDrawing();
				MainActivity.super.onBackPressed();
				
				
				
			}
			
		});
		saveDialog.setNegativeButton("No", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
				MainActivity.super.onBackPressed();
			}
		});
		saveDialog.show();
		
		
	}
   

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_new:
	        	//drawView.startNew();
	        	refreshDrawing();
	            return true;
	        case R.id.action_brush:
	        	//selectBrush();
	        	selectBrushChooser();
	        	return true;
	        case R.id.action_save:
	        	saveDrawing();
	        	return true;
	        case R.id.action_erase:
	        	eraseChooser();
	        	return true;
	        case R.id.action_share:
	        	shareDrawing();
	        	return true;
	        case R.id.action_selectcolor:
	        	selectColorChooser();
	        	return true;
	        case R.id.action_removeAds:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + getPackageName() + ".pro"));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "No Google.Play installed :(");
                }
                return true;
	        	
	        	 
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	////////////////////////////
	public void shareDrawing(){
		//Take snapshot and share PNG
		//save drawing attach to Notification Bar and let User Open Image to share.
				 //I need this for later lol
				
				AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
				saveDialog.setTitle("Save drawing");
				saveDialog.setMessage("Save and Share drawing?");
				saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
					
					public void onClick(DialogInterface dialog, int which){
						//Create an Album
//						saveThisDrawing();
						String path = Environment.getExternalStorageDirectory().toString();
						path = path + "/Finger Draw/";
						File dir = new File(path);

						if (!dir.isDirectory()) {
					        dir.mkdirs();
						}

						//save drawing
						drawView.setDrawingCacheEnabled(true);
						//attempt to save
						String imgSaved = MediaStore.Images.Media.insertImage(
								getContentResolver(), drawView.getDrawingCache(),
								UUID.randomUUID().toString().substring(0, 11)+".png", "a drawing");
						//feedback
						//UUID.randomUUID()
						
						try {
							drawView.setDrawingCacheEnabled(true);
							File file = new File(dir, "Drawing"+UUID.randomUUID().toString().substring(0, 11)+".png");
							FileOutputStream fOut = new FileOutputStream(file);
							Bitmap bm =  drawView.getDrawingCache(); //current Bitmap
							bm.compress(Bitmap.CompressFormat.PNG, 90, fOut);
							 
							//fOut.flush();
							fOut.close();
							showSavedInGallery(file);
							//add to Notification Tray
						    Intent intent = new Intent();
						    intent.setAction(Intent.ACTION_VIEW); //we intend to view
						    intent.setDataAndType(Uri.fromFile(file), "image/*"); //get full path
						    //
						    PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
						    //Add screenshotted Bitmap to Notification Bar
							NotificationManager notifyManager = 
							        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
							NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(MainActivity.this);
							mBuilder.setLargeIcon(bm);
							mBuilder.setSmallIcon(R.drawable.ic_launcher);
							mBuilder.setContentText("Tap to open");
							mBuilder.setContentIntent(pIntent); //Onl
							//mBuilder.addAction(R.drawable.ic_launcher, "", pIntent);
							notifyManager.notify(1, mBuilder.build());
							
							//share here
							Uri bmpUri = Uri.fromFile(file); //we have the path + extension lol
							Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND); //open that dialog
							shareIntent.setType("image/*"); //any image type
							shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
							startActivity(Intent.createChooser(shareIntent, "Share via"));
							
							
							
							
							
							
						} catch (FileNotFoundException e) {
							
							e.printStackTrace();
						} catch (IOException e) {
							Toast unsavedToast = Toast.makeText(getApplicationContext(), 
									"Oops! Image could not be saved and shared.", Toast.LENGTH_SHORT);
							unsavedToast.show();
							e.printStackTrace();
						}
						

						
						if(imgSaved!=null){
							Toast savedToast = Toast.makeText(getApplicationContext(), 
									"Drawing saved to Gallery!", Toast.LENGTH_SHORT);
							savedToast.show();
						}
					
						drawView.destroyDrawingCache();
					}
				});
				saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which){
						dialog.cancel();
					}
				});
				saveDialog.show();
				//picture is saved. Lets Share it
				EasyTracker easyTracker = EasyTracker.getInstance(this);

				  // MapBuilder.createEvent().build() returns a Map of event fields and values
				  // that are set and sent with the hit.
				  easyTracker.send(MapBuilder
				      .createEvent("ui_action_share",     // Event category (required)
				                   "button_press_share",  // Event action (required)
				                   "share_button",   // Event label
				                   null)            // Event value
				      .build()
				  );
			
				
	}
	//Dialog function when Paint Icon is clicked
	public void selectColorChooser(){
		//Open dialog box with all colors in paletLayout
		AlertDialog.Builder brushDialog = new AlertDialog.Builder(MainActivity.this);
		LayoutInflater inflater = this.getLayoutInflater();
		View v = inflater.inflate(R.layout.color_palet_layout, null);
		brushDialog.setView(v);
		brushDialog.setTitle("Select Color");
		brushDialog.setPositiveButton("OK", null);
		brushDialog.create();
		brushDialog.show();

	}
	//user clicked paint
	public void paintClicked(View view){
		//use chosen color

		//set erase false
		drawView.setErase(false);
		drawView.setBrushSize(drawView.getLastBrushSize());

		if(view!=currPaint){
			ImageButton imgView = (ImageButton)view;
			String color = view.getTag().toString();
			drawView.setColor(color);
			//update ui
			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint=(ImageButton)view;
		}
		
	}
	/**
	 * Function to refresh drawing view.
	 * Move to Utils File soon.
	 * */
	public void refreshDrawing(){
		AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
		newDialog.setTitle("New drawing");
		newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
		newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				drawView.startNew();
				dialog.dismiss();
			}
		});
		newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		newDialog.show();
	}
	/**
	 * Take File parameter and rescans system to make sure it is available to show in Gallery
	 * @param file The file to scan for.
	 */
	public void showSavedInGallery(File file){
		MediaScannerConnection.scanFile(this, new String[] {file.toString()}, null, 
				new MediaScannerConnection.OnScanCompletedListener() {
					
					@Override
					public void onScanCompleted(String path, Uri uri) {
						//Log.e("ExternalStorage", "Path: " + path);
						//Log.e("ExternalStorage", "URI:" + uri);
						
						
					}
				});
	
	}
	public void selectBrushChooser(){
		//create seekbar
		
		//EasyTracker easyTracker = EasyTracker.getInstance(this);

		AlertDialog.Builder brushDialog = new AlertDialog.Builder(MainActivity.this);
		LayoutInflater inflater = this.getLayoutInflater();
		View v = inflater.inflate(R.layout.brush_seek_chooser, null);
		brushDialog.setView(v);
		brushDialog.setTitle("Slide to Change Brush Size");
		brushDialog.setPositiveButton("OK", null);
		//brushDialog.setContentView(R.layout.brush_seek_chooser);
		SeekBar seekBar =(SeekBar)v.findViewById(R.id.selectBrush);
		//TextView text = (TextView)findViewById(R.id.max_Value);
		seekBar.setMax(100);
		seekBar.setProgress(0); //start at 0
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

		        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		            //progress = progress + 10; // Add the minimum value (10)
		        	
		            //text.setText(String.valueOf(1)); //throwing null Exception
		            drawView.setErase(false);
		            drawView.setBrushSize((float)progress);
		            drawView.setLastBrushSize((float)progress);
		        }

		        @Override
		        public void onStartTrackingTouch(SeekBar seekBar) {
		        	//TODO:When user taps on screen do
		        }

		        @Override
		        public void onStopTrackingTouch(SeekBar seekBar) {}

		    });
		 brushDialog.create();
		 brushDialog.show();

		
	}
	//select a brush
	public void selectBrush(){
		//TODO: http://stackoverflow.com/questions/9792888/android-seekbar-set-progress-value
		//Allow User to selec brush size(in dp). Use Picker class and pass values chosesn by user
		//back into setBrushSize() function
		//FLow: OnClick open seekBar and do some funky stuff!
		final Dialog brushDialog = new Dialog(this);
		brushDialog.setTitle("Brush size:");
		brushDialog.setContentView(R.layout.brush_chooser);
		//listen for clicks on size buttons
		ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
		smallBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				drawView.setErase(false);
				drawView.setBrushSize(smallBrush);
				drawView.setLastBrushSize(smallBrush);
				brushDialog.dismiss();
			}
		});
		ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
		mediumBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				drawView.setErase(false);
				drawView.setBrushSize(mediumBrush);
				drawView.setLastBrushSize(mediumBrush);
				brushDialog.dismiss();
			}
		});
		ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
		largeBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				drawView.setErase(false);
				drawView.setBrushSize(largeBrush);
				drawView.setLastBrushSize(largeBrush);
				brushDialog.dismiss();
			}
		});
		//show and wait for user interaction
		brushDialog.show();
	}
	
	public void saveDrawing(){
		//save drawing attach to Notification Bar and let User Open Image to share.
		AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
		saveDialog.setTitle("Save drawing");
		saveDialog.setMessage("Save drawing to device Gallery?");
		saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				saveThisDrawing();
			}
		});
		saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		saveDialog.show();
		
		EasyTracker easyTracker = EasyTracker.getInstance(this);

		  // MapBuilder.createEvent().build() returns a Map of event fields and values
		  // that are set and sent with the hit.
		  easyTracker.send(MapBuilder
		      .createEvent("ui_action_save",     // Event category (required)
		                   "button_press_save",  // Event action (required)
		                   "save_button",   // Event label
		                   null)            // Event value
		      .build()
		  );
		
	}
	//erase
	public void eraseChooser(){
		AlertDialog.Builder brushDialog = new AlertDialog.Builder(MainActivity.this);
		LayoutInflater inflater = this.getLayoutInflater();
		View v = inflater.inflate(R.layout.brush_seek_chooser, null);
		brushDialog.setView(v);
		brushDialog.setTitle("Slide to Change Eraser Size");
		brushDialog.setPositiveButton("OK", null);
		//brushDialog.setContentView(R.layout.brush_seek_chooser);
		SeekBar seekBar =(SeekBar)v.findViewById(R.id.selectBrush);
		//TextView text = (TextView)findViewById(R.id.max_Value);
		seekBar.setMax(100);
		seekBar.setProgress(0); //start at 0
		drawView.setErase(true);
        drawView.setBrushSize(5);
        drawView.setLastBrushSize(5);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

		        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		            //progress = progress + 10; // Add the minimum value (10)
		        	
		            //text.setText(String.valueOf(1)); //throwing null Exception
		            drawView.setErase(true);
		            drawView.setBrushSize((float)progress);
		            drawView.setLastBrushSize((float)progress);
		        }

		        @Override
		        public void onStartTrackingTouch(SeekBar seekBar) {
		        	//TODO:When user taps on screen do
		        }

		        @Override
		        public void onStopTrackingTouch(SeekBar seekBar) {}

		    });
		 brushDialog.create();
		 brushDialog.show();

		
	}
	
	public void erase(){
		//switch to erase - choose size
		//LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		LinearLayout paletLayout = (LinearLayout)findViewById(R.id.paletLayout);
		//currPaint = (ImageButton)paintLayout.getChildAt(10); //get the white color
		paletLayout.setClickable(false); //cant be clicked so we dont get null exception
		//drawView.setColor("#FFFFFFFF");
		final Dialog brushDialog = new Dialog(this);
		brushDialog.setTitle("Eraser size:");
		brushDialog.setContentView(R.layout.brush_chooser);
		//size buttons
		ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
		smallBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				drawView.setErase(true);
				drawView.setBrushSize(smallBrush);
				
				brushDialog.dismiss();
			}
		});
		ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
		mediumBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				drawView.setErase(true);
				drawView.setBrushSize(mediumBrush);
				
				
				brushDialog.dismiss();
			}
		});
		ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
		largeBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				drawView.setErase(true);
				drawView.setBrushSize(largeBrush);
				
				brushDialog.dismiss();
			}
		});
		brushDialog.show();
		
	}

}