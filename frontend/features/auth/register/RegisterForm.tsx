"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { validateForm, type FieldErrors } from "./validation";

type RegisterResponse = {
  code?: string;
  message?: string;
  details?: Record<string, string>;
};

export default function RegisterForm() {
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState("");
  const [success, setSuccess] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError("");
    setSuccess(false);

    const form = event.currentTarget;
    const formData = new FormData(form);
    const errors = validateForm(formData);
    setFieldErrors(errors);

    if (Object.keys(errors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetch("/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username: String(formData.get("username")).trim(),
          email: String(formData.get("email")).trim(),
          password: String(formData.get("password")),
        }),
      });
      const result = (await response.json()) as RegisterResponse;

      if (!response.ok) {
        if (result.details) {
          setFieldErrors((current) => ({
            ...current,
            username: result.details?.username,
            email: result.details?.email,
            password: result.details?.password,
          }));
        }

        if (response.status === 409) {
          setFormError("Email này đã được sử dụng. Bạn hãy thử email khác nhé.");
        } else {
          setFormError(
            result.message ?? "Đăng ký chưa thành công. Vui lòng thử lại.",
          );
        }
        return;
      }

      setSuccess(true);
      form.reset();
      setFieldErrors({});
    } catch {
      setFormError("Không gửi được yêu cầu. Hãy kiểm tra kết nối rồi thử lại.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="page-shell">
      <section className="intro-panel" aria-label="Giới thiệu MyWiki">
        <Link className="brand" href="/" aria-label="MyWiki trang chủ">
          <span className="brand-mark" aria-hidden="true">
            M
          </span>
          <span>MyWiki</span>
        </Link>

        <div className="intro-copy">
          <p className="eyebrow">YOUR IDEAS, CONNECTED</p>
          <h1>Mỗi thế giới lớn lao đều bắt đầu từ một ý tưởng.</h1>
          <p className="intro-description">
            Tạo một không gian riêng để gom nhặt ý tưởng, xây dựng nhân vật và
            kết nối những mảnh ghép trong thế giới của bạn.
          </p>
        </div>

        <div className="floating-note" aria-hidden="true">
          <span className="note-dot" />
          <span>Ý tưởng mới đang chờ bạn...</span>
          <span className="note-sparkle">✳</span>
        </div>
        <p className="panel-footer">Một góc nhỏ cho những thế giới thật lớn.</p>
      </section>

      <section className="form-panel">
        <div className="form-wrap">
          <div className="form-heading">
            <p className="eyebrow">BẮT ĐẦU HÀNH TRÌNH</p>
            <h2>Tạo tài khoản</h2>
            <p className="form-subtitle">
              Đăng ký miễn phí để bắt đầu xây dựng thế giới của bạn.
            </p>
          </div>

          {success ? (
            <div className="notice success-notice" role="status">
              <span className="notice-icon" aria-hidden="true">
                ✓
              </span>
              <div>
                <strong>Tạo tài khoản thành công!</strong>
                <p>
                  Tài khoản của bạn đã sẵn sàng. Đăng nhập sẽ là bước tiếp theo.
                </p>
                <button
                  className="text-button"
                  onClick={() => setSuccess(false)}
                  type="button"
                >
                  Tạo thêm tài khoản
                </button>
              </div>
            </div>
          ) : (
            <form className="auth-form" onSubmit={handleSubmit} noValidate>
              <label className="field">
                <span>Tên hiển thị</span>
                <input
                  autoComplete="name"
                  name="username"
                  placeholder="Ví dụ: An Nguyễn"
                  maxLength={50}
                  aria-invalid={Boolean(fieldErrors.username)}
                  aria-describedby={
                    fieldErrors.username ? "username-error" : undefined
                  }
                />
                {fieldErrors.username && (
                  <small className="field-error" id="username-error">
                    {fieldErrors.username}
                  </small>
                )}
              </label>

              <label className="field">
                <span>Email</span>
                <input
                  autoComplete="email"
                  name="email"
                  type="email"
                  placeholder="ban@example.com"
                  maxLength={255}
                  aria-invalid={Boolean(fieldErrors.email)}
                  aria-describedby={fieldErrors.email ? "email-error" : undefined}
                />
                {fieldErrors.email && (
                  <small className="field-error" id="email-error">
                    {fieldErrors.email}
                  </small>
                )}
              </label>

              <label className="field">
                <span>Mật khẩu</span>
                <input
                  autoComplete="new-password"
                  name="password"
                  type="password"
                  placeholder="Ít nhất 6 ký tự"
                  minLength={6}
                  aria-invalid={Boolean(fieldErrors.password)}
                  aria-describedby={
                    fieldErrors.password ? "password-error" : "password-hint"
                  }
                />
                {fieldErrors.password ? (
                  <small className="field-error" id="password-error">
                    {fieldErrors.password}
                  </small>
                ) : (
                  <small className="field-hint" id="password-hint">
                    Dùng ít nhất 6 ký tự.
                  </small>
                )}
              </label>

              <label className="field">
                <span>Nhập lại mật khẩu</span>
                <input
                  autoComplete="new-password"
                  name="confirmPassword"
                  type="password"
                  placeholder="Nhập lại mật khẩu của bạn"
                  aria-invalid={Boolean(fieldErrors.confirmPassword)}
                  aria-describedby={
                    fieldErrors.confirmPassword
                      ? "confirm-password-error"
                      : undefined
                  }
                />
                {fieldErrors.confirmPassword && (
                  <small className="field-error" id="confirm-password-error">
                    {fieldErrors.confirmPassword}
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
                    Đang tạo tài khoản...
                  </>
                ) : (
                  <>
                    Tạo tài khoản <span aria-hidden="true">→</span>
                  </>
                )}
              </button>

              <p className="terms">
                Bằng việc đăng ký, bạn đồng ý sử dụng MyWiki một cách có trách
                nhiệm.
              </p>
            </form>
          )}

          <p className="login-prompt">
            Đã có tài khoản?{" "}
            <Link className="auth-link" href="/login">
              Đăng nhập
            </Link>
          </p>
        </div>
      </section>
    </main>
  );
}
