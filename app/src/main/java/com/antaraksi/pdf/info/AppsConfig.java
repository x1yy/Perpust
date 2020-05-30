package com.antaraksi.pdf.info;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.antaraksi.android.utils.Dips;
import com.antaraksi.android.utils.LOG;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.ebookdroid.PerpustApp;

public class AppsConfig {

    public static final String PRO_PERPUST_READER = "com.antaraksi.pro.pdf.reader";
    public static final String PERPUST_READER = "com.antaraksi.pdf.reader";
    public static final boolean ADS_ON_PAGE = false;
    public static int MUPDF_1_11 = 111;
    public static int MUPDF_1_16 = 116;
    public static boolean isDOCXSupported = Build.VERSION.SDK_INT >= 26;
    public static boolean isCloudsEnable = false;


    public static  boolean isPDF_DRAW_ENABLE(){
        return PerpustApp.MUPDF_VERSION  == MUPDF_1_11;
    }

    public static boolean checkIsProInstalled(final Context a) {
        if (a == null) {
            LOG.d("no-ads error context null");
            return true;
        }
        if (!isGooglePlayServicesAvailable(a)) {
            //no ads for old android and eink
            LOG.d("no-ads isGooglePlayServicesAvailable not available");
            return true;
        }
        if (Build.VERSION.SDK_INT <= 16 || Dips.isEInk()) {
            LOG.d("no-ads old device or eink");
            //no ads for old android and eink
            return true;
        }

        boolean is_pro = isPackageExisted(a, PRO_PERPUST_READER);
        return is_pro;
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static boolean isPackageExisted(final Context a, final String targetPackage) {
        try {
            final PackageManager pm = a.getPackageManager();
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (final NameNotFoundException e) {
            return false;
        }
        return true;
    }

}
