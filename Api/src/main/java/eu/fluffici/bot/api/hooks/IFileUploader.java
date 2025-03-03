/*
---------------------------------------------------------------------------------
File Name : IFileUploader

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/06/2024
Last Modified : 15/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.hooks;

import java.io.IOException;

public interface IFileUploader {
    public void uploadFile(FileBuilder file, FIleUploadCallback callback) throws IOException;
}