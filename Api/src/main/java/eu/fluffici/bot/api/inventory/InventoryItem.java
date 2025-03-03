package eu.fluffici.bot.api.inventory;

/*
---------------------------------------------------------------------------------
File Name : InventoryItem.java

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


import eu.fluffici.bot.api.item.EquipmentSlot;
import eu.fluffici.bot.api.item.EquipmentType;
import eu.fluffici.bot.api.Rarity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class InventoryItem {
    private int itemId;
    private String itemSlug;
    private int quantity;
    private int durability;
    private int maxDurability;
    private boolean isBreakable;
    private String name;
    private String description;
    private Rarity rarity;
    private EquipmentType equipmentType;
    private EquipmentSlot equipmentSlug;
    private boolean isStackable;
    private boolean isEquipped;
    private boolean isEatable;

    private double resistance;

    public boolean isMythic() {
        return rarity == Rarity.MYTHIC;
    }
    public boolean isLegendary() {
        return rarity == Rarity.LEGENDARY;
    }
    public boolean isEpic() {
        return rarity == Rarity.EPIC;
    }
    public boolean isRare() {
        return rarity == Rarity.RARE;
    }
    public boolean isUncommon() {
        return rarity == Rarity.UNCOMMON;
    }

    public boolean isEquipment() { return (this.equipmentSlug != EquipmentSlot.NONE || this.equipmentType != EquipmentType.ITEM); }

    public boolean isCommon() { return rarity == Rarity.COMMON; }

    public boolean isArmor() { return equipmentType == EquipmentType.ARMOR; }
    public boolean isSpell() { return equipmentType == EquipmentType.SPELL; }
    public boolean isSword() { return equipmentType == EquipmentType.SWORD; }
    public boolean isPickaxe() { return equipmentType == EquipmentType.PICKAXE; }
    public boolean isAxe() { return equipmentType == EquipmentType.AXE; }
    public boolean isHoe() { return equipmentType == EquipmentType.HOE; }
    public boolean isBow() { return equipmentType == EquipmentType.BOW; }
    public boolean isStaff() { return equipmentType == EquipmentType.STAFF; }
    public boolean isNecklace() { return equipmentType == EquipmentType.NECKLACE; }
    public boolean isFragment() { return equipmentType == EquipmentType.FRAGMENT; }

    public boolean isNotEquipped() {
        return !this.isEquipped();
    }

    public boolean isNotEquipment() {
        return this.equipmentSlug == EquipmentSlot.NONE;
    }

    public boolean hasEnough() {
        return this.quantity > 0;
    }

    public int durabilityPercentage() {
        if (this.maxDurability <= 0)
            return 0;
        return (int) ((this.durability / (double) this.maxDurability) * 100);
    }

    public String durabilityText() {
        return (this.durability + "/" + this.maxDurability) + " (" + this.durabilityPercentage() + "%)";
    }

    public boolean isBroken() { return durability <= 0; }

}
