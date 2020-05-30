package com.antaraksi.ui2;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.antaraksi.android.utils.Apps;
import com.antaraksi.android.utils.JsonDB;
import com.antaraksi.android.utils.LOG;
import com.antaraksi.android.utils.TxtUtils;
import com.antaraksi.dao2.FileMeta;
import com.antaraksi.drive.GFile;
import com.antaraksi.ext.CacheZipUtils.CacheDir;
import com.antaraksi.ext.EbookMeta;
import com.antaraksi.model.AppData;
import com.antaraksi.model.AppProfile;
import com.antaraksi.model.AppSP;
import com.antaraksi.model.SimpleMeta;
import com.antaraksi.model.TagData;
import com.antaraksi.pdf.info.Clouds;
import com.antaraksi.pdf.info.ExtUtils;
import com.antaraksi.pdf.info.IMG;
import com.antaraksi.pdf.info.R;
import com.antaraksi.pdf.info.io.SearchCore;
import com.antaraksi.pdf.info.model.BookCSS;
import com.antaraksi.pdf.search.activity.msg.MessageSync;
import com.antaraksi.pdf.search.activity.msg.MessageSyncFinish;
import com.antaraksi.pdf.search.activity.msg.UpdateAllFragments;
import com.antaraksi.sys.ImageExtractor;
import com.antaraksi.sys.TempHolder;
import com.antaraksi.tts.TTSNotification;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.ebookdroid.common.settings.books.SharedBooks;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class BooksService extends IntentService {
    public static String TAG = "BooksService";
    public static String INTENT_NAME = "BooksServiceIntent";
    public static String ACTION_SEARCH_ALL = "ACTION_SEARCH_ALL";
    public static String ACTION_REMOVE_DELETED = "ACTION_REMOVE_DELETED";
    public static String ACTION_SYNC_DROPBOX = "ACTION_SYNC_DROPBOX";
    public static String ACTION_RUN_SYNCRONICATION = "ACTION_RUN_SYNCRONICATION";
    public static String RESULT_SYNC_FINISH = "RESULT_SYNC_FINISH";
    public static String RESULT_SEARCH_FINISH = "RESULT_SEARCH_FINISH";
    public static String RESULT_BUILD_LIBRARY = "RESULT_BUILD_LIBRARY";
    public static String RESULT_SEARCH_COUNT = "RESULT_SEARCH_COUNT";
    public static volatile boolean isRunning = false;
    Handler handler;
    boolean isStartForeground = false;
    Runnable timer2 = new Runnable() {

        @Override
        public void run() {
            sendBuildingLibrary();
            handler.postDelayed(timer2, 250);
        }
    };
    private MediaSessionCompat mediaSessionCompat;
    private List<FileMeta> itemsMeta = new LinkedList<FileMeta>();
    Runnable timer = new Runnable() {

        @Override
        public void run() {
            sendProggressMessage();
            handler.postDelayed(timer, 250);
        }
    };

    public BooksService() {
        super("BooksService");
        handler = new Handler();
        LOG.d("BooksService", "Create");
    }

    public static void sendFinishMessage(Context c) {
        Intent intent = new Intent(INTENT_NAME).putExtra(Intent.EXTRA_TEXT, RESULT_SEARCH_FINISH);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }

    public static void startForeground(Activity a, String action) {
        final Intent intent = new Intent(a, BooksService.class).setAction(action);
        a.startService(intent);

//        if (Build.VERSION.SDK_INT >= 26) {
//            a.startForegroundService(intent);
//        } else {
//            a.startService(intent);
//
//        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStartForeground = false;
        LOG.d("BooksService", "onDestroy");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //startMyForeground();
    }

    public void startMyForeground() {
        if (!isStartForeground) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                Notification notification = new NotificationCompat.Builder(this, TTSNotification.DEFAULT) //
                        .setSmallIcon(R.drawable.glyphicons_748_synchronization1) //
                        .setContentTitle(Apps.getApplicationName(this)) //
                        .setContentText(getString(R.string.please_wait_books_are_being_processed_))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)//
                        .build();

                startForeground(TTSNotification.NOT_ID_2, notification);
            }
            AppProfile.init(this);
            isStartForeground = true;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //startMyForeground();


        if (intent == null) {
            return;
        }

        try {
            if (isRunning) {
                LOG.d(TAG, "BooksService", "Is-running");
                return;
            }

            isRunning = true;
            LOG.d(TAG, "BooksService", "Action", intent.getAction());

            //TESET


            if (ACTION_RUN_SYNCRONICATION.equals(intent.getAction())) {
                if (AppSP.get().isEnableSync) {


                    AppProfile.save(this);


                    try {
                        EventBus.getDefault().post(new MessageSync(MessageSync.STATE_VISIBLE));
                        AppSP.get().syncTimeStatus = MessageSync.STATE_VISIBLE;
                        GFile.sycnronizeAll(this);

                        AppSP.get().syncTime = System.currentTimeMillis();
                        AppSP.get().syncTimeStatus = MessageSync.STATE_SUCCESS;
                        EventBus.getDefault().post(new MessageSync(MessageSync.STATE_SUCCESS));
                    } catch (UserRecoverableAuthIOException e) {
                        GFile.logout(this);
                        AppSP.get().syncTimeStatus = MessageSync.STATE_FAILE;
                        EventBus.getDefault().post(new MessageSync(MessageSync.STATE_FAILE));
                    } catch (Exception e) {
                        AppSP.get().syncTimeStatus = MessageSync.STATE_FAILE;
                        EventBus.getDefault().post(new MessageSync(MessageSync.STATE_FAILE));
                        LOG.e(e);
                    }

                    if (GFile.isNeedUpdate) {
                        LOG.d("GFILE-isNeedUpdate", GFile.isNeedUpdate);
                        TempHolder.get().listHash++;
                        EventBus.getDefault().post(new UpdateAllFragments());
                    }

                }

            }


            if (ACTION_REMOVE_DELETED.equals(intent.getAction())) {
                List<FileMeta> all = AppDB.get().getAll();

                for (FileMeta meta : all) {
                    if (meta == null) {
                        continue;
                    }

                    if (Clouds.isCloud(meta.getPath())) {
                        continue;
                    }

                    File bookFile = new File(meta.getPath());
                    if (ExtUtils.isMounted(bookFile)) {
                        if (!bookFile.exists()) {
                            AppDB.get().delete(meta);
                            LOG.d("BooksService Delete-setIsSearchBook", meta.getPath());
                        }
                    }

                }

                List<FileMeta> localMeta = new LinkedList<FileMeta>();
                if(JsonDB.isEmpty(BookCSS.get().searchPathsJson)){
                    sendFinishMessage();
                    return;
                }

                for (final String path : JsonDB.get(BookCSS.get().searchPathsJson)) {
                    if (path != null && path.trim().length() > 0) {
                        final File root = new File(path);
                        if (root.isDirectory()) {
                            LOG.d(TAG, "Search in " + root.getPath());
                            SearchCore.search(localMeta, root, ExtUtils.seachExts);
                        }
                    }
                }


                for (FileMeta meta : localMeta) {
                    if (!all.contains(meta)) {
                        FileMetaCore.createMetaIfNeedSafe(meta.getPath(), true);
                        LOG.d("BooksService add book", meta.getPath());
                    }
                }


                List<FileMeta> allNone = AppDB.get().getAllByState(FileMetaCore.STATE_NONE);
                for (FileMeta m : allNone) {
                    LOG.d("BooksService-createMetaIfNeedSafe-service", m.getTitle(),m.getPath(), m.getTitle());
                    FileMetaCore.createMetaIfNeedSafe(m.getPath(), false);
                }

                sendFinishMessage();
                Clouds.get().syncronizeGet();

            } else if (ACTION_SEARCH_ALL.equals(intent.getAction())) {
                LOG.d(ACTION_SEARCH_ALL);
                //TempHolder.listHash++;
                //AppDB.get().getDao().detachAll();

                AppProfile.init(this);

                ImageExtractor.clearErrors();
                IMG.clearDiscCache();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        IMG.clearMemoryCache();
                    }
                });




                AppDB.get().deleteAllData();
                itemsMeta.clear();

                handler.post(timer);


                for (final String path : JsonDB.get(BookCSS.get().searchPathsJson)) {
                    if (path != null && path.trim().length() > 0) {
                        final File root = new File(path);
                        if (root.isDirectory()) {
                            LOG.d("Search in: " + root.getPath());
                            SearchCore.search(itemsMeta, root, ExtUtils.seachExts);
                        }
                    }
                }


                for (FileMeta meta : itemsMeta) {
                    meta.setIsSearchBook(true);
                }

                final List<SimpleMeta> allExcluded = AppData.get().getAllExcluded();

                if (TxtUtils.isListNotEmpty(allExcluded)) {
                    for (FileMeta meta : itemsMeta) {
                        if (allExcluded.contains(SimpleMeta.SyncSimpleMeta(meta.getPath()))) {
                            meta.setIsSearchBook(false);
                        }
                    }
                }

                final List<FileMeta> allSyncBooks = AppData.get().getAllSyncBooks();
                if (TxtUtils.isListNotEmpty(allSyncBooks)) {
                    for (FileMeta meta : itemsMeta) {
                        for (FileMeta sync : allSyncBooks) {
                            if (meta.getTitle().equals(sync.getTitle()) && !meta.getPath().equals(sync.getPath())) {
                                meta.setIsSearchBook(false);
                                LOG.d(TAG, "remove-dublicate", meta.getPath());
                            }
                        }

                    }
                }


                itemsMeta.addAll(AppData.get().getAllFavoriteFiles(false));
                itemsMeta.addAll(AppData.get().getAllFavoriteFolders());


                AppDB.get().saveAll(itemsMeta);

                handler.removeCallbacks(timer);

                sendFinishMessage();

                handler.post(timer2);

                for (FileMeta meta : itemsMeta) {
                    File file = new File(meta.getPath());
                    FileMetaCore.get().upadteBasicMeta(meta, file);
                }

                AppDB.get().updateAll(itemsMeta);
                sendFinishMessage();


                for (FileMeta meta : itemsMeta) {
                    if(FileMetaCore.isSafeToExtactBook(meta.getPath())) {
                        EbookMeta ebookMeta = FileMetaCore.get().getEbookMeta(meta.getPath(), CacheDir.ZipService, true);
                        FileMetaCore.get().udpateFullMeta(meta, ebookMeta);
                    }
                }

                SharedBooks.updateProgress(itemsMeta, true);
                AppDB.get().updateAll(itemsMeta);


                itemsMeta.clear();

                handler.removeCallbacks(timer2);
                sendFinishMessage();
                CacheDir.ZipService.removeCacheContent();

                Clouds.get().syncronizeGet();

                TagData.restoreTags();


                List<FileMeta> allNone = AppDB.get().getAllByState(FileMetaCore.STATE_NONE);
                for (FileMeta m : allNone) {
                    LOG.d("BooksService-createMetaIfNeedSafe-service", m.getTitle(),m.getPath(), m.getTitle());
                    FileMetaCore.createMetaIfNeedSafe(m.getPath(), false);
                }


                sendFinishMessage();

            } else if (ACTION_SYNC_DROPBOX.equals(intent.getAction())) {
                Clouds.get().syncronizeGet();
                sendFinishMessage();
            }


        } finally {
            isRunning = false;
        }
        //stopSelf();
    }

    private void sendFinishMessage() {
        try {
            //AppDB.get().getDao().detachAll();
        } catch (Exception e) {
            LOG.e(e);
        }

        sendFinishMessage(this);
        EventBus.getDefault().post(new MessageSyncFinish());
    }

    private void sendProggressMessage() {
        Intent itent = new Intent(INTENT_NAME).putExtra(Intent.EXTRA_TEXT, RESULT_SEARCH_COUNT).putExtra(Intent.EXTRA_INDEX, itemsMeta.size());
        LocalBroadcastManager.getInstance(this).sendBroadcast(itent);
    }

    private void sendBuildingLibrary() {
        Intent itent = new Intent(INTENT_NAME).putExtra(Intent.EXTRA_TEXT, RESULT_BUILD_LIBRARY);
        LocalBroadcastManager.getInstance(this).sendBroadcast(itent);
    }

}
