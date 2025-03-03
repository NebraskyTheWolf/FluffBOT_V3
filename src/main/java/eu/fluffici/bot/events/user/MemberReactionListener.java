package eu.fluffici.bot.events.user;

/*
---------------------------------------------------------------------------------
File Name : MemberReactionListener.java

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
import eu.fluffici.bot.api.hooks.PlayerBean;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MemberReactionListener extends ListenerAdapter {

    private final List<String> allowedChannels = new ArrayList<>();

    private final FluffBOT instance;

    public MemberReactionListener(FluffBOT instance) {
        this.instance = instance;

        this.allowedChannels.add("710963319183507507");
        this.allowedChannels.add("813135645215817738");
    }

    @Override
    public void onMessageReactionAdd(@NonNull MessageReactionAddEvent event) {
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        PlayerBean handler = this.instance.getUserManager().fetchUser(event.getMember());
        if (handler == null) {
            return;
        }

        if (this.allowedChannels.contains(event.getChannel().getId())) {
            Member member = event.getMember();

            if (member != null) {
                List<RichCustomEmoji> emojis = event.getGuild().getEmojisByName(event.getEmoji().getName(), true);
                if (!emojis.isEmpty()) {
                    String emojiId = event.getGuild().getEmojisByName(event.getEmoji().getName(), true).get(0).getId();
                    String emojiUp = this.instance.getEmojiConfig().getProperty("neon_thumbsup");
                    String emojiDown = this.instance.getEmojiConfig().getProperty("neon_thumbsdown");

                    if (emojiId.equals(emojiUp)) {
                        this.instance.getUserManager().addKarma(handler, 1);
                        this.instance.getLogger().info("%s got 1 positive karma point.", member.getId());
                    }

                    if (emojiId.equals(emojiDown)) {
                        this.instance.getUserManager().removeKarma(handler, 1);
                        this.instance.getLogger().info("%s got 1 negative karma point.", member.getId());
                    }
                }
            }
        } else {
            this.instance.getLogger().debug("%s is trying to react in a ignored channel.", event.getMember().getEffectiveName());
        }
    }
}
