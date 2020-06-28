package com.tsak.ftb.mmmsearch.searcher;

import java.net.MalformedURLException;
import java.net.URL;

public class ThreadInfo {

    private URL url;
    private String title;
    private String mail;

    ThreadInfo(URL url, String title, String mail) {

        try {
            this.url = new URL(url.toString());
        } catch (MalformedURLException e) {}
        this.title = title;
        this.mail = mail;
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

    public String mail() {
        return mail;
    }

    public String toString() {
        return "board:" + url + " title:" + title;
    }

    public ThreadInfo clone() {
        return new ThreadInfo(url, title, mail);
    }
}
