/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mangelok.simrec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;


/**
 *
 * @author Michael Knight
 */
public class DisplayReceipt extends Activity {

    final private static String[] dayNames = {"Sunday", "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday"};
    final public static String[] monthNames = {"January", "February", "March", "April",
        "May", "June", "July", "August", "September", "October", "November",
        "December"};
    public static int[] nCurrentDate = new int[4];//0 for day, 1 for month, 2 for year
    private String sFilePath;
    private String pdfname;

    @Override
    public void onCreate(Bundle savedInstance) {
        DecimalFormat form = new DecimalFormat("#0.00");
        super.onCreate(savedInstance);
        Bundle b = this.getIntent().getExtras();
        setContentView(R.layout.main);
        getID();
        TextView amount = (TextView) findViewById(R.id.amount);
        TextView name = (TextView) findViewById(R.id.name);
        TextView date = (TextView) findViewById(R.id.date);
        TextView comments = (TextView) findViewById(R.id.comments);
        comments.setText(b.getString("COMMENTS"));
        if (b.getString("COMMENTS")==null){
        	TextView addcom=(TextView)findViewById(R.id.addcm);
        	addcom.setText("");
        }
        amount.setText(numConv.convertPrice(b.getDouble("AMOUNT")) + " - $" + form.format(b.getDouble("AMOUNT")) + " ");
        name.setText(b.getString("NAME"));
        
        Calendar currentCal = Calendar.getInstance();
        nCurrentDate[0] = currentCal.get(Calendar.DAY_OF_MONTH);
        nCurrentDate[1] = currentCal.get(Calendar.MONTH);
        nCurrentDate[2] = currentCal.get(Calendar.YEAR);
        nCurrentDate[3] = currentCal.get(Calendar.DAY_OF_WEEK);
        String sDate = dayNames[nCurrentDate[3] - 1] + ", " + monthNames[nCurrentDate[1]] + " "
                + nCurrentDate[0] + " " + nCurrentDate[2];
        date.setText("DATE:"+sDate);

    }

    public static int getAmountOfReceipts(){
    	int nNewID = 0;
        File rDir = new File(SimpleReceipt.storageDir + "receipts/");
        rDir.mkdirs();
        int[] rIDs = new int[rDir.listFiles().length];
        for (int nCounter = 0; nCounter < rDir.listFiles().length; nCounter++) {
            String sFileName = rDir.listFiles()[nCounter].getName();
            if (sFileName.contains(".")) {
                try{
                rIDs[nCounter] = Integer.parseInt(sFileName.substring(0, sFileName.indexOf(".")));}
                catch(NumberFormatException e){}
                if (rIDs[nCounter] > nNewID) {
                    nNewID = rIDs[nCounter];
                }
            }
        }
        return nNewID;
    }
    public void getID() {
        TextView rNo = (TextView) findViewById(R.id.rno);
        int nNewID = getAmountOfReceipts();
        nNewID++;
        DecimalFormat IDForm = new DecimalFormat("#000");
        rNo.setText(IDForm.format(nNewID));
        this.sFilePath = SimpleReceipt.storageDir + "receipts/" + IDForm.format(nNewID) + ".png";
    }


    public void print(View view)throws IOException, URISyntaxException {

        DecimalFormat form = new DecimalFormat("#0.00");
        Bundle b = this.getIntent().getExtras();
        RelativeLayout rec = (RelativeLayout)findViewById(R.id.fullreceipt);

        Bitmap largeBitmap = Bitmap.createBitmap(rec.getWidth(), rec.getHeight(), Bitmap.Config.RGB_565);
        Canvas largeCanvas = new Canvas(largeBitmap);

        rec.draw(largeCanvas);



        FileOutputStream out = null;
        try {
            out = new FileOutputStream(this.sFilePath);

            largeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();
        }
        out.flush();
        out.close();

        bmptopdf();
        File f = new File(this.pdfname);






        PackageManager pm = getPackageManager();
        if(isAppInstalled(this, "com.pauloslf.cloudprint")) {
            Intent intent = new Intent(Intent.ACTION_SEND);

            intent.setPackage("com.pauloslf.cloudprint");

            intent.setDataAndType(Uri.fromFile(f), "text/html");
            startActivity(intent);
        }
        else{
            Toast.makeText(DisplayReceipt.this,
                    "Failure printing pdf!",
                    Toast.LENGTH_SHORT).show();
        }
///data/app/com.pauloslf.cloudprint-1/base.apk=com.pauloslf.cloudprint]




    }
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
//boolean whatsappFound = AndroidHelper.isAppInstalled(getActivity(), "com.whatsapp");

    public void send(View view) throws IOException, URISyntaxException {


        DecimalFormat form = new DecimalFormat("#0.00");
        Bundle b = this.getIntent().getExtras();
        RelativeLayout rec = (RelativeLayout)findViewById(R.id.fullreceipt);
        
        Bitmap largeBitmap = Bitmap.createBitmap(rec.getWidth(), rec.getHeight(), Bitmap.Config.RGB_565);
        Canvas largeCanvas = new Canvas(largeBitmap);
        
        rec.draw(largeCanvas);
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("image/png");

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(this.sFilePath);
            
            largeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();
        }
        out.flush();
        out.close();
        
        bmptopdf();
        File f = new File(this.pdfname);
        
        
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Receipt of $" + form.format(b.getDouble("AMOUNT")));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello " + b.getString("NAME") + ",\n"
                + "Your receipt for " + form.format(b.getDouble("AMOUNT")) + " is attached to this email.\n\nThanks,\nAllan");
       
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
        
        Uri r = (Uri)emailIntent.getExtras().get(Intent.EXTRA_STREAM);
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches) {
            if (info.activityInfo.name.toLowerCase().contains("mail")) {
                best = info;
            }else{
            	Log.i("RECEIPTS", info.activityInfo.name);
            }
        }
        if (best != null) {
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        }
        startActivity(emailIntent);
        setResult(2);
        
        
    }
    public void back(View v){
        finish();
        System.exit(0);
    }
	public void bmptopdf(){
		 try {
			   
			        pdfname=sFilePath.replace(".png", ".pdf");
			        Document document = new Document();

			
			        PdfWriter.getInstance(document,
			                new FileOutputStream(pdfname));
			        document.open();

			        Image image1 = Image.getInstance(sFilePath);
             float documentWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
             float documentHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
             image1.scaleToFit(documentWidth, documentHeight);
			        document.add(image1);

			        
			       
			        document.close();
			   
			}  catch(Exception e){
				 Toast.makeText(DisplayReceipt.this, 
			                "Failure doing bmp-to-pdf!", 
			                Toast.LENGTH_SHORT).show();
				 
			 }
}
}
