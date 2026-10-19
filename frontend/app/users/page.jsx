"use client";

import { useState } from "react";
import Link from "next/link";
import AdminShell from "../../components/AdminShell";
import ServiceTabs from "../../components/ServiceTabs";
import { members } from "../../lib/member-mocks";

export default function MembersPage() {
  const [keyword, setKeyword] = useState("");
  const [search, setSearch] = useState("");

  const rows = members.filter((member) =>
    `${member.name} ${member.email}`
      .toLocaleLowerCase()
      .includes(search.toLocaleLowerCase()),
  );

  return (
    <AdminShell active="users">
      <p className="breadcrumb">관리자공간 / 관리자센터</p>
      <h1>회원 / 권한 관리</h1>
      <p className="description">
        회원 상태와 역할을 조회하고 변경 이력을 관리하세요.
      </p>

      <ServiceTabs />

      <form
        className="search"
        onSubmit={(event) => {
          event.preventDefault();
          setSearch(keyword.trim());
        }}
      >
        <label htmlFor="member-search">회원 검색</label>
        <div className="search-row">
          <input
            id="member-search"
            placeholder="이름 또는 이메일 입력"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button type="submit">검색</button>
        </div>
      </form>

      <p className="result-count" role="status">총 {rows.length}건</p>

      <div className="table-wrap">
        <table>
          <caption className="sr-only">회원 목록</caption>
          <thead>
            <tr>
              {["회원", "이메일", "역할", "상태", "관리"].map((text) => (
                <th key={text} scope="col">{text}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((member) => (
              <tr key={member.id}>
                <td>{member.name}</td>
                <td>{member.email}</td>
                <td>{member.role}</td>
                <td>{member.status}</td>
                <td>
                  <Link
                    className="member-detail-link"
                    href={`/users/${member.id}`}
                    aria-label={`${member.name} 상세 보기`}
                  >
                    상세 보기
                  </Link>
                </td>
              </tr>
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={5} className="empty-result">
                  조건에 맞는 회원이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <p className="member-guide">
        권한과 이용 상태를 변경할 때는 변경 사유와 담당자를 기록합니다.
      </p>
      <p className="screen-note">현재는 예시 데이터입니다.</p>
    </AdminShell>
  );
}
