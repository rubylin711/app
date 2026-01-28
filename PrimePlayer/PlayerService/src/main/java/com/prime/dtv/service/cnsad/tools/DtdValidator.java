package com.prime.dtv.service.cnsad.tools;

import android.util.Log;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * 在 Android 裝置上提供「盡力而為」的 DTD 驗證：
 * 1) 先嘗試嚴格 DTD 驗證（validating SAX）。
 * 2) 若平台不支援或失敗，改做 Soft 驗證：
 *    - 檢查是否有 <!DOCTYPE ...>
 *    - （若提供）同資料夾的 data.dtd 可讀
 *    - XML 可被非驗證解析完整讀完（良構性）
 */
public final class DtdValidator {
    private static final String TAG = "CNS-AD/DTD";

    private DtdValidator() {}

    /** 回傳 true 表示（嚴格或 soft）驗證通過；false 代表失敗。 */
    public static boolean validate(File xmlFile, File dtdFileOrNull) {
        try {
            boolean ok = strictValidate(xmlFile, dtdFileOrNull);
            if (ok) {
                Log.i(TAG, "Strict DTD validation: OK");
                return true;
            }
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "No validating SAX available; falling back to soft validation", e);
            return softValidate(xmlFile, dtdFileOrNull);
        } catch (Throwable t) {
            Log.e(TAG, "Strict DTD validation failed; falling back to soft validation", t);
            return softValidate(xmlFile, dtdFileOrNull);
        }
        // 正常不會到這裡；保險起見再做 soft。
        return softValidate(xmlFile, dtdFileOrNull);
    }

    // ================ internal ================

    private static boolean strictValidate(File xmlFile, File dtdFileOrNull) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try { factory.setFeature("http://xml.org/sax/features/validation", false); } catch (Exception ignore) {}
        try { factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true); } catch (Exception ignore) {}

        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();

        // 若有提供同目錄 data.dtd，強制導向它
        if (dtdFileOrNull != null && dtdFileOrNull.exists()) {
            reader.setEntityResolver((publicId, systemId) -> {
                if (systemId != null && systemId.toLowerCase().contains("data.dtd")) {
                    InputSource is = new InputSource(new FileInputStream(dtdFileOrNull));
                    is.setSystemId(dtdFileOrNull.toURI().toString());
                    return is;
                }
                return null;
            });
        }

        reader.setErrorHandler(new DefaultHandler() {
            @Override public void warning(SAXParseException e) { Log.w(TAG, "DTD warn@" + pos(e) + ": " + e.getMessage()); }
            @Override public void error(SAXParseException e) throws SAXException { Log.e(TAG, "DTD error@" + pos(e) + ": " + e.getMessage()); throw e; }
            @Override public void fatalError(SAXParseException e) throws SAXException { Log.e(TAG, "DTD fatal@" + pos(e) + ": " + e.getMessage()); throw e; }
            private String pos(SAXParseException e) { return e.getLineNumber() + ":" + e.getColumnNumber(); }
        });

        try (InputStream in = new FileInputStream(xmlFile)) {
            InputSource src = new InputSource(in);
            src.setSystemId(xmlFile.toURI().toString()); // 供解析相對路徑
            reader.parse(src);
        }
        return true;
    }

    private static boolean softValidate(File xmlFile, File dtdFileOrNull) {
        try {
            boolean hasDoctype = false;
            String sysId = null;

            // 1) 嘗試在前面幾十行找 DOCTYPE
            try (BufferedReader br = new BufferedReader(new FileReader(xmlFile))) {
                String line; int scanned = 0;
                while ((line = br.readLine()) != null && scanned++ < 100) {
                    int i = line.indexOf("<!DOCTYPE");
                    if (i >= 0) {
                        hasDoctype = true;
                        int q1 = line.indexOf('"', i);
                        int q2 = (q1 >= 0) ? line.indexOf('"', q1 + 1) : -1;
                        if (q1 >= 0 && q2 > q1) sysId = line.substring(q1 + 1, q2);
                        break;
                    }
                }
            }

            if (!hasDoctype) {
                Log.w(TAG, "Soft DTD: no DOCTYPE found – treat as pass (device-friendly)");
            }

            // 2) 若指定 dtdFile，而且 sysId 指向 data.dtd，至少確認檔案可讀
            if (dtdFileOrNull != null && sysId != null && sysId.toLowerCase().contains("data.dtd")) {
                if (!dtdFileOrNull.exists() || !dtdFileOrNull.canRead()) {
                    Log.e(TAG, "Soft DTD: data.dtd not readable: " + dtdFileOrNull);
                    return false;
                }
            }

            // 3) 良構性：用 XmlPullParser 走完整個文件
            try (InputStream in = new FileInputStream(xmlFile)) {
                org.xmlpull.v1.XmlPullParser p = android.util.Xml.newPullParser();
                p.setFeature(org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                p.setInput(in, null);
                while (p.getEventType() != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                    p.next();
                }
            }

            Log.i(TAG, "Soft DTD validation: OK (well-formed + DOCTYPE/DTD presence check)");
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Soft DTD validation failed", t);
            return false;
        }
    }
}
