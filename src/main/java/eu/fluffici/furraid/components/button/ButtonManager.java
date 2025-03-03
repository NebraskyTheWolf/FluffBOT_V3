package eu.fluffici.furraid.components.button;

/*
---------------------------------------------------------------------------------
File Name : ButtonManager.java

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


import eu.fluffici.bot.api.interactions.ButtonBuilder;
import eu.fluffici.bot.api.interactions.SelectMenu;
import eu.fluffici.furraid.FurRaidDB;
import eu.fluffici.furraid.components.button.quarantine.SelectQuarantineActions;
import eu.fluffici.furraid.components.button.support.ButtonCloseTicket;
import eu.fluffici.furraid.components.button.support.ButtonOpenTicket;
import eu.fluffici.furraid.components.button.verification.ButtonVerification;
import eu.fluffici.furraid.components.button.verification.SelectVerificationActions;
import lombok.Getter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ButtonManager {
    private final List<ButtonBuilder> buttonBuilders = new CopyOnWriteArrayList<>();
    @Getter
    private final List<SelectMenu<?>> selectMenus = new CopyOnWriteArrayList<>();

    public void load() {
        this.buttonBuilders.clear();
        this.selectMenus.clear();

        this.buttonBuilders.add(new ButtonVerification());

        this.buttonBuilders.add(new ButtonCloseTicket());
        this.buttonBuilders.add(new ButtonOpenTicket());

        this.buttonBuilders.add(new ButtonVerification());

        this.selectMenus.add(new SelectVerificationActions());
        this.selectMenus.add(new SelectQuarantineActions());

        this.buttonBuilders.forEach(cmd -> {
            cmd.setEmbed(FurRaidDB.getInstance().getEmbed());
            cmd.setLanguageManager(FurRaidDB.getInstance().getLanguageManager());
        });

        this.selectMenus.forEach(cmd -> {
            cmd.setEmbed(FurRaidDB.getInstance().getEmbed());
            cmd.setLanguageManager(FurRaidDB.getInstance().getLanguageManager());

            FurRaidDB.getInstance().getJda().addEventListener(cmd);
        });
    }

    public List<ButtonBuilder> getAllButtons() {
        return this.buttonBuilders;
    }

    public ButtonBuilder findByName(String customId) {
        AtomicReference<ButtonBuilder> commandReference = new AtomicReference<>();

        this.buttonBuilders.forEach(cmd -> {
            String id = customId;
            if (id.indexOf('#') != -1) {
                id = id.split("#")[0];
            }

            if (cmd.getCustomId().equals(id)) {
                commandReference.set(cmd);
            }
        });

        return commandReference.get();
    }

    public SelectMenu<?> findSelectByName(String customId) {
        AtomicReference<SelectMenu<?>> commandReference = new AtomicReference<>();

        this.selectMenus.forEach(cmd -> {
            String id = customId;
            if (id.indexOf('#') != -1) {
                id = id.split("#")[0];
            }

            if (cmd.getCustomId().equals(id)) {
                commandReference.set(cmd);
            }
        });

        return commandReference.get();
    }
}
