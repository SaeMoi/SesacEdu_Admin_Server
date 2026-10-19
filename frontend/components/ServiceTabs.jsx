import Link from "next/link";
const tabs = [["instructors", "/", "강사 신청"], ["settlements", "/settlements", "정산"], ["qna", "/qna", "Q&A"], ["lectures", "/lectures", "강의"]];
export default function ServiceTabs({ active }) {
  return <nav className="section-tabs" aria-label="서비스 관리">
    {tabs.map(([key, href, label]) => <Link key={key} href={href}
      className={active === key ? "selected" : undefined}
      aria-current={active === key ? "page" : undefined}>{label}</Link>)}

  </nav>;
}
