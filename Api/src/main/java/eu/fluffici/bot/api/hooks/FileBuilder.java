/*
---------------------------------------------------------------------------------
File Name : FileBuilder

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/06/2024
Last Modified : 15/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.hooks;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Getter
@Builder
public class FileBuilder {
    private String fileName;
    private String bucketName;
    private InputStream data;
}