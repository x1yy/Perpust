package com.antaraksi.dao2;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.Map;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 *
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig fileMetaDaoConfig;
    private final DaoConfig dictMetaDaoConfig;

    private final FileMetaDao fileMetaDao;
    private final DictMetaDao dictMetaDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        fileMetaDaoConfig = daoConfigMap.get(FileMetaDao.class).clone();
        fileMetaDaoConfig.initIdentityScope(type);

        dictMetaDaoConfig = daoConfigMap.get(DictMetaDao.class).clone();
        dictMetaDaoConfig.initIdentityScope(type);

        fileMetaDao = new FileMetaDao(fileMetaDaoConfig, this);
        dictMetaDao = new DictMetaDao(dictMetaDaoConfig, this);

        registerDao(FileMeta.class, fileMetaDao);
        registerDao(DictMeta.class, dictMetaDao);
    }

    public void clear() {
        fileMetaDaoConfig.clearIdentityScope();
        dictMetaDaoConfig.clearIdentityScope();
    }

    public FileMetaDao getFileMetaDao() {
        return fileMetaDao;
    }

    public DictMetaDao getDictMetaDao() {
        return dictMetaDao;
    }

}
