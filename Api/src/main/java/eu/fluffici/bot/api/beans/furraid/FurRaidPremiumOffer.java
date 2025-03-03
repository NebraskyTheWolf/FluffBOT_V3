/*
---------------------------------------------------------------------------------
File Name : FurRaidPremiumOffer

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FurRaidPremiumOffer {
    private int id;
    private String offerSlug;
    private String offerName;
    private String offerDescription;
    private long offerPrice;
    private double offerDiscount;
    private double offerVat;
    private List<OfferFeatures> offerFeatures;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfferFeatures {
        private boolean included;
        private String description;
    }
}