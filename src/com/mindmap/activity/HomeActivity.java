package com.mindmap.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeActivity extends Activity {

	Button button;
	Button browsebutton;
	
	public static List<String> item = null;
	public static List<String> filenameList = null;
	private List<String> path = null;
	private String root = "/data/data/com.mindmap.activity/files";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        addListenerOnButton(); 
        getfilelist(root);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
   
    public void addListenerOnButton() {
    	 
		final Context context = this;
 
		button = (Button) findViewById(R.id.button1);
 
		button.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
 
			    Intent intent = new Intent(context, WorkspaceActivity.class);
                            startActivity(intent); 
			}
		});
		
		browsebutton = (Button) findViewById(R.id.button2);
		 
		browsebutton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
 
			    Intent intent = new Intent(context, FileBrowser.class);
                            startActivity(intent); 
			}
		});
	}
    
    public void getfilelist(String dirPath) {
    	item = new ArrayList<String>();
    	path = new ArrayList<String>();
    	filenameList = new ArrayList<String>();
    	File f = new File(dirPath);
    	File[] files = f.listFiles();
    	if (!dirPath.equals(root)) {
    		item.add(root);
    		path.add(root);
    		item.add("../");
    		path.add(f.getParent());
    	}
    	if(files!=null)
    	{
    		for (int i = 0; i < files.length; i++) {
				File file = files[i];
				path.add(file.getPath());
				if (file.isDirectory())
					item.add(file.getName() + "/");
				else {
					int length = file.getName().length();
					String filename = (file.getName()).substring(0, length - 3);
					System.out.println("file array is :" + filename);
					item.add(filename);
					filenameList.add(filename);
				}

			}
    	}
    	
    }
    
    
    
}
