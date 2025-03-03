package eu.fluffici.bot.components.modal;

/*
---------------------------------------------------------------------------------
File Name : ModalManager.java

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
import eu.fluffici.bot.api.interactions.Modal;
import eu.fluffici.bot.components.modal.clan.ModalClanCreate;
import eu.fluffici.bot.components.modal.clan.ModalClanUpdate;
import eu.fluffici.bot.utils.Embed;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
public class ModalManager {
    private final List<Modal> modals = new CopyOnWriteArrayList<>();
    private final Object[] lock = new Object[] {};

    public void load() {
        synchronized (this.lock) {
            this.modals.clear();

            this.modals.add(new ModalClanCreate());
            this.modals.add(new ModalClanUpdate());

            this.modals.forEach(cmd -> {
                cmd.setEmbed(new Embed(FluffBOT.getInstance()));
                cmd.setLanguageManager(FluffBOT.getInstance().getLanguageManager());
            });
        }
    }

    public Modal findByName(String name) {
        AtomicReference<Modal> commandReference = new AtomicReference<>();

        this.modals.forEach(cmd -> {
            if (cmd.getCustomId().equals(name)) {
                commandReference.set(cmd);
            }
        });

        return commandReference.get();
    }
}
