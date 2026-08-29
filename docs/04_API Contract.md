# API Contract

## 1. Mục đích

Tài liệu này là hợp đồng giữa frontend và backend của MyWiki. Mọi endpoint mới hoặc thay đổi endpoint hiện có cần được cập nhật trong tài liệu trước khi triển khai.

## 2. Quy ước chung

- Base URL: `/api`
- Request và response sử dụng `Content-Type: application/json` khi có body.
- Tên field sử dụng `camelCase`.
- ID tài nguyên sử dụng kiểu số nguyên dương.
- API sử dụng HTTP session do Spring Security quản lý.
- Password chỉ xuất hiện trong request đăng ký/đăng nhập và không bao giờ xuất hiện trong response.
- Tất cả response list đều sử dụng format phân trang thống nhất (xem mục 4.1).

## 3. Format lỗi thống nhất

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "email": "Email must be valid"
  },
  "timestamp": "2026-08-25T16:00:00",
  "path": "/api/auth/register"
}
```

### 3.1 Mã lỗi chuẩn

| Code                 | HTTP status  | Ý nghĩa                                            |
|:---------------------|:------------:|:---------------------------------------------------|
| `VALIDATION_ERROR`   |    `400`     | Request không hợp lệ                               |
| `UNAUTHENTICATED`    |    `401`     | Chưa đăng nhập hoặc thông tin đăng nhập không đúng |
| `ACCESS_DENIED`      |    `403`     | Không có quyền                                     |
| `RESOURCE_NOT_FOUND` |    `404`     | Không tìm thấy tài nguyên                          |
| `CONFLICT`           |    `409`     | Trùng dữ liệu hoặc xung đột nghiệp vụ              |
| `INTERNAL_ERROR`     |    `500`     | Lỗi hệ thống                                       |

### 3.2 Quy ước rõ ràng cho response lỗi

- `timestamp` dùng ISO-8601, ví dụ: `2026-08-25T16:00:00`.
- `path` là đường request bắt đầu bằng `/api/...`.
- `details` chỉ xuất hiện khi có lỗi validation hoặc lỗi nghiệp vụ có trường chi tiết cụ thể.
- Nếu không có trường chi tiết, response tối thiểu phải có: `code`, `message`, `timestamp`, `path`.

## 4. Quy ước cho list API

### 4.1 Pagination và sorting

Mọi endpoint list phải hỗ trợ query params sau:

- `page`: số trang bắt đầu từ `0` (default `0`)
- `size`: số item mỗi trang (default `20`, max `100`)
- `sort`: field để sắp xếp, ví dụ `updatedAt`, `createdAt`, `name`
- `direction`: `asc` hoặc `desc` (default `desc`)

Response list chuẩn:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Sample"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "sort": "updatedAt",
  "direction": "desc"
}
```

### 4.2 Validation cho query params

- `page` phải là số nguyên >= 0.
- `size` phải là số nguyên từ 1 đến 100.
- `sort` chỉ được dùng các field được phép sort trên resource đó.
- `direction` chỉ chấp nhận `asc` hoặc `desc`.
- Nếu query params không hợp lệ, trả `400 VALIDATION_ERROR`.

### 4.3 Kiểu dữ liệu chuẩn

- `String`: UTF-8, không null nếu required.
- `Integer`/`Long`: số nguyên dương.
- `Boolean`: `true` / `false`.
- `LocalDate`: ISO `YYYY-MM-DD`.
- `LocalDateTime`: ISO-8601 `YYYY-MM-DDTHH:mm:ss` hoặc `...Z` nếu deploy qua UTC.
- URL phải là format hợp lệ nếu có giá trị.
- `description`, `bio`, `content` được phép null nếu không dùng, nhưng nếu có thì phải tuân theo max length của resource.

## 5. Authentication API

### 5.1 Quyết định thiết kế

- Register và login không yêu cầu authentication.
- Sau khi login thành công, server tạo hoặc cập nhật session.
- Client sử dụng cookie `JSESSIONID` cho request tiếp theo.
- Logout và `/auth/me` yêu cầu người dùng đã đăng nhập.
- CSRF được bật cho các request thay đổi dữ liệu.
- Endpoint `/api/auth/csrf` cung cấp CSRF token cho client.

### 5.2 Danh sách endpoint

| Method  | Endpoint             |  Auth  |    Request body    | Success  |        Response        | Error statuses |
|:-------:|:---------------------|:------:|:------------------:|:--------:|:----------------------:|:---------------|
| `POST`  | `/api/auth/register` | Không  | `RegisterRequest`  |  `201`   | `AuthMessageResponse`  | `400`, `409`   |
| `POST`  | `/api/auth/login`    | Không  |   `LoginRequest`   |  `200`   |    `LoginResponse`     | `400`, `401`   |
| `POST`  | `/api/auth/logout`   |  User  |       Không        |  `204`   |     Không có body      | `401`          |
|  `GET`  | `/api/auth/me`       |  User  |       Không        |  `200`   | `CurrentUserResponse`  | `401`          |
|  `GET`  | `/api/auth/csrf`     | Không  |       Không        |  `200`   |  `CsrfTokenResponse`   | `500`          |

### 5.3 `POST /api/auth/register`

Đăng ký tài khoản người dùng mới.

Request:

```json
{
  "username": "Username",
  "email": "user@mywiki.com",
  "password": "password123"
}
```

| Field      | Type   | Required  | Rule                                   |
|:-----------|:-------|:---------:|:---------------------------------------|
| `username` | string |    Có     | Không rỗng, tối đa 50 ký tự            |
| `email`    | string |    Có     | Đúng định dạng email, tối đa 255 ký tự |
| `password` | string |    Có     | Không rỗng, tối thiểu 6 ký tự          |

Response `201 Created`:

```json
{
  "message": "Registration successful"
}
```

Errors:
- `400 Bad Request`: Thiếu field hoặc dữ liệu không hợp lệ.
- `409 Conflict`: Email đã tồn tại.

### 5.4 `POST /api/auth/login`

Xác thực người dùng và tạo session đăng nhập.

Request:

```json
{
  "email": "user@mywiki.com",
  "password": "password123"
}
```

| Field      | Type   | Required  | Rule                 |
|:-----------|:-------|:---------:|:---------------------|
| `email`    | string |    Có     | Đúng định dạng email |
| `password` | string |    Có     | Không rỗng           |

Response `200 OK`:

```json
{
  "email": "user@mywiki.com",
  "message": "Login successful"
}
```

Errors:
- `400 Bad Request`: Thiếu field hoặc email không hợp lệ.
- `401 Unauthorized`: Email hoặc password không đúng.

### 5.5 `POST /api/auth/logout`

Hủy session hiện tại và xóa cookie session phía client.

Response `204 No Content`:

Không có response body.

Errors:
- `401 Unauthorized`: Người dùng chưa đăng nhập.

### 5.6 `GET /api/auth/me`

Lấy thông tin người dùng hiện đang đăng nhập.

Response `200 OK`:

```json
{
  "email": "user@mywiki.com"
}
```

Errors:
- `401 Unauthorized`: Người dùng chưa đăng nhập.

### 5.7 `GET /api/auth/csrf`

Lấy CSRF token để client gửi kèm các request thay đổi dữ liệu như `POST`, `PUT` và `DELETE`.

Response `200 OK`:

```json
{
  "token": "csrf-token-value"
}
```

### 5.8 Checklist triển khai và kiểm thử Auth

- [ ] Register trả `201 Created`.
- [ ] Logout trả `204 No Content`.
- [ ] Password được hash trước khi lưu database.
- [ ] Không trả `password` hoặc `passwordHash`.
- [ ] Validation trả `400`.
- [ ] Email trùng trả `409`.
- [ ] Login sai thông tin trả `401`.
- [ ] Request cần đăng nhập bị từ chối khi chưa có session.
- [ ] Cập nhật Controller test theo contract này.
- [ ] Kiểm tra session, cookie và CSRF bằng API testing.

## 6. User API

### 6.1 Quyết định thiết kế

- Tất cả endpoint của User đều yêu cầu người dùng đã đăng nhập.
- Người dùng chỉ được cập nhật thông tin tài khoản của chính mình.
- Không trả `password` hoặc `passwordHash` trong response.
- `GET /api/users/me` dùng để lấy profile hiện tại.
- `PUT /api/users/me` dùng để cập nhật profile.
- `PUT /api/users/me/password` dùng để đổi mật khẩu.

### 6.2 Danh sách endpoint

| Method  | Endpoint                 | Auth  |       Request body       | Success  |           Response            | Error statuses |
|:-------:|:-------------------------|:-----:|:------------------------:|:--------:|:-----------------------------:|:---------------|
|  `GET`  | `/api/users/me`          | User  |          Không           |  `200`   | `CurrentUserProfileResponse`  | `401`          |
|  `PUT`  | `/api/users/me`          | User  |  `UpdateAccountRequest`  |  `200`   |     `AuthMessageResponse`     | `400`, `401`   |
|  `PUT`  | `/api/users/me/password` | User  | `ChangePasswordRequest`  |  `200`   |     `AuthMessageResponse`     | `400`, `401`   |

### 6.3 `GET /api/users/me`

Lấy thông tin profile người dùng hiện tại.

Response `200 OK`:

```json
{
  "userId": 1,
  "username": "Username",
  "email": "user@mywiki.com",
  "avatarUrl": "https://example.com/avatar.jpg",
  "dateOfBirth": "2000-01-01",
  "bio": "Writer"
}
```

Errors:
- `401 Unauthorized`: Người dùng chưa đăng nhập.

### 6.4 `PUT /api/users/me`

Cập nhật thông tin tài khoản hiện tại.

Request:

```json
{
  "username": "New username",
  "avatarUrl": "https://example.com/avatar.jpg",
  "dateOfBirth": "2000-01-01",
  "bio": "Writer"
}
```

| Field         | Type                  | Required  | Rule                                              |
|:--------------|:----------------------|:---------:|:--------------------------------------------------|
| `username`    | string                |   Không   | Tối đa 50 ký tự                                   |
| `avatarUrl`   | string                |   Không   | Tối đa 500 ký tự, phải là URL hợp lệ nếu cung cấp |
| `dateOfBirth` | string (`YYYY-MM-DD`) |   Không   | Đúng định dạng ngày                               |
| `bio`         | string                |   Không   | Tối đa 500 ký tự                                  |

Response `200 OK`:

```json
{
  "message": "Account updated successfully"
}
```

Errors:
- `400 Bad Request`: Dữ liệu không hợp lệ.
- `401 Unauthorized`: Người dùng chưa đăng nhập.

### 6.5 `PUT /api/users/me/password`

Thay đổi mật khẩu của tài khoản hiện tại.

Request:

```json
{
  "currentPassword": "old-password",
  "newPassword": "new-password",
  "confirmNewPassword": "new-password"
}
```

| Field                | Type   | Required  | Rule                          |
|:---------------------|:-------|:---------:|:------------------------------|
| `currentPassword`    | string |    Có     | Không rỗng, tối thiểu 6 ký tự |
| `newPassword`        | string |    Có     | Không rỗng, tối thiểu 6 ký tự |
| `confirmNewPassword` | string |    Có     | Phải khớp với `newPassword`   |

Response `200 OK`:

```json
{
  "message": "Password changed successfully"
}
```

Errors:
- `400 Bad Request`: Thiếu field, mật khẩu mới không khớp, mật khẩu hiện tại sai hoặc dữ liệu không hợp lệ.
- `401 Unauthorized`: Người dùng chưa đăng nhập.

### 6.6 Checklist triển khai và kiểm thử User

- [ ] `GET /api/users/me` trả `200` và không trả `passwordHash`.
- [ ] `PUT /api/users/me` trả `200` với message chuẩn.
- [ ] `PUT /api/users/me/password` trả `200` khi mật khẩu đúng và khớp xác nhận.
- [ ] Mật khẩu mới được hash trước khi lưu database.
- [ ] Mật khẩu hiện tại sai trả `400 Bad Request` với message/chi tiết lỗi rõ ràng.
- [ ] `newPassword` và `confirmNewPassword` không khớp trả `400`.
- [ ] Không cho cập nhật hoặc đổi mật khẩu cho tài khoản khác.
- [ ] Cập nhật Controller test theo contract này.
- [ ] Kiểm tra session auth với cookie và response payload.

## 7. Workspace API

### 7.1 Quyết định thiết kế

- `GET /api/workspaces` trả danh sách workspace có thể xem được.
- Nếu user chưa đăng nhập, chỉ trả workspace `Public`.
- Nếu user đã đăng nhập, trả `Public` + workspace do chính user sở hữu.
- `GET /api/workspaces/{workspaceId}` cho phép truy cập nếu workspace là `Public` hoặc user là owner.
- Chỉ owner mới được sửa/xóa workspace.
- `visibility` chỉ chấp nhận `Public` hoặc `Private`.

### 7.2 Danh sách endpoint

|  Method   | Endpoint                        |      Auth       |    Request body     | Success  |           Response            | Error statuses      |
|:---------:|:--------------------------------|:---------------:|:-------------------:|:--------:|:-----------------------------:|:--------------------|
|   `GET`   | `/api/workspaces`               | Không/Optional  |        Không        |  `200`   | `PaginatedWorkspaceResponse`  | `403`, `404`        |
|   `GET`   | `/api/workspaces/{workspaceId}` | Không/Optional  |        Không        |  `200`   |      `WorkspaceResponse`      | `403`, `404`        |
|  `POST`   | `/api/workspaces`               |      User       | `WorkspaceRequest`  |  `201`   |      `WorkspaceResponse`      | `400`, `401`        |
|   `PUT`   | `/api/workspaces/{workspaceId}` |      User       | `WorkspaceRequest`  |  `200`   |      `WorkspaceResponse`      | `400`, `403`, `404` |
| `DELETE`  | `/api/workspaces/{workspaceId}` |      User       |        Không        |  `204`   |         Không có body         | `403`, `404`        |

### 7.3 `GET /api/workspaces`

Lấy danh sách workspace được hiển thị cho user hiện tại.

Response `200 OK`:

```json
{
  "content": [
    {
      "workspaceId": 1,
      "ownerId": 7,
      "ownerUsername": "alice",
      "name": "My Workspace",
      "description": "Project notes",
      "visibility": "Private",
      "createdAt": "2026-08-25T16:00:00",
      "updatedAt": "2026-08-25T16:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "sort": "updatedAt",
  "direction": "desc"
}
```

Errors:
- Không trả `401 Unauthorized` khi người dùng chưa đăng nhập, vì endpoint cho phép truy cập workspace `Public` mà không cần auth.
- `403 Forbidden`: nếu workspace bị khóa hoặc user không đủ quyền xem dữ liệu nhạy cảm (nếu có rule riêng trong service).
- `404 Not Found`: không tìm thấy workspace nào phù hợp với filter hoặc không tồn tại dữ liệu.

### 7.4 `GET /api/workspaces/{workspaceId}`

Lấy chi tiết workspace theo ID.

Response `200 OK`:

```json
{
  "workspaceId": 1,
  "ownerId": 7,
  "ownerUsername": "alice",
  "name": "My Workspace",
  "description": "Project notes",
  "visibility": "Public",
  "createdAt": "2026-08-25T16:00:00",
  "updatedAt": "2026-08-25T16:00:00"
}
```

Errors:
- `403 Forbidden`: workspace là `Private` và user không phải owner.
- `404 Not Found`: không tìm thấy workspace.

### 7.5 `POST /api/workspaces`

Tạo workspace mới cho user đang đăng nhập.

Request:

```json
{
  "name": "My Workspace",
  "description": "Project notes",
  "visibility": "Private"
}
```

| Field         | Type   | Required  | Rule                            |
|:--------------|:-------|:---------:|:--------------------------------|
| `name`        | string |    Có     | Không rỗng, tối đa 100 ký tự    |
| `description` | string |   Không   | Tối đa 500 ký tự                |
| `visibility`  | string |    Có     | Phải là `Public` hoặc `Private` |

Response `201 Created`:

```json
{
  "workspaceId": 1,
  "ownerId": 7,
  "ownerUsername": "alice",
  "name": "My Workspace",
  "description": "Project notes",
  "visibility": "Private",
  "createdAt": "2026-08-25T16:00:00",
  "updatedAt": "2026-08-25T16:00:00"
}
```

Errors:
- `400 Bad Request`: dữ liệu không hợp lệ hoặc `visibility` không hợp lệ.
- `401 Unauthorized`: người dùng chưa đăng nhập.
- `409 Conflict`: tên workspace đã tồn tại với owner này.

### 7.6 `PUT /api/workspaces/{workspaceId}`

Cập nhật dữ liệu workspace của chính user.

Request:

```json
{
  "name": "Updated Workspace",
  "description": "Updated notes",
  "visibility": "Public"
}
```

Response `200 OK`:

```json
{
  "workspaceId": 1,
  "ownerId": 7,
  "ownerUsername": "alice",
  "name": "Updated Workspace",
  "description": "Updated notes",
  "visibility": "Public",
  "createdAt": "2026-08-25T16:00:00",
  "updatedAt": "2026-08-29T12:00:00"
}
```

Errors:
- `400 Bad Request`: dữ liệu không hợp lệ hoặc `visibility` không hợp lệ.
- `403 Forbidden`: user không phải owner.
- `404 Not Found`: không tìm thấy workspace.
- `409 Conflict`: tên workspace trùng với workspace khác của owner.

### 7.7 `DELETE /api/workspaces/{workspaceId}`

Xóa workspace hiện tại.

Response `204 No Content`:

Không có response body.

Errors:
- `403 Forbidden`: user không phải owner.
- `404 Not Found`: không tìm thấy workspace.

### 7.8 Checklist triển khai và kiểm thử Workspace

- [ ] `GET /api/workspaces` trả đúng workspace `Public` cho người chưa đăng nhập.
- [ ] `GET /api/workspaces` trả `Public + workspace của user` khi đã đăng nhập.
- [ ] `GET /api/workspaces/{id}` chặn private workspace nếu không phải owner.
- [ ] `POST /api/workspaces` tạo mới với `201 Created`.
- [ ] `PUT /api/workspaces/{id}` chỉ owner mới sửa được.
- [ ] `DELETE /api/workspaces/{id}` chỉ owner mới xóa được.
- [ ] `visibility` phải là `Public` hoặc `Private`.
- [ ] Tên workspace trùng với owner khác trả `409`.
- [ ] Không trả `password`/`passwordHash` trong bất kỳ response workspace nào.
- [ ] Cập nhật controller/service test theo contract này.

## 8. Folder API

### 8.1 Quyết định thiết kế

- Folder thuộc về workspace và có thể có `parentFolderId` để xây cây thư mục.
- `GET /api/workspaces/{workspaceId}/folders` trả danh sách folder trong workspace mà user được xem.
- Nếu workspace là `Private`, chỉ owner mới có quyền xem/sửa/xóa folder.
- Chỉ owner của workspace có quyền tạo, cập nhật, xóa folder.
- Không cho di chuyển folder vào chính nó hoặc vào descendant của chính nó.
- Tên folder phải unique theo `workspaceId` và `parentFolderId`.

### 8.2 Danh sách endpoint

|  Method   | Endpoint                                           |      Auth       |   Request body   | Success  |          Response          | Error statuses             |
|:---------:|:---------------------------------------------------|:---------------:|:----------------:|:--------:|:--------------------------:|:---------------------------|
|   `GET`   | `/api/workspaces/{workspaceId}/folders`            | Không/Optional  |      Không       |  `200`   | `PaginatedFolderResponse`  | `403`, `404`               |
|   `GET`   | `/api/workspaces/{workspaceId}/folders/{folderId}` | Không/Optional  |      Không       |  `200`   |      `FolderResponse`      | `403`, `404`               |
|  `POST`   | `/api/workspaces/{workspaceId}/folders`            |      User       | `FolderRequest`  |  `201`   |      `FolderResponse`      | `400`, `403`, `404`, `409` |
|   `PUT`   | `/api/workspaces/{workspaceId}/folders/{folderId}` |      User       | `FolderRequest`  |  `200`   |      `FolderResponse`      | `400`, `403`, `404`, `409` |
| `DELETE`  | `/api/workspaces/{workspaceId}/folders/{folderId}` |      User       |      Không       |  `204`   |       Không có body        | `403`, `404`               |

### 8.3 `GET /api/workspaces/{workspaceId}/folders`

Lấy danh sách folder trong workspace.

Response `200 OK`:

```json
{
  "content": [
    {
      "folderId": 1,
      "workspaceId": 10,
      "parentFolderId": null,
      "name": "Characters",
      "description": "Character notes",
      "createdAt": "2026-08-25T16:00:00",
      "updatedAt": "2026-08-25T16:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "sort": "name",
  "direction": "asc"
}
```

Errors:
- `403 Forbidden`: workspace là `Private` và user không phải owner.
- `404 Not Found`: workspace không tồn tại.

### 8.4 `GET /api/workspaces/{workspaceId}/folders/{folderId}`

Lấy thông tin chi tiết folder theo ID.

Response `200 OK`:

```json
{
  "folderId": 1,
  "workspaceId": 10,
  "parentFolderId": null,
  "name": "Characters",
  "description": "Character notes",
  "createdAt": "2026-08-25T16:00:00",
  "updatedAt": "2026-08-25T16:00:00"
}
```

Errors:
- `403 Forbidden`: không có quyền xem workspace.
- `404 Not Found`: không tìm thấy workspace hoặc folder.

### 8.5 `POST /api/workspaces/{workspaceId}/folders`

Tạo folder mới trong workspace.

Request:

```json
{
  "name": "Characters",
  "description": "Character notes",
  "parentFolderId": 3
}
```

| Field            | Type    | Required  | Rule                                              |
|:-----------------|:--------|:---------:|:--------------------------------------------------|
| `name`           | string  |    Có     | Không rỗng, tối đa 100 ký tự                      |
| `description`    | string  |   Không   | Tối đa 500 ký tự                                  |
| `parentFolderId` | integer |   Không   | Nếu cung cấp, phải là folder thuộc cùng workspace |

Response `201 Created`:

```json
{
  "folderId": 1,
  "workspaceId": 10,
  "parentFolderId": 3,
  "name": "Characters",
  "description": "Character notes",
  "createdAt": "2026-08-25T16:00:00",
  "updatedAt": "2026-08-25T16:00:00"
}
```

Errors:
- `400 Bad Request`: dữ liệu không hợp lệ.
- `403 Forbidden`: user không phải owner của workspace.
- `404 Not Found`: workspace hoặc parent folder không tồn tại.
- `409 Conflict`: tên folder đã tồn tại cùng level trong workspace.

### 8.6 `PUT /api/workspaces/{workspaceId}/folders/{folderId}`

Cập nhật folder hiện có.

Request:

```json
{
  "name": "Updated Characters",
  "description": "Updated notes",
  "parentFolderId": 5
}
```

Response `200 OK`:

```json
{
  "folderId": 1,
  "workspaceId": 10,
  "parentFolderId": 5,
  "name": "Updated Characters",
  "description": "Updated notes",
  "createdAt": "2026-08-25T16:00:00",
  "updatedAt": "2026-08-29T12:00:00"
}
```

Errors:
- `400 Bad Request`: dữ liệu không hợp lệ hoặc di chuyển vào descendant của chính nó.
- `403 Forbidden`: user không phải owner của workspace.
- `404 Not Found`: workspace hoặc folder không tồn tại.
- `409 Conflict`: tên folder trùng với cùng parent hoặc root level.

### 8.7 `DELETE /api/workspaces/{workspaceId}/folders/{folderId}`

Xóa folder khỏi workspace. Nếu folder có subtree, toàn bộ các folder con cũng bị xóa theo logic service.

Response `204 No Content`:

Không có response body.

Errors:
- `403 Forbidden`: user không phải owner.
- `404 Not Found`: workspace hoặc folder không tồn tại.

### 8.8 Checklist triển khai và kiểm thử Folder

- [ ] `GET /api/workspaces/{workspaceId}/folders` trả đúng danh sách cho workspace `Public`/owner.
- [ ] `GET /api/workspaces/{workspaceId}/folders/{folderId}` chặn private workspace nếu không phải owner.
- [ ] `POST /api/workspaces/{workspaceId}/folders` trả `201 Created`.
- [ ] `PUT /api/workspaces/{workspaceId}/folders/{folderId}` cho phép cập nhật name/description/parentFolderId.
- [ ] Không cho di chuyển folder vào chính nó hoặc tree con của nó.
- [ ] Tên folder unique theo `workspaceId` + `parentFolderId`.
- [ ] `DELETE /api/workspaces/{workspaceId}/folders/{folderId}` xóa subtree và page liên quan.
- [ ] Chỉ owner của workspace mới tạo/sửa/xóa folder.
- [ ] Cập nhật controller/service test theo contract này.

## 9. Page API

### 9.1 Quyết định thiết kế

- Page nằm trong một workspace và có thể thuộc về một folder cụ thể.
- `GET /api/workspaces/{workspaceId}/pages` trả danh sách page mà user có quyền xem trong workspace.
- Nếu workspace là `Private`, chỉ owner mới được xem/sửa/xóa page.
- Chỉ owner của workspace mới có quyền tạo, cập nhật, xóa page.
- `title` của page phải unique trong workspace.

### 9.2 Danh sách endpoint

|  Method   | Endpoint                                       |      Auth       |  Request body  | Success  |         Response         | Error statuses             |
|:---------:|:-----------------------------------------------|:---------------:|:--------------:|:--------:|:------------------------:|:---------------------------|
|   `GET`   | `/api/workspaces/{workspaceId}/pages`          | Không/Optional  |     Không      |  `200`   | `PaginatedPageResponse`  | `403`, `404`               |
|   `GET`   | `/api/workspaces/{workspaceId}/pages/{pageId}` | Không/Optional  |     Không      |  `200`   |      `PageResponse`      | `403`, `404`               |
|  `POST`   | `/api/workspaces/{workspaceId}/pages`          |      User       | `PageRequest`  |  `201`   |      `PageResponse`      | `400`, `403`, `404`, `409` |
|   `PUT`   | `/api/workspaces/{workspaceId}/pages/{pageId}` |      User       | `PageRequest`  |  `200`   |      `PageResponse`      | `400`, `403`, `404`, `409` |
| `DELETE`  | `/api/workspaces/{workspaceId}/pages/{pageId}` |      User       |     Không      |  `204`   |      Không có body       | `403`, `404`               |

### 9.3 `GET /api/workspaces/{workspaceId}/pages`

Lấy danh sách page trong workspace.

Response `200 OK`:

```json
{
  "content": [
    {
      "pageId": 1,
      "workspaceId": 10,
      "folderId": 2,
      "title": "Overview",
      "content": "# Overview\nThis is the page content",
      "createdAt": "2026-08-25T16:00:00",
      "updatedAt": "2026-08-25T16:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "sort": "updatedAt",
  "direction": "desc"
}
```

Errors:
- `403 Forbidden`: workspace là `Private` và user không phải owner.
- `404 Not Found`: workspace không tồn tại.

### 9.4 `GET /api/workspaces/{workspaceId}/pages/{pageId}`

Lấy chi tiết page theo ID.

Response `200 OK`:

```json
{
  "pageId": 1,
  "workspaceId": 10,
  "folderId": 2,
  "title": "Overview",
  "content": "# Overview\nThis is the page content",
  "createdAt": "2026-08-25T16:00:00",
  "updatedAt": "2026-08-25T16:00:00"
}
```

Errors:
- `403 Forbidden`: không có quyền xem workspace.
- `404 Not Found`: không tìm thấy workspace hoặc page.

### 9.5 `POST /api/workspaces/{workspaceId}/pages`

Tạo page mới trong workspace.

Request:

```json
{
  "title": "Overview",
  "content": "# Overview\nThis is the page content",
  "folderId": 2
}
```

| Field      | Type    | Required  | Rule                              |
|:-----------|:--------|:---------:|:----------------------------------|
| `title`    | string  |    Có     | Không rỗng, tối đa 200 ký tự      |
| `content`  | string  |   Không   | Nội dung văn bản tùy ý            |
| `folderId` | integer |   Không   | Nếu có, phải thuộc cùng workspace |

Response `201 Created`:

```json
{
  "pageId": 1,
  "workspaceId": 10,
  "folderId": 2,
  "title": "Overview",
  "content": "# Overview\nThis is the page content",
  "createdAt": "2026-08-25T16:00:00",
  "updatedAt": "2026-08-25T16:00:00"
}
```

Errors:
- `400 Bad Request`: dữ liệu không hợp lệ.
- `403 Forbidden`: user không phải owner của workspace.
- `404 Not Found`: workspace hoặc folder không tồn tại.
- `409 Conflict`: tiêu đề page đã tồn tại trong workspace.

### 9.6 `PUT /api/workspaces/{workspaceId}/pages/{pageId}`

Cập nhật page hiện có.

Request:

```json
{
  "title": "Updated Overview",
  "content": "# Updated Overview\nNew content",
  "folderId": 5
}
```

Response `200 OK`:

```json
{
  "pageId": 1,
  "workspaceId": 10,
  "folderId": 5,
  "title": "Updated Overview",
  "content": "# Updated Overview\nNew content",
  "createdAt": "2026-08-25T16:00:00",
  "updatedAt": "2026-08-29T12:00:00"
}
```

Errors:
- `400 Bad Request`: dữ liệu không hợp lệ.
- `403 Forbidden`: user không phải owner.
- `404 Not Found`: workspace, folder hoặc page không tồn tại.
- `409 Conflict`: tiêu đề page trùng với page khác trong workspace.

### 9.7 `DELETE /api/workspaces/{workspaceId}/pages/{pageId}`

Xóa page khỏi workspace. Khi xóa page, các link liên quan đến page đó cũng bị xóa.

Response `204 No Content`:

Không có response body.

Errors:
- `403 Forbidden`: user không phải owner.
- `404 Not Found`: workspace hoặc page không tồn tại.

### 9.8 Checklist triển khai và kiểm thử Page

- [ ] `GET /api/workspaces/{workspaceId}/pages` chỉ trả page của workspace đang xem được.
- [ ] `GET /api/workspaces/{workspaceId}/pages/{pageId}` chặn private workspace nếu không phải owner.
- [ ] `POST /api/workspaces/{workspaceId}/pages` trả `201 Created`.
- [ ] `PUT /api/workspaces/{workspaceId}/pages/{pageId}` cho phép sửa nội dung/page move sang folder khác.
- [ ] `title` unique trong workspace.
- [ ] `DELETE /api/workspaces/{workspaceId}/pages/{pageId}` xóa page và link liên quan.
- [ ] Chỉ owner của workspace mới tạo/sửa/xóa page.
- [ ] Cập nhật controller/service test theo contract này.

## 10. Link API

### 10.1 Quyết định thiết kế

- Link là mối quan hệ giữa `sourcePageId` và `targetPageId` trong cùng workspace.
- `GET /api/workspaces/{workspaceId}/pages/{sourcePageId}/links` trả danh sách link đi ra từ page gốc.
- Chỉ owner của workspace mới có quyền tạo/xóa link.
- Một page không được link tới chính nó.
- Link không định nghĩa update; chỉ tạo và xóa.

### 10.2 Danh sách endpoint

|  Method   | Endpoint                                                            |      Auth       |  Request body  | Success  |         Response         | Error statuses      |
|:---------:|:--------------------------------------------------------------------|:---------------:|:--------------:|:--------:|:------------------------:|:--------------------|
|   `GET`   | `/api/workspaces/{workspaceId}/pages/{sourcePageId}/links`          | Không/Optional  |     Không      |  `200`   | `PaginatedLinkResponse`  | `403`, `404`        |
|  `POST`   | `/api/workspaces/{workspaceId}/pages/{sourcePageId}/links`          |      User       | `LinkRequest`  |  `201`   |      `LinkResponse`      | `400`, `403`, `404` |
| `DELETE`  | `/api/workspaces/{workspaceId}/pages/{sourcePageId}/links/{linkId}` |      User       |     Không      |  `204`   |      Không có body       | `403`, `404`        |

### 10.3 `GET /api/workspaces/{workspaceId}/pages/{sourcePageId}/links`

Lấy tất cả link từ một page nguồn.

Response `200 OK`:

```json
{
  "content": [
    {
      "linkId": 10,
      "workspaceId": 1,
      "sourcePageId": 20,
      "sourcePageTitle": "Source page",
      "targetPageId": 21,
      "targetPageTitle": "Target page",
      "anchorText": "See details",
      "createdAt": "2026-08-25T16:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "sort": "createdAt",
  "direction": "desc"
}
```

Errors:
- `403 Forbidden`: workspace là `Private` và user không phải owner.
- `404 Not Found`: workspace hoặc page không tồn tại.

### 10.4 `POST /api/workspaces/{workspaceId}/pages/{sourcePageId}/links`

Tạo link từ page nguồn tới page đích.

Request:

```json
{
  "targetPageId": 21,
  "anchorText": "See details"
}
```

| Field          | Type    | Required  | Rule                              |
|:---------------|:--------|:---------:|:----------------------------------|
| `targetPageId` | integer |    Có     | Phải là page thuộc cùng workspace |
| `anchorText`   | string  |    Có     | Không rỗng, tối đa 200 ký tự      |

Response `201 Created`:

```json
{
  "linkId": 10,
  "workspaceId": 1,
  "sourcePageId": 20,
  "sourcePageTitle": "Source page",
  "targetPageId": 21,
  "targetPageTitle": "Target page",
  "anchorText": "See details",
  "createdAt": "2026-08-25T16:00:00"
}
```

Errors:
- `400 Bad Request`: dữ liệu không hợp lệ hoặc source = target.
- `403 Forbidden`: user không phải owner của workspace.
- `404 Not Found`: workspace hoặc page không tồn tại.

### 10.5 `DELETE /api/workspaces/{workspaceId}/pages/{sourcePageId}/links/{linkId}`

Xóa một link cụ thể khỏi page nguồn.

Response `204 No Content`:

Không có response body.

Errors:
- `403 Forbidden`: user không phải owner.
- `404 Not Found`: workspace, page hoặc link không tồn tại.

### 10.6 Checklist triển khai và kiểm thử Link

- [ ] `GET /api/workspaces/{workspaceId}/pages/{sourcePageId}/links` trả đúng danh sách link.
- [ ] `POST /api/workspaces/{workspaceId}/pages/{sourcePageId}/links` trả `201 Created`.
- [ ] Không cho tạo link nếu `targetPageId` là page nguồn.
- [ ] Chỉ owner của workspace mới tạo/xóa link.
- [ ] `DELETE /api/workspaces/{workspaceId}/pages/{sourcePageId}/links/{linkId}` trả `204`.
- [ ] Cập nhật controller/service test theo contract này.

## 11. Postman-ready setup

### 11.1 Variables mẫu cho Postman

Tạo environment với các biến sau:

| Variable          | Example value           | Mục đích                             |
|:------------------|:------------------------|:-------------------------------------|
| `baseUrl`         | `http://localhost:8080` | Base URL của backend                 |
| `apiBase`         | `{{baseUrl}}/api`       | Base URL API                         |
| `csrfToken`       | `''`                    | Lưu token trả về từ `/api/auth/csrf` |
| `currentEmail`    | `user@mywiki.com`       | Email đang test                      |
| `currentPassword` | `password123`           | Password đang test                   |

### 11.2 Quy ước chạy trong Postman

- Mọi request API sử dụng URL dạng `{{apiBase}}/...`.
- Với request không cần auth, không cần thêm header đặc biệt.
- Với request cần đăng nhập, giữ session cookie `JSESSIONID` trong Postman bằng `Cookie Jar`.
- Với request thay đổi dữ liệu (`POST`, `PUT`, `DELETE`), thêm header:

```http
X-XSRF-TOKEN: {{csrfToken}}
```

- Khi cần lấy CSRF token, gọi request trước:

```http
GET {{apiBase}}/auth/csrf
```

Sau đó lưu `response.body.token` vào biến `csrfToken`.

### 11.3 Sample request cho Postman

#### 1) Register

```http
POST {{apiBase}}/auth/register
Content-Type: application/json

{
  "username": "Demo User",
  "email": "demo@mywiki.com",
  "password": "password123"
}
```

#### 2) Login

```http
POST {{apiBase}}/auth/login
Content-Type: application/json

{
  "email": "demo@mywiki.com",
  "password": "password123"
}
```

#### 3) Lấy current user

```http
GET {{apiBase}}/auth/me
```

#### 4) Lấy CSRF token

```http
GET {{apiBase}}/auth/csrf
```

#### 5) Tạo workspace mới

```http
POST {{apiBase}}/workspaces
Content-Type: application/json
X-XSRF-TOKEN: {{csrfToken}}

{
  "name": "My Workspace",
  "description": "Project notes",
  "visibility": "Private"
}
```

#### 6) Lấy danh sách workspace

```http
GET {{apiBase}}/workspaces
```

#### 7) Tạo folder trong workspace

```http
POST {{apiBase}}/workspaces/1/folders
Content-Type: application/json
X-XSRF-TOKEN: {{csrfToken}}

{
  "name": "Characters",
  "description": "Character notes",
  "parentFolderId": null
}
```

#### 8) Tạo page trong workspace

```http
POST {{apiBase}}/workspaces/1/pages
Content-Type: application/json
X-XSRF-TOKEN: {{csrfToken}}

{
  "title": "Overview",
  "content": "# Overview\nThis is the page content",
  "folderId": 1
}
```

#### 9) Tạo link giữa 2 page

```http
POST {{apiBase}}/workspaces/1/pages/1/links
Content-Type: application/json
X-XSRF-TOKEN: {{csrfToken}}

{
  "targetPageId": 2,
  "anchorText": "See details"
}
```

## 12. Checklist final trước khi dùng Postman

- [ ] Base URL và environment đã được set đúng.
- [ ] Có cookie jar bật cho session auth.
- [ ] `/api/auth/csrf` được gọi trước các request thay đổi dữ liệu.
- [ ] Header `X-XSRF-TOKEN` được gửi đúng.
- [ ] Request body đúng `camelCase` và JSON format.
- [ ] Response error được xử lý theo format chuẩn `code/message/details`.
- [ ] Mỗi endpoint đã có sample request trong Postman.
- [ ] Test các flow chính: register → login → get me → create workspace → create folder → create page → create link.
