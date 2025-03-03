/*
---------------------------------------------------------------------------------
File Name : FarmingType

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 10/06/2024
Last Modified : 10/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game.farm;

import eu.fluffici.bot.api.item.EquipmentType;
import lombok.Getter;

/**
 * FarmingType is an enum class that represents different types of farming in a farming game.
 * Each farming type is associated with a specific equipment type required for that type of farming.
 */
@Getter
public enum FarmingType {
    MINER(EquipmentType.PICKAXE, false),
    BUTCHER(EquipmentType.SWORD, true),
    HUNTER(EquipmentType.SWORD, true),
    ALCHEMIST(EquipmentType.NECKLACE, true),
    LUMBERJACK(EquipmentType.AXE, true),
    FARMER(EquipmentType.HOE, true),
    FISHERMAN(EquipmentType.STAFF, true),
    HERBALIST(EquipmentType.SWORD, true),
    BLACKSMITH(EquipmentType.SWORD, true),
    CARPENTER(EquipmentType.SWORD, true),
    GARDENER(EquipmentType.HOE, true),
    BREWER(EquipmentType.HOE, true);

    private final EquipmentType equipmentType;
    private final boolean isRestricted;
    FarmingType(EquipmentType equipmentType, boolean isRestricted) {
        this.equipmentType = equipmentType;
        this.isRestricted = isRestricted;
    }
}