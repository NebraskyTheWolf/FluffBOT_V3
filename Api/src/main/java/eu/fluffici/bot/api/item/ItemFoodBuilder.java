/*
---------------------------------------------------------------------------------
File Name : ItemFoodBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.item;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemFoodBuilder {
    private String slug;
    private double satiety;
    private double healingFactor;
    private double manaFactor;
}