/*
---------------------------------------------------------------------------------
File Name : ItemInformationBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/06/2024
Last Modified : 08/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemInformationBuilder {
    private String slug;
    private double damageFactor;
}