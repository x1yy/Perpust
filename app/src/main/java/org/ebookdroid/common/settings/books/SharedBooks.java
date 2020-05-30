package org.ebookdroid.common.settings.books;

import com.antaraksi.android.utils.IO;
import com.antaraksi.android.utils.LOG;
import com.antaraksi.android.utils.Objects;
import com.antaraksi.android.utils.TxtUtils;
import com.antaraksi.dao2.FileMeta;
import com.antaraksi.model.AppBook;
import com.antaraksi.model.AppProfile;
import com.antaraksi.pdf.info.ExtUtils;
import com.antaraksi.ui2.AppDB;

import org.perpust.LinkedJSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedBooks {


    public static void updateProgress(List<FileMeta> list, boolean updateTime) {

        for (FileMeta meta : list) {
            try {
                AppBook book = SharedBooks.load(meta.getPath());
                meta.setIsRecentProgress(book.p);
                if (updateTime) {
                    meta.setIsRecentTime(book.t);
                }
            } catch (Exception e) {
                LOG.e(e);
            }
        }
        AppDB.get().updateAll(list);
    }

    public static Map<String, AppBook> cache = new HashMap<>();

    public static void deleteProgress(String path) {
        cache.clear();
        for (File fileName : AppProfile.getAllFiles(AppProfile.APP_PROGRESS_JSON)) {
            LinkedJSONObject linkedJsonObject = IO.readJsonObject(fileName);
            String key = ExtUtils.getFileName(path);
            if (linkedJsonObject.has(key)) {
                linkedJsonObject.remove(key);
                IO.writeObjAsync(fileName, linkedJsonObject);
                LOG.d("deleteProgress", path);
            }
        }
    }

    public static AppBook load(String fileName) {
        LOG.d("SharedBooks-load", fileName);

        if (cache.containsKey(fileName)) {
            LOG.d("SharedBooks-load-from-cache", fileName);
            return cache.get(fileName);
        }

        AppBook res = new AppBook(fileName);
        AppBook original = null;

        for (File file : AppProfile.getAllFiles(AppProfile.APP_PROGRESS_JSON)) {
            final AppBook load = load(IO.readJsonObject(file), fileName);
            load.path = fileName;

            if (file.equals(AppProfile.syncProgress) && load != null) {
                original = load;
            }

            if (load.t >= res.t) {
                res = load;
            }
        }
        if (original != null) {
            original.p = res.p;
            original.t = Math.max(res.t, original.t);
            LOG.d("SharedBooks-load1 original", fileName, res.p);
            cache.put(fileName, original);
            return original;
        }

        LOG.d("SharedBooks-load1 general", fileName, res.p);
        cache.put(fileName, res);
        return res;

    }

    private static AppBook load(LinkedJSONObject obj, String fileName) {
        AppBook bs = new AppBook(fileName);
        try {

            LOG.d("SharedBooks-load", bs.path);
            final String key = ExtUtils.getFileName(fileName);
            if (!obj.has(key)) {
                return bs;
            }
            final LinkedJSONObject rootObj = obj.getJSONObject(key);
            Objects.loadFromJson(bs, rootObj);
        } catch (Exception e) {
            LOG.e(e);
        }
        return bs;
    }

    public static void save(AppBook bs) {
        save(bs, true);
    }

    static int phash = -1;

    public static void save(AppBook bs, boolean inThread) {

        int hash = bs.hashCode();
        if (phash == hash) {
            LOG.d("SharedBooks-Save", "skip", hash);
            return;
        }
        phash = hash;
        LOG.d("SharedBooks-Save", "inThread " + inThread);


        if (bs == null || TxtUtils.isEmpty(bs.path)) {
            LOG.d("Can't save AppBook");
            return;
        }

        try {
            final LinkedJSONObject obj = IO.readJsonObject(AppProfile.syncProgress);

            if (bs.p > 1) {
                bs.p = 0;
            }

            final String fileName = ExtUtils.getFileName(bs.path);
            final LinkedJSONObject value = Objects.toJSONObject(bs);
            obj.put(fileName, value);
            cache.put(fileName, bs);

            LOG.d("SharedBooks-Save", value);


            if (inThread) {
                IO.writeObj(AppProfile.syncProgress, obj);
            } else {
                IO.writeObjAsync(AppProfile.syncProgress, obj);
            }
        } catch (Exception e) {
            LOG.e(e);
        }


    }


}
