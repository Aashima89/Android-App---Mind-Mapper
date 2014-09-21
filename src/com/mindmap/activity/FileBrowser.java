package com.mindmap.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileBrowser extends ListActivity {

	private List<String> item = null;
	private List<String> path = null;
	private String root = "/data/data/com.mindmap.activity/files";
	//private TextView myPath;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse);
		//myPath = (TextView) findViewById(R.id.path);
		getDir(root);
	}

	private void getDir(String dirPath) {
		//myPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		File f = new File(dirPath);
		File[] files = f.listFiles();
		if (!dirPath.equals(root)) {
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}
		if(files.length!=0){
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			path.add(file.getPath());
			if (file.isDirectory())
				item.add(file.getName() + "/");
			else {
				int length = file.getName().length(); 
				String filename = (file.getName()).substring(0, length-3);
				System.out.println("file array is :" + filename);
				item.add(filename);
			}
				
		}
		final ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
				R.layout.row, item);
		setListAdapter(fileList);
		ListView listView = getListView();
		SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                            	
                            	
                            	
                            	return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, final int[] reverseSortedPositions) {
                            	new AlertDialog.Builder(FileBrowser.this)
                        		.setMessage("Do you want delete the MindMap?")
                        		.setPositiveButton("Yes",
                        				new DialogInterface.OnClickListener() {
                        					public void onClick(DialogInterface arg0, int arg1) {
                        						// save code goes here
                        						for (int position : reverseSortedPositions) {
                        							System.out.println("position on swipe is"+position);
                        							final File file = new File(path.get(position));
                        							HomeActivity.filenameList.remove(fileList.getItem(position));
                        							fileList.remove(fileList.getItem(position));
                        							file.delete();
                        							//System.out.println("file name in browser is : "+fileList.getItem(position));
                        							
                                                }
                                                fileList.notifyDataSetChanged();
                        					}
                        				})
                        		.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        			public void onClick(DialogInterface arg0, int arg1) {
                        				
                        			}
                        		}).show();
                            	
                            }
                        });
        listView.setOnTouchListener(touchListener);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		System.out.println("on click position"+position);
		final File file = new File(path.get(position));
		if (file.isDirectory()) {
			if (file.canRead())
				getDir(path.get(position));
			else {
				new AlertDialog.Builder(this)
						.setTitle(
								"[" + file.getName()
										+ "] folder can't be read!")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										
									}
								}).show();
			}
		} else {
			final Context context = this;
			Intent intent = new Intent(context, WorkspaceActivity.class);
			final File currentFile = file;
			/*try {
				String fileData = FileBrowser.readFileToString(currentFile);
			} catch (IOException e) {
				
				e.printStackTrace();
			}*/
			intent.putExtra("filename",currentFile);
			//intent.putExtra(value, fileData);
            startActivity(intent);
            
			/*new AlertDialog.Builder(this)
					.setTitle("[" + file.getName() + "]")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(context, MainActivity2.class);
									final File currentFile = file;
									/*try {
										String fileData = FileBrowser.readFileToString(currentFile);
									} catch (IOException e) {
										
										e.printStackTrace();
									}
									intent.putExtra("filename",currentFile);
									//intent.putExtra(value, fileData);
		                            startActivity(intent);
								}
							}).show();*/
		}
	}
}