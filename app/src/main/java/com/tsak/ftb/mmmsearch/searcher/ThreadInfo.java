package com.tsak.ftb.mmmsearch.searcher;

import java.net.MalformedURLException;
import java.net.URL;

public class ThreadInfo {

    private URL url;
    private String title;

    ThreadInfo(URL url, String title) {

        try {
            this.url = new URL(url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.title = title;
    }

    public URL threadURL() {
        return url;
    }

    public String title() {
        return title;
    }

    public String titleEscapeHtml() {
        return SearcherUtility.replaceHtmlTag(title);
    }

    public String toString() {
        return "board:" + url + " title:" + title;
    }

    public ThreadInfo clone() {
        return new ThreadInfo(url, title);
    }
}
