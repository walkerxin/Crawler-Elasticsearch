package com.github.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    private static final String INITIAL_URL = "https://sina.cn";

    public static void main(String[] args) throws IOException {

        // 已处理的链接池
        Set<String> processedLinkPool = new HashSet<>();
        // 待处理的链接池
        List<String> toBeProcessLinkPool = new ArrayList<>();
        toBeProcessLinkPool.add(INITIAL_URL);

        while (!toBeProcessLinkPool.isEmpty()) {
            String link = toBeProcessLinkPool.remove(toBeProcessLinkPool.size() - 1);
            if (processedLinkPool.contains(link) || !isInterestingUrl(link)) {
                continue;
            }

            Document doc = getPageHtmlStructure(link);

            ArrayList<Element> aTags = doc.select("a");
            for (Element aTag : aTags) {
                String href = aTag.attr("href");
                if (href.startsWith("http") || href.startsWith("//")) {
                    toBeProcessLinkPool.add(href);
                }
            }

            Elements article = doc.select(".page_main article.art_box");
            if (!article.isEmpty()) {
                String title = article.select("h1").text();
                String ass = article.select(".art_time").text();
                String content = article.select(".art_content").text();
                System.out.println(title + "; time" + ass + "; \ncontent=" + content);
            }

            processedLinkPool.add(link);
        }
    }

    private static Document getPageHtmlStructure(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        RequestConfig.custom().setConnectTimeout(3 * 1000);             // 设置链接超时
        RequestConfig.custom().setConnectionRequestTimeout(3 * 1000);   // 设置读取超时
        System.out.println(link);

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            String html = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            EntityUtils.consume(entity);
            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestingUrl(String link) {
        return link != null && isBeginningOrNewsUrl(link) && !isLoginUrl(link);
    }

    private static boolean isBeginningOrNewsUrl(String link) {
        return link.equals(INITIAL_URL) || link.contains("news.sina.cn");
    }

    private static boolean isLoginUrl(String link) {
        return link.contains("passport.sina.cn");
    }
}
