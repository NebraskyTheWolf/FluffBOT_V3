package eu.fluffici.bot.api.events;

/*
---------------------------------------------------------------------------------
File Name : ModalEvent.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



import eu.fluffici.bot.api.interactions.Modal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

@Getter
@AllArgsConstructor
public class ModalEvent {
    private ModalInteractionEvent interactionEvent;
    private Modal commandHandle;
}
