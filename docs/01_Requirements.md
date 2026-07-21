# 1. Yêu cầu chức năng (Functional Requirements)
## FR-01. Quản lý tài khoản
- Hệ thống cho phép người dùng quản lý tài khoản cá nhân.
- Đăng ký tài khoản.
- Đăng nhập.
- Đăng xuất.
- Cập nhật thông tin cá nhân.
- Đổi mật khẩu.

## FR-02. Quản lý Workspace
Hệ thống cho phép người dùng tạo và quản lý nhiều không gian làm việc.
- Tạo Workspace.
- Đổi tên Workspace.
- Xóa Workspace.
- Chuyển đổi giữa các Workspace.

## FR-03. Quản lý Folder
Hệ thống cho phép người dùng tổ chức dữ liệu trong Workspace theo cấu trúc thư mục.
- Tạo Folder.
- Đổi tên Folder.
- Xóa Folder.
- Di chuyển Folder.
- Tạo Folder con (lồng nhau).

## FR-04. Quản lý Page
Hệ thống cho phép người dùng tạo và quản lý các Page trong Workspace. Mỗi Page là một đơn vị lưu trữ tri thức của hệ thống.
- Tạo Page.
- Đổi tên Page.
- Xóa Page.
- Di chuyển Page giữa các Folder.
Lưu ý: Chức năng chỉnh sửa nội dung sẽ thuộc FR-05.

## FR-05. Chế độ chỉnh sửa (Edit Mode)
Hệ thống cho phép chỉnh sửa nội dung của một Page.
- Mở chế độ chỉnh sửa.
- Chỉnh sửa nội dung.
- Lưu thay đổi.
- Hủy chỉnh sửa.

## FR-06. Chế độ xem (View Mode)
Hệ thống cho phép người dùng xem nội dung của Page.
- Hiển thị nội dung Page.
- Điều hướng giữa các Page thông qua liên kết.
- Chuyển sang chế độ chỉnh sửa.
Đây là chế độ mặc định của hệ thống.

## FR-07. Liên kết giữa các Page
Hệ thống cho phép tạo mối liên kết giữa các Page.
- Tạo liên kết đến Page khác.
- Điều hướng sang Page được liên kết.

## FR-08. Tìm kiếm
Hệ thống cho phép tìm kiếm Page.
- Tìm theo tiêu đề.
- Tìm theo nội dung.
- Hiển thị kết quả phù hợp.