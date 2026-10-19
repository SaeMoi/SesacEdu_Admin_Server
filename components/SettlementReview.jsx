"use client";
import { useRef, useState } from "react";
import Link from "next/link";
import { totals, won } from "../lib/settlement-mocks";

export default function SettlementReview({ record }) {
  const dialog = useRef(null);
  const [status, setStatus] = useState(record.status);
  const [mode, setMode] = useState("approve");
  const [reason, setReason] = useState("");
  const [savedReason, setSavedReason] = useState(record.reason || "");
  const [notice, setNotice] = useState("");
  const sum = totals(record);
  const open = (nextMode) => { setMode(nextMode); setReason(""); dialog.current.showModal(); };
  const submit = (event) => {
    event.preventDefault();
    if (status !== "승인 대기" || (mode === "reject" && !reason.trim())) return;
    setStatus(mode === "reject" ? "거절" : "승인 완료");
    setSavedReason(mode === "reject" ? reason.trim() : "");
    setNotice("화면 상태를 변경했습니다. 예시 동작이며 서버에는 저장하지 않았습니다.");
    dialog.current.close();
  };

  return <>
    <section className="applicant-summary settlement-person" aria-label="정산 대상">
      <div><h2>{record.name}</h2><p>정산 기간 {record.period} · {record.email}</p></div>
      <span className="application-status">{status}</span>
    </section>
    <div className="table-wrap"><table>
      <caption className="sr-only">강의별 정산 내역</caption>
      <thead><tr>{["강의", "매출", "공제", "정산 금액"].map((text) => <th key={text} scope="col">{text}</th>)}</tr></thead>
      <tbody>{record.items.map((item) => <tr key={item.id}>
        <td>{item.name}</td><td>{won(item.sales)}</td><td>{won(item.deduction)}</td><td>{won(item.sales - item.deduction)}</td>
      </tr>)}</tbody>
    </table></div>
    <section className="settlement-total" aria-label="정산 합계">
      <p>총 매출 {won(sum.sales)} · 총 공제 {won(sum.deduction)}</p>
      <div><span>정산 합계</span><strong>{won(sum.amount)}</strong></div>
    </section>
    <p className="screen-note">지급 계좌와 지급 예정일은 API 연결 후 표시합니다.</p>
    {savedReason && <section className="review-reason"><h2>거절 사유</h2><p>{savedReason}</p></section>}
    <div className="detail-actions">
      <Link className="action-button action-back" href="/settlements">목록으로</Link>
      {status === "승인 대기" && <>
        <button type="button" className="action-button settlement-reject" onClick={() => open("reject")}>정산 거절</button>
        <button type="button" className="action-button action-reject" onClick={() => open("approve")}>정산 승인</button>
      </>}
    </div>
    <p className="screen-note" role="status">{notice}</p>
    <p className="screen-note">예시 데이터입니다. 새로고침하거나 목록으로 이동하면 원래 데이터가 표시됩니다.</p>
    <dialog ref={dialog} className="review-dialog" aria-labelledby="settlement-dialog-title">
      <form onSubmit={submit}>
        <div className="review-dialog-heading">
          <h2 id="settlement-dialog-title">{mode === "reject" ? "정산 거절 사유" : "정산 승인 확인"}</h2>
          <button type="button" aria-label="팝업 닫기" onClick={() => dialog.current.close()}>×</button>
        </div>
        <div className="settlement-dialog-summary"><p>{record.name} / {record.period}</p><p>정산 금액 {won(sum.amount)}</p></div>
        {mode === "reject" ? <div className="review-reason-input">
          <label htmlFor="settlement-reason">거절 사유 *</label>
          <textarea id="settlement-reason" value={reason} onChange={(event) => setReason(event.target.value)}
            maxLength={500} required placeholder="확인이 필요한 항목과 거절 사유를 작성해 주세요." />
          <p className="review-char-count">{reason.length} / 500</p>
        </div> : <p>정산 내역을 승인하시겠어요? 현재 단계에서는 실제 지급되지 않습니다.</p>}
        <div className="detail-actions">
          <button type="button" className="action-button action-back" onClick={() => dialog.current.close()}>취소</button>
          <button type="submit" className="action-button action-approve" disabled={mode === "reject" && !reason.trim()}>
            {mode === "reject" ? "거절 확정" : "승인 확정"}
          </button>
        </div>
      </form>
    </dialog>
  </>;
}
