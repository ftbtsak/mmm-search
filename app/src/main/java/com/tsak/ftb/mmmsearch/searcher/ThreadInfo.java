package com.tsak.ftb.mmmsearch.searcher;

import com.tsak.ftb.mmmsearch.utility.NetUtility;

import java.net.MalformedURLException;
import java.net.URL;

public class ThreadInfo {

    private final static String CAT_PATH = "cat";

    private URL url;
    private URL imgUrl;
    private String title;
    private String mail;

    private ThreadInfo(URL url, URL imgUrl, String title, String mail) {

        try {
            this.url = new URL(url.toString());
        } catch (MalformedURLException e) {}
        if (null != imgUrl) {
            try {
                this.imgUrl = new URL(imgUrl.toString());
            } catch (MalformedURLException e) {}
        }
        this.title = title;
        this.mail = mail;
    }

    public URL threadURL() {
        return url;
    }

    public URL imgUrl() {
        return imgUrl;
    }

    public String title() {
        return title;
    }

    public String titleEscapeHtml() {
        return SearcherUtility.replaceHtmlTag(title);
    }

    public String mail() {
        return mail;
    }

    public String toString() {
        return "board:" + url + " title:" + title;
    }

    public ThreadInfo clone() {
        return new ThreadInfo(url, imgUrl, title, mail);
    }

    private static URL makeCatImgURL(URL imgURL) {
        if (null == imgURL) {
            return null;
        }

        String[] splited = imgURL.getPath().split(NetUtility.PATH_SEPARATOR);
        if (4 == splited.length) {
            try {
                return new URL(imgURL.getProtocol() + NetUtility.PROTOCOL_SUFFIX + imgURL.getHost()
                        + NetUtility.PATH_SEPARATOR + splited[1] + NetUtility.PATH_SEPARATOR + CAT_PATH
                        + NetUtility.PATH_SEPARATOR + splited[3]);
            } catch (MalformedURLException e) {}
        }
        return imgURL;
    }

    static ThreadInfo newInstance(URL url, URL imgUrl, String title, String mail) {
        return new ThreadInfo(url, ThreadInfo.makeCatImgURL(imgUrl), title, mail);
    }
}
