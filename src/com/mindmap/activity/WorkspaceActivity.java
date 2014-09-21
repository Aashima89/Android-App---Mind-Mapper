package com.mindmap.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mindmap.view.CustomView;

@SuppressLint("NewApi")
public class WorkspaceActivity extends Activity implements OnLongClickListener,
		OnDragListener {

	private RelativeLayout layout;

	private TextView textView;
	private static int i = 1;
	// List<String> notes = new ArrayList<String>();
	List<TextView> text_view_list = new ArrayList<TextView>();
	boolean is_root = false;
	DocumentBuilder documentBuilder;
	Document write_doc;
	Document read_doc;
	File currentfilename;
	
	ArrayList<Integer> childdddd = new ArrayList<Integer>();
	
	public HashMap<Integer, ArrayList<Integer>> child_table = new HashMap<Integer, ArrayList<Integer>>();
	public HashMap<Integer, Integer> parent_table = new HashMap<Integer, Integer>();
	public HashMap<String, Integer> edge_table = new HashMap<String, Integer>();
	Point p;
	TextView currentTextView;
	PopupWindow popup;
	Animation anim = new AlphaAnimation(0.0f, 1.0f);
	
	
	public WorkspaceActivity() {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.workapce_action_menu_options, menu);
		return super.onCreateOptionsMenu(menu);
	}
	 
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warkspace);
		try {
			  ViewConfiguration config = ViewConfiguration.get(this);
			  Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

			  if (menuKeyField != null) {
			    menuKeyField.setAccessible(true);
			    menuKeyField.setBoolean(config, false);
			  }
			}
			catch (Exception e) {
			  // presumably, not relevant
			}
		
		findViewById(R.id.main_activity_layout).setOnDragListener(this);

		if (this.getIntent().getExtras() != null) {
			currentfilename = (File) this.getIntent().getExtras().get("filename");
			int length = currentfilename.getName().length();
			String title = "Workspace - "
					+ (currentfilename.getName()).substring(0, length - 3);
			getActionBar().setTitle(title);
			Log.d(FILENAME, "<================" + currentfilename.getName()
					+ "================>");
			Log.d("tagged log", "on click file");
			try {
				createFromFile(currentfilename);
				
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			is_root = true;
		} else {
			// blank new mindmap
			is_root = false;
			textView = (TextView) findViewById(R.id.edit_message);
			int tagbg = R.drawable.yellow;
			int tagtf = Typeface.NORMAL;
			int tagclr = Color.BLACK;
			String tagStr[] = { "" + tagbg + "", "" + tagtf + "", "" + tagclr + "" };
			textView.setHint("Edit Note");
			textView.setTag(tagStr);
			textView.setOnLongClickListener(this);
			textView.setX(135);
			textView.setY(40);
			textView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					openPopUpMenu(WorkspaceActivity.this, v);
				}
			});
		}

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();

		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			write_doc = documentBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void createFromFile(File currentfilename)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		read_doc = dBuilder.parse(currentfilename);
		layout = (RelativeLayout) findViewById(R.id.main_activity_layout);
		textView = (TextView) findViewById(R.id.edit_message);

		textView.requestFocus();
		
		System.out.println("Root element :"
				+ read_doc.getDocumentElement().getNodeName());
		Node mindmap = read_doc.getFirstChild();

		System.out.println("pappu" + mindmap.getNodeName());

		Node root_node = mindmap.getChildNodes().item(1);
		System.out.println("root note name " + root_node.getNodeName());
		Node bg = mindmap.getChildNodes().item(3);
		System.out.println("bgggg    ::   "+bg.getNodeName());
		bg_color = bg.getTextContent();
		Resources res = getResources();
		Drawable drawable;
		
		if("bg1".equals(bg_color))
			drawable = res.getDrawable(R.drawable.bg1);
		else if("bg2".equals(bg_color))
			drawable = res.getDrawable(R.drawable.bg2);
		else if("bg3".equals(bg_color))
			drawable = res.getDrawable(R.drawable.bg3);
		else if("bg4".equals(bg_color))
			drawable = res.getDrawable(R.drawable.bg4);
		else if("bg5".equals(bg_color))
			drawable = res.getDrawable(R.drawable.bg5);
		else if("bg6".equals(bg_color))
			drawable = res.getDrawable(R.drawable.bg6);
		else
			drawable = res.getDrawable(R.drawable.bg1);
		
		layout.setBackgroundDrawable(drawable);
		
		NodeList nl = root_node.getChildNodes();
		Node root_prop = nl.item(1);
		System.out.println("prop name:" + root_prop.getNodeName());
		NodeList root_prop_list = root_prop.getChildNodes();
		for (int i = 0; i < root_prop_list.getLength(); i++) {
			System.out.println("prop's child:"
					+ root_prop_list.item(i).getNodeName());
		}
		textView = (TextView) findViewById(R.id.edit_message);
		textView.setText(root_prop_list.item(1).getTextContent());
		// next_ed_text.setTop(Integer.parseInt(eElement.getElementsByTagName("position").item(0).getTextContent()));
		// textView.setTextColor(textView.getTextColors().getDefaultColor());

		// next_ed_text.setTypeface(Typeface.DEFAULT,currTypeFace);
		// textView.setTypeface(textView.getTypeface());

		textView.setOnLongClickListener(this);

		textView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				openPopUpMenu(WorkspaceActivity.this, v);
			}
		});

		String tagStr = root_prop_list.item(3).getTextContent();
		String[] tag = tagStr.split(",");
		textView.setTag(tag);

		// int backgroundImage =
		// Integer.parseInt(eElement.getElementsByTagName("background").item(0).getTextContent());
		textView.setBackgroundResource(Integer.parseInt(tag[0]));
		textView.setTypeface(Typeface.DEFAULT, Integer.parseInt(tag[1]));
		textView.setTextColor(Integer.parseInt(tag[2]));
		textView.setPadding(10, 5, 10, 5);
		textView.setX(Float.parseFloat(root_prop_list.item(5).getTextContent()));
		textView.setY(Float.parseFloat(root_prop_list.item(7).getTextContent()));

		
		int next_txt_ht = Integer.parseInt(root_prop_list.item(9)
				.getTextContent());

		System.out.println("----------------------------");

		Node children = nl.item(3);

		//System.out.println("children "+children.getNodeName());
		if (children != null) {
			NodeList child_list = children.getChildNodes();
			
			if (child_list != null) {
				for (int i = 1; i < child_list.getLength(); i = i + 2) {
					System.out.println("child nodes "+child_list.item(i).getNodeName());
					readChildFromFile(child_list.item(i), R.id.edit_message,
							textView.getX(), textView.getY(), next_txt_ht);
				}
			}
		}
		/*
		 * 
		 * for (int temp = 0; temp < nList.getLength(); temp++) { Node nNode =
		 * nList.item(temp); System.out.println("\nCurrent Element :" +
		 * nNode.getNodeName()); if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 * Element eElement = (Element) nNode; System.out.println("Node id : " +
		 * eElement.getElementsByTagName("text").item(0) .getTextContent()); i++;
		 * System.out.println("Value of i : " + i);
		 * layout.addView(setNewTextView(eElement), i); } }
		 */
		final ScrollView scroll_view_vert = (ScrollView)findViewById(R.id.main_scroll_view);
		final HorizontalScrollView scroll_view_hort = (HorizontalScrollView)findViewById(R.id.HorizontalScrollView);
		final int scrollX = (int)Float.parseFloat(root_prop_list.item(5).getTextContent());
		final int scrollY = (int)Float.parseFloat(root_prop_list.item(7).getTextContent());
		
		scroll_view_vert.post(new Runnable(){
			@Override
			public void run() {
				scroll_view_vert.smoothScrollTo(scrollX, scrollY);	
			}
		});
		
		scroll_view_hort.post(new Runnable(){
			@Override
			public void run() {
				scroll_view_hort.smoothScrollTo(scrollX, scrollY);	
			}
		});
		
		layout.requestLayout();
		layout.invalidate();
		/*
		 * textView.requestFocus(); textView.setHint("Edit Note");
		 * textView.setText(""); textView.setTextColor(Color.BLACK);
		 * textView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL); int tagbg =
		 * R.drawable.yellow; int tagtf = Typeface.NORMAL; int tagclr =
		 * Color.BLACK; String tagStr[] = { "" + tagbg + "", "" + tagtf + "", "" +
		 * tagclr + "" }; textView.setTag(tagStr);
		 */
	}

	private void readChildFromFile(Node children, int parent_id, float pX,
			float pY, int pH) {
		Node root_node = children;
		TextView next_text = new TextView(this);

		System.out.println("root note name " + root_node.getNodeName());
		int id = i++;

		NodeList nl = root_node.getChildNodes();
		Node root_prop = nl.item(1);
		System.out.println("prop name:" + root_prop.getNodeName());
		NodeList root_prop_list = root_prop.getChildNodes();
		for (int i = 0; i < root_prop_list.getLength(); i++) {
			System.out.println("prop's child:"
					+ root_prop_list.item(i).getNodeName());
		}

		next_text.setId(id);
		next_text.setText(root_prop_list.item(1).getTextContent());
		next_text.setOnLongClickListener(this);

		next_text.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				openPopUpMenu(WorkspaceActivity.this, v);
			}
		});

		String tagStr = root_prop_list.item(3).getTextContent();
		String[] tag = tagStr.split(",");
		next_text.setTag(tag);
		next_text.setBackgroundResource(Integer.parseInt(tag[0]));
		next_text.setTypeface(Typeface.DEFAULT, Integer.parseInt(tag[1]));
		next_text.setTextColor(Integer.parseInt(tag[2]));
		next_text.setPadding(10, 5, 10, 5);
		next_text.setX(Float.parseFloat(root_prop_list.item(5).getTextContent()));
		next_text.setY(Float.parseFloat(root_prop_list.item(7).getTextContent()));

		int next_txt_ht = Integer.parseInt(root_prop_list.item(9)
				.getTextContent());

		System.out.println("----------------------------");
		layout.addView(next_text);

		parent_table.put(id, parent_id);
		String p_c = parent_id + ":" + id;

		// ---------------------------------------------------
		CustomView drawable_view = new CustomView(this);
		drawable_view.setId(i++);
		drawable_view.setStart_x(next_text.getX() + 23);
		// drawable_view.setStart_y(next_text.getY() - 23);
		drawable_view.setStart_y(next_text.getY());

		// View parent = findViewById(parent_id);
		drawable_view.setEnd_x(pX + 23);
		// drawable_view.setEnd_y(pY + parent.getHeight() - 23);
		drawable_view.setEnd_y(pY + pH);
		drawable_view.setBackgroundColor(Color.TRANSPARENT);
		float set_x = 0;
		float set_y = 0;
		if (pY > next_text.getY()) {
			set_y = pY + pH;
		} else {
			set_y = next_text.getY();
		}
		set_x = findViewById(parent_id).getX() > next_text.getX() ? findViewById(
				parent_id).getX() : next_text.getX();

		drawable_view.setLayoutParams(new RelativeLayout.LayoutParams(
				(int) set_x + 23, (int) set_y));
		layout.addView(drawable_view);
		edge_table.put(p_c, drawable_view.getId());

		if (child_table.containsKey((Integer) parent_id)) {
			ArrayList<Integer> pcList = child_table.get((Integer) parent_id);
			pcList.add(id);
			child_table.remove((Integer) parent_id);
			child_table.put((Integer) parent_id, pcList);
		} else {
			ArrayList<Integer> cList = new ArrayList<Integer>();
			cList.add(id);
			child_table.put((Integer) parent_id, cList);
		}
		Node child = nl.item(3);

		if (child != null) {
			NodeList child_list = child.getChildNodes();
			if (child_list != null) {
				for (int i = 1; i < child_list.getLength(); i = i + 2) {
					readChildFromFile(child_list.item(i), next_text.getId(),
							next_text.getX(), next_text.getY(), next_txt_ht);
				}
			}
		}
	}

	private View setNewTextView(Element eElement) {
		TextView next_ed_text = new TextView(this);
		text_view_list.add(next_ed_text);

		next_ed_text.setId(i);
		next_ed_text.setText(eElement.getElementsByTagName("text").item(0)
				.getTextContent());
		// next_ed_text.setTop(Integer.parseInt(eElement.getElementsByTagName("position").item(0).getTextContent()));
		next_ed_text.setTextColor(textView.getTextColors().getDefaultColor());

		// next_ed_text.setTypeface(Typeface.DEFAULT,currTypeFace);
		next_ed_text.setTypeface(textView.getTypeface());
		next_ed_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openPopUpMenu(WorkspaceActivity.this, v);
			}
		});
		String tagStr = eElement.getElementsByTagName("appearance").item(0)
				.getTextContent();
		String[] tag = tagStr.split(",");
		next_ed_text.setTag(tag);
		System.out.println("1st ele is" + tag[0]);

		// int backgroundImage =
		// Integer.parseInt(eElement.getElementsByTagName("background").item(0).getTextContent());
		next_ed_text.setBackgroundResource(Integer.parseInt(tag[0]));
		next_ed_text.setTypeface(Typeface.DEFAULT, Integer.parseInt(tag[1]));
		next_ed_text.setTextColor(Integer.parseInt(tag[2]));
		next_ed_text.setPadding(10, 5, 0, 0);
		return next_ed_text;
	}

	private void openPopUpMenu(Activity context, View v) {
		currentTextView = (TextView) findViewById(v.getId());
		int[] location = new int[2];
		currentTextView.getLocationOnScreen(location);

		// Initialize the Point with x, and y positions
		p = new Point();
		p.x = location[0];
		p.y = location[1];
		int popupWidth = 200;
		int popupHeight = 440;

		// Inflate the popup_layout.xml
		LinearLayout viewGroup = (LinearLayout) context
				.findViewById(R.id.popup_menu);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.popup_menu_options,
				viewGroup);

		// Creating the PopupWindow
		popup = new PopupWindow(context);
		popup.setContentView(layout);
		popup.setWidth(popupWidth);
		popup.setHeight(popupHeight);
		popup.setFocusable(true);

		// Some offset to align the popup a bit to the right, and a bit down,
		// relative to button's position.
		int OFFSET_X = 3;
		int OFFSET_Y = 50;

		// Clear the default translucent background
		// popup.setBackgroundDrawable(new BitmapDrawable());

		// Displaying the popup at the specified location, + offsets.
		popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y
				+ OFFSET_Y);

		// Getting a reference to Close button, and close the popup when
		// clicked.

	}

	public void onTextOption(View arg0) {
		popup.dismiss();
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Text (only first 128 characters will be saved)");

		final EditText input = new EditText(this);
		
		input.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
		input.setText(currentTextView.getText());
		alert.setView(input);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if(input.getText().length() > 128)
					currentTextView.setText(input.getText().subSequence(0, 128));
				else
					currentTextView.setText(input.getText());	
			}
		});
		alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				dialog.cancel();
			}
		});
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
	}

	public void onBackgroundOption(View v) {
		popup.dismiss();
		PopupWindow popupBackground = new PopupWindow(WorkspaceActivity.this);
		int[] location = new int[2];
		currentTextView.getLocationOnScreen(location);

		// Initialize the Point with x, and y positions
		p = new Point();
		p.x = location[0];
		p.y = location[1];
		int popupWidth = 400;
		int popupHeight = 100;

		// Inflate the popup_layout.xml
		// context = MainActivity2.this;
		LinearLayout viewGroup = (LinearLayout) (WorkspaceActivity.this)
				.findViewById(R.id.background_popup);
		LayoutInflater layoutInflater = (LayoutInflater) (WorkspaceActivity.this)
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.background_color_option,
				viewGroup);

		// Creating the PopupWindow

		popupBackground.setContentView(layout);
		popupBackground.setWidth(popupWidth);
		popupBackground.setHeight(popupHeight);
		popupBackground.setFocusable(true);

		// Some offset to align the popup a bit to the right, and a bit down,
		// relative to button's position.
		int OFFSET_X = 3;
		int OFFSET_Y = 50;

		// Clear the default translucent background
		// popup.setBackgroundDrawable(new BitmapDrawable());

		// Displaying the popup at the specified location, + offsets.
		popupBackground.showAtLocation(layout, Gravity.NO_GRAVITY,
				p.x + OFFSET_X, p.y + OFFSET_Y);
	}

	public void changeBackground(View v) {
		int btnId = v.getId();
		Log.d("id clicked ", "" + btnId);
		if (btnId == R.id.greenBack) {
			// Log.d("id clicked green", "" + btnId);
			currentTextView.setBackgroundResource(R.drawable.green);
			String[] tag = (String[]) currentTextView.getTag();
			tag[0] = "" + R.drawable.green + "";
			currentTextView.setTag(tag);

		} else if (btnId == R.id.yellowBack) {
			// Log.d("id clicked green", "" + btnId);
			currentTextView.setBackgroundResource(R.drawable.yellow);
			String[] tag = (String[]) currentTextView.getTag();
			tag[0] = "" + R.drawable.yellow + "";
			currentTextView.setTag(tag);

		} else if (btnId == R.id.blueBack) {
			currentTextView.setBackgroundResource(R.drawable.blue);
			String[] tag = (String[]) currentTextView.getTag();
			tag[0] = "" + R.drawable.blue + "";
			currentTextView.setTag(tag);

		} else if (btnId == R.id.pinkBack) {
			currentTextView.setBackgroundResource(R.drawable.pink);
			String[] tag = (String[]) currentTextView.getTag();
			tag[0] = "" + R.drawable.pink + "";
			currentTextView.setTag(tag);
		} else if (btnId == R.id.orangeBack) {
			currentTextView.setBackgroundResource(R.drawable.orange);
			String[] tag = (String[]) currentTextView.getTag();
			tag[0] = "" + R.drawable.orange + "";
			currentTextView.setTag(tag);
		}
	}

	public void onStyleOption(View v) {
		popup.dismiss();
		PopupWindow popupBackground = new PopupWindow(WorkspaceActivity.this);
		int[] location = new int[2];
		currentTextView.getLocationOnScreen(location);

		// Initialize the Point with x, and y positions
		p = new Point();
		p.x = location[0];
		p.y = location[1];
		int popupWidth = 380;
		int popupHeight = 100;

		// Inflate the popup_layout.xml
		// context = MainActivity2.this;
		LinearLayout viewGroup = (LinearLayout) (WorkspaceActivity.this)
				.findViewById(R.id.style_popup);
		LayoutInflater layoutInflater = (LayoutInflater) (WorkspaceActivity.this)
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.style_option, viewGroup);

		// Creating the PopupWindow

		popupBackground.setContentView(layout);
		popupBackground.setWidth(popupWidth);
		popupBackground.setHeight(popupHeight);
		popupBackground.setFocusable(true);

		// Some offset to align the popup a bit to the right, and a bit down,
		// relative to button's position.
		int OFFSET_X = 3;
		int OFFSET_Y = 50;

		// Clear the default translucent background
		// popup.setBackgroundDrawable(new BitmapDrawable());

		// Displaying the popup at the specified location, + offsets.
		popupBackground.showAtLocation(layout, Gravity.NO_GRAVITY,
				p.x + OFFSET_X, p.y + OFFSET_Y);
	}

	// int currTypeFace = Typeface.NORMAL;
	public void changeStyle(View v) {
		int btnId = v.getId();
		if (btnId == R.id.normal) {
			currentTextView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
			// currTypeFace = Typeface.NORMAL;
			String[] tag = (String[]) currentTextView.getTag();
			tag[1] = "" + Typeface.NORMAL + "";
			currentTextView.setTag(tag);
		} else if (btnId == R.id.bold) {
			currentTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			// currTypeFace = Typeface.BOLD;
			String[] tag = (String[]) currentTextView.getTag();
			tag[1] = "" + Typeface.BOLD + "";
			currentTextView.setTag(tag);
		} else if (btnId == R.id.italic) {
			currentTextView.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
			// currTypeFace = Typeface.ITALIC;
			String[] tag = (String[]) currentTextView.getTag();
			tag[1] = "" + Typeface.ITALIC + "";
			currentTextView.setTag(tag);
		} else if (btnId == R.id.boldItalic) {
			currentTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
			// currTypeFace = Typeface.BOLD_ITALIC;
			String[] tag = (String[]) currentTextView.getTag();
			tag[1] = "" + Typeface.BOLD_ITALIC + "";
			currentTextView.setTag(tag);
		}
	}

	public void onTextColorOption(View v) {
		popup.dismiss();
		PopupWindow popupBackground = new PopupWindow(WorkspaceActivity.this);
		int[] location = new int[2];
		currentTextView.getLocationOnScreen(location);

		// Initialize the Point with x, and y positions
		p = new Point();
		p.x = location[0];
		p.y = location[1];
		int popupWidth = 400;
		int popupHeight = 100;

		// Inflate the popup_layout.xml
		// context = MainActivity2.this;
		LinearLayout viewGroup = (LinearLayout) (WorkspaceActivity.this)
				.findViewById(R.id.textcolor_popup);
		LayoutInflater layoutInflater = (LayoutInflater) (WorkspaceActivity.this)
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.text_color_option,
				viewGroup);

		// Creating the PopupWindow

		popupBackground.setContentView(layout);
		popupBackground.setWidth(popupWidth);
		popupBackground.setHeight(popupHeight);
		popupBackground.setFocusable(true);

		// Some offset to align the popup a bit to the right, and a bit down,
		// relative to button's position.
		int OFFSET_X = 3;
		int OFFSET_Y = 50;

		// Clear the default translucent background
		// popup.setBackgroundDrawable(new BitmapDrawable());

		// Displaying the popup at the specified location, + offsets.
		popupBackground.showAtLocation(layout, Gravity.NO_GRAVITY,
				p.x + OFFSET_X, p.y + OFFSET_Y);
	}

	public void changeTextColor(View v) {
		int btnId = v.getId();
		System.out.println("button id is :" + btnId);

		if (btnId == R.id.red) {
			// Log.d("id clicked green", "" + btnId);
			currentTextView.setTextColor(Color.RED);
			String[] tag = (String[]) currentTextView.getTag();
			tag[2] = "" + Color.RED + "";
			System.out.println("text color is " + tag[2]);
			currentTextView.setTag(tag);

		} else if (btnId == R.id.green) {
			// Log.d("id clicked green", "" + btnId);
			currentTextView.setTextColor(Color.GREEN);
			String[] tag = (String[]) currentTextView.getTag();
			tag[2] = "" + Color.GREEN + "";
			currentTextView.setTag(tag);
		} else if (btnId == R.id.blue) {
			currentTextView.setTextColor(Color.BLUE);
			String[] tag = (String[]) currentTextView.getTag();
			tag[2] = "" + Color.BLUE + "";
			currentTextView.setTag(tag);
		} else if (btnId == R.id.magenta) {
			currentTextView.setTextColor(Color.MAGENTA);
			String[] tag = (String[]) currentTextView.getTag();
			tag[2] = "" + Color.MAGENTA + "";
			currentTextView.setTag(tag);
		} else if (btnId == R.id.black) {
			currentTextView.setTextColor(Color.BLACK);
			String[] tag = (String[]) currentTextView.getTag();
			tag[2] = "" + Color.BLACK + "";
			currentTextView.setTag(tag);
		}

	}

	public void onAddOption(View v) {
		layout = (RelativeLayout) findViewById(R.id.main_activity_layout);
		TextView newly_added = addNewTextView();

		CustomView drawable_view = new CustomView(this);
		drawable_view.setLayoutParams(new RelativeLayout.LayoutParams(
				(int) newly_added.getX()+2, (int) newly_added.getY()+2));
		System.out.println("X of currentTextView " + currentTextView.getX()
				+ ", Y of currenTextview: " + currentTextView.getY()
				+ ", height of current: " + currentTextView.getHeight()
				+ ", current width: " + currentTextView.getWidth());
		drawable_view.setStart_x(currentTextView.getX());
		drawable_view.setStart_y(currentTextView.getY()
				+ currentTextView.getHeight() - 23);
		drawable_view.setEnd_x(newly_added.getX());
		drawable_view.setEnd_y(newly_added.getY());
		drawable_view.setBackgroundColor(Color.TRANSPARENT);
		layout.addView(drawable_view);
		layout.addView(newly_added);

		parent_table.put(newly_added.getId(), currentTextView.getId());
		ArrayList<Integer> ct = child_table.get(currentTextView.getId());
		if (ct == null) {

			ArrayList<Integer> n_ct = new ArrayList<Integer>();
			n_ct.add(newly_added.getId());
			child_table.put(currentTextView.getId(), n_ct);
		} else {
			ct.add(newly_added.getId());
		}
		String edge_key = "" + currentTextView.getId() + ":"
				+ newly_added.getId();
		drawable_view.setId(i++);
		edge_table.put(edge_key, drawable_view.getId());
		System.out.println("Edge create between " + edge_key + ", id="
				+ drawable_view.getId() + ", object="
				+ (CustomView) findViewById(drawable_view.getId()));
		layout.requestLayout();
		layout.invalidate();
		popup.dismiss();
	}

	private TextView addNewTextView() {

		final float posY = currentTextView.getY();
		System.out.println("bottom posn " + posY);
		final float posX = currentTextView.getX();
		textView = new TextView(this);

		text_view_list.add(textView);

		textView.requestFocus();
		textView.setHint("Edit Note");
		textView.setText("");
		textView.setId(i);
		i++;
		textView.setTextColor(Color.BLACK);
		textView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
		textView.setBackgroundResource(R.drawable.yellow);
		int tagbg = R.drawable.yellow;
		int tagtf = Typeface.NORMAL;
		int tagclr = Color.BLACK;
		String tagStr[] = { "" + tagbg + "", "" + tagtf + "", "" + tagclr + "" };
		textView.setTag(tagStr);
		System.out.println("current text view is " + currentTextView
				+ ", its x position = " + currentTextView.getX()
				+ ", Its y position = " + currentTextView.getY());
		textView.setY(posY + currentTextView.getHeight() * 2);
		textView.setX(posX);
		textView.setPadding(10, 5, 10, 5);
		// textView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		textView.setWidth(100);
		System.out.println("new X position " + textView.getX() + " y position "
				+ textView.getY());

		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openPopUpMenu(WorkspaceActivity.this, v);
			}
		});

		// textView.setPadding(10, 5, 0, 0);
		textView.setTag(textView.getTag());
		textView.setOnLongClickListener(this);

		return textView;
	}

	public void onDeleteOption(View v) { // System.out.println("view id "+v.getId()+"  "+R.id.edit_message+"  "+currentTextView.getId());
		popup.dismiss();
		if (currentTextView.getId() != R.id.edit_message) {
			new AlertDialog.Builder(this)
					.setMessage("Do you want delete the Note?(All child notes will also be deleted)")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							try {
								RelativeLayout layout = (RelativeLayout) (WorkspaceActivity.this)
										.findViewById(R.id.main_activity_layout);
								layout.removeView(currentTextView);
								text_view_list.remove(currentTextView);
								List<Integer> child_list = child_table
										.get(currentTextView.getId());
								if (child_list != null) {

									for (Integer child : child_list) {
										layout.removeView(findViewById(child));
										String p_c = currentTextView.getId() + ":"
												+ child;
										if (edge_table.containsKey(p_c)) {
											int edge = edge_table.get(p_c);
											layout.removeView(findViewById(edge));
											edge_table.remove(p_c);
											parent_table.remove(child);
										}

									}
									child_table.remove(currentTextView.getId());
								}

								int parent = parent_table.get(currentTextView.getId());
								String p_c = parent + ":" + currentTextView.getId();
								if (edge_table.containsKey(p_c)) {
									int edge = edge_table.get(p_c);
									layout.removeView(findViewById(edge));
									edge_table.remove(p_c);
									parent_table.remove(currentTextView.getId());
								}
								ArrayList<Integer> par_chd = child_table.get(parent);
								par_chd.remove((Integer) currentTextView.getId());
								child_table.remove(parent);
								if (par_chd.size() > 0)
									child_table.put(parent, par_chd);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {

						}
					}).show();
		} else {
			Toast.makeText(getApplicationContext(),
					"Root note cannot be deleted.", Toast.LENGTH_SHORT).show();

		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitByBackKey();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void exitByBackKey() {
		new AlertDialog.Builder(this)
				.setMessage("Do you want to save the MindMap?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// save code goes here
						try {
							if (!is_root)
								saveToFile("Enter File Name");
							else {
								FILENAME = currentfilename.getName();
								writeToFile();
								Toast.makeText(getApplicationContext(),
										"MindMap saved", Toast.LENGTH_SHORT).show();
								finish();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						// finish();
						catch (TransformerException e) {
							e.printStackTrace();
						}
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				}).show();
	}

	private void saveToFile(String message) throws IOException {

		System.out.println("is root value : " + is_root);

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("FileName");
		alert.setMessage(message);

		final EditText input = new EditText(this);
		input.setText("");
		alert.setView(input);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

				FILENAME = input.getEditableText().toString();
				/*System.out.println("Does the filename already exists : "
						+ HomeActivity.filenameList.contains(FILENAME));*/
				if (!(FILENAME.equals("") || HomeActivity.filenameList
						.contains(FILENAME))) {

					HomeActivity.filenameList.add(FILENAME);
					System.out.println("File list before adding : " + FILENAME);
					for (int i = 0; i < HomeActivity.filenameList.size(); i++)
						System.out.println("File list : "
								+ HomeActivity.filenameList.get(i));

					correctFileName();
					try {
						writeToFile();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (TransformerConfigurationException e) {
						e.printStackTrace();
					} catch (TransformerException e) {
						e.printStackTrace();
					}
					Toast.makeText(getApplicationContext(), "MindMap saved",
							Toast.LENGTH_SHORT).show();
					finish();
				} else {
					try {
						if (HomeActivity.filenameList.contains(FILENAME))
							saveToFile("File name already exists!");
						else
							saveToFile("Enter File Name");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				dialog.cancel();
			}
		});
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
	}

	private void correctFileName() {
		if (!"myFile.mm".equals(FILENAME)) {
			FILENAME = FILENAME.trim();
			if (!FILENAME.matches("\\w+\\.mm")) {
				FILENAME = FILENAME.concat(".mm");
			}
		}
	}

	private void writeChildToFile(Element root_node, int id) {
		Element node = write_doc.createElement("Node");
		TextView root = (TextView) findViewById(id);
		node.setAttribute("id", "" + root.getId());

		Element prop = write_doc.createElement("prop");

		Element prop_text = write_doc.createElement("text");
		prop_text.appendChild(write_doc.createTextNode("" + root.getText()));
		Element prop_x = write_doc.createElement("XLoc");
		prop_x.appendChild(write_doc.createTextNode("" + root.getX()));
		Element prop_y = write_doc.createElement("YLoc");
		prop_y.appendChild(write_doc.createTextNode("" + root.getY()));
		Element appearance = write_doc.createElement("appearance");
		String[] tag = (String[]) root.getTag();
		String apr = new StringBuffer(tag[0]).append(",").append(tag[1])
				.append(",").append(tag[2]).toString();
		appearance.appendChild(write_doc.createTextNode(apr));

		Element prop_ht = write_doc.createElement("height");
		prop_ht.appendChild(write_doc.createTextNode("" + root.getHeight()));

		prop.appendChild(prop_text);
		prop.appendChild(appearance);
		prop.appendChild(prop_x);
		prop.appendChild(prop_y);
		prop.appendChild(prop_ht);
		node.appendChild(prop);

		if (child_table.containsKey(root.getId())) {
			ArrayList<Integer> child_list = child_table.get(root.getId());
			Element children = write_doc.createElement("children");
			for (int child_id : child_list) {
				writeChildToFile(children, child_id);
			}
			node.appendChild(children);
		}

		root_node.appendChild(node);
	}

	private void writeToFile() throws IOException, TransformerException {
		OutputStreamWriter outputStreamWriter;

		outputStreamWriter = new OutputStreamWriter(openFileOutput(FILENAME,
				Context.MODE_PRIVATE));
		Element mindmap = write_doc.createElement("MindMap");
		System.out.println("filename is : " + FILENAME);
		write_doc.appendChild(mindmap);

		Element root_node = write_doc.createElement("Node");
		TextView root = (TextView) findViewById(R.id.edit_message);
		root_node.setAttribute("id", "" + root.getId());

		Element prop = write_doc.createElement("prop");

		Element prop_text = write_doc.createElement("text");
		prop_text.appendChild(write_doc.createTextNode("" + root.getText()));
		Element prop_x = write_doc.createElement("XLoc");
		prop_x.appendChild(write_doc.createTextNode("" + root.getX()));
		Element prop_y = write_doc.createElement("YLoc");
		prop_y.appendChild(write_doc.createTextNode("" + root.getY()));
		Element appearance = write_doc.createElement("appearance");
		String[] tag = (String[]) root.getTag();
		String apr = new StringBuffer(tag[0]).append(",").append(tag[1])
				.append(",").append(tag[2]).toString();
		appearance.appendChild(write_doc.createTextNode(apr));

		Element prop_ht = write_doc.createElement("height");
		prop_ht.appendChild(write_doc.createTextNode("" + root.getHeight()));

		prop.appendChild(prop_text);
		prop.appendChild(appearance);
		prop.appendChild(prop_x);
		prop.appendChild(prop_y);
		prop.appendChild(prop_ht);
		root_node.appendChild(prop);

		if (child_table.containsKey(root.getId())) {
			ArrayList<Integer> child_list = child_table.get(root.getId());
			Element children = write_doc.createElement("children");
			for (int child_id : child_list) {
				writeChildToFile(children, child_id);
			}
			root_node.appendChild(children);
		}
		mindmap.appendChild(root_node);
		Element bg = write_doc.createElement("Background");
		bg.appendChild(write_doc.createTextNode("" + bg_color));
		mindmap.appendChild(bg);
		/*
		 * if (!textView.getText().toString().isEmpty()) {
		 * text_view_list.add(textView); } for (TextView ed : text_view_list) {
		 * Element node = document.createElement("Node"); node.setAttribute("id",
		 * "" + j++); Element position = document.createElement("position");
		 * position.appendChild(document.createTextNode("" + ed.getTop()));
		 * Element text = document.createElement("text");
		 * text.appendChild(document.createTextNode(ed.getText().toString()));
		 * Element appearance = document.createElement("appearance"); String[] tag
		 * = (String[]) ed.getTag(); String apr = new
		 * StringBuffer(tag[0]).append(",").append(tag[1])
		 * .append(",").append(tag[2]).toString();
		 * appearance.appendChild(document.createTextNode(apr));
		 * node.appendChild(position); node.appendChild(text);
		 * node.appendChild(appearance); root.appendChild(node); }
		 */

		/*
		 * outputStreamWriter.append(editText.getText().toString());
		 * outputStreamWriter.append("Data written");
		 */
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		Properties outFormat = new Properties();
		outFormat.setProperty(OutputKeys.INDENT, "yes");
		outFormat.setProperty(OutputKeys.METHOD, "xml");
		outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		outFormat.setProperty(OutputKeys.VERSION, "1.0");
		outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperties(outFormat);
		DOMSource domsrc = new DOMSource(write_doc.getDocumentElement());
		OutputStream output = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(output);
		transformer.transform(domsrc, result);
		String xmlString = output.toString();
		Log.d("xmlstring", xmlString);
		i = 1;
		outputStreamWriter.write(xmlString);
		// outputStreamWriter.flush();
		outputStreamWriter.close();
	}

	private static String FILENAME = "myFile.mm";

	@Override
	public boolean onDrag(View v, DragEvent event) {
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_activity_layout);

		int initial_x = 0;
		int initial_y = 0;
		switch (event.getAction()) {

		case DragEvent.ACTION_DRAG_STARTED:
			
			View view_started = (View) event.getLocalState();
			view_started.setVisibility(View.INVISIBLE);
			initial_x = (int)view_started.getX();
			initial_y = (int)view_started.getY();
			System.out.println("initial x and y "+ initial_x + " "+ initial_y);
			
			int id = view_started.getId();
			if (parent_table.containsKey(id)) {
				int p_id = parent_table.get(id);

				String p_c = "" + p_id + ":" + id;

				int edge = edge_table.get(p_c);

				System.out.println("Edge between " + p_c + ", id=" + edge
						+ ", object:" + (CustomView) findViewById(edge));
				layout.removeView((CustomView) findViewById(edge));
				edge_table.remove(p_c);
			}
			if (child_table.containsKey(id)) {
				List<Integer> child_list = child_table.get(id);
				if (child_list != null) {
					for (Integer next_child : child_list) {
						String c_c = id + ":" + next_child;
						if (edge_table.containsKey(c_c)) {
							int edge = edge_table.get(c_c);

							System.out.println("Edge between " + c_c + ", id=" + edge
									+ ", object:" + (CustomView) findViewById(edge));
							layout.removeView((CustomView) findViewById(edge));
							edge_table.remove(c_c);
						}

					}
				}
			}
			break;
		
		case DragEvent.ACTION_DROP:

			View view = (View) event.getLocalState();
			ViewGroup owner = (ViewGroup) view.getParent();
			System.out.println("before removing " + view.getId());
			owner.removeView(view);
			System.out.println("after adding " + view.getId());

			view.setX((int) event.getX() - view.getWidth() / 2);
			view.setY((int) event.getY() - view.getHeight() / 2);
			System.out.println("X set to: " + view.getX() + ", Y set to: "
					+ view.getY());
			view.setVisibility(View.VISIBLE);
			
			final ScrollView scroll_view_vert = (ScrollView)findViewById(R.id.main_scroll_view);
			final HorizontalScrollView scroll_view_hort = (HorizontalScrollView)findViewById(R.id.HorizontalScrollView);
			final int scrollX = (int) (event.getX() - view.getWidth() / 2);
			final int scrollY = (int) (event.getY() - view.getHeight() / 2);
			scroll_view_vert.post(new Runnable() {
				@Override
				public void run() {
					scroll_view_vert.smoothScrollTo(scrollX-200, scrollY-200);
				}
			});
			scroll_view_hort.post(new Runnable() {
				@Override
				public void run() {
					scroll_view_hort.smoothScrollTo(scrollX-200, scrollY-200);
				}
			});
			
			int my_id = view.getId();

			if (parent_table.containsKey(my_id)) {
				int parent_id = parent_table.get(my_id);
				String par_chd = "" + parent_id + ":" + my_id;
				CustomView drawable_view = new CustomView(this);
				drawable_view.setId(i++);
				drawable_view.setStart_x(view.getX());
				drawable_view.setStart_y(view.getY() - 23);
				View parent = findViewById(parent_id);
				drawable_view.setEnd_x(parent.getX());
				drawable_view.setEnd_y(parent.getY() + parent.getHeight() - 23);
				drawable_view.setBackgroundColor(Color.TRANSPARENT);
				float set_x = 0;
				float set_y = 0;
				if (parent.getY() > view.getY()) {
					set_y = parent.getY() - 23 + parent.getHeight();
				} else {
					set_y = view.getY();
				}
				set_x = findViewById(parent_id).getX() > view.getX() ? findViewById(
						parent_id).getX()
						: view.getX();

				drawable_view.setLayoutParams(new RelativeLayout.LayoutParams(
						(int) set_x, (int) set_y));
				layout.addView(drawable_view);
				edge_table.put(par_chd, drawable_view.getId());
			}
			List<Integer> children_list = child_table.get(my_id);
			if (children_list != null) {
				for (Integer next_child : children_list) {
					String c_c = my_id + ":" + next_child;
					CustomView child_edge = new CustomView(this);
					child_edge.setId(i++);
					child_edge.setStart_x(view.getX());
					child_edge.setStart_y(view.getY() - 23 + view.getHeight());
					child_edge.setEnd_x(findViewById(next_child).getX());
					child_edge.setEnd_y(findViewById(next_child).getY() - 23);
					child_edge.setBackgroundColor(Color.TRANSPARENT);
					float set_x = findViewById(next_child).getX() > view.getX() ? findViewById(
							next_child).getX()
							: view.getX();
					float set_y = findViewById(next_child).getY() > view.getY() ? findViewById(
							next_child).getY()
							: view.getY();
					child_edge.setLayoutParams(new RelativeLayout.LayoutParams(
							(int) set_x, (int) set_y));
					layout.addView(child_edge);
					edge_table.put(c_c, child_edge.getId());
				}
			}
			layout.addView(view);
			layout.bringChildToFront(view);
			break;
		default:
			break;
		}
		return true;
	}

	/*
	 * public boolean onDrag(View layoutview, DragEvent dragevent) { int action =
	 * dragevent.getAction(); boolean containsDraggable; View dragView = (View)
	 * dragevent.getLocalState();
	 * 
	 * float newX = 0, newY = 0;
	 * 
	 * switch (action) { case DragEvent.ACTION_DRAG_STARTED: Log.d("LOGCAT",
	 * "Drag event started"); break; case DragEvent.ACTION_DRAG_ENTERED:
	 * Log.d("LOGCAT", "Drag event entered into "+layoutview.toString());
	 * containsDraggable = true; break; case DragEvent.ACTION_DRAG_EXITED:
	 * Log.d("LOGCAT", "Drag event exited from "+layoutview.toString());
	 * containsDraggable = false; break; case (DragEvent.ACTION_DROP):
	 * Log.d("LOGCAT", "Dropped"); View view = (View) dragevent.getLocalState();
	 * ViewGroup owner = (ViewGroup) view.getParent(); owner.removeView(view);
	 * 
	 * MarginLayoutParams marginParams = new
	 * MarginLayoutParams(view.getLayoutParams()); int left = (int)
	 * dragevent.getX() + 5 - view.getWidth() / 2; int top = (int)
	 * dragevent.getY() + 5 - view.getHeight() / 2;
	 * System.out.println("drag X position " + left + " y position " + top);
	 * marginParams.setMargins(left, top, 0, 0); LinearLayout.LayoutParams
	 * layoutParams = new LinearLayout.LayoutParams(marginParams);
	 * view.setLayoutParams(layoutParams); layout = (RelativeLayout)
	 * findViewById(R.id.main_activity_layout); layout.addView(view);
	 * view.setVisibility(View.VISIBLE); break; case DragEvent.ACTION_DRAG_ENDED:
	 * Log.d("LOGCAT", "Drag ended"); break; default: break; } return true; }
	 */

	@Override
	public boolean onLongClick(View view) {

		System.out.println("before starting drag " + view.getId());
		DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

		view.startDrag(null, shadowBuilder, view, 0);
		view.setVisibility(View.VISIBLE);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Log.d("Item id: ", "" + item.getItemId());
		int item_id = item.getItemId();
		switch (item_id) {
		case R.id.download:
			downloadAsImage();
			break;
		case R.id.background:
			changeLayoutBackground();
			break;
		case R.id.play:
			playMindMap();
			
			break;
		}
		return true;
	}

	private void changeLayoutBackground() {
		// TODO Auto-generated method stub
		 Intent intent = new Intent(this, LayoutBackgroundActivity.class);
       startActivityForResult(intent, 1);
		
	}
	String bg_color;
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		  if (requestCode == 1) {

		     if(resultCode == RESULT_OK){      
		   	  bg_color=data.getStringExtra("result"); 
		         System.out.println("result is "+bg_color);
		         RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_activity_layout);
		         Resources res = getResources();
		         Drawable drawable;
		         if("bg1".equals(bg_color))
		   			drawable = res.getDrawable(R.drawable.bg1);
		         else if("bg2".equals(bg_color))
		   			drawable = res.getDrawable(R.drawable.bg2);
		   		else if("bg3".equals(bg_color))
		   			drawable = res.getDrawable(R.drawable.bg3);
		   		else if("bg4".equals(bg_color))
		   			drawable = res.getDrawable(R.drawable.bg4);
		   		else if("bg5".equals(bg_color))
		   			drawable = res.getDrawable(R.drawable.bg5);
		   		else if("bg6".equals(bg_color))
		   			drawable = res.getDrawable(R.drawable.bg6);
		   		else
		   			drawable = res.getDrawable(R.drawable.bg1);
		        layout.setBackgroundDrawable(drawable);
		        //layout.setBackgroundResource(R.drawable.yellow);
		     }
		     if (resultCode == RESULT_CANCELED) {    
		         //Write your code if there's no result
		     }
		  }
		}
	public void downloadAsImage()
	{
		View v = findViewById(R.id.main_activity_layout);
		v.setDrawingCacheEnabled(true);
		v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
		v.buildDrawingCache(true);
		//ImageView img_view = (ImageView)findViewById(R.id.image_view);
		
		//Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
		//Bitmap b = v.getDrawingCache();
		Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		//Bitmap b = v.getDrawingCache();
		v.draw(new Canvas(b));
		v.setDrawingCacheEnabled(false);
		//v.destroyDrawingCache();
		
		//img_view.setImageBitmap(b);
		
		String fileName = new SimpleDateFormat("yyyyMMddhhmm'_mindmap.jpg'").format(new Date());
		
      //File myPath = new File(extr, fileName);
      FileOutputStream fos = null;
      //String extr = Environment.getExternalStorageDirectory().toString() +   File.separator + "Folder";
      //String fileName = new SimpleDateFormat("yyyyMMddhhmm'_report.jpg'").format(new Date());
      File root =  Environment.getExternalStorageDirectory().getAbsoluteFile();
      File file = new File(root.getAbsoluteFile() +File.separator + "MindMap");
      
      //File myPath = new File(extr, fileName);
      //FileOutputStream fos = null;
      try {
      	if(!file.exists() && !file.isDirectory()){
      		file.mkdirs();
      	}
      	//file.mkdirs();
      	File new_file = new File(file.getAbsolutePath()+File.separator + fileName);
      	 new_file.createNewFile();
      	 
          //fos = new FileOutputStream(myPath);
      	 fos = new FileOutputStream(new_file);
          b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
          fos.flush();
          fos.close();
          Toast.makeText(getApplicationContext(),
 					"MindMap saved to gallery.", Toast.LENGTH_SHORT).show();
          MediaStore.Images.Media.insertImage(getContentResolver(), b, "Screen", "screen");
          
      }catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
	}
	
	private void playMindMap() {
		
	 TextView root = (TextView)findViewById(R.id.edit_message);
	
		List<Animation> anims = new ArrayList<Animation>();
		
		ArrayList<Integer> child_queue = new ArrayList<Integer>();
		child_queue.add(root.getId());int idx = 0;
		while(idx!=child_queue.size()){
			Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
			animation.setDuration(1000);
			 animation.setStartOffset(anim.getDuration() * idx);
		    anims.add(animation);
			int id = child_queue.get(idx++);
			
			if(child_table.containsKey(id))
			{
				ArrayList<Integer> list = child_table.get(id);
				
				for(int i=0;i<list.size();i++){
					int chd = list.get(i);
					String par_chd = id+":"+chd;
					if(edge_table.containsKey(par_chd)){
						int eid = edge_table.get(par_chd);
						View ev = (View)findViewById(eid);
						ev.setVisibility(View.INVISIBLE);
						child_queue.add(eid);
					}
					child_queue.add(chd);
				}
			}
			View ctv = (View)findViewById(id);
			ctv.setVisibility(View.INVISIBLE);
		}
		for(int i = 0 ; i < child_queue.size() ; i++)
		{
			int child_id = child_queue.get(i);
			final View ctv = (View)findViewById(child_id);
			//Animation animation = AnimationUtils.makeOutAnimation(this, false);
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.textview_animation);
			animation.setDuration(1000);
			
			 animation.setStartOffset(animation.getDuration() * i);
			 
			 animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					ctv.setVisibility(View.VISIBLE);
					System.out.println("Is an edge? "+(ctv instanceof CustomView));
					if(ctv instanceof CustomView == false)
					{
						HorizontalScrollView h_scroll = (HorizontalScrollView) findViewById(R.id.HorizontalScrollView);
						ObjectAnimator h_animator = ObjectAnimator.ofInt(h_scroll,
								"scrollX", (int) ctv.getX() - 200);
						ScrollView v_scroll = (ScrollView) findViewById(R.id.main_scroll_view);
						ObjectAnimator v_animator = ObjectAnimator.ofInt(v_scroll,
								"scrollY", (int) ctv.getY() - 200);
						h_animator.setDuration(1000);

						v_animator.setDuration(1000);
						h_animator.start();
						v_animator.start();
					}
				}
			});
			ctv.startAnimation(animation);
		}	
		System.out.println("bfs size : "+child_queue.size());
	}
}
