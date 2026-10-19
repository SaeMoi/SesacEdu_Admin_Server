import Link from "next/link";

export default function AdminHeader() {
  const siteUrl =
    process.env.NEXT_PUBLIC_USER_SITE_URL || "http://localhost:5173";
  const base = siteUrl.replace(/\/+$/, "");

  return (
    <header className="admin-site-header">
      <div className="admin-site-header-inner">
        <Link href="/dashboard" className="admin-site-logo">
          <img
            src="/design/logo.png"
            alt="새싹에듀 관리자센터"
            width="130"
            height="48"
          />
        </Link>

        <nav className="admin-site-nav" aria-label="사용자 사이트 메뉴">
          <a href={`${base}/courses`}>강의</a>
          <a href={`${base}/community`}>커뮤니티</a>
        </nav>

        <div className="admin-header-search">
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            aria-hidden="true"
          >
            <circle cx="10.5" cy="10.5" r="6.5" />
            <path d="m16 16 4.5 4.5" />
          </svg>
          <input
            type="search"
            aria-label="통합 검색 — 준비 중"
            placeholder="회원·강의·문의 검색"
            disabled
          />
        </div>

        <span className="admin-header-role">관리자</span>

        <a className="admin-site-view" href={base}>
          사이트 보기
        </a>
      </div>
    </header>
  );
}
