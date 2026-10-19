import { notFound } from "next/navigation";
import AdminShell from "../../../components/AdminShell";
import MemberProfile from "../../../components/MemberProfile";
import { members } from "../../../lib/member-mocks";

export default async function MemberPage({ params }) {
  const { userId } = await params;
  const member = members.find((item) => String(item.id) === userId);

  if (!member) notFound();

  return (
    <AdminShell active="users">
      <MemberProfile member={member} />
    </AdminShell>
  );
}
