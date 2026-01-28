/**
 *
 */
package com.dolphin.dtv;

/**
 * Callback interface for UI.
 * 底层通知UI的消息回调接口
 *
 */
public interface IDTVListener {
	/**
	 * <p>Call back method for UI use.<br>
	 * <p>UI应用注册的回调函数接口<br>
	 * @param messageID
	 *      integer, Event ID
	 *      CN:事件编号
	 * @param param1
	 *      Private callback parameter one
	 *      CN: 私有回调参数1
	 * @param param2
	 *      Private callback parameter two
	 *      CN: 私有回调参数2
	 * @param obj
	 *      Private callback parameter three
     *      CN: 私有回调参数3
	 */
	void notifyMessage(int messageID, int param1, int param2, Object obj);
}
