/*
---------------------------------------------------------------------------------
File Name : PatchRequest

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.hooks;

import lombok.Getter;
import lombok.Setter;

/**
 * This class represents a Patch Request. It is a generic class, where the type parameter T represents the type of data being patched.
 *
 * The PatchRequest class has the following properties:
 * - userId: A long value representing the user id associated with the patch request.
 * - bitfield: A long value representing the bitfield associated with the patch request.
 * - data: A generic type T representing the actual data being patched.
 *
 * This class provides getters and setters for the above properties.
 *
 * Example Usage:
 *
 *      PatchRequest<GuildSettings> patchSettings = new PatchRequest<>();
 *      patchSettings.setUserId(123456789);
 *      patchSettings.setBitfield(Permission.ADMINISTRATOR.getRawValue());
 *      patchSettings.setData(new GuildSettings());
 *
 * @param <T> The type of data being patched
 */
@Getter
@Setter
public class PatchRequest<T> {
    private long userId;
    private long bitfield;
    private T data;
}