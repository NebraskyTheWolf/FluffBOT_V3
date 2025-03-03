/*
---------------------------------------------------------------------------------
File Name : OfferQuota

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OfferQuota {
    private int offerId;
    private int localBlacklist;
    private int localWhitelist;
    private boolean hasVerificationFeature;
    private boolean hasTicketFeature;
    private boolean hasAntiSpam;
    private boolean hasAntiRaid;
    private boolean hasAntiScam;
    private boolean hasModerationFeature;
    private boolean hasInviteTracker;
}