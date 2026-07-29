# Cấu trúc dự án
- Dự án lựa chọn mô hình Kiến trúc mã nguồn là: Modular Monolith

## Cấu trúc thư mục

```text
my-wiki/
│
├── docs/
│   ├── 00_Overview.md
│   ├── 01_Requirements.md
│   └── 02_System Analysis.md
│   └── 03_Project Structure.md
│
├── src/
|   ├── Auth
|   ├── User
|   ├── Workspace
|   ├── Folder
|   ├── Page
|   ├── Link
|   ├── Search
|   ├── Permission (Access Control)
|   └── Shared (Common)
│
├── .gitignore
├── LICENSE
└── README.md
```

### 1. Auth Module
Chịu trách nhiệm xác thực người dùng và quản lý trạng thái đăng nhập của hệ thống. Module này đảm bảo chỉ người dùng hợp lệ mới có thể truy cập các chức năng yêu cầu xác thực

Nghiệp vụ:
- Đăng nhập
- Đăng xuất
- Kiểm tra email + mật khẩu
- Hash / Verify Password
- Quản lý Session
- Kiểm tra trạng thái đăng nhập

### 2. User Module
Quản lý thông tin tài khoản người dùng, bao gồm hồ sơ cá nhân và các thông tin liên quan đến tài khoản. Đây là nơi lưu trữ và xử lý dữ liệu của người dùng trong hệ thống.

Nghiệp vụ:
- Đăng ký tài khoản
- Cập nhật thông tin
- Đổi mật khẩu
- Xem thông tin tài khoản

### 3. Workspace Module
Quản lý các Workspace, là không gian làm việc chính của người dùng. Module này chịu trách nhiệm tổ chức, quản lý và điều phối các thành phần bên trong Workspace.

Nghiệp vụ:
- Tạo Workspace
- Đổi tên
- Xóa Workspace
- Quyền truy cập (Public/Private)
- Danh sách Workspace

### 4. Folder Module
Quản lý cấu trúc thư mục trong từng Workspace. Module này hỗ trợ tổ chức nội dung theo dạng phân cấp, giúp người dùng sắp xếp tài liệu một cách khoa học.

Nghiệp vụ:
- Tạo Folder
- Đổi tên
- Di chuyển Folder
- Xóa Folder 

### 5. Page Module 
Quản lý các trang nội dung (Page), nơi lưu trữ và chỉnh sửa thông tin. Đây là module trung tâm của hệ thống, chịu trách nhiệm xử lý nội dung tài liệu.

Nghiệp vụ:
- Tạo Page
- Đổi tên
- Chỉnh sửa nội dung
- Lưu nội dung
- Xóa Page

### 6. Link Module
Quản lý các liên kết giữa các Page trong cùng một Workspace, giúp người dùng kết nối và điều hướng giữa các nội dung liên quan.

Nghiệp vụ:
- Tạo Link
- Xóa Link
- Kiểm tra Link

### 7. Search Module
Cung cấp chức năng tìm kiếm các đối tượng trong hệ thống như Workspace, Folder và Page, giúp người dùng truy xuất thông tin nhanh chóng.

Nghiệp vụ:
- Tìm kiếm Workspace
- Tìm kiếm Folder
- Tìm kiếm Page

### 8. Permission Module
Quản lý quyền truy cập và quyền thao tác của người dùng đối với các tài nguyên trong hệ thống, đảm bảo các chức năng chỉ được thực hiện bởi những người dùng có quyền phù hợp.

Nghiệp vụ:
- Cấp phép View
- Cấp phép Edit
- Cấp phép xóa

### 9. Shared Module
Chứa các thành phần dùng chung cho toàn bộ hệ thống, bao gồm các hằng số, tiện ích (utilities), bộ kiểm tra dữ liệu (validators), ngoại lệ (exceptions) và các thành phần hỗ trợ khác nhằm giảm trùng lặp mã nguồn và tăng khả năng tái sử dụng.

## Thứ tự triển khai Module
Shared
    ↓
User
    ↓
Auth
    ↓
Workspace
    ↓
Folder
    ↓
Page
    ↓
Link
    ↓
Permission
    ↓
Search