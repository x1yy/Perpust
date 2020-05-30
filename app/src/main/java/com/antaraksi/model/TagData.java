package com.antaraksi.model;

import com.antaraksi.android.utils.IO;
import com.antaraksi.android.utils.LOG;
import com.antaraksi.android.utils.StringDB;
import com.antaraksi.dao2.FileMeta;
import com.antaraksi.ui2.AppDB;

import org.perpust.JSONException;
import org.perpust.LinkedJSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class TagData {


    public static class Tag implements MyPath.RelativePath {
        public String path;
        public String tags;

        public Tag() {
        }

        public Tag(String path, String tags) {
            this.path = MyPath.toRelative(path);
            this.tags = tags;
        }

        public String getPath() {
            return MyPath.toAbsolute(path);
        }

        public void setPath(String path) {
            this.path = MyPath.toRelative(path);
        }
    }


    public static void saveTags(FileMeta meta) {
        saveTags(meta.getPath(), meta.getTag());
    }

    public static void saveTags(String path, String tags) {
        try {
            LinkedJSONObject obj = IO.readJsonObject(AppProfile.syncTags);
            obj.put(MyPath.toRelative(path), tags);
            IO.writeObjAsync(AppProfile.syncTags, obj);
            LOG.d("saveTags", tags, path);
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public static String getTags(String path) {
        try {
            LinkedJSONObject obj = IO.readJsonObject(AppProfile.syncTags);
            return obj.getString(MyPath.toRelative(path));
        } catch (Exception e) {
            LOG.e(e);
        }
        return "";
    }

    public static void restoreTags() {
        LOG.d("restoreTags");

        final List<FileMeta> allWithTag = AppDB.get().getAllWithTag();
        for (FileMeta m : allWithTag) {
            m.setTag(null);
        }
        AppDB.get().updateAll(allWithTag);


        for (File file : AppProfile.getAllFiles(AppProfile.APP_TAGS_JSON)) {
            LinkedJSONObject obj = IO.readJsonObject(file);

            final Iterator<String> keys = obj.keys();

            while (keys.hasNext()) {
                final String key = keys.next();

                try {

                    Tag tag = new Tag(key, obj.getString(key));
                    LOG.d("restoreTags-in", tag.path, tag.tags);

                    FileMeta load = AppDB.get().load(tag.getPath());
                    if (load != null) {
                        if (load.getTag() != null) {
                            load.setTag(StringDB.merge(load.getTag(), tag.tags));
                            LOG.d("restoreTags-do-merge", tag.getPath(), load.getTag());
                        } else {
                            load.setTag(tag.tags);
                            LOG.d("restoreTags-do", tag.getPath(), tag.tags);

                        }
                        AppDB.get().update(load);
                    }
                } catch (JSONException e) {
                    LOG.e(e);
                }

            }
        }
        AppDB.get().clearSession();


    }

}
