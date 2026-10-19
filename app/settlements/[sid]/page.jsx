import { notFound } from "next/navigation";
import AdminShell from "../../../components/AdminShell";
import ServiceTabs from "../../../components/ServiceTabs";
import SettlementReview from "../../../components/SettlementReview";
import { settlements } from "../../../lib/settlement-mocks";

export default async function SettlementPage({ params }) {
  const { sid } = await params;
  const record = settlements.find((item) => String(item.id) === sid);
  if (!record) notFound();
  return <AdminShell active="settlements">
    <p className="breadcrumb">관리자공간 / 관리자센터</p>
    <h1>정산 상세</h1>
    <p className="description">강의별 매출과 공제 내역을 확인한 뒤 승인 또는 거절하세요.</p>
    <ServiceTabs active="settlements" />
    <SettlementReview record={record} />
  </AdminShell>;
}
