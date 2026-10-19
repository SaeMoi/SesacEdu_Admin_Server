"use client";
import { useState } from "react";
import Link from "next/link";
import AdminShell from "../../components/AdminShell";
import ServiceTabs from "../../components/ServiceTabs";
import { questions } from "../../lib/qna-mocks";

export default function QnaListPage() {
  const [status, setStatus] = useState("전체");
  const [keyword, setKeyword] = useState("");
  const [search, setSearch] = useState("");
  const rows = questions.filter((item) => {
    const label = item.answer ? "답변 완료" : "미답변";
    const matchesText = `${item.title} ${item.author}`.toLocaleLowerCase().includes(search.toLocaleLowerCase());
    return (status === "전체" || status === label) && matchesText;
  });

  return <AdminShell active="qna">
    <p className="breadcrumb">관리자공간 / 관리자센터</p>
    <h1>Q&amp;A 목록</h1>
    <p className="description">서비스 문의를 조회하고 답변 상태를 확인하세요.</p>
    <ServiceTabs active="qna" />
    <div className="filters" aria-label="답변 상태">
      {["전체", "미답변", "답변 완료"].map((item) => <button key={item} type="button"
        className={status === item ? "selected" : ""} aria-pressed={status === item}
        onClick={() => setStatus(item)}>{item}</button>)}
    </div>
    <form className="search" onSubmit={(event) => { event.preventDefault(); setSearch(keyword.trim()); }}>
      <label htmlFor="qna-search">질문 검색</label>
      <div className="search-row">
        <input id="qna-search" placeholder="제목 또는 작성자 입력" value={keyword} onChange={(event) => setKeyword(event.target.value)} />
        <button type="submit">검색</button>
      </div>
    </form>
    <p className="result-count" role="status">총 {rows.length}건</p>
    <div className="table-wrap"><table>
      <caption className="sr-only">서비스 문의 목록</caption>
      <thead><tr>{["질문", "작성자", "상태", "작성일"].map((text) => <th key={text} scope="col">{text}</th>)}</tr></thead>
      <tbody>{rows.map((item) => <tr key={item.id}>
        <td><Link className="qna-title-link" href={`/qna/${item.id}`}>{item.title}</Link></td>
        <td>{item.author}</td>
        <td><Link className={`qna-status ${item.answer ? "is-answered" : ""}`} href={`/qna/${item.id}`}
          aria-label={`${item.title} ${item.answer ? "답변 수정" : "답변 작성"}`}>
          {item.answer ? "답변 완료" : "미답변"}
        </Link></td>
        <td>{item.createdAt.slice(5)}</td>
      </tr>)}
      {rows.length === 0 && <tr><td className="empty-result" colSpan={4}>조건에 맞는 문의가 없습니다.</td></tr>}
      </tbody>
    </table></div>
    <p className="screen-note">예시 데이터입니다. 질문 제목이나 상태를 누르면 답변 화면으로 이동합니다.</p>
  </AdminShell>;
}
