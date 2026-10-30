"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useStoredTheme } from "@/features/theme/useStoredTheme";
import {
  validateLoginForm,
  type LoginFieldErrors,
} from "./validation";

export default function LoginForm() {
  const router = useRouter();
  const theme = useStoredTheme();
  const [fieldErrors, setFieldErrors] = useState<LoginFieldErrors>({});
  const [formError, setFormError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError("");

    const form = event.currentTarget;
    const formData = new FormData(form);
    const errors = validateLoginForm(formData);
    setFieldErrors(errors);

    if (Object.keys(errors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: String(formData.get("email")).trim(),
          password: String(formData.get("password")),
        }),
      });

      if (!response.ok) {
        setFormError(
          response.status === 401
            ? "Email hoặc mật khẩu không chính xác."
            : "Đăng nhập chưa thành công. Vui lòng thử lại.",
        );
        return;
      }

      router.push("/");
    } catch {
      setFormError("Không gửi được yêu cầu. Hãy kiểm tra kết nối rồi thử lại.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="page-shell" data-theme={theme}>
      <section className="intro-panel" aria-label="Giới thiệu MyWiki">
        <Link className="brand" href="/" aria-label="MyWiki trang chủ">
          <span className="brand-mark" aria-hidden="true">
            M
          </span>
          <span>MyWiki</span>
        </Link>

        <div className="intro-copy">
          <p className="eyebrow">CHÀO MỪNG BẠN TRỞ LẠI</p>
          <h1>Tiếp tục xây dựng thế giới của riêng bạn.</h1>
          <p className="intro-description">
            Đăng nhập để trở lại với những ý tưởng và câu chuyện bạn đang tạo
            dựng.
          </p>
        </div>
        <p className="panel-footer">Một góc nhỏ cho những thế giới thật lớn.</p>
      </section>

      <section className="form-panel">
        <div className="form-wrap">
          <div className="form-heading">
            <p className="eyebrow">MYWIKI</p>
            <h2>Đăng nhập</h2>
            <p className="form-subtitle">
              Nhập thông tin tài khoản để tiếp tục.
            </p>
          </div>

          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <label className="field">
              <span>Email</span>
              <input
                autoComplete="email"
                name="email"
                type="email"
                placeholder="ban@example.com"
                maxLength={255}
                aria-invalid={Boolean(fieldErrors.email)}
                aria-describedby={
                  fieldErrors.email ? "login-email-error" : undefined
                }
              />
              {fieldErrors.email && (
                <small className="field-error" id="login-email-error">
                  {fieldErrors.email}
                </small>
              )}
            </label>

            <label className="field">
              <span>Mật khẩu</span>
              <input
                autoComplete="current-password"
                name="password"
                type="password"
                aria-invalid={Boolean(fieldErrors.password)}
                aria-describedby={
                  fieldErrors.password ? "login-password-error" : undefined
                }
              />
              {fieldErrors.password && (
                <small className="field-error" id="login-password-error">
                  {fieldErrors.password}
                </small>
              )}
            </label>

            {formError && (
              <div className="notice error-notice" role="alert">
                <span className="notice-icon" aria-hidden="true">
                  !
                </span>
                <p>{formError}</p>
              </div>
            )}

            <button
              className="submit-button"
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <span className="spinner" aria-hidden="true" />
                  Đang đăng nhập...
                </>
              ) : (
                <>
                  Đăng nhập <span aria-hidden="true">→</span>
                </>
              )}
            </button>
          </form>

          <p className="login-prompt">
            Chưa có tài khoản?{" "}
            <Link className="auth-link" href="/register">
              Đăng ký
            </Link>
          </p>
        </div>
      </section>
    </main>
  );
}
