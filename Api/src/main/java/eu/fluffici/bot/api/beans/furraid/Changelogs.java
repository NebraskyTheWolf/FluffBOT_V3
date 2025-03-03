/*
---------------------------------------------------------------------------------
File Name : Changelogs

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/07/2024
Last Modified : 04/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid;

import com.google.gson.JsonArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Changelogs {
    private String title;
    private String description;
    private String bannerURL;
    private String version;
    private String build;
    private JsonArray features;
    private JsonArray bugs;
    private String note;
}