package eu.fluffici.bot.components.commands.developer;

/*
---------------------------------------------------------------------------------
File Name : CommandConvert.java

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


import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@CommandHandle
public class CommandConvert extends Command {
    private final Map<Long, byte[]> MASK_TO_BYTES = new HashMap<>();


    public CommandConvert() {
        super("convert-lang", "Convert all Czech & Slovak letters to their unicode format.", CommandCategory.DEVELOPER);

        this.getOptions().put("isDeveloper", true);
        this.getOptionData().add(new OptionData(OptionType.ATTACHMENT, "attachment", "The file to convert."));
        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));

        MASK_TO_BYTES.put(0x0001L, new byte[]{(byte) 0xC3, (byte) 0x81}); // 'Á'
        MASK_TO_BYTES.put(0x0002L, new byte[]{(byte) 0xC3, (byte) 0x84}); // 'Ä'
        MASK_TO_BYTES.put(0x0004L, new byte[]{(byte) 0xC4, (byte) 0x8C}); // 'Č'
        MASK_TO_BYTES.put(0x0008L, new byte[]{(byte) 0xC4, (byte) 0x8E}); // 'Ď'
        MASK_TO_BYTES.put(0x0010L, new byte[]{(byte) 0xC3, (byte) 0x89}); // 'É'
        MASK_TO_BYTES.put(0x0020L, new byte[]{(byte) 0xC4, (byte) 0x9A}); // 'Ě'
        MASK_TO_BYTES.put(0x0040L, new byte[]{(byte) 0xC3, (byte) 0x8D}); // 'Í'
        MASK_TO_BYTES.put(0x0080L, new byte[]{(byte) 0xC4, (byte) 0xB9}); // 'Ĺ'
        MASK_TO_BYTES.put(0x0100L, new byte[]{(byte) 0xC4, (byte) 0xBD}); // 'Ľ'
        MASK_TO_BYTES.put(0x0200L, new byte[]{(byte) 0xC5, (byte) 0x87}); // 'Ň'
        MASK_TO_BYTES.put(0x0400L, new byte[]{(byte) 0xC3, (byte) 0x93}); // 'Ó'
        MASK_TO_BYTES.put(0x0800L, new byte[]{(byte) 0xC3, (byte) 0x94}); // 'Ô'
        MASK_TO_BYTES.put(0x1000L, new byte[]{(byte) 0xC5, (byte) 0x94}); // 'Ŕ'
        MASK_TO_BYTES.put(0x2000L, new byte[]{(byte) 0xC5, (byte) 0x98}); // 'Ř'
        MASK_TO_BYTES.put(0x4000L, new byte[]{(byte) 0xC5, (byte) 0xA0}); // 'Š'
        MASK_TO_BYTES.put(0x8000L, new byte[]{(byte) 0xC5, (byte) 0xA4}); // 'Ť'
        MASK_TO_BYTES.put(0x00010000L, new byte[]{(byte) 0xC3, (byte) 0x9A}); // 'Ú'
        MASK_TO_BYTES.put(0x00020000L, new byte[]{(byte) 0xC5, (byte) 0xAE}); // 'Ů'
        MASK_TO_BYTES.put(0x00040000L, new byte[]{(byte) 0xC3, (byte) 0x9D}); // 'Ý'
        MASK_TO_BYTES.put(0x00080000L, new byte[]{(byte) 0xC5, (byte) 0xBD}); // 'Ž'
        MASK_TO_BYTES.put(0x00100000L, new byte[] {(byte)0xC3, (byte)0xA1}); // 'á'
        MASK_TO_BYTES.put(0x00200000L, new byte[] {(byte)0xC3, (byte)0xA4}); // 'ä'
        MASK_TO_BYTES.put(0x00400000L, new byte[] {(byte)0xC4, (byte)0x8D}); // 'č'
        MASK_TO_BYTES.put(0x00800000L, new byte[] {(byte)0xC4, (byte)0x8F}); // 'ď'
        MASK_TO_BYTES.put(0x01000000L, new byte[] {(byte)0xC3, (byte)0xA9}); // 'é'
        MASK_TO_BYTES.put(0x02000000L, new byte[] {(byte)0xC4, (byte)0x9B}); // 'ě'
        MASK_TO_BYTES.put(0x04000000L, new byte[] {(byte)0xC3, (byte)0xAD}); // 'í'
        MASK_TO_BYTES.put(0x08000000L, new byte[] {(byte)0xC4, (byte)0xBA}); // 'ĺ'
        MASK_TO_BYTES.put(0x10000000L, new byte[] {(byte)0xC4, (byte)0xBE}); // 'ľ'
        MASK_TO_BYTES.put(0x20000000L, new byte[] {(byte)0xC5, (byte)0x88}); // 'ň'
        MASK_TO_BYTES.put(0x40000000L, new byte[] {(byte)0xC3, (byte)0xB3}); // 'ó'
        MASK_TO_BYTES.put(0x0000000100000000L, new byte[] {(byte)0xC3, (byte)0xB4}); // 'ô'
        MASK_TO_BYTES.put(0x0000000200000000L, new byte[] {(byte)0xC5, (byte)0x95}); // 'ŕ'
        MASK_TO_BYTES.put(0x0000000400000000L, new byte[] {(byte)0xC5, (byte)0x99}); // 'ř'
        MASK_TO_BYTES.put(0x0000000800000000L, new byte[] {(byte)0xC5, (byte)0xA1}); // 'š'
        MASK_TO_BYTES.put(0x0000001000000000L, new byte[] {(byte)0xC5, (byte)0xA5}); // 'ť'
        MASK_TO_BYTES.put(0x0000002000000000L, new byte[] {(byte)0xC3, (byte)0xBA}); // 'ú'
        MASK_TO_BYTES.put(0x0000004000000000L, new byte[] {(byte)0xC5, (byte)0xAF}); // 'ů'
        MASK_TO_BYTES.put(0x0000008000000000L, new byte[] {(byte)0xC3, (byte)0xBD}); // 'ý'
        MASK_TO_BYTES.put(0x0000010000000000L, new byte[] {(byte)0xC5, (byte)0xBE}); // 'ž'
    }

    @Override
    @SneakyThrows
    public void execute(CommandInteraction interaction) {
        Message.Attachment attachment = interaction.getOption("attachment").getAsAttachment();
        InputStream file = attachment.getProxy().download().get(30, TimeUnit.SECONDS);

        byte[] fileBytes = file.readAllBytes();

        if (fileBytes.length == 0) {
            interaction.replyEmbeds(this.buildError("Your file is empty.")).queue();
        } else {
            interaction.replyEmbeds(this.buildSuccess("Your file was converted.")).addFiles(FileUpload.fromData(replaceMasksWithBytes(fileBytes), GameId.generateId() + ".properties")).queue();
        }
    }

    /**
     * Replaces masks in the input byte array with corresponding bytes.
     *
     * @param input The input byte array.
     * @return The resulting byte array with masks replaced by bytes.
     */
    public byte[] replaceMasksWithBytes(@NotNull byte[] input) {
        long[] inputMasks = new long[input.length / 4];
        for (int i = 0; i < input.length; i += 4) {
            long mask = ((input[i] & 0xFFL) << 24) | ((input[i + 1] & 0xFFL) << 16)
                    | ((input[i + 2] & 0xFFL) << 8) | (input[i + 3] & 0xFFL);
            inputMasks[i / 4] = mask;
        }

        printHexTable(inputMasks);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (long mask : inputMasks) {
            if (MASK_TO_BYTES.containsKey(mask)) {
                try {
                    output.write(MASK_TO_BYTES.get(mask));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return output.toByteArray();
    }

    /**
     * Prints a hex table of the given long array masks, displaying the offset and hex values of each element.
     *
     * @param*/
    public static void printHexTable(@NotNull long[] masks) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < masks.length; i++) {
            if (i % 16 == 0) {
                sb.append(String.format("%04X:   ", i));
            }

            sb.append(String.format("%02X ", masks[i]));

            if (i % 16 == 15 || i == masks.length - 1) {
                sb.append("\n");
            }
        }

        System.out.println(sb.toString());
    }
}
