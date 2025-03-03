/*
---------------------------------------------------------------------------------
File Name : PatchSettings

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid.patch;

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.hooks.PatchRequest;
import net.dv8tion.jda.api.Permission;

/**
 * This class represents the settings for patching guild settings. It extends the PatchRequest class,
 * specifying the type parameter as GuildSettings.
 */
public class PatchSettings extends PatchRequest<GuildSettings> {{
    this.setBitfield(Permission.BAN_MEMBERS.getRawValue());
}}