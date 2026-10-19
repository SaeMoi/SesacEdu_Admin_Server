"use client";

import ServiceTabs from "../components/ServiceTabs";

import { useState } from "react";
import Link from "next/link";
import AdminShell from "../components/AdminShell";
import { applications } from "../lib/instructor-mocks";


export default function HomePage() {
  const [keyword, setKeyword] = useState("");
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("전체");

  const visibleApplications = applications.filter((application) => {
    const matchesName = application.name.includes(search);
    const matchesStatus =
      status === "전체" || application.status === status;
    return matchesName && matchesStatus;
  });

  return (
    <>
      <AdminShell>
          <p className="breadcrumb">관리자공간 / 관리자센터</p>
          <h1>강사(신청) 목록</h1>
          <p className="description">
            신청자의 전문 분야와 제출 자료를 확인하고 승인하세요.
          </p>

      <ServiceTabs active="instructors" />

          <div className="filters" aria-label="신청 상태 필터">
            {["전체", "승인 완료", "승인 대기"].map((item) => (
              <button
                key={item}
                type="button"
                className={status === item ? "selected" : ""}
                aria-pressed={status === item}
                onClick={() => setStatus(item)}
              >
                {item}
              </button>
            ))}
          </div>

          <form
            className="search"
            onSubmit={(event) => {
              event.preventDefault();
              setSearch(keyword.trim());
            }}
          >
            <label htmlFor="applicant-search">신청자 검색</label>
            <div className="search-row">
              <input
                id="applicant-search"
                placeholder="이름"
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
              />
              <button type="submit">검색</button>
            </div>
          </form>

          <p className="result-count" role="status">
            총 {visibleApplications.length}건
          </p>

          <div className="table-wrap">
            <table>
              <caption className="sr-only">강사 신청 예시 목록</caption>
              <thead>
                <tr>
                  <th scope="col">신청자</th>
                  <th scope="col">전문 분야</th>
                  <th scope="col">신청일</th>
                  <th scope="col">상태</th>
                  <th scope="col">관리</th>
                </tr>
              </thead>
              <tbody>
                {visibleApplications.map((application) => (
                  <tr key={application.id}>
                    <td>{application.name}</td>
                    <td>{application.specialty}</td>
                    <td>{application.appliedAt}</td>
                    <td>{application.status}</td>
                    <td>
                      <Link
                        className="detail-button"
                        href={`/instructors/${application.id}`}
                      >
                        상세 확인
                      </Link>
                    </td>
                  </tr>
                ))}
                {visibleApplications.length === 0 && (
                  <tr>
                    <td colSpan={5} className="empty-result">
                      조건에 맞는 신청자가 없습니다.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <p className="screen-note">
            예시 데이터로 검색·상태 필터·상세 화면을 확인할 수 있습니다. API는 아직 연결하지 않았습니다.
          </p>
      </AdminShell>
    </>
  );
}
