import Link from "next/link";
import AdminShell from "../../components/AdminShell";
import ServiceTabs from "../../components/ServiceTabs";
import { applications } from "../../lib/instructor-mocks";
import { settlements } from "../../lib/settlement-mocks";
import { questions } from "../../lib/qna-mocks";

export default function DashboardPage() {
  const cards = [
    {
      title: "강사 승인 대기",
      count: applications.filter(
        (item) => item.status === "승인 대기",
      ).length,
      description: "새로운 강사 신청을 검토해 주세요.",
      href: "/",
      linkText: "신청 바로 확인하기",
      className: "dashboard-instructors",
    },
    {
      title: "정산 승인 대기",
      count: settlements.filter(
        (item) => item.status === "승인 대기",
      ).length,
      description: "요청된 정산 내역을 확인해 주세요.",
      href: "/settlements",
      linkText: "정산 바로 확인하기",
      className: "dashboard-settlements",
    },
    {
      title: "미답변 문의",
      count: questions.filter(
        (item) => !item.answer,
      ).length,
      description: "답변을 기다리는 문의가 있어요.",
      href: "/qna",
      linkText: "문의 바로 확인하기",
      className: "dashboard-questions",
    },
  ];

  return (
    <AdminShell active="dashboard">
      <p className="breadcrumb">관리자공간 / 관리자센터</p>
      <h1>운영 대시보드</h1>
      <p className="description">
        승인 대기와 문의 처리 현황을 한눈에 확인하세요.
      </p>

      <ServiceTabs />

      <section className="dashboard-grid" aria-label="운영 현황">
        {cards.map((card) => (
          <article
            key={card.title}
            className={`dashboard-card ${card.className}`}
          >
            <h2>{card.title}</h2>

            <p className="dashboard-count">
              <strong>{card.count}</strong>
              <span>건</span>
            </p>

            <p className="dashboard-description">
              {card.description}
            </p>

            <Link className="dashboard-link" href={card.href}>
              {card.linkText}
              <span aria-hidden="true"> →</span>
            </Link>
          </article>
        ))}
      </section>

      <p className="screen-note">
        필요한 항목의 바로 확인하기 버튼을 눌러 상세 목록으로 이동하세요.
      </p>
      <p className="screen-note">
        예시 데이터 기준입니다. 앞 화면에서 임시로 변경한 상태는
        이 집계에 반영되지 않습니다.
      </p>
    </AdminShell>
  );
}
