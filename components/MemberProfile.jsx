"use client";

import { useState } from "react";
import Link from "next/link";
import ServiceTabs from "./ServiceTabs";

export default function MemberProfile({ member }) {
  const [savedStatus, setSavedStatus] = useState(member.status);
  const [nextStatus, setNextStatus] = useState(member.status);
  const [reason, setReason] = useState("");
  const [savedReason, setSavedReason] = useState("");
  const [notice, setNotice] = useState("");

  const fields = [
    ["이름", member.name],
    ["닉네임", member.nickname],
    ["이메일", member.email],
    ["생년월일", member.birthday],
    ["주소", member.address],
  ];

  const submit = (event) => {
    event.preventDefault();
    const trimmedReason = reason.trim();

    if (!trimmedReason || savedStatus === nextStatus) return;

    setSavedStatus(nextStatus);
    setSavedReason(trimmedReason);
    setReason("");
    setNotice(
      `${nextStatus} 상태로 변경한 화면 미리보기입니다. 서버에는 저장하지 않았습니다.`,
    );
  };

  return (
    <>
      <p className="breadcrumb">관리자공간 / 관리자센터</p>
      <h1>회원 프로필 조회</h1>
      <p className="description">
        회원의 기본정보를 확인하고 이용 상태를 변경하세요.
      </p>

      <ServiceTabs />

      <section className="member-summary" aria-label="회원 요약">
        <span className="member-avatar" aria-hidden="true">
          {member.name.slice(0, 1)}
        </span>
        <div className="member-summary-info">
          <h2>{member.name}</h2>
          <p>{member.role} · {member.email}</p>
        </div>
        <span className="member-status">{savedStatus}</span>
      </section>

      <section className="member-basic" aria-labelledby="member-basic-title">
        <h2 id="member-basic-title">기본정보</h2>
        <dl className="member-fields">
          {fields.map(([label, value]) => (
            <div key={label} className={label === "주소" ? "member-address" : ""}>
              <dt>{label}</dt>
              <dd>{value}</dd>
            </div>
          ))}
        </dl>
      </section>

      <form className="member-status-form" onSubmit={submit}>
        <div className="member-status-heading">
          <h2>이용 상태 변경</h2>
          <p>현재 상태: {savedStatus}</p>
        </div>

        <div className="member-status-controls">
          <fieldset className="member-status-options">
            <legend className="sr-only">변경할 이용 상태</legend>
            {["정상", "제한"].map((value) => (
              <label
                key={value}
                className={`member-status-option ${
                  nextStatus === value ? "is-selected" : ""
                }`}
              >
                <input
                  type="radio"
                  name="member-status"
                  value={value}
                  checked={nextStatus === value}
                  onChange={() => {
                    setNextStatus(value);
                    setNotice("");
                  }}
                />
                {value}
              </label>
            ))}
          </fieldset>

          <div className="member-reason-input">
            <label htmlFor="member-status-reason">변경 사유 *</label>
            <input
              id="member-status-reason"
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              placeholder="상태 변경 사유를 입력해 주세요"
              maxLength={500}
              required
            />
          </div>
        </div>

        {savedReason && (
          <section className="review-reason">
            <h2>이번 화면에서 입력한 변경 사유</h2>
            <p>{savedReason}</p>
          </section>
        )}

        <p className="screen-note">
          예시 동작입니다. 담당자 기록과 실제 작업 이력 저장은 API 연결 후 적용합니다.
        </p>

        <div className="detail-actions">
          <Link className="action-button action-back" href="/users">
            목록으로
          </Link>
          <button
            className="action-button action-reject"
            type="submit"
            disabled={!reason.trim() || nextStatus === savedStatus}
          >
            변경사항 저장
          </button>
        </div>
      </form>

      <p className="screen-note" role="status">{notice}</p>
      <p className="screen-note">
        새로고침하거나 목록으로 이동하면 원래 예시 데이터가 표시됩니다.
      </p>
    </>
  );
}
