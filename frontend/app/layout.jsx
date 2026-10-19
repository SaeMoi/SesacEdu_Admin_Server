import "./globals.css";

export const metadata = {
  title: "새싹에듀 관리자",
  description: "새싹에듀 관리자센터",
};

export default function RootLayout({ children }) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
