package eu.fluffici.bot.components.commands.misc;

/*
---------------------------------------------------------------------------------
File Name : CommandNickname.java

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
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.util.regex.Pattern;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

@CommandHandle
@SuppressWarnings("All")
public class CommandNickname extends Command {
    private final FluffBOT instance;

    public CommandNickname(FluffBOT instance) {
        super("nickname", "Změní přezdívku na serveru.", CommandCategory.MISC);

        this.instance = instance;

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);

        this.getOptionData().add(new OptionData(OptionType.STRING, "nickname", "Enter your nickname.", true));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String nickname = interaction.getOption("nickname").getAsString();
        PlayerBean player = this.instance.getUserManager().fetchUser(interaction.getUser());

        if (getUserManager().hasItem(interaction.getUser(), "nickname_coin", 1)) {
            handleConfirmation(interaction, this.getLanguageManager().get("command.nickname.confirm", nickname), new ConfirmCallback() {
                @Override
                public void confirm(ButtonInteraction interaction) throws Exception {
                    if (nickname.length() > 32) {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.nickname.toolong"))).setEphemeral(true).queue();
                    } else if (containsUrl(nickname)) {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.nickname.containsurl"))).setEphemeral(true).queue();
                    } else if (validateIp(nickname)) {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.nickname.containsip"))).setEphemeral(true).queue();
                    } else if (containsSpecialCharacters(nickname)) {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.nickname.contains_s_chars"))).setEphemeral(true).queue();
                    } else {
                        player.setHasNickname(true);
                        player.setNickname(nickname);
                        instance.getUserManager().saveUser(player);

                        interaction.getMember().modifyNickname(nickname).reason("User used /prezdivka").queue();
                        interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.nickname.edited", nickname))).setEphemeral(true).queue();

                        instance.getGameServiceManager().decrementQuantity(interaction.getUser(), instance.getGameServiceManager().fetchItem("nickname_coin"), 1);
                    }
                }

                @Override
                public void cancel(ButtonInteraction interaction) throws Exception {
                    interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.nickname.cancelled"))).setEphemeral(true).queue();
                }
            });
        } else {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.nickname.not_enough_coins"))).setEphemeral(true).queue();
        }
    }

    public static boolean containsUrl(String input) {
        String regex = "(https?|ftp|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern p = Pattern.compile(regex);
        return p.matcher(input).find();
    }

    public static boolean validateIp(String ipAddress) {
        String regex = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\D(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\D(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\D(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        Pattern p = Pattern.compile(regex);
        return p.matcher(ipAddress).matches();
    }

    public static boolean containsSpecialCharacters(String input) {
        return !Pattern.matches("[a-zA-Z0-9áäčďéěíĺľňóôřŕšťúůýžÁÄČĎÉĚÍĹĽŇÓÔŘŔŠŤÚŮÝŽ ]*", input);
    }
}
