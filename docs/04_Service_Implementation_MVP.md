# Service Implementation Plan
## UserService
```text 
register()
    ├── nhận thông tin đăng ký
    ├── kiểm tra email đã tồn tại chưa
    │      └── có → từ chối
    ├── kiểm tra các điều kiện nghiệp vụ
    ├── xử lý password
    ├── tạo User
    └── lưu User


login()
    ├── nhận email/ID + password
    ├── tìm User
    │      ├── bằng email
    │      └── hoặc bằng ID
    ├── xác thực password
    ├── tạo Session/JWT
    └── trả kết quả đăng nhập

logout()
    ├── xác nhận đăng xuất (nếu có)
    ├── hủy Session/JWT
    └── chuyển về trang đăng nhập/trang chủ
    
updateAccount()
    ├── lấy thông tin tài khoản hiện tại
    ├── người dùng chỉnh sửa thông tin
    ├── kiểm tra dữ liệu
    ├── cập nhật User
    └── lưu User

changePassword()
    ├── nhận mật khẩu hiện tại
    ├── nhận mật khẩu mới
    ├── nhận xác nhận mật khẩu mới
    ├── kiểm tra dữ liệu
    ├── xác thực mật khẩu hiện tại
    ├── kiểm tra password mới
    ├── cập nhật password
    └── lưu User

```

## WorkspaceService

## FolderService

## PageService

## LinkService