package com.wanted.codebombalms.chatbot.presentation.api;

public class ChatResponseMessage {

    private ChatResponseMessage() {}

    // ROOM_CREATED 삭제
    public static final String FIRST_MESSAGE_SENT = "채팅방이 생성되고 메시지가 전송되었습니다.";
    public static final String ROOM_RETRIEVED = "채팅방 목록 조회에 성공했습니다.";
    public static final String MESSAGES_RETRIEVED = "채팅 내역 조회에 성공했습니다.";
    public static final String MESSAGE_SENT = "메시지 전송에 성공했습니다.";
    public static final String ROOM_DELETED = "채팅방이 삭제되었습니다.";

}
