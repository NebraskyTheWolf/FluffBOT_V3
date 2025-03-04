/*
---------------------------------------------------------------------------------
File Name : OfferManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.manager;

import eu.fluffici.bot.api.beans.furraid.OfferQuota;
import eu.fluffici.furraid.FurRaidDB;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The OfferManager class manages OfferQuota objects.
 */
@Getter
public class OfferManager {
    private final List<OfferQuota> quotas = new CopyOnWriteArrayList<>();

    public OfferManager() {
        this.quotas.add(new OfferQuota(1, 15, 15, false,false,false, true,false,true, false));
        this.quotas.add(new OfferQuota(2, 60, 60, true,true,false, true,false,true, false));
        this.quotas.add(new OfferQuota(3, 120, 120, true,true,true, true,true,true, true));
    }

    /**
     * Finds the OfferQuota associated with the given offer ID.
     *
     * @param id The ID of the offer.
     * @return The OfferQuota associated with the offer.
     */
    public OfferQuota findByOffer(int id) {
        for (OfferQuota quota : quotas) {
            if (quota.getOfferId() == id) {
                return quota;
            }
        }

        return new OfferQuota(id, 0, 0, false,false,false, false,false,false, false);
    }

    /**
     * Retrieves the OfferQuota associated with a guild.
     *
     * @param guildId The ID of the guild.
     * @return The OfferQuota associated with the guild.
     */
    public OfferQuota getByGuild(long guildId) {
        int offerId = FurRaidDB.getInstance().getGameServiceManager().getGuildPremium(guildId).getOfferId();

        return this.findByOffer(offerId);
    }
}