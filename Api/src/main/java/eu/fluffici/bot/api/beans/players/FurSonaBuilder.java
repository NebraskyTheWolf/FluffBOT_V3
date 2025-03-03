/*
---------------------------------------------------------------------------------
File Name : FursonaBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/07/2024
Last Modified : 08/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.players;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.*;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.io.Serializable;

/**
 * The FurSonaBuilder class is used to build a FurSona object by setting its various attributes.
 * It provides a convenient way to create a FurSona instance with multiple properties.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FurSonaBuilder implements Serializable {
    public UserSnowflake ownerId;
    public int characterAge;
    public int characterColor;
    public String characterName;
    public String characterQuote;
    public String characterSpecie;
    public String characterPictureURL;
    public String characterGender;
    public String characterRefsheetURL;
    public String characterPronouns;
    public String characterDescriptions;
    public JsonElement characterExtra;
    public JsonElement characterExtraPictures;
}