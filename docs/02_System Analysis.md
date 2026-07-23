# 1. List of Actor
- Guest: Người chưa đăng nhập. Chỉ được xem và tìm kiếm các Workspace ở chế độ Public.
- User: Người đã đăng nhập. Có thể quản lý tài khoản, Workspace, Folder, Page của mình; xem Workspace Public và Workspace của chính mình; tìm kiếm dữ liệu theo quyền truy cập.

# 2. List of Use Case
## UC-01. Đăng ký tài khoản
**Actors:** Guest  

**Summary:** Cho phép khách tạo một tài khoản mới để sử dụng hệ thống.  

**Pre-condition:**
- Người dùng chưa đăng nhập.

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
**Actors:** Guest

**Summary:** Cho phép khách đăng nhập vào hệ thống bằng tài khoản đã đăng ký.

**Pre-condition:**
- Người dùng chưa đăng nhập.
- Người dùng đã có tài khoản trong hệ thống.

**Main Flow:**
1. Người dùng chọn chức năng Đăng nhập.
2. Hệ thống hiển thị biểu mẫu đăng nhập.
3. Người dùng nhập email hoặc ID và mật khẩu.
4. Hệ thống kiểm tra thông tin đăng nhập.
5. Hệ thống xác thực tài khoản.
6. Hệ thống tạo phiên đăng nhập (Session hoặc JWT).
7. Hệ thống chuyển người dùng đến trang chính.

**Alternative Flow:**
- Người dùng nhập sai email hoặc mật khẩu.
    1. Hệ thống hiển thị thông báo lỗi "Email hoặc mật khẩu không chính xác".
    2. Hệ thống yêu cầu người dùng nhập lại thông tin đăng nhập.
- Người dùng để trống email hoặc mật khẩu.
    1. Hệ thống đánh dấu các trường dữ liệu còn thiếu và hiển thị thông báo tương ứng.
    2. Cho phép người dùng bổ sung thông tin.
- Người dùng hủy thao tác đăng nhập.
    1. Hệ thống hủy quá trình đăng nhập.
    2. Người dùng vẫn ở trạng thái chưa đăng nhập.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình xác thực hoặc tạo phiên đăng nhập.
    1. Hệ thống hiển thị thông báo đăng nhập thất bại.
    2. Không tạo phiên đăng nhập.

**Post-condition:**
- Người dùng đã đăng nhập thành công.
- Phiên đăng nhập được tạo.
- Người dùng có thể sử dụng các chức năng dành cho tài khoản đã đăng nhập.


## UC-03. Đăng xuất
**Actors:** User

**Summary:** Cho phép người dùng kết thúc phiên đăng nhập và đăng xuất khỏi hệ thống.

**Pre-condition:**
- Người dùng đã đăng nhập.

**Main Flow:**
1. Người dùng chọn chức năng Đăng xuất.
2. Hệ thống yêu cầu xác nhận đăng xuất (nếu có).
3. Người dùng xác nhận đăng xuất.
4. Hệ thống hủy phiên đăng nhập (Session hoặc JWT).
5. Hệ thống chuyển người dùng về trang đăng nhập hoặc trang chủ.
6. Hệ thống hiển thị thông báo đăng xuất thành công.

**Alternative Flow:**
- Người dùng hủy thao tác đăng xuất.
    1. Hệ thống hủy quá trình đăng xuất.
    2. Người dùng tiếp tục sử dụng hệ thống với phiên đăng nhập hiện tại.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình hủy phiên đăng nhập.
    1. Hệ thống hiển thị thông báo đăng xuất thất bại.
    2. Phiên đăng nhập hiện tại vẫn được duy trì.

**Post-condition:**
- Phiên đăng nhập của người dùng đã bị hủy.
- Người dùng ở trạng thái chưa đăng nhập.
- Người dùng không còn quyền truy cập các chức năng yêu cầu đăng nhập cho đến khi đăng nhập lại.


## UC-04. Cập nhật thông tin cá nhân
**Actors:** User

**Summary:** Cho phép người dùng cập nhật thông tin cá nhân của tài khoản.

**Pre-condition:**
- Người dùng đã đăng nhập.

**Main Flow:**
1. Người dùng chọn chức năng Cập nhật thông tin cá nhân.
2. Hệ thống hiển thị thông tin hiện tại của tài khoản.
3. Người dùng chỉnh sửa các thông tin cần cập nhật.
4. Người dùng chọn chức năng Lưu.
5. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
6. Hệ thống cập nhật thông tin tài khoản.
7. Hệ thống hiển thị thông báo cập nhật thành công.

**Alternative Flow:**
- Người dùng nhập thiếu hoặc sai định dạng thông tin.
    1. Hệ thống đánh dấu các trường dữ liệu không hợp lệ và hiển thị thông báo tương ứng.
    2. Cho phép người dùng chỉnh sửa và lưu lại.
- Thông tin cập nhật không có thay đổi.
    1. Hệ thống thông báo "Không có thay đổi để cập nhật".
    2. Không thực hiện cập nhật dữ liệu.
- Người dùng hủy thao tác cập nhật.
    1. Hệ thống hủy quá trình cập nhật.
    2. Thông tin tài khoản được giữ nguyên.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình cập nhật dữ liệu.
    1. Hệ thống hiển thị thông báo cập nhật thất bại.
    2. Thông tin tài khoản không bị thay đổi.

**Post-condition:**
- Thông tin tài khoản được cập nhật thành công.
- Người dùng tiếp tục sử dụng hệ thống với thông tin mới.


## UC-05. Đổi mật khẩu
**Actors:** User

**Summary:** Cho phép người dùng thay đổi mật khẩu của tài khoản sau khi đã đăng nhập.

**Pre-condition:**
- Người dùng đã đăng nhập.

**Main Flow:**
1. Người dùng chọn chức năng Đổi mật khẩu.
2. Hệ thống hiển thị biểu mẫu đổi mật khẩu.
3. Người dùng nhập mật khẩu hiện tại, mật khẩu mới và xác nhận mật khẩu mới.
4. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
5. Hệ thống xác thực mật khẩu hiện tại.
6. Hệ thống cập nhật mật khẩu mới.
7. Hệ thống hiển thị thông báo đổi mật khẩu thành công.

**Alternative Flow:**
- Người dùng nhập sai mật khẩu hiện tại.
    1. Hệ thống hiển thị thông báo "Mật khẩu hiện tại không chính xác".
    2. Yêu cầu người dùng nhập lại mật khẩu hiện tại.
- Mật khẩu mới và xác nhận mật khẩu không trùng khớp.
    1. Hệ thống hiển thị thông báo "Mật khẩu xác nhận không khớp".
    2. Cho phép người dùng nhập lại.
- Mật khẩu mới không đáp ứng yêu cầu của hệ thống.
    1. Hệ thống hiển thị thông báo về yêu cầu của mật khẩu.
    2. Người dùng nhập lại mật khẩu mới.
- Mật khẩu mới trùng với mật khẩu hiện tại.
    1. Hệ thống thông báo "Mật khẩu mới không được trùng với mật khẩu hiện tại."
    2. Người dùng nhập lại mật khẩu mới.
- Người dùng hủy thao tác đổi mật khẩu.
    1. Hệ thống hủy quá trình đổi mật khẩu.
    2. Mật khẩu của tài khoản được giữ nguyên.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình cập nhật mật khẩu.
    1. Hệ thống hiển thị thông báo đổi mật khẩu thất bại.
    2. Mật khẩu cũ vẫn được giữ nguyên.

**Post-condition:**
- Mật khẩu mới được cập nhật thành công trong hệ thống.
- Người dùng có thể sử dụng mật khẩu mới cho các lần đăng nhập tiếp theo.


## UC-06. Tạo Workspace
**Actors:** User

**Summary:** Cho phép người dùng tạo một Workspace mới để lưu trữ và quản lý các Folder và Page.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn chức năng Tạo Workspace.
2. Hệ thống hiển thị biểu mẫu tạo Workspace.
3. Người dùng nhập tên Workspace và chọn chế độ hiển thị (Public hoặc Private).
4. Người dùng xác nhận Tạo Workspace.
5. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
6. Hệ thống tạo Workspace mới.
7. Hệ thống hiển thị Workspace vừa tạo.

**Alternative Flow:**
- Tên Workspace đã tồn tại trong danh sách Workspace của người dùng.
    1. Hệ thống hiển thị thông báo "Tên Workspace đã tồn tại".
    2. Yêu cầu người dùng nhập tên khác.
- Người dùng để trống hoặc nhập tên Workspace không hợp lệ.
    1. Hệ thống hiển thị thông báo lỗi tương ứng.
    2. Cho phép người dùng chỉnh sửa và tạo lại.
- Người dùng hủy thao tác tạo Workspace.
    1. Hệ thống hủy quá trình tạo Workspace.
    2. Không tạo Workspace mới.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình tạo Workspace.
    1. Hệ thống hiển thị thông báo tạo Workspace thất bại.
    2. Không tạo Workspace mới.

**Post-condition:**
- Một Workspace mới được tạo và lưu trong hệ thống.
- Workspace thuộc quyền sở hữu của người dùng.
- Workspace chưa chứa Folder hoặc Page.


## UC-07. Đổi tên Workspace
**Actors:** User

**Summary:** Cho phép người dùng đổi tên Workspace do mình sở hữu.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace cần đổi tên thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn một Workspace.
2. Người dùng chọn chức năng Đổi tên Workspace.
3. Hệ thống hiển thị tên Workspace hiện tại.
4. Người dùng nhập tên mới cho Workspace.
5. Người dùng chọn chức năng Lưu.
6. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
7. Hệ thống cập nhật tên Workspace.
8. Hệ thống hiển thị thông báo đổi tên thành công.

**Alternative Flow:**
- Tên Workspace mới đã tồn tại trong danh sách Workspace của người dùng.
    1. Hệ thống hiển thị thông báo "Tên Workspace đã tồn tại".
    2. Yêu cầu người dùng nhập tên khác.
- Người dùng để trống hoặc nhập tên Workspace không hợp lệ.
    1. Hệ thống hiển thị thông báo lỗi tương ứng.
    2. Cho phép người dùng chỉnh sửa và lưu lại.
- Người dùng hủy thao tác đổi tên.
    1. Hệ thống hủy quá trình đổi tên.
    2. Workspace giữ nguyên tên cũ.
- Tên Workspace mới giống với tên hiện tại.
    1. Không thực hiện cập nhật.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình cập nhật tên Workspace.
    1. Hệ thống hiển thị thông báo đổi tên Workspace thất bại.
    2. Workspace vẫn giữ nguyên tên cũ.

**Post-condition:**
- Tên Workspace được cập nhật thành công.
- Các Folder và Page trong Workspace không bị thay đổi.

## UC-08. Xóa Workspace
**Actors:** User

**Summary:** Cho phép người dùng xóa một Workspace do mình sở hữu.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace cần xóa thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn một Workspace.
2. Người dùng chọn chức năng Xóa Workspace.
3. Hệ thống hiển thị hộp thoại xác nhận xóa.
4. Người dùng xác nhận thao tác xóa.
5. Hệ thống xóa Workspace cùng toàn bộ Folder và Page thuộc Workspace đó.
6. Hệ thống hiển thị thông báo xóa Workspace thành công.

**Alternative Flow:**
- Người dùng hủy thao tác xóa.
    1. Hệ thống hủy quá trình xóa.
    2. Workspace và toàn bộ dữ liệu bên trong được giữ nguyên.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình xóa Workspace.
    1. Hệ thống hiển thị thông báo xóa Workspace thất bại.
    2. Workspace và toàn bộ dữ liệu bên trong vẫn được giữ nguyên.

**Post-condition:**
- Workspace bị xóa khỏi hệ thống.
- Tất cả Folder và Page thuộc Workspace đó cũng bị xóa.

## UC-09. Thiết lập chế độ Public/Private cho Workspace
**Actors:** User

**Summary:** Cho phép người dùng thay đổi chế độ hiển thị của Workspace giữa Public và Private.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace cần thay đổi thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn một Workspace.
2. Người dùng chọn chức năng Thiết lập chế độ hiển thị.
3. Hệ thống hiển thị chế độ hiện tại của Workspace.
4. Người dùng chọn chế độ mới (Public hoặc Private).
5. Người dùng chọn chức năng Lưu.
6. Hệ thống cập nhật chế độ hiển thị của Workspace.
7. Hệ thống hiển thị thông báo cập nhật thành công.

**Alternative Flow:**
- Người dùng chọn chế độ hiển thị giống với chế độ hiện tại.
    1. Hệ thống không thực hiện cập nhật.
- Người dùng hủy thao tác thay đổi chế độ hiển thị.
    1. Hệ thống hủy quá trình cập nhật.
    2. Workspace giữ nguyên chế độ hiển thị ban đầu.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình cập nhật chế độ hiển thị.
    1. Hệ thống hiển thị thông báo cập nhật thất bại.
    2. Workspace giữ nguyên chế độ hiển thị trước đó.

**Post-condition:**
- Chế độ hiển thị của Workspace được cập nhật thành công.
- Quyền truy cập vào Workspace được áp dụng theo chế độ mới:
    - Public: mọi người đều có thể xem nội dung Workspace.
    - Private: chỉ chủ sở hữu Workspace mới có thể xem nội dung Workspace.

## UC-10. Tạo Folder
**Actors:** User

**Summary:** Cho phép người dùng tạo Folder mới trong Workspace hoặc trong một Folder khác để tổ chức và lưu trữ Page.

**Pre_condition:**
- Người dùng đã đăng nhập.
- Workspace cần tạo Folder thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn Workspace hoặc Folder cha.
2. Người dùng chọn chức năng tạo Folder.
3. Hệ thống hiển thị biểu mẫu tạo Folder.
4. Người dùng nhập tên Folder.
5. Người dùng xác nhận tạo Folder.
6. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
7. Hệ thống tạo Folder mới.
8. Hệ thống hiển thị Folder vừa tạo.

**Alternative Flow:**
- Tên Folder đã tồn tại trong cùng cấp với Folder vừa tạo.
    1. Hệ thống hiển thị "Tên Folder đã tồn tại."
    2. Yêu cầu người dùng nhập tên khác.
- Người dùng để trống hoặc nhập tên Folder không hợp lệ.
    1. Hệ thống hiển thị thông báo lỗi tương ứng.
    2. Cho phép người dùng chỉnh sửa và tạo lại.
- Người dùng hủy thao tác tạo Folder
    1. Hệt thống hủy quá trình tạo Folder.
    2. Không tạo Folder mới.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình tạo Folder.
    1. Hệ thống hiển thị thông báo tạo Folder thất bại.
    2. Không tạo Folder mới.

**Post-condition:**
- Một Folder mới được tạo trong Workspace hoặc Folder cha đã chọn.
- Folder mới chưa chứa Folder con hoặc Page.


## UC-11. Đổi tên Folder
**Actors:** User

**Summary:** Cho phép người dùng đổi tên một Folder do mình sở hữu.

**Pre-condition:** 
- Người dùng đã đăng nhập.
- Workspace chứa Folder thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn một Folder.
2. Người dùng chọn chức năng Đổi tên Folder.
3. Hệ thống hiển thị tên Folder hiện tại.
4. Người dùng nhập tên mới cho Folder.
5. Người dùng chọn chức năng Lưu.
6. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
7. Hệ thống cập nhật tên Folder.
8. Hệ thống hiển thị thông báo đổi tên thành công.

**Alternative Flow:**
- Tên Folder mới đã tồn tại trong cùng một cấp với Folder hiện tại.
    1. Hệ thống hiển thị thông báo "Tên Folder đã tồn tại."
    2. Yêu cầu người dùng nhập tên khác.
- Người dùng để trống tên hoặc tên Folder không hợp lệ.
    1. Hệ thống hiển thị thông báo lỗi tương ứng.
    2. Cho phép người dùng chỉnh sửa và lưu lại.
- Tên Folder mới giống tên hiện tại.
    1. Không thực hiện cập nhật.
- Người dùng hủy thao tác đổi tên.
    1. Hệ thống hủy quá trình đổi tên.
    2. Folder giữ nguyên tên cũ.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình cập nhật tên Folder.
    1. Hệ thống hiển thị thông báo đổi tên Folder thất bại.
    2. Folder vẫn giữ nguyên tên cũ.

**Post-condition:**
- Tên Folder được cập nhật thành công.
- Cấu trúc Folder và các Page bên trong không bị thay đổi.


## UC-12. Di chuyển Folder
**Actors:** User

**Summary:** Cho phép người dùng di chuyển một Folder đến vị trí khác trong cùng Workspace.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace chứa Folder thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn một Folder.
2. Người dùng chọn chức năng Di chuyển Folder.
3. Hệ thống hiển thị danh sách các vị trí có thể di chuyển đến trong Workspace.
4. Người dùng chọn Folder cha mới hoặc chọn Workspace làm vị trí đích.
5. Người dùng xác nhận thao tác di chuyển.
6. Hệ thống kiểm tra tính hợp lệ của vị trí đích.
7. Hệ thống di chuyển Folder cùng toàn bộ Folder con và Page bên trong đến vị trí mới.
8. Hệ thống hiển thị thông báo di chuyển thành công.

**Alternative Flow:**
- Tên Folder đã tồn tại tại vị trí đích.
    1. Hệ thống hiển thị thông báo "Tên Folder đã tồn tại".
    2. Yêu cầu người dùng chọn vị trí khác hoặc đổi tên Folder.
- Người dùng chọn chính Folder đó hoặc một Folder con của nó làm vị trí đích.
    1. Hệ thống hiển thị thông báo "Không thể di chuyển Folder vào chính nó hoặc Folder con của nó".
    2. Yêu cầu người dùng chọn vị trí khác.
- Người dùng chọn Workspace khác làm vị trí đích.
    1. Hệ thống hiển thị thông báo "Không thể di chuyển Folder sang Workspace khác".
    2. Yêu cầu người dùng chọn vị trí khác.
- Người dùng hủy thao tác di chuyển.
    1. Hệ thống hủy quá trình di chuyển.
    2. Folder giữ nguyên vị trí ban đầu.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình di chuyển Folder.
    1. Hệ thống hiển thị thông báo di chuyển Folder thất bại.
    2. Folder vẫn giữ nguyên vị trí ban đầu.

**Post-condition:**
- Folder được di chuyển đến vị trí mới trong cùng Workspace.
- Toàn bộ Folder con và các Page bên trong Folder được di chuyển theo.
- Cấu trúc dữ liệu của Workspace vẫn được đảm bảo hợp lệ.

## UC-13. Xóa Folder
**Actors:** User

**Summary:** Cho phép người dùng xóa một Folder do mình sở hữu.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace chứa Folder thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn một Folder.
2. Người dùng chọn chức năng Xóa Folder.
3. Hệ thống hiển thị hộp thoại xác nhận xóa.
4. Người dùng xác nhận thao tác xóa.
5. Hệ thống xóa Folder cùng toàn bộ Folder con và các Page thuộc Folder đó.
6. Hệ thống hiển thị thông báo xóa Folder thành công.

**Alternative Flow:**
- Người dùng hủy thao tác xóa.
    1. Hệ thống hủy quá trình xóa.
    2. Folder và toàn bộ dữ liệu bên trong được giữ nguyên.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình xóa Folder.
    1. Hệ thống hiển thị thông báo xóa Folder thất bại.
    2. Folder và toàn bộ dữ liệu bên trong vẫn được giữ nguyên.

**Post-condition:**
- Folder bị xóa khỏi hệ thống.
- Toàn bộ Folder con và các Page thuộc Folder đó cũng bị xóa.


## UC-14. Tạo Page
**Actors:** User

**Summary:** Cho phép người dùng tạo một Page mới trong Workspace hoặc trong một Folder thuộc Workspace.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace chứa Page thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn Workspace hoặc Folder cần tạo Page.
2. Người dùng chọn chức năng Tạo Page.
3. Hệ thống hiển thị biểu mẫu tạo Page.
4. Người dùng nhập tên Page.
5. Người dùng chọn chức năng Tạo.
6. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
7. Hệ thống tạo Page mới.
8. Hệ thống mở Page vừa tạo ở chế độ chỉnh sửa.

**Alternative Flow:**
- Tên Page đã tồn tại trong Workspace.
    1. Hệ thống hiển thị thông báo "Tên Page đã tồn tại trong Workspace này".
    2. Yêu cầu người dùng nhập tên khác.
- Người dùng để trống hoặc nhập tên Page không hợp lệ.
    1. Hệ thống hiển thị thông báo lỗi tương ứng.
    2. Cho phép người dùng chỉnh sửa và tạo lại.
- Người dùng hủy thao tác tạo Page.
    1. Hệ thống hủy quá trình tạo Page.
    2. Không tạo Page mới.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình tạo Page.
    1. Hệ thống hiển thị thông báo tạo Page thất bại.
    2. Không tạo Page mới.

**Post-condition:**
- Một Page mới được tạo trong Workspace hoặc Folder đã chọn.
- Page mới chưa có nội dung.


## UC-15. Đổi tên Page
**Actors:** User

**Summary:** Cho phép người dùng đổi tên một Page do mình sở hữu.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace chứa Page thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn một Page.
2. Người dùng chọn chức năng Đổi tên Page.
3. Hệ thống hiển thị tên Page hiện tại.
4. Người dùng nhập tên mới cho Page.
5. Người dùng chọn chức năng Lưu.
6. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
7. Hệ thống cập nhật tên Page.
8. Hệ thống hiển thị thông báo đổi tên thành công.

**Alternative Flow:**
- Tên Page mới đã tồn tại trong Workspace.
    1. Hệ thống hiển thị thông báo "Tên Page đã tồn tại trong Workspace này".
    2. Yêu cầu người dùng nhập tên khác.
- Người dùng để trống hoặc nhập tên Page không hợp lệ.
    1. Hệ thống hiển thị thông báo lỗi tương ứng.
    2. Cho phép người dùng chỉnh sửa và lưu lại.
- Tên Page mới giống với tên hiện tại.
    1. Hệ thống hiển thị thông báo "Tên Page không thay đổi".
    2. Không thực hiện cập nhật.
- Người dùng hủy thao tác đổi tên.
    1. Hệ thống hủy quá trình đổi tên.
    2. Page giữ nguyên tên cũ.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình cập nhật tên Page.
    1. Hệ thống hiển thị thông báo đổi tên Page thất bại.
    2. Page vẫn giữ nguyên tên cũ.

**Post-condition:**
- Tên Page được cập nhật thành công.
- Nội dung của Page không bị thay đổi.

## UC-16. Chỉnh sửa nội dung Page
**Actors:** User

**Summary:** Cho phép người dùng chỉnh sửa nội dung của một Page do mình sở hữu.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace chứa Page thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng mở một Page.
2. Người dùng chuyển sang chế độ chỉnh sửa (nếu chưa ở Edit Mode).
3. Hệ thống hiển thị nội dung hiện tại của Page.
4. Người dùng chỉnh sửa nội dung Page.
5. Người dùng chọn chức năng Lưu.
6. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
7. Hệ thống lưu nội dung mới của Page.
8. Hệ thống hiển thị thông báo lưu thành công.

**Alternative Flow:**
- Người dùng không thay đổi nội dung Page.
    1. Không thực hiện cập nhật dữ liệu.
- Người dùng hủy thao tác chỉnh sửa.
    1. Hệ thống hủy quá trình chỉnh sửa.
    2. Nội dung Page được giữ nguyên.
- Người dùng đang chỉnh sửa nội dung nhưng rời hệ thống.
    1. Hệ thống phát hiện có thay đổi chưa được lưu.
    2. Hệ thống hiển thị thông báo xác nhận:
       "Bạn có thay đổi chưa được lưu. Bạn có muốn lưu trước khi rời khỏi trang không?"
    3. Người dùng chọn một trong các tùy chọn:
       - Lưu: Hệ thống lưu nội dung rồi chuyển sang trang mới.
       - Không lưu: Hệ thống hủy các thay đổi và chuyển sang trang mới.
       - Hủy: Hệ thống ở lại Page hiện tại để người dùng tiếp tục chỉnh sửa.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình lưu nội dung Page.
    1. Hệ thống hiển thị thông báo lưu thất bại.
    2. Nội dung Page vẫn được giữ nguyên như trước khi chỉnh sửa.
- Phiên làm việc bị gián đoạn ngoài ý muốn (mất điện, sập nguồn, trình duyệt hoặc thiết bị bị tắt đột ngột).
    1. Hệ thống không nhận được yêu cầu lưu.
    2. Các thay đổi chưa được lưu không được ghi vào cơ sở dữ liệu.
    3. Nội dung của Page được giữ ở trạng thái của lần lưu gần nhất.

**Post-condition:**
- Nội dung mới của Page được lưu thành công.
- Các thay đổi được cập nhật và hiển thị khi Page được mở.

## UC-17. Di chuyển Page
**Actors:** User

**Summary:** Cho phép người dùng di chuyển một Page đến vị trí khác trong cùng Workspace.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace chứa Page thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn một Page.
2. Người dùng chọn chức năng Di chuyển Page.
3. Hệ thống hiển thị danh sách các vị trí có thể di chuyển đến trong Workspace.
4. Người dùng chọn Folder đích hoặc chọn Workspace làm vị trí đích.
5. Người dùng xác nhận thao tác di chuyển.
6. Hệ thống kiểm tra tính hợp lệ của vị trí đích.
7. Hệ thống di chuyển Page đến vị trí mới.
8. Hệ thống hiển thị thông báo di chuyển thành công.

**Alternative Flow:**
- Người dùng chọn Workspace khác làm vị trí đích.
    1. Hệ thống hiển thị thông báo "Không thể di chuyển Page sang Workspace khác".
    2. Yêu cầu người dùng chọn vị trí khác.
- Người dùng hủy thao tác di chuyển.
    1. Hệ thống hủy quá trình di chuyển.
    2. Page giữ nguyên vị trí ban đầu.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình di chuyển Page.
    1. Hệ thống hiển thị thông báo di chuyển Page thất bại.
    2. Page vẫn giữ nguyên vị trí ban đầu.

**Post-condition:**
- Page được di chuyển đến vị trí mới trong cùng Workspace.
- Nội dung và các liên kết của Page không bị thay đổi.

## UC-18. Xóa Page
**Actors:** User

**Summary:** Cho phép người dùng xóa một Page do mình sở hữu.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace chứa Page thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng chọn một Page.
2. Người dùng chọn chức năng Xóa Page.
3. Hệ thống hiển thị hộp thoại xác nhận xóa.
4. Người dùng xác nhận thao tác xóa.
5. Hệ thống xóa Page khỏi Workspace.
6. Hệ thống hiển thị thông báo xóa Page thành công.

**Alternative Flow:**
- Người dùng hủy thao tác xóa.
    1. Hệ thống hủy quá trình xóa.
    2. Page được giữ nguyên.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình xóa Page.
    1. Hệ thống hiển thị thông báo xóa Page thất bại.
    2. Page vẫn được giữ nguyên.

**Post-condition:**
- Page bị xóa khỏi hệ thống.
- Các liên kết trỏ đến Page này trở thành liên kết không hợp lệ. Khi người dùng mở các liên kết đó, hệ thống sẽ thông báo rằng Page không tồn tại.

## UC-19. Tạo liên kết giữa các Page
**Actors:** User

**Summary:** Cho phép người dùng tạo liên kết từ một đoạn văn bản trong Page hiện tại đến một Page khác trong cùng Workspace.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace chứa Page thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng mở một Page.
2. Người dùng chọn một đoạn văn bản trong nội dung Page.
3. Người dùng chọn chức năng Tạo liên kết.
4. Hệ thống hiển thị danh sách các Page trong cùng Workspace.
5. Người dùng chọn Page cần liên kết.
6. Hệ thống tạo liên kết từ đoạn văn bản đã chọn đến Page được chọn.
7. Hệ thống lưu thay đổi.
8. Hệ thống hiển thị thông báo tạo liên kết thành công.

**Alternative Flow:**
- Người dùng không chọn đoạn văn bản nào.
    1. Hệ thống hiển thị thông báo yêu cầu chọn đoạn văn bản trước khi tạo liên kết.
- Người dùng hủy thao tác tạo liên kết.
    1. Hệ thống hủy quá trình tạo liên kết.
    2. Không tạo liên kết mới.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình tạo liên kết.
    1. Hệ thống hiển thị thông báo tạo liên kết thất bại.
    2. Không tạo liên kết mới.

**Post-condition:**
- Đoạn văn bản được chọn trở thành liên kết đến Page đã chọn.
- Người dùng có thể nhấp vào đoạn văn bản đó để mở Page được liên kết trong chế độ xem.

## UC-20. Xóa liên kết giữa các Page
**Actors:** User

**Summary:** Cho phép người dùng xóa liên kết từ một đoạn văn bản trong Page hiện tại đến một Page khác.

**Pre-condition:**
- Người dùng đã đăng nhập.
- Workspace chứa Page thuộc quyền sở hữu của người dùng.
- Người dùng đang ở chế độ chỉnh sửa (Edit Mode).

**Main Flow:**
1. Người dùng mở một Page.
2. Người dùng chọn một đoạn văn bản đã được liên kết.
3. Người dùng chọn chức năng Xóa liên kết.
4. Hệ thống xóa liên kết khỏi đoạn văn bản đã chọn.
5. Hệ thống lưu thay đổi.
6. Hệ thống hiển thị thông báo xóa liên kết thành công.

**Alternative Flow:**
- Đoạn văn bản được chọn không chứa liên kết.
    1. Hệ thống hiển thị thông báo "Đoạn văn bản chưa được liên kết".
    2. Không thực hiện xóa liên kết.
- Người dùng hủy thao tác xóa liên kết.
    1. Hệ thống hủy quá trình xóa liên kết.
    2. Liên kết được giữ nguyên.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình xóa liên kết.
    1. Hệ thống hiển thị thông báo xóa liên kết thất bại.
    2. Liên kết vẫn được giữ nguyên.

**Post-condition:**
- Liên kết giữa đoạn văn bản và Page đích được xóa.
- Đoạn văn bản vẫn được giữ nguyên trong nội dung Page.

## UC-21. Xem Workspace/Folder/Page (View Mode)
**Actors:** Guest, User

**Summary:** Cho phép Guest xem Workspace Public, User xem Workspace Public và Workspace thuộc quyền sở hữu.

**Pre-condition:**
- Hệ thống đang ở chế độ xem (View Mode) (chế độ mặc định).
- Người dùng có quyền truy cập Workspace cần xem.

**Main Flow:**
1. Người dùng mở một Workspace.
2. Hệ thống hiển thị danh sách Folder và Page trong Workspace.
3. Người dùng chọn một Folder hoặc một Page.
4. Nếu người dùng chọn Folder, hệ thống hiển thị các Folder con và các Page trong Folder đó.
5. Nếu người dùng chọn Page, hệ thống hiển thị nội dung của Page.
6. Người dùng có thể nhấp vào đoạn văn bản đã được liên kết trong nội dung Page.
7. Hệ thống mở Page tương ứng.

**Alternative Flow:**
- Người dùng không có quyền truy cập Workspace.
    1. Hệ thống hiển thị thông báo "Bạn không có quyền truy cập Workspace này".
    2. Không hiển thị nội dung Workspace.
- Người dùng nhấp vào liên kết trỏ đến một Page đã bị xóa.
    1. Hệ thống hiển thị thông báo "Page không tồn tại".

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình tải dữ liệu.
    1. Hệ thống hiển thị thông báo tải dữ liệu thất bại.
    2. Không hiển thị nội dung tương ứng.

**Post-condition:**
- Workspace, Folder hoặc Page được hiển thị theo quyền truy cập của người dùng.
- Không có dữ liệu nào của hệ thống bị thay đổi.

## UC-22. Chuyển sang chế độ chỉnh sửa (Edit Mode)
**Actors:** User

**Summary:** Cho phép người dùng chuyển Workspace của mình từ chế độ xem (View Mode) sang chế độ chỉnh sửa (Edit Mode).

**Pre-condition:**
- Người dùng đã đăng nhập.
- Người dùng đang ở chế độ xem (View Mode).
- Người dùng đang mở một Workspace thuộc khu vực Workspace của mình.

**Main Flow:**
1. Người dùng mở một Workspace của mình.
2. Người dùng chọn chức năng Chuyển sang Edit Mode.
3. Hệ thống chuyển Workspace sang chế độ chỉnh sửa.
4. Hệ thống hiển thị giao diện Edit Mode.

**Alternative Flow:**
- Người dùng hủy thao tác chuyển chế độ.
    1. Hệ thống hủy thao tác.
    2. Workspace vẫn ở chế độ xem (View Mode).

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình chuyển chế độ.
    1. Hệ thống hiển thị thông báo chuyển sang Edit Mode thất bại.
    2. Workspace vẫn ở chế độ xem (View Mode).

**Post-condition:**
- Workspace được mở ở chế độ chỉnh sửa (Edit Mode).
- Người dùng có thể thực hiện các chức năng quản lý Workspace, Folder, Page và liên kết giữa các Page.

## UC-23. Tìm kiếm Workspace
**Actors:** Guest, User

**Summary:** Cho phép người dùng tìm kiếm Workspace theo từ khóa.

**Pre-condition:**
- Hệ thống đang hoạt động.
- Người dùng đang ở trang tìm kiếm.

**Main Flow:**
1. Người dùng chọn chức năng Tìm kiếm.
2. Hệ thống hiển thị ô tìm kiếm và các bộ lọc dữ liệu.
3. Người dùng chọn bộ lọc **Workspace**.
4. Người dùng nhập từ khóa cần tìm.
5. Người dùng thực hiện tìm kiếm.
6. Hệ thống tìm kiếm các Workspace phù hợp với từ khóa và quyền truy cập của người dùng.
7. Hệ thống hiển thị danh sách kết quả tìm kiếm.
8. Người dùng chọn một Workspace từ danh sách kết quả.
9. Hệ thống mở Workspace được chọn.

**Alternative Flow:**
- Không tìm thấy Workspace phù hợp.
    1. Hệ thống hiển thị thông báo "Không tìm thấy Workspace phù hợp".
- Người dùng hủy thao tác tìm kiếm.
    1. Hệ thống hủy quá trình tìm kiếm.
    2. Quay về màn hình trước đó.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình tìm kiếm.
    1. Hệ thống hiển thị thông báo tìm kiếm thất bại.
    2. Không hiển thị kết quả tìm kiếm.

**Post-condition:**
- Danh sách Workspace phù hợp với từ khóa được hiển thị theo quyền truy cập của người dùng.

## UC-24. Tìm kiếm Folder
**Actors:** Guest, User

**Summary:** Cho phép người dùng tìm kiếm Folder theo từ khóa.

**Pre-condition:**
- Hệ thống đang hoạt động.
- Người dùng đang ở trang tìm kiếm.

**Main Flow:**
1. Người dùng chọn chức năng Tìm kiếm.
2. Hệ thống hiển thị ô tìm kiếm và các bộ lọc dữ liệu.
3. Người dùng chọn bộ lọc **Folder**.
4. Người dùng nhập từ khóa cần tìm.
5. Người dùng thực hiện tìm kiếm.
6. Hệ thống tìm kiếm các Folder phù hợp với từ khóa và quyền truy cập của người dùng.
7. Hệ thống hiển thị danh sách kết quả tìm kiếm.
8. Người dùng chọn một Folder từ danh sách kết quả.
9. Hệ thống mở Workspace chứa Folder và hiển thị Folder được chọn.

**Alternative Flow:**
- Không tìm thấy Folder phù hợp.
    1. Hệ thống hiển thị thông báo "Không tìm thấy Folder phù hợp".
- Người dùng hủy thao tác tìm kiếm.
    1. Hệ thống hủy quá trình tìm kiếm.
    2. Quay về màn hình trước đó.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình tìm kiếm.
    1. Hệ thống hiển thị thông báo tìm kiếm thất bại.
    2. Không hiển thị kết quả tìm kiếm.

**Post-condition:**
- Danh sách Folder phù hợp với từ khóa được hiển thị theo quyền truy cập của người dùng.

## UC-25. Tìm kiếm Page
**Actors:** Guest, User

**Summary:** Cho phép người dùng tìm kiếm Page theo từ khóa.

**Pre-condition:**
- Hệ thống đang hoạt động.
- Người dùng đang ở trang tìm kiếm.

**Main Flow:**
1. Người dùng chọn chức năng Tìm kiếm.
2. Hệ thống hiển thị ô tìm kiếm và các bộ lọc dữ liệu.
3. Người dùng chọn bộ lọc **Page**.
4. Người dùng nhập từ khóa cần tìm.
5. Người dùng thực hiện tìm kiếm.
6. Hệ thống tìm kiếm các Page phù hợp với từ khóa và quyền truy cập của người dùng.
7. Hệ thống hiển thị danh sách kết quả tìm kiếm.
8. Người dùng chọn một Page từ danh sách kết quả.
9. Hệ thống mở Workspace chứa Page và hiển thị nội dung của Page được chọn.

**Alternative Flow:**
- Không tìm thấy Page phù hợp.
    1. Hệ thống hiển thị thông báo "Không tìm thấy Page phù hợp".
- Người dùng hủy thao tác tìm kiếm.
    1. Hệ thống hủy quá trình tìm kiếm.
    2. Quay về màn hình trước đó.

**Exception Flow:**
- Hệ thống xảy ra lỗi trong quá trình tìm kiếm.
    1. Hệ thống hiển thị thông báo tìm kiếm thất bại.
    2. Không hiển thị kết quả tìm kiếm.

**Post-condition:**
- Danh sách Page phù hợp với từ khóa được hiển thị theo quyền truy cập của người dùng.
