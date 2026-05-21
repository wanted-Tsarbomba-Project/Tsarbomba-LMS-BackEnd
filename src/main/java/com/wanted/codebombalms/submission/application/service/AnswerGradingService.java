package com.wanted.codebombalms.submission.application.service;

import org.springframework.stereotype.Service;

@Service
public class AnswerGradingService {

    public boolean gradeTextAnswer(String correctAnswer, String submittedAnswer) {
        return correctAnswer != null && correctAnswer.equals(submittedAnswer);
    }
}
