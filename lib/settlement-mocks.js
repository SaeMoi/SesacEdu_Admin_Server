export const settlements = [
  { id: 1, name: "김새싹", email: "instructor1@example.test", period: "2026.10", status: "승인 대기",
    items: [{ id: 1, name: "웹 보안 기초", sales: 980000, deduction: 196000 },
            { id: 2, name: "리눅스 입문", sales: 260000, deduction: 52000 }] },
  { id: 2, name: "이학습", email: "instructor2@example.test", period: "2026.10", status: "승인 대기",
    items: [{ id: 3, name: "시스템 기초", sales: 850000, deduction: 170000 }] },
  { id: 3, name: "박보안", email: "instructor3@example.test", period: "2026.10", status: "승인 완료",
    items: [{ id: 4, name: "로그 분석", sales: 400000, deduction: 80000 }] },
  { id: 4, name: "김새싹", email: "instructor1@example.test", period: "2026.09", status: "거절",
    reason: "매출 집계 내역 재확인이 필요합니다.",
    items: [{ id: 5, name: "웹 보안 기초", sales: 300000, deduction: 60000 }] },
];
export const won = (value) => `${value.toLocaleString("ko-KR")}원`;
export const totals = (record) => record.items.reduce((sum, item) => ({
  sales: sum.sales + item.sales,
  deduction: sum.deduction + item.deduction,
  amount: sum.amount + item.sales - item.deduction,
}), { sales: 0, deduction: 0, amount: 0 });
