package org.ebookdroid.droids;

import com.antaraksi.android.utils.LOG;
import com.antaraksi.ext.CacheZipUtils;
import com.antaraksi.ext.FooterNote;
import com.antaraksi.ext.TxtExtract;

import org.ebookdroid.core.codec.CodecDocument;
import org.ebookdroid.droids.mupdf.codec.MuPdfDocument;
import org.ebookdroid.droids.mupdf.codec.PdfContext;

import java.util.Map;

public class TxtContext extends PdfContext {

    @Override
    public CodecDocument openDocumentInner(String fileName, String password) {
        Map<String, String> notes = null;
        try {
            FooterNote extract = TxtExtract.extract(fileName, CacheZipUtils.CACHE_BOOK_DIR.getPath());
            fileName = extract.path;
            notes = extract.notes;
            LOG.d("new-file name", fileName);
        } catch (Exception e) {
            LOG.e(e);
        }

        MuPdfDocument muPdfDocument = new MuPdfDocument(this, MuPdfDocument.FORMAT_PDF, fileName, password);
        muPdfDocument.setFootNotes(notes);
        return muPdfDocument;
    }
}
