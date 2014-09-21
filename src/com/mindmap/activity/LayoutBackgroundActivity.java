package com.mindmap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

public class LayoutBackgroundActivity extends Activity {
	
	@Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.layout_background_option);
       
   }
	String tag;
	public void onBGSelected(View v){
		 tag = (String) v.getTag();
		System.out.println("bg selected "+tag);
		
		Intent returnIntent = new Intent();
		 returnIntent.putExtra("result",tag);
		 setResult(RESULT_OK,returnIntent);     
		 finish();
	}
	@Override
	public void finish() {
	  // Prepare data intent 
		System.out.println("bg selected "+tag);
		Intent returnIntent = new Intent();
		 returnIntent.putExtra("result",tag);
		 setResult(RESULT_OK,returnIntent); 
	  super.finish();
	} 
}
