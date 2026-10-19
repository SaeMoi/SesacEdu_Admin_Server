"use client";

import { useState } from "react";
import AdminShell from "../../components/AdminShell";
import ServiceTabs from "../../components/ServiceTabs";

const initialLectures = [
  {
    id: 1,
    name: "웹 보안 기초",
    instructor: "김새싹",
    status: "공개",
    students: 124,
    description: "인증과 접근 권한부터 입력값 검증까지 배우는 강의입니다.",
  },
  {
    id: 2,
    name: "리눅스 입문",
    instructor: "이학습",
    status: "공개",
    students: 86,
    description: "리눅스 기본 명령어와 서버 운영의 기초를 배우는 강의입니다.",
  },
  {
    id: 3,
    name: "보안 로그 분석",
    instructor: "박보안",
    status: "비공개",
    students: 0,
    description: "서버 로그를 확인하고 보안 이벤트를 분석하는 강의입니다.",
  },
];

export default function LecturesPage() {
  const [lectures, setLectures] = useState(initialLectures);
  const [status, setStatus] = useState("전체");
  const [keyword, setKeyword] = useState("");
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState(null);
  const [view, setView] = useState("list");
  const [notice, setNotice] = useState("");

  const rows = lectures.filter((lecture) => {
    const matchesStatus =
      status === "전체" || lecture.status === status;
    const matchesSearch =
      `${lecture.name} ${lecture.instructor}`
        .toLocaleLowerCase()
        .includes(search.toLocaleLowerCase());

    return matchesStatus && matchesSearch;
  });

  const open = (lecture, nextView) => {
    setSelected(lecture);
    setView(nextView);
    setNotice("");
  };

  const back = () => {
    setSelected(null);
    setView("list");
  };

  const remove = () => {
    if (!selected || view !== "delete") return;

    const name = selected.name;
    setLectures((current) =>
      current.filter((lecture) => lecture.id !== selected.id),
    );
    back();
    setNotice(
      `${name}을 예시 목록에서 삭제했습니다. 서버 데이터는 변경하지 않았습니다.`,
    );
  };

  return (
    <AdminShell active="lectures">
      <p className="breadcrumb">관리자공간 / 관리자센터</p>

      <h1>
        {view === "delete"
          ? "강의 강제 삭제 확인"
          : view === "detail"
            ? "강의 상세"
            : "전체 강의 관리"}
      </h1>

      <p className="description">
        {view === "delete"
          ? "선택한 강의와 수강생 정보를 확인한 뒤 삭제하세요."
          : "서비스의 강의를 확인하고 필요한 강의를 관리하세요."}
      </p>

      <ServiceTabs active="lectures" />

      <p className="screen-note" role="status">{notice}</p>

      {view === "list" ? (
        <>
          <div className="filters" aria-label="강의 공개 상태">
            {["전체", "공개", "비공개"].map((item) => (
              <button
                key={item}
                type="button"
                className={status === item ? "selected" : ""}
                aria-pressed={status === item}
                onClick={() => setStatus(item)}
              >
                {item}
              </button>
            ))}
          </div>

          <form
            className="search"
            onSubmit={(event) => {
              event.preventDefault();
              setSearch(keyword.trim());
            }}
          >
            <label htmlFor="lecture-search">강의 검색</label>
            <div className="search-row">
              <input
                id="lecture-search"
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
                placeholder="강의명 또는 강사명 입력"
              />
              <button type="submit">검색</button>
            </div>
          </form>

          <p className="result-count" role="status">
            총 {rows.length}건
          </p>

          <div className="table-wrap">
            <table>
              <caption className="sr-only">전체 강의 목록</caption>
              <thead>
                <tr>
                  {["강의", "강사", "상태", "수강생", "관리"].map((text) => (
                    <th key={text} scope="col">{text}</th>
                  ))}
                </tr>
              </thead>

              <tbody>
                {rows.map((lecture) => (
                  <tr key={lecture.id}>
                    <td>{lecture.name}</td>
                    <td>{lecture.instructor}</td>
                    <td>{lecture.status}</td>
                    <td>{lecture.students}명</td>
                    <td>
                      <div className="lecture-row-actions">
                        <button
                          className="detail-button"
                          type="button"
                          onClick={() => open(lecture, "detail")}
                          aria-label={`${lecture.name} 상세 확인`}
                        >
                          상세
                        </button>
                        <span aria-hidden="true">/</span>
                        <button
                          className="detail-button"
                          type="button"
                          onClick={() => open(lecture, "delete")}
                          aria-label={`${lecture.name} 삭제 확인`}
                        >
                          삭제
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}

                {rows.length === 0 && (
                  <tr>
                    <td colSpan={5} className="empty-result">
                      조건에 맞는 강의가 없습니다.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      ) : selected && (
        <section
          className="lecture-confirm"
          aria-labelledby="lecture-detail-title"
        >
          <h2 id="lecture-detail-title">
            {view === "delete"
              ? `${selected.name}를 삭제할까요?`
              : selected.name}
          </h2>

          <div className="lecture-info-tags">
            <span>강사 {selected.instructor}</span>
            <span>수강생 {selected.students}명</span>
            <span>{selected.status}</span>
          </div>

          {view === "delete" ? (
            <p className="lecture-confirm-copy">
              삭제 후 강의에 접근할 수 없습니다.
              <br />
              수강생과 결제 내역에 미치는 영향을 먼저 확인해 주세요.
              <br />
              현재는 예시 목록에서만 삭제됩니다.
            </p>
          ) : (
            <p className="lecture-confirm-copy">
              {selected.description}
            </p>
          )}

          <div className="detail-actions">
            <button
              type="button"
              className="action-button action-back"
              onClick={back}
            >
              {view === "delete" ? "취소" : "목록으로"}
            </button>

            <button
              type="button"
              className="action-button lecture-danger"
              onClick={
                view === "delete"
                  ? remove
                  : () => setView("delete")
              }
            >
              {view === "delete" ? "강제 삭제" : "삭제 확인"}
            </button>
          </div>
        </section>
      )}

      <p className="screen-note">
        예시 데이터입니다. 새로고침하면 삭제한 강의도 원래 상태로 돌아옵니다.
      </p>
    </AdminShell>
  );
}
