/*
---------------------------------------------------------------------------------
File Name : CommandSetupVerification

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.commands.developer;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class CommandSetupVerification extends Command {
    public CommandSetupVerification() {
        super("setup-verification", "Setup the initial messages and interactions", CommandCategory.DEVELOPER);

        this.getOptions().put("isDeveloper", true);
        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        if (interaction.getChannel() instanceof TextChannel channel) {
            channel.sendMessageEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                            .setDescription("""
                                    ## Základní informace
                                    **Vítej na našem serveru! Pro přístup se nejprve musíš ověřit.**
                                    * Jsi na Discordovém serveru komunity Fluffíci. Naším cílem je poskytnout prostory pro socializaci a možnost poznat nové lidi v rámci českého a slovenského furry fandomu. Pořádáme akce pro Fluffíci, z.s., organizujeme eventy na serveru (například filmové večery, hraní her, kreslení atd.) a také je zde prostor pro obyčejný pokec.
                                    * Server je kompletně SFW. Cokoliv, co do této kategorie nespadá, zde není tolerováno.
                                    * Minimální věk dle pravidel platformy Discord je 15 let v ČR a 16 let v SR.
                                    * Trollové, raideři a jiné osoby s obdobnými záměry nejsou vítáni.
                                    ## Jak se ověřit?
                                    ** Kliknutím na tlačítko 'Ověření' pod tímto oknem zahájíš verifikaci.**
                                    * Během verifikace budeš odkázán/a na pravidla serveru, které je třeba si projít a odsouhlasit.
                                    * Abychom se přesvědčili, že nemáš nekalé úmysly, je potřeba alespoň stručně odpovědět na několik otázek. Snaž se odepisovat konkrétně a stručně.
                                    * Po odeslání žádosti o ověření vyčkej, než moderátorský tým zkontroluje tvoji žádost. Doba čekání je obvykle do hodiny, max během několika hodin v závislosti na čase odeslání žádosti.
                                    * Nebude-li tvoje žádost dostatečně adekvátní či až příliš stručná, bude zamítnuta. V případě zamítnutí máš možnost odeslat novou žádost.
                                    * Kdyby cokoliv nebylo jasné, využij možnost ticketu.""")
                            .setColor(Color.GREEN)
                    .build()
            ).setComponents(
                    ActionRow.of(
                            FluffBOT.getInstance().getButtonManager().findByName("row:verify").build(Emoji.fromCustom("ic_verify", 1247531501252251678L, false)),
                            FluffBOT.getInstance().getButtonManager().findByName("row:open-ticket").build(Emoji.fromCustom("ic_ticket", 1247537159045648424L, false))
                    ),
                    ActionRow.of(
                            Button.link("https://discord.com/channels/606534136806637589/606556413183000671", "Pravidla")
                    )
            ).queue();
        }

        interaction.replyEmbeds(this.buildSuccess("Verification message settled up")).setEphemeral(true).queue();
    }
}