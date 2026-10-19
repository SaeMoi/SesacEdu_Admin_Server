import ServiceTabs from "../../../components/ServiceTabs";
import InstructorReview from "../../../components/InstructorReview";
import { notFound } from "next/navigation";
import AdminShell from "../../../components/AdminShell";
import { applications } from "../../../lib/instructor-mocks";

export default async function InstructorDetailPage({ params }) {
  const { userId } = await params;
  const application = applications.find(
    (item) => String(item.id) === userId,
  );

  if (!application) notFound();

  return (
    <AdminShell>
      <p className="breadcrumb">관리자공간 / 관리자센터</p>
      <h1>강사(신청) 상세</h1>
      <p className="description">
        프로필과 증빙 자료를 검토하고 강사 신청을 승인하세요.
      </p>

      <ServiceTabs active="instructors" />

      <InstructorReview application={application}>


      <dl className="application-detail">
        <div>
          <dt>강사 소개</dt>
          <dd className="detail-field detail-introduction">
            소개 내용은 API 연결 후 표시합니다.
          </dd>
        </div>
        <div>
          <dt>전문 분야</dt>
          <dd className="detail-field">{application.specialty}</dd>
        </div>
        <div>
          <dt>신청일</dt>
          <dd className="detail-field">{application.appliedAt}</dd>
        </div>
        <div>
          <dt>경력 및 증빙</dt>
          <dd className="detail-field">
            현재 예시 데이터에는 첨부 자료가 없습니다.
          </dd>
        </div>
      </dl>

      </InstructorReview>
    </AdminShell>
  );
}
