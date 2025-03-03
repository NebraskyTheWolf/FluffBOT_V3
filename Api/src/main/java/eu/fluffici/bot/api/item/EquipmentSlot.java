/*
---------------------------------------------------------------------------------
File Name : EquipementSlot

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/06/2024
Last Modified : 04/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.item;

import lombok.Getter;

@Getter
public enum EquipmentSlot {
    HELMET(460.5,213.48),
    CHEST(460.5,286.48),
    LEGGING(460.5,359.48),
    BOOTS(460.5,432.48),

    SWORD(460,535.48),
    AXE(538,535.48),
    STAFF(616,535.48),
    NECKLACE(693,535.48),

    PICKAXE(460,624.48),
    HOE(538,624.48),
    BOW(616,624.48),
    FRAGMENT(693,624.48),

    NONE(0, 0);

    private final double xPosition;
    private final double yPosition;

    EquipmentSlot(double xPosition, double yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }
}