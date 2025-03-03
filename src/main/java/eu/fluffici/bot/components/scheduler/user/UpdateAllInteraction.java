package eu.fluffici.bot.components.scheduler.user;

/*
---------------------------------------------------------------------------------
File Name : UpdateAllInteraction.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.interactions.Task;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.concurrent.TimeUnit;
@SuppressWarnings("ALL")
public class UpdateAllInteraction extends Task {

    private final FluffBOT instance;

    public UpdateAllInteraction(FluffBOT instance) {
        this.instance = instance;

        this.instance.getLogger().debug("Loading UpdateAllInteraction scheduler.");
    }

    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                this.instance.getGameServiceManager().updateExpiredInteractions();
                this.instance.getGameServiceManager().getAllInteractions().forEach(inter -> {
                    if (inter.isAttached() && inter.getMessageId() != null && !inter.isUpdated()) {
                        if (inter.isAcknowledged()) {
                            if (inter.isDm()) {
                                try {
                                    Message message = this.instance.getJda().getUserById(inter.getUserId())
                                            .openPrivateChannel()
                                            .complete()
                                            .retrieveMessageById(inter.getMessageId()).complete();

                                    Button oldButton = message.getActionRows().get(0).getButtons().get(0)
                                            .asDisabled().withStyle(ButtonStyle.DANGER);

                                    this.instance.getJda().getUserById(inter.getUserId())
                                            .openPrivateChannel()
                                            .complete()
                                            .editMessageEmbedsById(inter.getMessageId())
                                            .setEmbeds(message.getEmbeds())
                                            .setActionRow(oldButton)
                                            .complete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Message message = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                                        .getTextChannelById(inter.getChannelId())
                                        .retrieveMessageById(inter.getMessageId()).complete();
                                Button oldButton = message.getActionRows().get(0).getButtons().get(0)
                                        .asDisabled().withStyle(ButtonStyle.DANGER);
                                this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                                        .getTextChannelById(inter.getChannelId())
                                        .editMessageEmbedsById(inter.getMessageId())
                                        .setEmbeds(message.getEmbeds())
                                        .setActionRow(oldButton)
                                        .complete();
                            }

                            try {
                                this.instance.getGameServiceManager().setUpdated(inter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (inter.isExpired()) {
                            Message message = this.instance.getJda().getUserById(inter.getUserId())
                                    .openPrivateChannel()
                                    .complete()
                                    .retrieveMessageById(inter.getMessageId()).complete();

                            if (inter.isDm()) {
                                try {
                                    this.instance.getJda().getUserById(inter.getUserId())
                                            .openPrivateChannel()
                                            .complete()
                                            .editMessageEmbedsById(inter.getMessageId())
                                            .setEmbeds(message.getEmbeds())
                                            .setActionRow(Button.of(ButtonStyle.SECONDARY, "acknowledged", "Expired").asDisabled())
                                            .complete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                                        .getTextChannelById(inter.getChannelId())
                                        .editMessageEmbedsById(inter.getMessageId())
                                        .setEmbeds(message.getEmbeds())
                                        .setActionRow(Button.of(ButtonStyle.SECONDARY, "acknowledged", "Expired").asDisabled())
                                        .complete();
                            }

                            try {
                                this.instance.getGameServiceManager().setUpdated(inter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } /*
                        else {
                            this.instance.getLogger().debug("Cannot update %s interaction because no message were attached.", inter.getInteractionId());
                        }
                     */
                });
            } catch (Exception e) {
                this.instance.getLogger().error("Error while updating the interactions.", e);
                e.printStackTrace();
            }
        }, 1, 10, TimeUnit.SECONDS);
    }
}
