/**
 *
 */
package com.prime.dtv;

/**
 * Callback interface for UI.
 */
public interface IDTVListener {
    /**
     * <p>Call back method for UI use.<br>
     *
     * @param messageID integer, Event ID
     * @param param1    Private callback parameter one
     * @param param2    Private callback parameter two
     * @param obj       Private callback parameter three
     */
    void notifyMessage(int messageID, int param1, int param2, Object obj);
}
