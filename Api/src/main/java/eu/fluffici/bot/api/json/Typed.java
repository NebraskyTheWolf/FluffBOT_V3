/*
---------------------------------------------------------------------------------
File Name : Typed

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/07/2024
Last Modified : 08/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.json;

import lombok.Data;

@Data
public class Typed<T> {
    T data;
}