import AdminHeader from "./AdminHeader";

export default function AdminShell({ children, active = "instructors" }) {
  return (
    <>
      <AdminHeader />

      <div className="admin-layout">
        <aside className="sidebar">
          <p className="eyebrow">ADMIN</p>
          <h2>관리자센터</h2>

          <p className="menu-label">서비스 관리</p>
          <nav aria-label="관리자 메뉴">
            <a href="/" aria-current={active === "instructors" ? "page" : undefined}>강사 관리</a>
            <a href="/settlements" aria-current={active === "settlements" ? "page" : undefined}>정산 관리</a>
            <a href="/qna" aria-current={active === "qna" ? "page" : undefined}>Q&amp;A 관리</a>
            <a href="/lectures" aria-current={active === "lectures" ? "page" : undefined}>강의 관리</a>
          </nav>

          <p className="menu-label">추가 제안</p>
          <nav aria-label="운영 메뉴">
            <a href="/dashboard" aria-current={active === "dashboard" ? "page" : undefined}>운영 대시보드</a>
            <a href="/users" aria-current={active === "users" ? "page" : undefined}>회원 / 권한</a>
            <a href="/audit-logs" aria-current={active === "audit-logs" ? "page" : undefined}>관리자 작업 이력</a>
          </nav>
        </aside>

        <main>{children}</main>
      </div>
    </>
  );
}
