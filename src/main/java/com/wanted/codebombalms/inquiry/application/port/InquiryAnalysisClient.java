package com.wanted.codebombalms.inquiry.application.port;

import com.wanted.codebombalms.inquiry.application.command.RequestInquiryAnalysisCommand;

// 문의 원문을 Python AI 분석 서버에 전달하는 외부 호출 포트다.
public interface InquiryAnalysisClient {

    void analyze(RequestInquiryAnalysisCommand command);
}
