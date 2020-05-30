package org.ebookdroid;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.multidex.MultiDexApplication;

import com.antaraksi.android.utils.Apps;
import com.antaraksi.android.utils.Dips;
import com.antaraksi.android.utils.LOG;
import com.antaraksi.ext.CacheZipUtils;
import com.antaraksi.pdf.info.AppsConfig;
import com.antaraksi.pdf.info.BuildConfig;
import com.antaraksi.pdf.info.IMG;
import com.antaraksi.pdf.info.R;
import com.antaraksi.pdf.info.TintUtil;
import com.antaraksi.tts.TTSNotification;
import com.google.android.gms.ads.MobileAds;

import org.ebookdroid.droids.mupdf.codec.MuPdfDocument;

import java.io.PrintWriter;
import java.io.StringWriter;




public class PerpustApp extends MultiDexApplication {

    public static Context context;
    public final static int MUPDF_VERSION;


    static {
        System.loadLibrary("mypdf");
        System.loadLibrary("mobi");
        System.loadLibrary("antiword");
        MUPDF_VERSION = MuPdfDocument.getMupdfVersion();
    }

    @Override
    public void onCreate() {
        super.onCreate();



        context = getApplicationContext();
        Dips.init(this);

        if (!AppsConfig.checkIsProInstalled(this)) {
            MobileAds.initialize(this, Apps.getMetaData(this, "com.google.android.gms.ads.APPLICATION_ID"));
        }


        LOG.isEnable = BuildConfig.DEBUG || BuildConfig.LOG || BuildConfig.IS_BETA;

        TTSNotification.initChannels(this);


        CacheZipUtils.init(this);

        IMG.init(this);

        LOG.d("Build", "Build.MANUFACTURER", Build.MANUFACTURER);
        LOG.d("Build", "Build.PRODUCT", Build.PRODUCT);
        LOG.d("Build", "Build.DEVICE", Build.DEVICE);
        LOG.d("Build", "Build.BRAND", Build.BRAND);
        LOG.d("Build", "Build.MODEL", Build.MODEL);

        LOG.d("Build", "Build.screenWidth", Dips.screenWidthDP(), Dips.screenWidth());

        LOG.d("Build.Context", "Context.getFilesDir()", getFilesDir());
        LOG.d("Build.Context", "Context.getCacheDir()", getCacheDir());
        LOG.d("Build.Context", "Context.getExternalCacheDir", getExternalCacheDir());
        LOG.d("Build.Context", "Context.getExternalFilesDir(null)", getExternalFilesDir(null));
        LOG.d("Build.Context", "Environment.getExternalStorageDirectory()", Environment.getExternalStorageDirectory());
        LOG.d("Build.Height", Dips.screenHeight());


        if (false) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, final Throwable e) {
                    LOG.e(e);
                    e.printStackTrace();
                    try {

                        StringWriter errors = new StringWriter();
                        e.printStackTrace(new PrintWriter(errors));
                        String log = errors.toString();
                        log = log + "/n";
                        log = log + Build.MANUFACTURER + "/n";
                        log = log + Build.PRODUCT + "/n";
                        log = log + Build.DEVICE + "/n";
                        log = log + Build.BRAND + "/n";
                        log = log + Build.BRAND + "/n";
                        log = log + Build.MODEL + "/n";
                        log = log + Build.VERSION.SDK_INT + "/n";
                        Apps.onCrashEmail(context, log, context.getString(R.string.application_error_please_send_this_report_by_emial));

                        System.exit(1);

                    } catch (Exception e1) {
                        LOG.e(e1);
                    }
                }
            });
        }


    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LOG.d("AppState save onLowMemory");
        IMG.clearMemoryCache();
        TintUtil.clean();
    }

}