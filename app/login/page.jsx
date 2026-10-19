import AdminHeader from "../../components/AdminHeader";
import AdminLoginForm from "../../components/AdminLoginForm";

export const metadata = {
  title: "관리자 로그인 · 새싹에듀",
};

export default function LoginPage() {
  return (
    <>
      <AdminHeader />

      <main className="admin-login-page">
        <p className="eyebrow">ADMIN</p>
        <h1>관리자 로그인</h1>
        <p className="description">
          관리자 계정으로 로그인해 주세요.
        </p>

        <AdminLoginForm />

        <p className="admin-login-guide">
          관리자에게 부여된 계정으로 이용할 수 있습니다.
        </p>
      </main>
    </>
  );
}
