"use client";

import { useState } from "react";

export default function AdminLoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [notice, setNotice] = useState("");

  const submit = (event) => {
    event.preventDefault();

    setNotice(
      "로그인 서비스 연결을 준비 중입니다. 현재는 로그인할 수 없습니다.",
    );
  };

  return (
    <form className="admin-login-form" onSubmit={submit}>
      <div className="admin-login-field">
        <label htmlFor="admin-email">이메일</label>
        <input
          id="admin-email"
          name="email"
          type="email"
          autoComplete="username"
          autoCapitalize="none"
          spellCheck={false}
          placeholder="관리자 이메일을 입력해 주세요"
          value={email}
          onChange={(event) => {
            setEmail(event.target.value);
            setNotice("");
          }}
          onBlur={() => setEmail((value) => value.trim())}
          maxLength={100}
          required
        />
      </div>

      <div className="admin-login-field">
        <label htmlFor="admin-password">비밀번호</label>

        <div className="admin-password-control">
          <input
            id="admin-password"
            name="password"
            type={showPassword ? "text" : "password"}
            autoComplete="current-password"
            placeholder="비밀번호를 입력해 주세요"
            value={password}
            onChange={(event) => {
              setPassword(event.target.value);
              setNotice("");
            }}
            required
          />

          <button
            type="button"
            className="admin-password-toggle"
            aria-controls="admin-password"
            aria-label="비밀번호 표시"
            aria-pressed={showPassword}
            onClick={() => setShowPassword((value) => !value)}
          >
            {showPassword ? "숨기기" : "보기"}
          </button>
        </div>
      </div>

      <p className="admin-login-notice" role="status">
        {notice}
      </p>

      <button className="admin-login-submit" type="submit">
        로그인
      </button>
    </form>
  );
}
