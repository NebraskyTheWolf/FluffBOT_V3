package eu.fluffici.bot.api;/*
---------------------------------------------------------------------------------
File Name : eu.fluffici.bot.api.ProfileMigration

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//Parsing the old FLuffBOT profile's data
public class ProfileMigration {

    private String _id;
    private int tokens;
    private int events;
    private int levels;
    private int exp;
    private int upvote;
    private int messages;
    private int work;
    private long upvoteCooldown;
    private long cooldown;
    private boolean hasWelcomeToServer;
    private boolean hasVerified;
    private boolean hasEmoji;
    private boolean hasGifEmoji;
    private boolean hasRolePravy;
    private boolean hasRoleVerny;
    private boolean hasBoosted;
    private boolean hasFirstEvent;
    private boolean hasFurryArtist;
    private boolean hasActiveFurry;
    private boolean hasLoyalFurry;
    private boolean has1000tokens;
    private boolean has5000tokens;
    private boolean hasFirstBuy;
    private boolean has50Work;
    private boolean has25Upvotes;
    private boolean shopHasSpecialColors;
    private boolean shopHasOwnRoom;
    private String userID;
    private String guildID;
    private long lastEdit;
    private int __v;
    private boolean has5EventPoints;
    private String gifemojiName;
    private boolean shopHasOwnColorRole;
    private boolean shopHasRPRoom;
    private boolean shopHasOwnRole;
    private boolean hasChannelAchiv;
    private boolean hasEmojiAchiv;
    private boolean hasGifEmojiAchiv;
    private boolean hasRoleAchiv;
    private boolean hasRoleplayer;
    private boolean hasSpecialColor;
    private int birthdayDay;
    private int birthdayMonth;
    private int birthdayYear;
    private boolean blocked;
    private long gambleCooldown;
    private String lastUpvote;
    private boolean hasNapad;
    private long inventoryBus;
    private int inventoryCheese;
    private int inventoryCookies;
    private boolean relationshipActive;
    private boolean relationshipPending;
    private boolean hasHolidays2021;
    private String relationshipPendingId;
    private String relationshipId;
    private int workLevel;
    private boolean hasAnniversary2021;
    private boolean active;
    private boolean hasChristmas2021;
    private boolean booster;
    private int cooldownBooster;
    private boolean hasMonthlyActive;
    private int monthMessages;
    private boolean beta;
    private boolean clanActive;
    private boolean hasRoleNejvernejsi;
    private boolean relationship2Active;
    private String clanID;
    private String nickname;
    private int halloween2021;
    private boolean hasHalloween2021;
    private String shopOwnRoomID;
    private int advent2021;
    private int joinCount;
    private boolean settingsBoosts;
    private boolean nicknameBlock;
    private int payment;
    private int workExp;
    private int workLevel0; // Note: Renamed to avoid clash with workLevel
    private boolean advent2021Claimed;
    private long notifyCooldown;
    private boolean hasHolidays2022;
    private int inventoryBludistak;
    private int halloween2022;
    private int advent2022;
    private boolean hasChristmas2022;
    private String shopOwnRoleID;
}
