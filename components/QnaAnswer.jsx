"use client";
import { useRef, useState } from "react";
import Link from "next/link";
import ServiceTabs from "./ServiceTabs";

export default function QnaAnswer({ question }) {
  const [savedAnswer, setSavedAnswer] = useState(question.answer);
  const [draft, setDraft] = useState(question.answer);
  const [notice, setNotice] = useState("");
  const noticeRef = useRef(null);
  const submit = (event) => {
    event.preventDefault();
    const value = draft.trim();
    if (!value) return;
    const updating = Boolean(savedAnswer);
    setSavedAnswer(value);
    setDraft(value);
    setNotice(`${updating ? "답변 수정" : "답변 등록"} 상태 미리보기입니다. 서버에는 저장하지 않았습니다.`);
    noticeRef.current?.focus();
  };
  return <>
    <p className="breadcrumb">관리자공간 / 관리자센터</p>
    <h1>Q&amp;A 답변 {savedAnswer ? "수정" : "작성"}</h1>
    <p className="description">{savedAnswer ? "등록된 답변을 수정하고 변경 내용을 저장하세요." : "문의 내용을 확인하고 사용자에게 답변을 남겨 주세요."}</p>
    <ServiceTabs active="qna" />
    <section className="qna-question" aria-labelledby="qna-question-title">
      <div className="qna-question-heading">
        <h2 id="qna-question-title">{question.title}</h2>
        <span className={`qna-status ${savedAnswer ? "is-answered" : ""}`}>{savedAnswer ? "답변 완료" : "미답변"}</span>
      </div>
      <p className="qna-question-meta">{question.author} / {question.createdAt}</p>
      <p className="qna-question-content">{question.content}</p>
    </section>
    {savedAnswer && <section className="qna-saved-answer" aria-labelledby="saved-answer-title">
      <h2 id="saved-answer-title">기존 관리자 답변</h2>
      {question.answeredAt && savedAnswer === question.answer && <p className="screen-note">기존 답변 등록일 {question.answeredAt}</p>}
      <p>{savedAnswer}</p>
    </section>}
    <form className="qna-answer-form" onSubmit={submit}>
      <label htmlFor="admin-answer">{savedAnswer ? "답변 수정" : "관리자 답변"}</label>
      <textarea id="admin-answer" value={draft} onChange={(event) => setDraft(event.target.value)}
        required maxLength={2000} placeholder="사용자에게 안내할 내용을 작성해 주세요." />
      <p className="review-char-count">{draft.length} / 2,000</p>
      <div className="detail-actions">
        <Link className="action-button action-back" href="/qna">취소</Link>
        <button type="submit" className="action-button action-approve"
          disabled={!draft.trim() || draft.trim() === savedAnswer}>
          {savedAnswer ? "수정 저장" : "답변 등록"}
        </button>
      </div>
    </form>
    <p className="screen-note" role="status" tabIndex={-1} ref={noticeRef}>{notice}</p>
    <p className="screen-note">예시 데이터로 동작합니다. 새로고침하거나 목록으로 이동하면 원래 데이터가 표시됩니다.</p>
  </>;
}
