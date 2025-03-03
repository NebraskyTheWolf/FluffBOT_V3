package eu.fluffici.bot.api.inventory;

/*
---------------------------------------------------------------------------------
File Name : InventoryBuilder.java

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
import eu.fluffici.bot.api.upgrade.UserUpgradeBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Builder
public class InventoryBuilder {
    private InventoryMetadata metadata;
    private List<InventoryItem> items;
    private List<UserUpgradeBuilder> upgrades;

    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }
    public boolean hasUpgrades() {
        return upgrades != null && !upgrades.isEmpty();
    }
    public boolean hasMetadata() {
        return metadata != null;
    }
    public boolean hasMythicItems() {
        return items != null && items.stream().anyMatch(InventoryItem::isMythic);
    }
    public boolean hasLegendaryItems() {
        return items != null && items.stream().anyMatch(InventoryItem::isLegendary);
    }
    public boolean hasEpicItems() {
        return items != null && items.stream().anyMatch(InventoryItem::isEpic);
    }
    public boolean hasRareItems() {
        return items != null && items.stream().anyMatch(InventoryItem::isRare);
    }
    public boolean hasUncommonItems() {
        return items != null && items.stream().anyMatch(InventoryItem::isUncommon);
    }
    public boolean hasCommonItems() {
        return items != null && items.stream().anyMatch(InventoryItem::isCommon);
    }
    public boolean isInventoryFull() {
        return items != null && items.size() >= metadata.getMaximumSlots();
    }

    public int calculateFreeSpace() {
        return metadata.getMaximumSlots() - (items != null ? items.size() : 0);
    }

    public boolean hasNoItems() {
        return items == null || items.isEmpty();
    }

    public boolean isSlotOccupied(EquipmentSlot equipmentSlot) {
        return items != null && items.stream().anyMatch(item -> item.getEquipmentSlug() == equipmentSlot);
    }

    public boolean isSlotOccupied(EquipmentType equipmentType) {
        return items != null && items.stream().anyMatch(item -> item.getEquipmentType() == equipmentType);
    }

    public InventoryItem getItemOnSlot(EquipmentSlot equipmentSlot) {
        if (items != null) {
            return items.stream()
                    .filter(item -> item.getEquipmentSlug().equals(equipmentSlot))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public InventoryItem getItemOnType(EquipmentType equipmentType) {
        if (items != null) {
            return items.stream()
                    .filter(item -> item.getEquipmentType().equals(equipmentType))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public double totalResistance() {
        double sum = items.stream()
                .map(InventoryItem::getResistance)
                .reduce(0.0, Double::sum);

        if (sum > 1.0) {
            sum = 1.0;
        }

        return sum;
    }

    public boolean hasMythicUpgrades() {
        return upgrades != null && upgrades.stream().anyMatch(UserUpgradeBuilder::isMythic);
    }

    public boolean hasLegendaryUpgrades() {
        return upgrades != null && upgrades.stream().anyMatch(UserUpgradeBuilder::isLegendary);
    }

    public boolean hasEpicUpgrades() {
        return upgrades != null && upgrades.stream().anyMatch(UserUpgradeBuilder::isEpic);
    }

    public boolean hasRareUpgrades() {
        return upgrades != null && upgrades.stream().anyMatch(UserUpgradeBuilder::isRare);
    }

    public boolean hasUncommonUpgrades() {
        return upgrades != null && upgrades.stream().anyMatch(UserUpgradeBuilder::isUncommon);
    }

    public boolean hasCommonUpgrades() {
        return upgrades != null && upgrades.stream().anyMatch(UserUpgradeBuilder::isCommon);
    }

    public boolean hasArmorItems() {
        return items != null && items.stream().anyMatch(InventoryItem::isArmor);
    }

    public boolean hasSpellItems() {
        return items != null && items.stream().anyMatch(InventoryItem::isSpell);
    }

    public boolean hasSwordItems() {
        return items != null && items.stream().anyMatch(InventoryItem::isSword);
    }
}
