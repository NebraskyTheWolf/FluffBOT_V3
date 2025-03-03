/*
---------------------------------------------------------------------------------
File Name : UrlExtractor

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The UrlExtractor class is used to extract URLs from a given text.
 */
public class UrlExtractor {

    // Pattern to match URLs, including those with optional http/https and port numbers
    private static final Pattern URL_PATTERN = Pattern.compile("((https?://)?[\\w-]+(\\.[\\w-]+)+\\.?(:\\d+)?(/\\S*)?)");

    /**
     * Extracts URLs from a given text.
     *
     * @param text The text to extract URLs from.
     * @return A list of extracted URLs.
     */
    public List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            String url = matcher.group(0)
                    .replaceAll("https?://", "")
                    .replaceAll("^/", "")
                    .replaceAll("\\)$", "");
            urls.add(url);
        }
        return urls;
    }
}