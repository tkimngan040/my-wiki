# MyWiki frontend

Frontend Next.js dùng App Router và TypeScript.

## Chạy local

1. Đảm bảo backend Spring Boot và SQL Server đang chạy ở `http://localhost:8080`.
2. Tại thư mục này, cài dependency và chạy dev server:

   ```bash
   npm install
   npm run dev
   ```

3. Mở `http://localhost:3000`.

Trang đăng ký gửi request qua route phía server `POST /api/auth/register` của Next.js, route này chuyển tiếp đến backend. Cách này giúp trình duyệt không cần gọi khác origin và không cần cấu hình CORS cho flow đăng ký.

Có thể tạo file `.env.local` để đổi địa chỉ backend:

```env
BACKEND_API_URL=http://localhost:8080
```

Backend hiện yêu cầu username, email hợp lệ và mật khẩu tối thiểu 6 ký tự. Đăng ký thành công chưa tự đăng nhập; flow đăng nhập sẽ được làm riêng.
