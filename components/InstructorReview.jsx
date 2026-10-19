"use client";

import Link from "next/link";
import { useRef, useState } from "react";

export default function InstructorReview({ application, children }) {
  const dialog = useRef(null);
  const [status, setStatus] = useState(application.status);
  const [mode, setMode] = useState("approve");
  const [reason, setReason] = useState("");
  const [savedReason, setSavedReason] = useState("");
  const [notice, setNotice] = useState("");

  const open = (nextMode) => {
    setMode(nextMode);
    setReason("");
    dialog.current.showModal();
  };

  const close = () => dialog.current.close();

  const submit = (event) => {
    event.preventDefault();
    if (status !== "승인 대기") return;

    if (mode === "reject") {
      const value = reason.trim();
      if (!value) return;
      setStatus("반려");
      setSavedReason(value);
      setNotice("반려 상태 미리보기입니다. 서버에는 저장하지 않았습니다.");
    } else {
      setStatus("승인 완료");
      setNotice("승인 완료 상태 미리보기입니다. 서버에는 저장하지 않았습니다.");
    }

    close();
  };

  return (
    <>
      <section className="applicant-summary" aria-label="신청자 요약">
        <div>
          <h2>{application.name}</h2>
          <p>이메일 정보는 API 연결 후 표시합니다.</p>
        </div>
        <span className="application-status">{status}</span>
      </section>

      {children}

      {savedReason && (
        <section className="review-reason" aria-label="반려 사유">
          <h2>반려 사유</h2>
          <p>{savedReason}</p>
        </section>
      )}

      <div className="detail-actions">
        <Link className="action-button action-back" href="/">목록으로</Link>
        {status === "승인 대기" && (
          <>
            <button
              className="action-button action-approve"
              type="button"
              onClick={() => open("approve")}
            >
              강사 승인
            </button>
            <button
              className="action-button action-reject"
              type="button"
              onClick={() => open("reject")}
            >
              강사 거절
            </button>
          </>
        )}
      </div>

      <p className="screen-note" role="status">{notice}</p>
      <p className="screen-note">
        예시 데이터로 동작합니다. 새로고침하면 원래 상태로 돌아옵니다.
      </p>

      <dialog ref={dialog} className="review-dialog" aria-labelledby="review-title">
        <form onSubmit={submit}>
          <div className="review-dialog-heading">
            <h2 id="review-title">
              {mode === "reject" ? "강사 신청 반려" : "강사 신청 승인"}
            </h2>
            <button type="button" onClick={close} aria-label="팝업 닫기">×</button>
          </div>

          <p>
            {application.name} 님의 강사 신청을
            {mode === "reject" ? " 반려하시겠어요?" : " 승인하시겠어요?"}
          </p>

          {mode === "reject" && (
            <div className="review-reason-input">
              <label htmlFor="rejection-reason">반려 사유 *</label>
              <textarea
                id="rejection-reason"
                value={reason}
                onChange={(event) => setReason(event.target.value)}
                maxLength={500}
                required
                placeholder="보완이 필요한 내용을 구체적으로 작성해 주세요."
              />
              <p className="review-char-count">{reason.length} / 500</p>
            </div>
          )}

          <div className="detail-actions">
            <button type="button" className="action-button action-back" onClick={close}>
              취소
            </button>
            <button
              type="submit"
              className="action-button action-reject"
              disabled={mode === "reject" && !reason.trim()}
            >
              {mode === "reject" ? "반려 확정" : "승인 확정"}
            </button>
          </div>
        </form>
      </dialog>
    </>
  );
}
