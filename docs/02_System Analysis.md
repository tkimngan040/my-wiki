# 1. List of Actor
- Guest: Người chưa đăng nhập. Chỉ được xem và tìm kiếm các Workspace ở chế độ Public.
- User: Người đã đăng nhập. Có thể quản lý tài khoản, Workspace, Folder, Page của mình; xem Workspace Public và Workspace của chính mình; tìm kiếm dữ liệu theo quyền truy cập.

# 2. List of Use Case
## UC-01. Đăng ký tài khoản
**Actors:** Guest  
**Summary:** Cho phép khách tạo một tài khoản mới để sử dụng hệ thống.  
**Pre-condition:**
- Người dùng chưa đăng nhập.
- Người dùng chưa có tài khoản với email đã đăng ký.  
**Main Flow:**
1. Người dùng chọn chức năng Đăng ký.
2. Hệ thống hiển thị biểu mẫu đăng ký.
3. Người dùng nhập thông tin tài khoản.
4. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
5. Hệ thống lưu tài khoản mới.
6. Hệ thống thông báo đăng ký thành công.  
**Alternative Flow:**
- Địa chỉ email đã được sử dụng để đăng ký tài khoản khác.
    1. Hệ thống hiển thị thông báo lỗi "Email đã được sử dụng".
    2. Hệ thống giữ lại các thông tin đã nhập (trừ mật khẩu và email) và yêu cầu người dùng nhập lại email khác.
- Người dùng nhập thiếu hoặc sai định dạng thông tin.
    1. Hệ thống đánh dấu đỏ các trường dữ liệu không hợp lệ kèm thông báo chi tiết.
    2. Cho phép người dùng chỉnh sửa.
- Mật khẩu không đáp ứng yêu cầu của hệ thống.
    1. Hệ thống hiển thị thông báo về yêu cầu của mật khẩu.
    2. Người dùng nhập lại mật khẩu mới.
- Người dùng hủy thao tác đăng ký.
    1. Hệ thống hủy quá trình đăng ký.
    2. Không tạo tài khoản mới.  
**Post-condition:**
- Một tài khoản mới được tạo trong hệ thống.

## UC-02. Đăng nhập
**Actor:** Guest  

UC-03. Đăng xuất
UC-04. Cập nhật thông tin cá nhân
UC-05. Đổi mật khẩu
UC-06. Tạo Workspace
UC-07. Đổi tên Workspace
UC-08. Xóa Workspace
UC-09. Thiết lập chế độ Public/Private cho Workspace
UC-10. Tạo Folder
UC-11. Đổi tên Folder
UC-12. Di chuyển Folder
UC-13. Xóa Folder
UC-14. Tạo Page
UC-15. Đổi tên Page
UC-16. Chỉnh sửa nội dung Page
UC-17. Di chuyển Page
UC-18. Xóa Page
UC-19. Tạo liên kết giữa các Page
UC-20. Xóa liên kết giữa các Page
UC-21. Xem Workspace/Page
UC-22. Chuyển sang chế độ chỉnh sửa (Edit Mode)
UC-23. Tìm kiếm Workspace
UC-24. Tìm kiếm Folder
UC-25. Tìm kiếm Page