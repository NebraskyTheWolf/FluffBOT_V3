/*
---------------------------------------------------------------------------------
File Name : ModalCallback

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.modal.custom;

import net.dv8tion.jda.api.interactions.modals.ModalInteraction;

public interface ModalCallback {
    void execute(ModalInteraction interaction, String acceptanceId) throws Exception;
}
