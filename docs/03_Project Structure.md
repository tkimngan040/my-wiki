# Cấu trúc dự án
- Dự án được xây dựng theo mô hình Controller – Service – Repository.

## Cấu trúc thư mục

```text
my-wiki/
│
├── docs/
│   ├── images_Diagram/
│   ├── 00_Overview.md
│   ├── 01_Requirements.md
│   ├── 02_System Analysis.md
│   └── 03_Project Structure.md
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── mywiki/
│   │   │           ├── config/
│   │   │           ├── controller/
│   │   │           ├── service/
|   |   |           |   ├── impl
|   │   |           |   └── interfaces
│   │   │           ├── repository/
│   │   │           ├── model/
|   |   |           |   ├── entity
|   |   |           |   └── dto
│   │   │           ├── exception/
│   │   │           ├── util/
│   │   │           └── MyWikiApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md
```
## Vai trò của từng thư mục
```text
------------------------------------------------------------------------------------------
| Thư mục      | Vai trò                                                                 |
| ------------ | ----------------------------------------------------------------------- |
|  config      | Chứa cấu hình của ứng dụng (database, CORS, Security...).               |
|  controller  | Nhận request từ frontend/Postman và trả response.                       |
|  service     | Xử lý nghiệp vụ của hệ thống.                                           |
|  repository  | Làm việc trực tiếp với SQL Server.                                      |
|  entity      | Class ánh xạ với các bảng trong database ( Users ,  Pages ...).         |
|  dto         | Object dùng để nhận/trả dữ liệu giữa client và server.                  |
|  exception   | Xử lý lỗi tập trung.                                                    |
|  util        | Các hàm tiện ích dùng chung.                                            |
|  resources   | File cấu hình và tài nguyên của Spring Boot ( application.properties ). |
|  test        | Chứa code kiểm thử (test).                                              |
------------------------------------------------------------------------------------------
```