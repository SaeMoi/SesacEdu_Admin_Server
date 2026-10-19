"use client";

import { useRef, useState } from "react";
import AdminShell from "../../components/AdminShell";
import ServiceTabs from "../../components/ServiceTabs";

const logs = [
  {
    id: "DEMO-001",
    time: "2026.10.19 14:30",
    admin: "운영관리자",
    action: "강사 승인",
    target: "김새싹",
    result: "성공",
    before: "승인 대기",
    after: "승인 완료",
    reason: "신청 정보와 제출 자료를 검토했습니다.",
  },
  {
    id: "DEMO-002",
    time: "2026.10.19 14:20",
    admin: "운영관리자",
    action: "정산 거절",
    target: "이학습 / 2026.10",
    result: "성공",
    before: "승인 대기",
    after: "거절",
    reason: "정산 집계 자료 재확인이 필요합니다.",
  },
  {
    id: "DEMO-003",
    time: "2026.10.19 14:10",
    admin: "회원관리자",
    action: "회원 상태 변경",
    target: "박보안",
    result: "성공",
    before: "정상",
    after: "제한",
    reason: "서비스 이용 정책 위반 내역을 확인했습니다.",
  },
  {
    id: "DEMO-004",
    time: "2026.10.19 14:00",
    admin: "운영관리자",
    action: "강의 삭제",
    target: "웹 보안 기초",
    result: "실패",
    before: "공개",
    after: "공개",
    reason: "예시 오류입니다. 강의는 변경되지 않았습니다.",
  },
];

export default function AuditLogsPage() {
  const dialog = useRef(null);
  const [keyword, setKeyword] = useState("");
  const [search, setSearch] = useState("");
  const [action, setAction] = useState("전체");
  const [result, setResult] = useState("전체");
  const [selected, setSelected] = useState(null);

  const actions = [...new Set(logs.map((item) => item.action))];

  const rows = logs.filter((item) => {
    const text = `${item.id} ${item.admin} ${item.action} ${item.target}`;

    return (
      (action === "전체" || item.action === action) &&
      (result === "전체" || item.result === result) &&
      text.toLocaleLowerCase().includes(search.toLocaleLowerCase())
    );
  });

  const open = (item) => {
    setSelected(item);
    dialog.current.showModal();
  };

  const fields = selected
    ? [
        ["이력 번호", selected.id],
        ["처리 시각", selected.time],
        ["담당 관리자", selected.admin],
        ["작업 종류", selected.action],
        ["대상", selected.target],
        ["처리 결과", selected.result],
        ["변경 전", selected.before],
        ["변경 후", selected.after],
        ["사유 / 처리 설명", selected.reason],
      ]
    : [];

  return (
    <AdminShell active="audit-logs">
      <p className="breadcrumb">관리자공간 / 관리자센터</p>
      <h1>관리자 작업 이력</h1>
      <p className="description">
        관리자 작업의 담당자와 처리 결과를 확인하세요.
      </p>

      <ServiceTabs />

      <div className="audit-filter-row">
        <label>
          작업 종류
          <select value={action} onChange={(e) => setAction(e.target.value)}>
            <option value="전체">전체</option>
            {actions.map((value) => (
              <option key={value} value={value}>{value}</option>
            ))}
          </select>
        </label>

        <label>
          처리 결과
          <select value={result} onChange={(e) => setResult(e.target.value)}>
            {["전체", "성공", "실패"].map((value) => (
              <option key={value} value={value}>{value}</option>
            ))}
          </select>
        </label>
      </div>

      <form
        className="search"
        onSubmit={(event) => {
          event.preventDefault();
          setSearch(keyword.trim());
        }}
      >
        <label htmlFor="audit-search">작업 이력 검색</label>
        <div className="search-row">
          <input
            id="audit-search"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="관리자, 대상, 작업 종류 또는 이력 번호 입력"
          />
          <button type="submit">검색</button>
        </div>
      </form>

      <p className="result-count" role="status">총 {rows.length}건</p>

      <div className="table-wrap">
        <table className="audit-table">
          <caption className="sr-only">관리자 작업 예시 이력</caption>
          <thead>
            <tr>
              {["시각", "관리자", "작업", "대상", "결과", "상세"].map((text) => (
                <th key={text} scope="col">{text}</th>
              ))}
            </tr>
          </thead>

          <tbody>
            {rows.map((item) => (
              <tr key={item.id}>
                <td className="audit-time">{item.time}</td>
                <td>{item.admin}</td>
                <td>{item.action}</td>
                <td>{item.target}</td>
                <td>
                  <span className={`audit-result ${
                    item.result === "실패" ? "is-failed" : ""
                  }`}>
                    {item.result}
                  </span>
                </td>
                <td>
                  <button
                    type="button"
                    className="detail-button"
                    aria-label={`${item.id} 상세 보기`}
                    onClick={() => open(item)}
                  >
                    상세 보기
                  </button>
                </td>
              </tr>
            ))}

            {rows.length === 0 && (
              <tr>
                <td colSpan={6} className="empty-result">
                  조건에 맞는 작업 이력이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <p className="screen-note">
        화면 확인용 예시 기록입니다.
        다른 화면에서 수행한 임시 작업과 연결되어 있지 않습니다.
      </p>

      <dialog
        ref={dialog}
        className="review-dialog"
        aria-labelledby="audit-detail-title"
      >
        <div className="review-dialog-heading">
          <h2 id="audit-detail-title">작업 이력 상세</h2>
          <button
            type="button"
            aria-label="상세 팝업 닫기"
            onClick={() => dialog.current.close()}
          >
            ×
          </button>
        </div>

        <dl className="audit-details">
          {fields.map(([label, value]) => (
            <div key={label}>
              <dt>{label}</dt>
              <dd>{value}</dd>
            </div>
          ))}
        </dl>

        <div className="detail-actions">
          <button
            type="button"
            className="action-button action-back"
            onClick={() => dialog.current.close()}
          >
            닫기
          </button>
        </div>
      </dialog>
    </AdminShell>
  );
}
