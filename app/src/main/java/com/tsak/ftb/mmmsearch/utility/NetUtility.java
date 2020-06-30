package com.tsak.ftb.mmmsearch.utility;

import android.graphics.drawable.Drawable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetUtility {

    public enum FIND_REGEX {
        NOTHING,
        STARTS_WITH,
        CONTAINS,
        ;
    }

    public final static String PATH_SEPARATOR = "/";
    public final static String PROTOCOL_SUFFIX = "://";
    public final static String DEFAULT_ENCODING = "SHIFT_JIS";

    public enum PROTOCOL {
        HTTP("http" + PROTOCOL_SUFFIX),
        HTTPS("https" + PROTOCOL_SUFFIX),
        ;

        private String value;

        PROTOCOL(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public static PROTOCOL findProtocol(String target) {

            for (PROTOCOL key : PROTOCOL.values()) {
                if (key.name().equals(target.toUpperCase())) {
                    return key;
                }
            }
            return HTTP;
        }

        public static String[] NAMES = Collections.unmodifiableList(new ArrayList<String>() {{
            for (PROTOCOL protocol :PROTOCOL.values()) {
                add(protocol.name());
            }
        }}).toArray(new String[PROTOCOL.values().length]);
    }

    private final static int TIMEOUT_MS = 60000;

    private NetUtility() {
        throw new AssertionError();
    }

    public static String readHtml(URL url) throws NetUtilException {

        List<String> lines = NetUtility.readHtmlLines(url, FIND_REGEX.NOTHING, "");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line)
            .append("\n");
        }
        return sb.toString().trim();
    }

    public static List<String> readHtmlLines(URL url, FIND_REGEX findRegex, String regex) throws NetUtilException {

        List<String> lines = new ArrayList<>();
        BufferedReader br = null;
        try {
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setSSLSocketFactory(NetUtility.createNonAuthContext().getSocketFactory());
            con.setConnectTimeout(TIMEOUT_MS);
            con.setReadTimeout(TIMEOUT_MS);
            br = new BufferedReader(new InputStreamReader(con.getInputStream(), DEFAULT_ENCODING));
            String line;
            while (null != (line = br.readLine())) {
                lines.add(line);
                if (!"".equals(regex)) {
                    boolean isFound = false;
                    switch (findRegex) {
                        case STARTS_WITH:
                            isFound = line.startsWith(regex);
                            break;
                        case CONTAINS:
                            isFound = line.contains(regex);
                            break;
                        default:
                            break;
                    }
                    if (isFound) {
                        br.close();
                        return Collections.unmodifiableList(lines);
                    }
                }
            }
            br.close();
            if (FIND_REGEX.NOTHING != findRegex) {
                throw new NetUtilException(url, findRegex, regex);
            }
        } catch (IOException e) {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException ignored) {}
            }
            throw new NetUtilException(url, findRegex, regex);
        } catch (NetUtilException e) {}
        return Collections.unmodifiableList(lines);
    }

    public static Drawable readImage(URL url) {

        Drawable image = null;
        try {
            InputStream is = (InputStream) url.getContent();
            image = Drawable.createFromStream(is, "");
            is.close();
        } catch (IOException ignored) {}
        return image;
    }

    private static SSLContext createNonAuthContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { new NonAuthTrustManager() }, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException ignored) {}
        return sslContext;
    }

    private static class NonAuthTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            try {} catch (Exception ignored) {}
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            try {} catch (Exception ignored) {}
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    }

    public static class NetUtilException extends Exception {

        private URL url;
        private FIND_REGEX findRegex = FIND_REGEX.NOTHING;
        private String regex = "";

        NetUtilException(URL url, FIND_REGEX findRegex, String regex) {
            super(url.toString());

            try {
                this.url = new URL(url.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            this.findRegex = findRegex;
            this.regex = regex;
        }

        public String toString() {
            return url + ":" + findRegex + ":" + regex;
        }
    }
}
