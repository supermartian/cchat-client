package com.vt.chatroom;

public enum MessageType
{
    JOIN("join"), KEY0("keyxchg_0"), KEY1("keyxchg_1"),KEY2("keyxchg_2"), LEAVE("leave"),
    M0("message_0"), M1("message_1"), ERROR("error"), UNKNOW("unknow");
    public String type;

    private MessageType(String type)
    {
        this.type = type;
    }

}
