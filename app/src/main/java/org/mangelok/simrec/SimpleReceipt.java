package org.mangelok.simrec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleReceipt extends Activity {

	public static String storageDir = "/mnt/sdcard/simplereceipts/";
	public ArrayList<String> autoFields;
	private Context appContext;
	private AutoCompleteTextView cName;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.generator);
		appContext = this;
		autoFields = new ArrayList<String>();
		this.cName = (AutoCompleteTextView) findViewById(R.id.name1);
		File f = new File(storageDir);
		File u = new File(storageDir + "fields.txt");
		if (!f.exists() || !u.exists()) {
			try {
				f.mkdirs();
				u.createNewFile();
			} catch (IOException ex) {
				Logger.getLogger(SimpleReceipt.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}

		try {
			initField();
		} catch (FileNotFoundException ex) {
			Log.e("ERROR", ex.getMessage());
		}
	}

	public void viewer(View v) {
		Intent i = new Intent(this, Viewer.class);
		startActivity(i);

	}

	public void writeField() throws IOException {
		AutoCompleteTextView name = (AutoCompleteTextView) findViewById(R.id.name1);
		autoFields.add(name.getText().toString());
		String eol = System.getProperty("line.separator");
		try {

			FileWriter fos = new FileWriter(storageDir + "fields.txt");
			String currentDetail = "";
			for (int nLineCounter = 0; nLineCounter < autoFields.size(); nLineCounter++) {
				currentDetail += autoFields.get(nLineCounter).toString() + eol;
			}
			fos.write(currentDetail);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			Log.e("writeField", "ERROR " + e.getMessage());

		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			initField();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(SimpleReceipt.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		try {
			initField();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(SimpleReceipt.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public void initField() throws FileNotFoundException {

		cName.setOnFocusChangeListener(new AdapterView.OnFocusChangeListener() {

			public void onFocusChange(View arg0, boolean arg1) {
				cName.dismissDropDown();
			}
		});

		Scanner fin = new Scanner(new FileReader(storageDir + "fields.txt"));

		while (fin.hasNext()) {
			String sCurrentLine = fin.nextLine();
			if (!autoFields.contains(sCurrentLine) && !sCurrentLine.equals("")) {
				autoFields.add(sCurrentLine);

			}
		}
		if (!autoFields.isEmpty()) {

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(appContext,
					R.layout.autocell, autoFields) {

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {

					return createView(position);
				}

				private RelativeLayout createView(final int position) {
					LayoutInflater mInflater = (LayoutInflater) appContext
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					ViewGroup rLay = (ViewGroup) mInflater.inflate(
							R.layout.autocell, null);
					final TextView text = (TextView) rLay.getChildAt(0);
					text.setText(getItem(position).toString());
					text.setOnClickListener(new View.OnClickListener() {

						public void onClick(View arg0) {
							cName.setText(text.getText());
							cName.dismissDropDown();

						}
					});
					ImageButton delete = (ImageButton) rLay.getChildAt(1);
					delete.setOnClickListener(new View.OnClickListener() {

						public void onClick(View arg0) {
							autoFields.remove(position);
							try {
								writeField();
							} catch (IOException ex) {
								Logger.getLogger(SimpleReceipt.class.getName())
										.log(Level.SEVERE, null, ex);
							}
							try {
								initField();
							} catch (FileNotFoundException ex) {
								Logger.getLogger(SimpleReceipt.class.getName())
										.log(Level.SEVERE, null, ex);
							}

						}
					});
					return (RelativeLayout) rLay;
				}

				@Override
				public View getDropDownView(int position, View convertView,
						ViewGroup parent) {
					return createView(position);
				}
			};

			Log.e("Adapter Set!", "Adapter:" + (adapter == null) + " "
					+ this.autoFields.get(0));
			cName.setAdapter(adapter);
		}
	}

	public void submit(View view) {

		EditText amount = (EditText) findViewById(R.id.amount1);
		AutoCompleteTextView name = (AutoCompleteTextView) findViewById(R.id.name1);

		try {
			writeField();
		} catch (IOException ex) {
		}
		if (name.getText() == null || amount.getText() == null
				|| amount.getText().toString().equals("")
				|| name.getText().toString().equals("")) {
			(Toast.makeText(this, "You must specify a name AND a price!",
					Toast.LENGTH_LONG)).show();
			return;
		}
		Intent displayIntent = new Intent(this, DisplayReceipt.class);
		displayIntent.putExtra("NAME", name.getText().toString());
		displayIntent.putExtra("AMOUNT",
				Double.parseDouble(amount.getText().toString()));
		EditText tv = (EditText) findViewById(R.id.commentes);
		String comments = (String) tv.getText().toString();
		if (!(comments == null || comments.equals("") || comments.equals(" "))) {
			displayIntent.putExtra("COMMENTS", comments);
		}

		startActivityForResult(displayIntent, 1);
	}

	@Override
	protected void onActivityResult(int rqCode, int rsCode, Intent data) {
		if (rsCode == 2) {
			finish();
		}
	}

	public void clear(View view) {
		try {
			writeField();

		} catch (IOException ex) {
			Logger.getLogger(SimpleReceipt.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		try {
			initField();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(SimpleReceipt.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		AutoCompleteTextView name = (AutoCompleteTextView) findViewById(R.id.name1);
		EditText amount = (EditText) findViewById(R.id.amount1);
		name.setText("");
		amount.setText("");
	}
}
