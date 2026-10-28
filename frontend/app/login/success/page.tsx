import Link from "next/link";
import "@/features/auth/auth.css";

export default function LoginSuccessPage() {
  return (
    <main className="form-panel auth-success">
      <div className="form-wrap">
        <Link className="brand" href="/" aria-label="MyWiki trang chủ">
          <span className="brand-mark" aria-hidden="true">
            M
          </span>
          <span>Mywiki</span>
        </Link>

        <div className="notice success-notice" role="status">
          <span className="notice-icon" aria-hidden="true">
            ✓
          </span>
          <div>
            <strong>Đã đăng nhập thành công!</strong>
            <p>Chào mừng bạn quay trở lại MyWiki.</p>
          </div>
        </div>

        <Link className="submit-button" href="/register">
          Về trang đăng ký <span aria-hidden="true">→</span>
        </Link>
      </div>
    </main>
  );
}
