package com.tsak.ftb.mmmsearch.searcher;

import androidx.core.text.HtmlCompat;

import com.tsak.ftb.mmmsearch.utility.NetUtility;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SearcherUtility {

    private final static String RES_PATH = NetUtility.PATH_SEPARATOR + "res" + NetUtility.PATH_SEPARATOR;
    private final static String THREAD_EXT = ".htm";
    private final static Pattern HOST_PATTERN = Pattern.compile("(.*\\.2chan\\.net)");

    private SearcherUtility() {
        throw new AssertionError();
    }

    static String replaceHtmlTag(String target) {

        return HtmlCompat.fromHtml(target.replaceAll("\\\\/", "/"),
                HtmlCompat.FROM_HTML_MODE_COMPACT).toString();
    }

    static URL makeThreadURL(String board, Integer id) throws MalformedURLException {
        return new URL(NetUtility.PROTOCOL_HTTPS + board + RES_PATH + id + THREAD_EXT);
    }

    static boolean checkBoard(String board) {

        String[] splitPath = board.split(NetUtility.PATH_SEPARATOR);
        if (2 > splitPath.length) {
            return false;
        }
        Matcher hostMatcher = HOST_PATTERN.matcher(splitPath[0]);
        if (hostMatcher.find()) {
            try {
                new URL(NetUtility.PROTOCOL_HTTPS + board);
                return true;
            } catch (MalformedURLException e) {}
        }
        return false;
    }
}