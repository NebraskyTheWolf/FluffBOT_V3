/*
---------------------------------------------------------------------------------
File Name : ItemCraftingMaterials

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.crafting;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemCraftingMaterials {
    private String materialSlug;
    private int quantity;
}