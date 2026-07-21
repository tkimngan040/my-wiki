# 1. Yêu cầu chức năng (Functional Requirements)
## FR-01. Quản lý tài khoản
Hệ thống cho phép người dùng quản lý tài khoản cá nhân.
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
- Đây là chế độ mặc định của hệ thống.

## FR-07. Liên kết giữa các Page
Hệ thống cho phép tạo mối liên kết giữa các Page.
- Tạo liên kết đến Page khác.
- Điều hướng sang Page được liên kết.

## FR-08. Tìm kiếm
Hệ thống cho phép tìm kiếm Page.
- Tìm theo tiêu đề.
- Tìm theo nội dung.
- Hiển thị kết quả phù hợp.

# 2. Yêu cầu phi chức năng (Non-functional Requirements)
## NFR-01. Hiệu năng (Performance)
Hệ thống cần đảm bảo khả năng phản hồi nhanh đối với các thao tác thông thường.
- Thời gian phản hồi cho các thao tác cơ bản không vượt quá 2 giây trong điều kiện sử dụng thông thường.
- Việc mở Page và chuyển đổi giữa các Page diễn ra mượt mà.
- Kết quả tìm kiếm được trả về trong thời gian không vượt quá 2 giây đối với dữ liệu thông thường.

## NFR-02. Bảo mật (Security)
Hệ thống cần đảm bảo an toàn cho tài khoản và dữ liệu của người dùng.
- Mật khẩu người dùng được lưu dưới dạng mã hóa.
- Chỉ người đã đăng nhập mới được sử dụng hệ thống.
- Hệ thống bảo vệ dữ liệu khỏi các truy cập không hợp lệ.

## NFR-03. Khả năng sử dụng (Usability)
Hệ thống cần có giao diện trực quan và dễ sử dụng.
- Chế độ xem (View Mode) là chế độ mặc định.
- Người dùng có thể chuyển sang chế độ chỉnh sửa (Edit Mode) khi cần.
- Các thao tác tạo, sửa, xóa được thực hiện với số bước tối thiểu.
- Giao diện thống nhất giữa các Workspace.

## NFR-04. Khả năng mở rộng (Scalability)
Hệ thống được thiết kế để dễ dàng bổ sung chức năng trong tương lai.
- Có thể bổ sung các module mới mà không ảnh hưởng đến các chức năng hiện có.
- Có thể mở rộng số lượng Workspace, Folder và Page.
- Kiến trúc hệ thống được thiết kế theo hướng dễ mở rộng và bổ sung chức năng trong các phiên bản tiếp theo.

## NFR-05. Khả năng bảo trì (Maintainability)
Hệ thống cần được xây dựng theo hướng dễ bảo trì và phát triển.
- Mã nguồn được tổ chức theo kiến trúc thống nhất.
- Các thành phần được phân tách rõ ràng theo chức năng.
- Hệ thống dễ dàng bảo trì và mở rộng.

# 3. Quy tắc hệ thống (System Rules)
Các quy tắc dưới đây là nền tảng trong thiết kế và vận hành của MyWiki. Mọi chức năng của hệ thống cần tuân thủ các quy tắc này.

## SR-01. Workspace là không gian làm việc độc lập
- Mỗi Workspace là một không gian làm việc riêng biệt.
- Dữ liệu giữa các Workspace được tách biệt hoàn toàn.
- Người dùng có thể tạo nhiều Workspace để quản lý các lĩnh vực khác nhau.

## SR-02. Cấu trúc tổ chức dữ liệu
Dữ liệu trong hệ thống được tổ chức theo cấu trúc phân cấp gồm Workspace, Folder và Page.
- Workspace là cấp cao nhất và là không gian làm việc độc lập.
- Workspace có thể chứa trực tiếp các Page hoặc các Folder.
- Folder có thể chứa các Page, Folder con hoặc cả hai.
- Page là đơn vị lưu trữ tri thức của hệ thống.

## SR-03. Mỗi Page chỉ thuộc một Workspace
- Một Page chỉ được phép thuộc về duy nhất một Workspace.
- Một Page có thể được di chuyển giữa các Folder trong cùng Workspace.
- Không cho phép di chuyển Page sang Workspace khác.

## SR-04. Mỗi Folder chỉ thuộc một Workspace
- Một Folder chỉ thuộc duy nhất một Workspace.
- Folder có thể chứa nhiều Folder con.
- Folder con luôn thuộc cùng Workspace với Folder cha.

## SR-05. Liên kết giữa các Page
- Một Page có thể liên kết đến nhiều Page khác.
- Một Page có thể được nhiều Page khác liên kết tới.
- Chỉ cho phép tạo liên kết giữa các Page trong cùng một Workspace.

## SR-06. Chế độ xem và chỉnh sửa
- View Mode là chế độ mặc định khi mở một Page.
- Người dùng chỉ chỉnh sửa nội dung khi chuyển sang Edit Mode.
- Sau khi lưu hoặc hủy chỉnh sửa, hệ thống quay lại View Mode.

## SR-07. Tổ chức dữ liệu
- Folder không lưu trữ nội dung tri thức.
- Nội dung tri thức chỉ được lưu trong Page.
- Việc thay đổi vị trí của Page hoặc Folder không làm thay đổi nội dung của Page.

## SR-08. Tên Page
- Trong cùng một Workspace, tên của các Page phải là duy nhất.

## SR-09. Tên Folder
- Trong cùng một Folder cha, tên của các Folder con phải là duy nhất.
