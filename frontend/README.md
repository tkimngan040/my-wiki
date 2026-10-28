# MyWiki frontend

Frontend Next.js dùng App Router và TypeScript.

## Chạy local

1. Đảm bảo backend Spring Boot và SQL Server đang chạy ở `http://localhost:8080`.
2. Tại thư mục này, cài dependency và chạy dev server:

   ```bash
   npm install
   npm run dev
   ```

3. Mở `http://localhost:3000/register` để đăng ký hoặc `http://localhost:3000/login` để đăng nhập (truy cập `/` sẽ chuyển đến trang đăng ký).

Trang đăng ký và đăng nhập gửi request qua các route phía server `POST /api/auth/register` và `POST /api/auth/login` của Next.js. Các route này chuyển tiếp tới backend; route đăng nhập cũng chuyển session cookie backend về trình duyệt.

Có thể tạo file `.env.local` để đổi địa chỉ backend:

```env
BACKEND_API_URL=http://localhost:8080
```

Backend hiện yêu cầu username, email hợp lệ và mật khẩu tối thiểu 6 ký tự. Đăng ký thành công chưa tự đăng nhập; flow đăng nhập sẽ được làm riêng.

## Cấu trúc chính

```text
app/
  api/auth/login/route.ts     # Chuyển tiếp đăng nhập và session cookie
  api/auth/register/route.ts  # API route chuyển tiếp tới backend
  login/page.tsx              # Route giao diện /login
  login/success/page.tsx      # Màn hình đăng nhập thành công
  register/page.tsx           # Route giao diện /register
  page.tsx                    # Chuyển / về /register
features/
  auth/
    auth.css                  # Giao diện dùng chung cho auth
    login/
      LoginForm.tsx           # Form và xử lý đăng nhập
      validation.ts           # Kiểm tra dữ liệu đăng nhập
    register/
      RegisterForm.tsx        # Form và xử lý đăng ký
      validation.ts           # Kiểm tra dữ liệu form
```
