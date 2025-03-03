/*
---------------------------------------------------------------------------------
File Name : FIleUploadCallback

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/06/2024
Last Modified : 15/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.hooks;

import okhttp3.Response;

/**
 * This interface represents a callback for file upload operations. It provides methods for notifying the caller
 * about the status of the upload operation - whether it is completed successfully or an error has occurred.
 */
public interface FIleUploadCallback {

    /**
     * Inform that the operation is completed successfully.
     *
     * @param response the response object containing the result of the operation
     */
    public void done(Response response);

    /**
     * Handles an error in the operation.
     *
     * @param response the response object containing the details of the error
     */
    public void error(Response response);
}
