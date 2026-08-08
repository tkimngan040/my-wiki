# MyWiki
MyWiki là hệ thống quản lý tri thức cá nhân, được phát triển nhằm hỗ trợ người dùng lưu trữ, tổ chức và liên kết thông tin một cách hiệu quả. Hệ thống hướng đến việc xây dựng một không gian làm việc linh hoạt, nơi người dùng có thể quản lý ghi chú, tài liệu, ý tưởng và thế giới truyện trong cùng một nền tảng.

## Mục tiêu
- Xây dựng một hệ thống quản lý tri thức cá nhân.
- Hỗ trợ quản lý ghi chú, tài liệu và ý tưởng.
- Hỗ trợ lưu trữ thế giới truyện (Worldbuilding) cho tiểu thuyết.
- Là dự án học tập và nghiên cứu về phát triển Backend.

## Đối tượng sử dụng
- Cá nhân có nhu cầu ghi chú và quản lý tri thức.
- Người viết truyện và xây dựng thế giới truyện.
- Sinh viên và người học muốn tổ chức tài liệu.

## Xây dựng hệ thống
- Ngôn ngữ lập trình: Java
- Framework: Spring Boot
- Cơ sở dữ liệu: SQL Server
- Kiến trúc mã nguồn: mô hình Layered Architecture (Controller – Service – Repository)


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
│   │   │           ├── repository/
│   │   │           ├── entity/
│   │   │           ├── dto/
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

## Prerequisites

- JDK 21
- Maven 3.9+
- SQL Server 2022 
- VS Code hoặc IntelliJ IDEA (khuyến nghị)

## Clone project
```text
https://github.com/tkimngan040/my-wiki.git
```

## Configure database


## Run project

