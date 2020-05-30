package com.antaraksi.pdf.search.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.antaraksi.android.utils.LOG;
import com.antaraksi.android.utils.Objects;
import com.antaraksi.android.utils.ResultResponse;
import com.antaraksi.model.AppState;
import com.antaraksi.pdf.info.R;
import com.antaraksi.pdf.info.TintUtil;

public abstract class AsyncProgressResultToastTask extends AsyncTask<Object, Object, Boolean> {

    ProgressDialog dialog;
    Context c;
    ResultResponse<Boolean> onResult;

    public AsyncProgressResultToastTask(Context c, ResultResponse<Boolean> onResult) {
        this.c = c;
        this.onResult = onResult;
    }

    public AsyncProgressResultToastTask(Context c) {
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(c, "", c.getString(R.string.please_wait));

        try {
            android.widget.ProgressBar pr = (android.widget.ProgressBar) Objects.getInstanceValue(dialog, "mProgress");
            pr.setSaveEnabled(false);
            TintUtil.setDrawableTint(pr.getIndeterminateDrawable().getCurrent(), AppState.get().isDayNotInvert ? TintUtil.color : Color.WHITE);
        } catch (Exception e) {
            LOG.e(e);
        }


    }


    @Override
    protected void onPostExecute(Boolean result) {
        try {
            dialog.dismiss();
        } catch (Exception e) {
            LOG.d(e);
        }
        if (onResult != null) {
            onResult.onResultRecive(result);
        }

        if (result) {
            Toast.makeText(c, R.string.success, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(c, R.string.fail, Toast.LENGTH_LONG).show();
        }
    }

}
