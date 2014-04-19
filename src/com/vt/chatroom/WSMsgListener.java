package com.vt.chatroom;

public interface WSMsgListener {
	void onOpen();
	void onMessage(String message);
	void onKeyXCHG(int round);
	void onKick(String reason);
	void onError(int errcode);
}
