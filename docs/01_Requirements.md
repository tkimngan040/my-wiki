**MyWiki - Version 1.0 MVP**
# 1. Yêu cầu chức năng (Functional Requirements)
## FR-01. Quản lý tài khoản
Hệ thống cho phép người dùng quản lý tài khoản cá nhân, bao gồm:
- Đăng ký tài khoản.
- Đăng nhập.
- Đăng xuất.
- Cập nhật thông tin cá nhân.
- Đổi mật khẩu.

## FR-02. Quản lý Workspace
- Mỗi Workspace là một không gian độc lập tách biệt khỏi các Workspace khác.
- Bên trong Workspace có chứa các Folder và các Page.
- Workspace có hai chế độ là công khai (Public) và riêng tư (Private), khi Workspace được đặt chế độ là công khai, tất cả mọi người đều đọc được thông tin trong Page trong Workspace đó. Ngược lại nếu Workspace được đặt là riêng tư thì chỉ người tạo ra Workspace đó mới đọc được.
- Chỉ có chủ nhân của Workspace mới chỉnh sửa được chính Workspace đó.
- Nội dung quản lý Workspace bao gồm thêm Workspace mới, đổi tên và xóa Workspace cũ. Khi xóa một Workspace thì tất cả các Folder và các Page có trong Workspace đó đều bị xóa.
- Các Workspace của cùng một người dùng không thể có tên trùng nhau.

## FR-03. Quản lý Folder
- Folder được dùng để lưu trữ và phân loại các Page.
- Một Folder có thể chứa nhiều Folder con.
- Nội dung quản lý Folder bao gồm thêm Folder mới, đổi tên hoặc xóa Folder cũ. Khi xóa một Folder thì các Folder con và các Page có trong Folder đó đều bị xóa.
- Các Folder có cùng cấp độ không thể có tên trùng nhau.
- Các Folder con có thể di chuyển vào Folder cha bất kỳ, nhưng Folder cha không thể di chuyển vào Folder con của chính nó. Các Folder trong một Workspace không thể di chuyển qua một Workspace khác.
- Khi Folder bị di chuyển thì các Folder con và các Page nằm trong Folder đó đều bị di chuyển theo.

## FR-04. Quản lý Page
- Page là nơi chứa thông tin.
- Page nằm trong Workspace hoặc nằm trong Folder thuộc Workspace.
- Các Page có thể di chuyển bất kỳ trong Workspace. Các Page trong một Workspace không thể di chuyển qua một Workspace khác.
- Nội dung chỉnh sửa Page bao gồm: thêm Page mới, sửa tên hoặc xóa Page cũ và chỉnh sửa thông tin trong Page. 
- Khi người dùng click vào một liên kết trỏ đến Page đã bị xóa, hệ thống sẽ thông báo cho người dùng rằng Page không tồn tại.
- Các Page trong cùng một Workspace không thể có tên trùng nhau (điều này để việc liên kết giữa các Page không xảy ra nhầm lẫn.)

## FR-05. Quản lý liên kết giữa các Page
- Hai Page thuộc cùng một Workspace có thể liên kết với nhau. Ngược lại, hai Page thuộc hai Workspace khác nhau không thể liên kết với nhau.
- Hai Page được liên kết với nhau thông qua tên Page, khi người xem click vào tên Page khác trong nội dung của một Page thì có thể mở xem Page khác đó.
- Khi một trong hai Page có liên kết với nhau bị đổi tên, hệ thống sẽ tự động cập nhật từ khóa link sang tên mới

## FR-06. Chế độ xem (View Mode)
- Trong chế độ xem, người chưa đăng nhập có thể xem các Page trong các Workspace được đặt ở chế độ công khai, người đã đăng nhập có thể xem các Workspace ở chế độ công khai và các Workspace của chính mình.
- Trong chế độ xem, người dùng chỉ có thể xem mà không thể chỉnh sửa. Người dùng cũng có thể click vào các từ khóa để mở các Page được liên kết với từ khóa đó để xem.

## FR-07. Chế độ chỉnh sửa (Edit Mode)
- Chỉ có người đã đăng nhập mới có thể chuyển qua chế độ chỉnh sửa.
- Trong chế độ chỉnh sửa, người dùng có thể quản lý cả Workspace, Folder và Page, tạo và xóa liên kết giữa các Page.

## FR-08. Tìm kiếm
- Người dùng có thể tìm kiếm Workspace, Folder hoặc Page bằng từ khóa. Kết quả trả về dựa vào tên của Workspace/Folder/Page.
- Hệ thống có phân loại dữ liệu thành Workspace, Folder và Page để hỗ trợ việc tìm kiếm. Sau khi người dùng nhập từ khóa để tìm kiếm, người dùng có thể bấm ô Workspace, Folder hoặc Page để xem kết quả tương ứng.
- Người chưa đăng nhập chỉ tìm thấy dữ liệu trong Workspace Public, người đã đăng nhập sẽ tìm thấy dữ liệu từ Workspace Public và các Workspace do chính họ sở hữu.

# 2. Yêu cầu phi chức năng (Non-Functional Requirements)
## NFR-01. Hiệu năng (Performance)
- Hệ thống phải phản hồi các thao tác thông thường (đăng nhập, mở Page, tạo Page, đổi tên, tìm kiếm...) trong thời gian không quá 2 giây trong điều kiện hoạt động bình thường.
- Hệ thống phải hỗ trợ tìm kiếm và hiển thị kết quả trong thời gian không quá 2 giây đối với dữ liệu của một người dùng.

## NFR-02. Bảo mật (Security)
- Mật khẩu người dùng phải được mã hóa (hash) trước khi lưu vào cơ sở dữ liệu.
- Người dùng chỉ được truy cập và chỉnh sửa các Workspace mà họ có quyền.
- Người chưa đăng nhập không được phép sử dụng các chức năng chỉnh sửa dữ liệu.
- Phiên đăng nhập phải được xác thực bằng JWT hoặc Session.

## NFR-03. Khả năng sử dụng (Usability)
- Giao diện phải thống nhất giữa các trang.
- Người dùng có thể thực hiện các thao tác chính mà không cần tài liệu hướng dẫn.

## NFR-04. Tính toàn vẹn dữ liệu (Data Integrity)
- Dữ liệu phải luôn đảm bảo tính nhất quán sau các thao tác thêm, sửa, xóa.
- Không được tồn tại Folder hoặc Page không thuộc Workspace nào.
- Các ràng buộc duy nhất (Unique) của Workspace, Folder và Page phải luôn được đảm bảo.

## NFR-05. Khả năng mở rộng (Scalability)
- Hệ thống phải được thiết kế theo kiến trúc module để có thể bổ sung các chức năng mới mà không ảnh hưởng đến các chức năng hiện có.
- Cấu trúc cơ sở dữ liệu phải hỗ trợ mở rộng cho các phiên bản tiếp theo như Tag, Version History, Graph View hoặc Workspace chia sẻ.

## NFR-06. Khả năng bảo trì (Maintainability)
- Mã nguồn phải được tổ chức theo kiến trúc rõ ràng, tách biệt giữa giao diện, xử lý nghiệp vụ và truy cập dữ liệu.
- Mã nguồn phải tuân theo quy tắc đặt tên thống nhất và dễ đọc.
- Các thành phần của hệ thống phải có khả năng tái sử dụng khi phù hợp.

## NFR-07. Khả năng tương thích (Compatibility)
- Hệ thống phải hoạt động trên các trình duyệt phổ biến như Google Chrome, Microsoft Edge và Mozilla Firefox.
- Giao diện phải hiển thị chính xác trên màn hình máy tính để bàn.

## NFR-08. Khả năng tin cậy (Reliability)
- Hệ thống phải đảm bảo dữ liệu đã lưu không bị mất sau khi người dùng hoàn thành thao tác lưu.
- Khi xảy ra lỗi, hệ thống phải hiển thị thông báo phù hợp và không làm hỏng dữ liệu hiện có.