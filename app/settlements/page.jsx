"use client";
import { useState } from "react";
import Link from "next/link";
import AdminShell from "../../components/AdminShell";
import ServiceTabs from "../../components/ServiceTabs";
import { settlements, totals, won } from "../../lib/settlement-mocks";

export default function SettlementsPage() {
  const periods = [...new Set(settlements.map((item) => item.period))].sort().reverse();
  const [period, setPeriod] = useState(periods[0]);
  const [status, setStatus] = useState("승인 대기");
  const monthly = settlements.filter((item) => item.period === period);
  const rows = monthly.filter((item) => item.status === status);
  const amount = rows.reduce((sum, item) => sum + totals(item).amount, 0);

  return <AdminShell active="settlements">
    <p className="breadcrumb">관리자공간 / 관리자센터</p>
    <h1>정산 목록</h1>
    <p className="description">강사의 정산 요청을 확인하고 상세 내역을 검토하세요.</p>
    <ServiceTabs active="settlements" />
    <div className="settlement-toolbar">
      <div className="filters" aria-label="정산 상태">
        {["승인 대기", "승인 완료", "거절"].map((item) => <button key={item} type="button"
          className={status === item ? "selected" : ""} aria-pressed={status === item}
          onClick={() => setStatus(item)}>
          {item}{item === "승인 대기" ? ` ${monthly.filter((row) => row.status === item).length}` : ""}
        </button>)}
      </div>
      <label className="settlement-period">정산 기간
        <select value={period} onChange={(event) => setPeriod(event.target.value)}>
          {periods.map((value) => <option key={value}>{value}</option>)}
        </select>
      </label>
    </div>
    <div className="settlement-summary" role="status">
      <div><p>{status} 정산</p><strong>총 {rows.length}건</strong></div>
      <div><p>{status === "승인 대기" ? "검토할 정산 금액" : "정산 금액 합계"}</p><strong>{won(amount)}</strong></div>
    </div>
    <div className="table-wrap"><table>
      <caption className="sr-only">정산 요청 목록</caption>
      <thead><tr>{["강사", "정산 기간", "정산 금액", "상태", "관리"].map((text) => <th key={text} scope="col">{text}</th>)}</tr></thead>
      <tbody>{rows.map((row) => <tr key={row.id}>
        <td>{row.name}</td><td>{row.period}</td><td>{won(totals(row).amount)}</td><td>{row.status}</td>
        <td><Link href={`/settlements/${row.id}`} className="detail-button">상세 보기</Link></td>
      </tr>)}
      {rows.length === 0 && <tr><td colSpan={5} className="empty-result">조건에 맞는 정산 내역이 없습니다.</td></tr>}
      </tbody>
    </table></div>
    <p className="screen-note">상세 보기에서 강의별 매출과 공제 금액을 확인할 수 있습니다. 현재는 예시 데이터입니다.</p>
  </AdminShell>;
}
