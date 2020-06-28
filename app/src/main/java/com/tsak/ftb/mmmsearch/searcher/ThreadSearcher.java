package com.tsak.ftb.mmmsearch.searcher;

import com.tsak.ftb.mmmsearch.utility.JsonUtility;
import com.tsak.ftb.mmmsearch.utility.NetUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tsak.ftb.mmmsearch.utility.NetUtility.DEFAULT_ENCODING;

public class ThreadSearcher {

    private final static String SEARCH_KEY = NetUtility.PATH_SEPARATOR + "futaba.php?guid=on&mode=search&keyword=";
    private final static String JSON_BEGIN_SIGNATURE = "<script type=\"text/javascript\">var ret=JSON.parse('";
    private final static String JSON_END_SIGNATURE = "');</script><script type=\"text/javascript\" src=\"";
    private final static String RES_KEY = "res";
    private final static String RESTO_KEY = "resto";
    private final static String COM_KEY = "com";
    private final static String TITLE_BEGIN_SIGNATURE = "<blockquote>";
    private final static String TITLE_END_SIGNATURE_1 = "</blockquote>";
    private final static String TITLE_END_SIGNATURE_2 = "<br>";
    private final static String TITLE_IP_BEGIN_SIGNATURE = "\\[<font color=\"#ff0000\">";
    private final static String TITLE_IP_END_SIGNATURE = "</font>\\]<br>";

    private List<String> boardList;
    private ThreadSearcherCallback callback;

    private ThreadSearcher(final List<String> boardList, ThreadSearcherCallback callback) {

        this.boardList = new ArrayList<String>() {{
            for (String board : boardList) {
                add(board);
            }
        }};
        this.callback = callback;
    }

    public void find(String query) {
        Map<String, List<Integer>> threadMap = _find(query);
        for (Map.Entry<String, List<Integer>> entry : threadMap.entrySet()) {
            for (Integer id : entry.getValue()) {
                List<String> lines;
                try {
                    URL threadURL = SearcherUtility.makeThreadURL(entry.getKey(), id);
                    lines = NetUtility.readHtmlLines(
                            threadURL,
                            NetUtility.FIND_REGEX.STARTS_WITH, TITLE_BEGIN_SIGNATURE);
                    String title = "";
                    if (!lines.isEmpty()) {
                        title = lines.get(lines.size() - 1)
                                .replaceFirst(TITLE_BEGIN_SIGNATURE, "")
                                .replaceFirst(TITLE_IP_BEGIN_SIGNATURE + ".*" + TITLE_IP_END_SIGNATURE, "")
                                .replaceFirst(TITLE_END_SIGNATURE_1 + ".*", "")
                                .replaceFirst(TITLE_END_SIGNATURE_2 + ".*", "");
                    }
                    callback.notify(new ThreadInfo(threadURL, title));
                } catch (NetUtility.NetUtilException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, List<Integer>> _find(String query) {

        Map<String, List<Integer>> threadIdMap = new LinkedHashMap<>();
        for (String board : boardList) {
            if (!threadIdMap.containsKey(board)) {
                threadIdMap.put(board, new ArrayList<Integer>());
            }
            try {
                threadIdMap.get(board)
                        .addAll(parseThreadIdList(
                        NetUtility.readHtml(
                                new URL(NetUtility.PROTOCOL_HTTPS + board
                                        + SEARCH_KEY + URLEncoder.encode(query, DEFAULT_ENCODING)))));
            } catch (IllegalArgumentException e) {
            } catch (UnsupportedEncodingException e) {
            } catch (NetUtility.NetUtilException e) {
            } catch (MalformedURLException e) {}
        }

        return Collections.unmodifiableMap(threadIdMap);
    }

    private List<Integer> parseThreadIdList(String html) {

        List<Integer> idList = new ArrayList<>();
        html = html.replaceAll("\\\\\"", "\\\"");
        int beginIndex = html.indexOf(JSON_BEGIN_SIGNATURE);
        int endIndex = html.lastIndexOf(JSON_END_SIGNATURE);
        if (-1 < beginIndex && beginIndex + JSON_BEGIN_SIGNATURE.length() < endIndex) {
            try {
                JSONObject jsonObject = JsonUtility.getJson(
                        new JSONObject(html.substring(beginIndex + JSON_BEGIN_SIGNATURE.length(), endIndex)),
                        RES_KEY);
                Iterator keys = jsonObject.keys();
                while(keys.hasNext()) {
                    Object obj = keys.next();
                    if (obj instanceof String) {
                        try {
                            Integer res = Integer.valueOf((String) obj);
                            Integer resTo = JsonUtility.getInteger(jsonObject, -1, obj, RESTO_KEY);
                            String com = JsonUtility.getString(jsonObject, "", obj, COM_KEY);
                            if (0 <= resTo && !"".equals(com)) {
                                Integer parentId = resTo;
                                if (0 == resTo) {
                                    parentId = res;
                                }
                                if (!idList.contains(parentId)) {
                                    idList.add(parentId);
                                }
                            }
                        } catch (NumberFormatException e) {}
                    }
                }
            } catch (JSONException e) {}
        }
        return Collections.unmodifiableList(idList);
    }

    public static ThreadSearcher newInstance(List<String> boardList, ThreadSearcherCallback callback) {

        List<String> validBoardList = new ArrayList<>();
        for (String board : boardList) {
            if (SearcherUtility.checkBoard(board)) {
                validBoardList.add(board);
            }
        }
        return new ThreadSearcher(validBoardList, callback);
    }

    abstract public static class ThreadSearcherCallback {
        abstract public void notify(ThreadInfo threadInfo);
    }
}
