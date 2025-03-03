package eu.fluffici.bot.api.beans.shop;

/*
---------------------------------------------------------------------------------
File Name : ItemDescriptionBean.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/


import eu.fluffici.bot.api.Rarity;
import eu.fluffici.bot.api.item.EquipmentSlot;
import eu.fluffici.bot.api.item.EquipmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ItemDescriptionBean {
    private int itemId;
    private String itemSlug;
    private String itemName;
    private String assetPath;
    private String itemDesc;
    private int priceTokens;
    private int priceCoins;
    private Rarity itemRarity;
    private boolean isPurchasable;
    private boolean isFishable;

    private boolean isEnchantable;
    private boolean isCraftable;
    private boolean isEatable;
    private boolean isDrinkable;
    private boolean isEquipment;
    private boolean isStackable;

    private EquipmentType equipmentType;
    private EquipmentSlot equipmentSlug;
    private EquipmentSlot requiredFarmingEquipment;

    private int requiredLevel;
    private int enchantability;
    private int durability;

    private double resistance;

    public int getSalePrice() {
       if (this.isFishable) {
           return this.priceTokens;
       } else {
           return this.priceTokens / 2;
       }
    }
}
