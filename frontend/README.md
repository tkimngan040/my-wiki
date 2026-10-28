# MyWiki frontend

Frontend Next.js dùng App Router và TypeScript.

## Chạy local

1. Đảm bảo backend Spring Boot và SQL Server đang chạy ở `http://localhost:8080`.
2. Tại thư mục này, cài dependency và chạy dev server:

   ```bash
   npm install
   npm run dev
   ```

3. Mở `http://localhost:3000` để xem trang chủ. Trước và sau khi đăng nhập, trang chủ vẫn giữ cùng giao diện khám phá; người đã đăng nhập có thêm điều hướng tài khoản và vào `/my-workspaces` khi chọn **Workspace của tôi**.

Trang đăng ký và đăng nhập gửi request qua các route phía server `POST /api/auth/register` và `POST /api/auth/login` của Next.js. Các route này chuyển tiếp tới backend; đăng nhập thành công sẽ quay về trang chủ và hiển thị thêm tiện ích tài khoản. Chọn **Workspace của tôi** để mở giao diện làm việc cá nhân.

Có thể tạo file `.env.local` để đổi địa chỉ backend:

```env
BACKEND_API_URL=http://localhost:8080
```

Backend hiện yêu cầu username, email hợp lệ và mật khẩu tối thiểu 6 ký tự. Đăng ký thành công chưa tự đăng nhập.

## Cấu trúc chính

```text
app/
  api/auth/login/route.ts     # Chuyển tiếp đăng nhập và session cookie
  api/auth/logout/route.ts    # Đăng xuất qua backend và CSRF token
  api/auth/register/route.ts  # API route chuyển tiếp tới backend
  api/account/profile/route.ts # Proxy hồ sơ người dùng đã đăng nhập
  api/account/session/route.ts # Kiểm tra session đăng nhập hiện tại
  api/account/workspaces/route.ts # Proxy Workspace theo session hiện tại
  api/public-content/route.ts # Đọc dữ liệu công khai, không chuyển tiếp session cookie
  dashboard/page.tsx          # Chuyển tiếp tương thích đến /my-workspaces
  my-workspaces/page.tsx      # Giao diện làm việc với Workspace cá nhân
  login/page.tsx              # Route giao diện /login
  login/success/page.tsx      # Màn hình đăng nhập thành công
  register/page.tsx           # Route giao diện /register
  page.tsx                    # Trang chủ dùng chung trước/sau đăng nhập
features/
  public-home/
    PublicHome.tsx            # Layout 3 cột, tìm kiếm và nội dung Workspace Public
    public-home.css           # Giao diện responsive và chế độ sáng/tối
  account-dashboard/
    AccountDashboard.tsx      # Dashboard tài khoản, danh sách Workspace và hồ sơ
    account-dashboard.css     # Giao diện responsive cho dashboard
  auth/
    auth.css                  # Giao diện dùng chung cho auth
    login/
      LoginForm.tsx           # Form và xử lý đăng nhập
      validation.ts           # Kiểm tra dữ liệu đăng nhập
    register/
      RegisterForm.tsx        # Form và xử lý đăng ký
      validation.ts           # Kiểm tra dữ liệu form
```
