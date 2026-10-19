import { notFound } from "next/navigation";
import AdminShell from "../../../components/AdminShell";
import QnaAnswer from "../../../components/QnaAnswer";
import { questions } from "../../../lib/qna-mocks";

export default async function QnaDetailPage({ params }) {
  const { iqid } = await params;
  const question = questions.find((item) => String(item.id) === iqid);
  if (!question) notFound();
  return <AdminShell active="qna"><QnaAnswer question={question} /></AdminShell>;
}
