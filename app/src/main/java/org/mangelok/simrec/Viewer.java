package org.mangelok.simrec;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class Viewer extends ListActivity {
	public static Context appcontext;
	private ReceiptsAdapter adapter;
	private static Receipt[] items;

	private File getReceipt(int nC) {
		DecimalFormat IDForm = new DecimalFormat("#000");
		File f = new File(SimpleReceipt.storageDir + "receipts/"
				+ IDForm.format(nC) + ".png");
		return f;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appcontext = this;
		items = loadReceipts();
		this.adapter = new ReceiptsAdapter(this, android.R.layout.simple_list_item_1, items);
		setListAdapter(this.adapter);
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0,
					final View arg1, final int arg2, long arg3) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(
						appcontext);
				builder.setMessage("Would you like to resend this receipt?")
						.setCancelable(true)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int id) {
										send(getReceipt(arg2 + 1));
									}
								});
				builder.setNegativeButton("No",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dg, int id) {
								dg.dismiss();
							}
						});
				AlertDialog confirm = builder.create();
				confirm.show();
				return false;
			}

		});
		//getListView().setDrawSelectorOnTop(true);
		
	}

	public static void send(File f) {

		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		emailIntent.setType("plain/text");

		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Resent Receipt");
		emailIntent
				.putExtra(
						Intent.EXTRA_TEXT,
						"Hello,\n"
								+ "Your receipt from Draper Knight is attached to this email.\n\nThanks,\nAllan");
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));

		final PackageManager pm = appcontext.getPackageManager();
		final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent,
				0);
		ResolveInfo best = null;
		for (final ResolveInfo info : matches) {
			if (info.activityInfo.packageName.endsWith(".gm")
					|| info.activityInfo.name.toLowerCase().contains("gmail")) {
				best = info;
			}
		}
		if (best != null) {
			emailIntent.setClassName(best.activityInfo.packageName,
					best.activityInfo.name);
		}
		appcontext.startActivity(emailIntent);
	}

	public Receipt[] loadReceipts() {
		Receipt[] r = new Receipt[DisplayReceipt.getAmountOfReceipts()];

		for (int nC = 0; nC < r.length; nC++) {
			r[nC] = new Receipt(getReceipt(nC + 1));
		}
		return r;
	}

	private class Receipt {
		View v;

		Receipt(File f) {
			ImageView a = new ImageView(appcontext);
			BitmapDrawable bd = new BitmapDrawable(f.getAbsolutePath());
			a.setImageDrawable(bd);
			v=a;
		}

	}

	private class ReceiptsAdapter extends ArrayAdapter<Receipt> {

		private Receipt[] items;

		public ReceiptsAdapter(Context context, int textViewResourceId,
				Receipt[] items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return items[position].v;
		}

	}
}
