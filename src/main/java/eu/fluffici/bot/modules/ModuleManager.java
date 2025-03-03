package eu.fluffici.bot.modules;

/*
---------------------------------------------------------------------------------
File Name : ModuleManager.java

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
import eu.fluffici.bot.api.module.Module;
import eu.fluffici.bot.modules.achievements.AchievementModule;
import eu.fluffici.bot.modules.moderation.ModerationModule;
import eu.fluffici.bot.modules.telegram.TelegramBOT;
import lombok.Getter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Getter
public class ModuleManager {
    private final List<Module> modules = new CopyOnWriteArrayList<>();
    private final Object[] lock = new Object[] {};

    private final FluffBOT instance;

    public ModuleManager(FluffBOT instance) {
        this.instance = instance;
    }

    public void load() {
        synchronized (this.lock) {
            this.modules.clear();

            this.modules.add(new AchievementModule(this.instance));
            this.modules.add(new ModerationModule(this.instance));
            this.modules.add(new TelegramBOT(this.instance));

            for (Module module : this.modules) {
                FluffBOT.getInstance().getJda().addEventListener(module);
            }
        }
    }

    public void enableAll() {
        CompletableFuture.runAsync(() -> this.modules.forEach(Module::onEnable));
    }

    public void disableAll() {
        CompletableFuture.runAsync(() -> this.modules.forEach(Module::onDisable));
    }
}
