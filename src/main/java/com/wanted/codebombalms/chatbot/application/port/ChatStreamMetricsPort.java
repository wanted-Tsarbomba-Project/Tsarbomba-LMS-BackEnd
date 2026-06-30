package com.wanted.codebombalms.chatbot.application.port;

/**
 * AI 응답 스트림의 생명주기를 관측 시스템에 기록하는 출력 포트.
 *
 * <p>토큰 사용량({@link RecordTokenUsagePort})과 책임을 분리한다 — 이쪽은 "스트림이 몇 개 떠 있고,
 * 얼마나 걸렸고, 어떻게 끝났나"(동시성·지연·중도이탈)다. application 은 이 인터페이스에만 의존하고,
 * 구현(Micrometer)은 infrastructure 에 둔다.
 */
public interface ChatStreamMetricsPort {

    /** 스트림 구독 시작 — 활성 스트림 수 +1. */
    void onStreamStart();

    /** 첫 토큰 수신까지의 시간(TTFT). 스트림당 1회만 호출. */
    void onFirstToken(long ttftNanos);

    /**
     * 스트림 종료 — 활성 스트림 수 -1, 전체 소요시간(outcome 별), 종료 신호 분포를 한 번에 기록.
     *
     * @param durationNanos 구독~종료까지 소요(ns)
     * @param outcome       {@code "success"} | {@code "error"} — AI 응답 정상 완료 여부
     * @param signal        reactor 종단 신호명({@code onComplete}/{@code cancel}/{@code onError})
     */
    void onStreamEnd(long durationNanos, String outcome, String signal);
}
