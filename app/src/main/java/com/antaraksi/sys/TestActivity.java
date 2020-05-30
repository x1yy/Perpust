package com.antaraksi.sys;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.antaraksi.android.utils.Dips;
import com.antaraksi.android.utils.LOG;
import com.antaraksi.pdf.info.R;

import mobi.librera.smartreflow.AndroidPlatformImage;
import mobi.librera.smartreflow.ImageUtils;
import mobi.librera.smartreflow.SmartReflow1;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageUtils.platformLogger = new ImageUtils.Logger() {
            @Override
            public void log(String str) {
                LOG.d(str);
            }
        };

        try {

            final AndroidPlatformImage input = new AndroidPlatformImage(BitmapFactory.decodeResource(getResources(), R.drawable.sample6));
            final AndroidPlatformImage output = new AndroidPlatformImage(input.getWidth() / 2, Dips.screenHeight());

            SmartReflow1 sm = new SmartReflow1();
            sm.process(input);
            sm.reflow(output);

            ImageView img = new ImageView(this);
            img.setImageBitmap(output.getImage());


            setContentView(img);
        } catch (Exception e) {
            LOG.e(e);
        }
    }
}
