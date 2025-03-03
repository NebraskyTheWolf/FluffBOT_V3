/*
---------------------------------------------------------------------------------
File Name : ItemCraftBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 05/06/2024
Last Modified : 05/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.crafting;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ItemCraftBuilder {
    private String slug;
    private int quantity;
    private List<ItemCraftingMaterials> materials;
    private int requiredLevel;
}