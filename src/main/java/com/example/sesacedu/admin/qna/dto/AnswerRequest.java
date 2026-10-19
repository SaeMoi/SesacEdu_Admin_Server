package com.example.sesacedu.admin.qna.dto;

/** 문의 답변 등록/수정 요청 바디. */
public class AnswerRequest {

    private String answer;  // 답변 본문 (필수)

    public AnswerRequest() {
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}