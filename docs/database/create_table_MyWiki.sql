/*
NOTE:
This file only records SQL commands that have been executed manually on SQL Server.
Changes to this file do not automatically affect the MyWiki application.
If database data needs to be modified, add the commands at the end of this file
and notify the developer so the changes can be executed manually on SQL Server.
*/

USE master;
GO

IF DB_ID(N'MyWiki') IS NULL
    CREATE DATABASE MyWiki;
GO

USE MyWiki;
GO

SET NOCOUNT ON;
GO

/*==========================================================
Users
==========================================================*/

CREATE TABLE Users
(
    UserId INT IDENTITY(1,1),

    Username NVARCHAR(50) NOT NULL,

    Email VARCHAR(255) NOT NULL,

    PasswordHash VARCHAR(255) NOT NULL,

    AvatarUrl VARCHAR(500) NULL,

    DateOfBirth DATE NULL,

    Bio VARCHAR(500) NULL,

    CreatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Users_CreatedAt
        DEFAULT GETDATE(),

    UpdatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Users_UpdatedAt
        DEFAULT GETDATE(),

    CONSTRAINT PK_Users
        PRIMARY KEY(UserId),

    CONSTRAINT UQ_Users_Email
        UNIQUE(Email)
);
GO

/*==========================================================
Workspaces
==========================================================*/

CREATE TABLE Workspaces
(
    WorkspaceId INT IDENTITY(1,1),

    OwnerId INT NOT NULL,

    Name NVARCHAR(100) NOT NULL,

    Description NVARCHAR(500),

    Visibility VARCHAR(10) NOT NULL,

    CreatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Workspaces_CreatedAt
        DEFAULT GETDATE(),

    UpdatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Workspaces_UpdatedAt
        DEFAULT GETDATE(),

    CONSTRAINT PK_Workspaces
        PRIMARY KEY(WorkspaceId),

    CONSTRAINT FK_Workspaces_Users
        FOREIGN KEY(OwnerId)
        REFERENCES Users(UserId),

    CONSTRAINT UQ_Workspaces_Owner_Name
        UNIQUE(OwnerId, Name),

    CONSTRAINT CK_Workspaces_Visibility
        CHECK (Visibility IN ('Public','Private'))
);
GO

/*==========================================================
Folders
==========================================================*/

CREATE TABLE Folders
(
    FolderId INT IDENTITY(1,1),

    WorkspaceId INT NOT NULL,

    ParentFolderId INT NULL,

    Name NVARCHAR(100) NOT NULL,

    Description NVARCHAR(500),

    CreatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Folders_CreatedAt
        DEFAULT GETDATE(),

    UpdatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Folders_UpdatedAt
        DEFAULT GETDATE(),

    CONSTRAINT PK_Folders
        PRIMARY KEY(FolderId),

    CONSTRAINT UQ_Folders_Workspace_Folder
        UNIQUE(WorkspaceId, FolderId),

    CONSTRAINT FK_Folders_Workspaces
        FOREIGN KEY(WorkspaceId)
        REFERENCES Workspaces(WorkspaceId)
        ON DELETE CASCADE,

    CONSTRAINT FK_Folders_Parent
        FOREIGN KEY(WorkspaceId, ParentFolderId)
        REFERENCES Folders(WorkspaceId, FolderId)
);
GO

CREATE UNIQUE INDEX UX_Folders_Root_Name
ON Folders(WorkspaceId, Name)
WHERE ParentFolderId IS NULL;

CREATE UNIQUE INDEX UX_Folders_Child_Name
ON Folders(WorkspaceId, ParentFolderId, Name)
WHERE ParentFolderId IS NOT NULL;
GO

/*==========================================================
Pages
==========================================================*/

CREATE TABLE Pages
(
    PageId INT IDENTITY(1,1),

	WorkspaceId INT NOT NULL,

	FolderId INT NULL,

    Title NVARCHAR(200) NOT NULL,

    Content NVARCHAR(MAX),

    CreatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Pages_CreatedAt
        DEFAULT GETDATE(),

    UpdatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Pages_UpdatedAt
        DEFAULT GETDATE(),

    CONSTRAINT PK_Pages
        PRIMARY KEY(PageId),

	CONSTRAINT FK_Pages_Workspaces
		FOREIGN KEY(WorkspaceId)
		REFERENCES Workspaces(WorkspaceId)
		ON DELETE CASCADE,

    CONSTRAINT UQ_Pages_Workspace_Page
        UNIQUE(WorkspaceId, PageId),

    CONSTRAINT FK_Pages_Folders
        FOREIGN KEY(WorkspaceId, FolderId)
        REFERENCES Folders(WorkspaceId, FolderId)
);
GO

CREATE UNIQUE INDEX UX_Pages_Root_Title
ON Pages(WorkspaceId, Title)
WHERE FolderId IS NULL;

CREATE UNIQUE INDEX UX_Pages_Folder_Title
ON Pages(WorkspaceId, FolderId, Title)
WHERE FolderId IS NOT NULL;
GO

/*==========================================================
Links
==========================================================*/

CREATE TABLE Links
(
    LinkId INT IDENTITY(1,1),

    WorkspaceId INT NOT NULL,

    SourcePageId INT NOT NULL,

    TargetPageId INT NOT NULL,

    AnchorText NVARCHAR(200) NOT NULL,

    CreatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Links_CreatedAt
        DEFAULT GETDATE(),

    CONSTRAINT PK_Links
        PRIMARY KEY(LinkId),

    CONSTRAINT FK_Links_Workspace
        FOREIGN KEY(WorkspaceId)
        REFERENCES Workspaces(WorkspaceId),

    CONSTRAINT FK_Links_SourcePage
        FOREIGN KEY(WorkspaceId, SourcePageId)
        REFERENCES Pages(WorkspaceId, PageId),

    CONSTRAINT FK_Links_TargetPage
        FOREIGN KEY(WorkspaceId, TargetPageId)
        REFERENCES Pages(WorkspaceId, PageId)
);
GO

/*==========================================================
INDEX
==========================================================*/

CREATE INDEX IX_Workspaces_OwnerId
ON Workspaces(OwnerId);

CREATE INDEX IX_Folders_WorkspaceId
ON Folders(WorkspaceId);

CREATE INDEX IX_Folders_ParentFolderId
ON Folders(ParentFolderId);

CREATE INDEX IX_Pages_WorkspaceId
ON Pages(WorkspaceId);

CREATE INDEX IX_Pages_FolderId
ON Pages(FolderId);

CREATE INDEX IX_Links_SourcePageId
ON Links(SourcePageId);

CREATE INDEX IX_Links_TargetPageId
ON Links(TargetPageId);
GO

/*==========================================================
RESET DATABASE
-- WARNING: This script deletes all MyWiki data.
-- Use only for local development or database reset.
==========================================================*/

IF OBJECT_ID(N'dbo.Links', N'U') IS NOT NULL DROP TABLE dbo.Links;
IF OBJECT_ID(N'dbo.Pages', N'U') IS NOT NULL DROP TABLE dbo.Pages;
IF OBJECT_ID(N'dbo.Folders', N'U') IS NOT NULL DROP TABLE dbo.Folders;
IF OBJECT_ID(N'dbo.Workspaces', N'U') IS NOT NULL DROP TABLE dbo.Workspaces;
IF OBJECT_ID(N'dbo.Users', N'U') IS NOT NULL DROP TABLE dbo.Users;
GO