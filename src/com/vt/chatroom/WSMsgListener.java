package com.vt.chatroom;

public interface WSMsgListener {
	void onMessage(String message);
	void onKeyXCHG(int round);
	void onKick();
	void onError(int errcode);
}
