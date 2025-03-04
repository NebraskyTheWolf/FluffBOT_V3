/*
---------------------------------------------------------------------------------
File Name : IconRegistry

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum IconRegistry {
    ICON_SHIELD_X("https://cdn.discordapp.com/attachments/1224419443300372592/1225391205173428244/shield-x.png"),
    ICON_MEDAL("https://cdn.discordapp.com/attachments/1224419443300372592/1225486474854797344/award.png"),
    ICON_CIRCLE_MINUS("https://cdn.discordapp.com/attachments/1224419443300372592/1225815039131390014/circle-minus.png"),
    ICON_FOLDER("https://cdn.discordapp.com/attachments/1224419443300372592/1225815279540633600/folder.png"),
    ICON_SORT("https://cdn.discordapp.com/attachments/1224419443300372592/1225842435066691675/sort-descending-numbers.png"),
    ICON_FLASK("https://cdn.discordapp.com/attachments/1224419443300372592/1225843626047836230/flask.png"),
    ICON_REPORT_SEARCH("https://cdn.discordapp.com/attachments/1224419443300372592/1225843862941990912/report-search.png"),
    ICON_SCROLL("https://cdn.discordapp.com/attachments/1224419443300372592/1225844427273142272/license.png"),
    ICON_QUESTION_MARK("https://cdn.discordapp.com/attachments/1224419443300372592/1225847106808447088/question-mark.png"),
    ICON_HEART("https://cdn.discordapp.com/attachments/1224419443300372592/1225861500384579645/heart.png"),
    ICON_WARNING("https://cdn.discordapp.com/attachments/1224419443300372592/1226168517124952145/alert-triangle.png"),
    ICON_CLOCK("https://cdn.discordapp.com/attachments/1224419443300372592/1226182691318267904/clock.png"),
    ICON_CIRCLE_SLASHED("https://cdn.discordapp.com/attachments/1224419443300372592/1226182691540701235/forbid.png"),
    ICON_HISTORY("https://cdn.discordapp.com/attachments/1224419443300372592/1226182691800743948/history.png"),
    ICON_MESSAGE_EXCLAMATION("https://cdn.discordapp.com/attachments/1224419443300372592/1226182802282774601/message-report.png"),
    ICON_USER_X("https://cdn.discordapp.com/attachments/1224419443300372592/1226183028326531102/user-x.png"),
    ICON_USER_PLUS("https://cdn.discordapp.com/attachments/1224419443300372592/1226183106072150026/user-plus.png"),
    ICON_LOCK("https://cdn.discordapp.com/attachments/1224419443300372592/1226183270832672860/lock.png"),
    ICON_CALENDER_EVENT("https://cdn.discordapp.com/attachments/1224419443300372592/1226184393987723454/calendar-time.png"),
    ICON_ALERT_CIRCLE("https://cdn.discordapp.com/attachments/1224419443300372592/1226465982596517958/alert-circle.png"),
    ICON_CLIPBOARD_CHECKED("https://cdn.discordapp.com/attachments/1224419443300372592/1226466931586891837/clipboard-check.png"),
    ICON_UPVOTE("https://cdn.discordapp.com/attachments/1224419443300372592/1226486478897414216/upvote_icon2.png"),
    ICON_BOOK("https://cdn.discordapp.com/attachments/1224419443300372592/1229151774875713676/book.png"),
    ICON_NOTE("https://cdn.discordapp.com/attachments/1224419443300372592/1229355043824865321/notes.png"),
    ICON_TRUCK("https://cdn.discordapp.com/attachments/1224419443300372592/1230502845787279401/forklift.png"),
    ICON_HEXAGON_CIRCLE("https://cdn.discordapp.com/attachments/1224419443300372592/1234596573296595024/alert-octagon.png"),
    ICON_SETTINGS("https://cdn.discordapp.com/attachments/1224419443300372592/1234597262508953720/adjustments-alt.png"),
    ICON_ART_BOARD("https://cdn.discordapp.com/attachments/1224419443300372592/1234597920905625630/artboard.png"),
    ICON_CHECKS("https://cdn.discordapp.com/attachments/1224419443300372592/1235139230452416532/checks.png"),
    ICON_ALERT("https://cdn.discordapp.com/attachments/1224419443300372592/1243627446721450044/ad-2.png"),
    ICON_UNLOCK("https://cdn.discordapp.com/attachments/1224419443300372592/1245423532209471549/lock-open.png"),
    ICON_FILE("https://cdn.discordapp.com/attachments/1224419443300372592/1245876158965219399/file-description.png"),
    ICON_FILE_CHART("https://cdn.discordapp.com/attachments/1224419443300372592/1245876159196172308/file-report.png"),
    ICON_FISH("https://cdn.discordapp.com/attachments/1224419443300372592/1246928544525979790/1717361331915.png");

    private final String url;

    private static final Map<String, IconRegistry> NAME_MAP = new HashMap<>();

    static {
        for (IconRegistry icon : values()) {
            NAME_MAP.put(icon.name(), icon);
        }
    }

    IconRegistry(String url) {
        this.url = url;
    }

    public static IconRegistry getIconFromName(String name) {
        return NAME_MAP.get(name.toUpperCase());
    }
}
