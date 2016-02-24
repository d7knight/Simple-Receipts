/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mangelok.simrec;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;


/**
 * @author Michael Knight
 */
public class DisplayReceipt extends Activity {

    final private static String[] dayNames = {"Sunday", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday", "Saturday"};
    final public static String[] monthNames = {"January", "February", "March", "April",
            "May", "June", "July", "August", "September", "October", "November",
            "December"};
    public static int[] nCurrentDate = new int[4];//0 for day, 1 for month, 2 for year
    private int receiptId;
    DecimalFormat IDForm = new DecimalFormat("#000");

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
        if (b.getString("COMMENTS") == null) {
            TextView addcom = (TextView) findViewById(R.id.addcm);
            addcom.setVisibility(View.GONE);
            comments.setVisibility(View.GONE);
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
        date.setText(sDate);
    }

    public static int getAmountOfReceipts() {
        int nNewID = 0;
        File rDir = new File(SimpleReceipt.storageDir + "receipts/");
        rDir.mkdirs();
        int[] rIDs = new int[rDir.listFiles().length];
        for (int nCounter = 0; nCounter < rDir.listFiles().length; nCounter++) {
            String sFileName = rDir.listFiles()[nCounter].getName();
            if (sFileName.contains(".")) {
                try {
                    rIDs[nCounter] = Integer.parseInt(sFileName.substring(0, sFileName.indexOf(".")));
                } catch (NumberFormatException e) {
                }
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
        rNo.setText(IDForm.format(nNewID));
        this.receiptId = nNewID;
    }

    private void createImage(String inputPath) {

        RelativeLayout rec = (RelativeLayout) findViewById(R.id.fullreceipt);
        float srcWidth = rec.getWidth();
        float srcHeight = rec.getHeight();
        float aspect = srcHeight / srcWidth;
        float dstWidth = 525;
        float dstHeight = aspect * dstWidth;

        Bitmap largeBitmap = Bitmap.createBitmap(rec.getWidth(), rec.getHeight(), Bitmap.Config.RGB_565);
        Canvas largeCanvas = new Canvas(largeBitmap);
        rec.draw(largeCanvas);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(inputPath);
            largeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(View view) throws IOException, URISyntaxException {
        String filePathNoExt = SimpleReceipt.storageDir + "receipts/" + IDForm.format(this.receiptId);

        String imageName = filePathNoExt + ".png";
        String pdfName = filePathNoExt + ".pdf";
        createImage(imageName);
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfName));
            document.open();
            Image finalImage = Image.getInstance(imageName);
            finalImage.scaleToFit(530, PageSize.LETTER.getHeight());
            document.add(finalImage);

            document.close();
        } catch (IOException e){
            Log.e("MAK", "Something went wrong with pdf conversion." + e.getMessage());
        } catch (BadElementException e) {
            Log.e("MAK", "Something went wrong with pdf conversion." + e.getMessage());
        } catch (DocumentException e) {
            Log.e("MAK", "Something went wrong with pdf conversion." + e.getMessage());
        }
        File attachment = new File(pdfName);
        DecimalFormat form = new DecimalFormat("#0.00");
        Bundle b = this.getIntent().getExtras();
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("application/pdf");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Receipt of $" + form.format(b.getDouble("AMOUNT")));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello " + b.getString("NAME") + ",\n"
                + "Your receipt for " + form.format(b.getDouble("AMOUNT")) + " is attached to this email.\n\nThanks,\nAllan");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));
        startActivity(emailIntent);
        setResult(2);
    }

    public void back(View v) {
        finish();
        System.exit(0);
    }
}
