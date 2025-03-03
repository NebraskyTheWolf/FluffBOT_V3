/*
---------------------------------------------------------------------------------
File Name : Purchase

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 05/06/2024
Last Modified : 05/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.shop.impl;

import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Purchase {
    private ItemDescriptionBean item;
    private int quantity;
    private int price;
}