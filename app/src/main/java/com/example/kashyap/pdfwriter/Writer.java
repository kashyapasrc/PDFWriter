package com.example.kashyap.pdfwriter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Writer {


    private final Context mContext;

    public Writer(Context context) {
        this.mContext = context;
    }

    void outputToFile(String pdfContent, String encoding) {
        File newFile = new File(Environment.getExternalStorageDirectory() + "/" + "helloworld.pdf");
        try {
            newFile.createNewFile();
            try {
                FileOutputStream pdfFile = new FileOutputStream(newFile);
                pdfFile.write(pdfContent.getBytes(encoding));
                pdfFile.close();

                Uri printFileUri = Uri.parse("file://" + newFile.getAbsolutePath());

                startIntent(printFileUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startIntent(Uri printFileUri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setPackage("com.dynamixsoftware.printershare");
        i.setDataAndType(printFileUri, "application/pdf");
        PackageManager manager = mContext.getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(i, 0);
        if (infos.size() > 0) {
            mContext.startActivity(i);
//Then there is an Application(s) can handle your intent
        } else {
//No Application can handle your intent
        }


    }
}
