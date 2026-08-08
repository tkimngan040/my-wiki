USE MyWiki;
GO

/*==========================================================
DROP TABLE
==========================================================*/

IF OBJECT_ID('Links','U') IS NOT NULL DROP TABLE Links;
IF OBJECT_ID('Pages','U') IS NOT NULL DROP TABLE Pages;
IF OBJECT_ID('Folders','U') IS NOT NULL DROP TABLE Folders;
IF OBJECT_ID('Workspaces','U') IS NOT NULL DROP TABLE Workspaces;
IF OBJECT_ID('Users','U') IS NOT NULL DROP TABLE Users;
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

    CONSTRAINT FK_Folders_Workspaces
        FOREIGN KEY(WorkspaceId)
        REFERENCES Workspaces(WorkspaceId)
        ON DELETE CASCADE,

    CONSTRAINT FK_Folders_Parent
        FOREIGN KEY(ParentFolderId)
        REFERENCES Folders(FolderId),

    CONSTRAINT UQ_Folders_Name
        UNIQUE(WorkspaceId, ParentFolderId, Name)
);
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

    CONSTRAINT FK_Pages_Folders
        FOREIGN KEY(FolderId)
        REFERENCES Folders(FolderId),

    CONSTRAINT UQ_Pages_Title
        UNIQUE(FolderId, Title)
);
GO

/*==========================================================
Links
==========================================================*/

CREATE TABLE Links
(
    LinkId INT IDENTITY(1,1),

    SourcePageId INT NOT NULL,

    TargetPageId INT NOT NULL,

    AnchorText NVARCHAR(200) NOT NULL,

    CreatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_Links_CreatedAt
        DEFAULT GETDATE(),

    CONSTRAINT PK_Links
        PRIMARY KEY(LinkId),

    CONSTRAINT FK_Links_SourcePage
        FOREIGN KEY(SourcePageId)
        REFERENCES Pages(PageId)
        ON DELETE CASCADE,

    CONSTRAINT FK_Links_TargetPage
        FOREIGN KEY(TargetPageId)
        REFERENCES Pages(PageId)
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