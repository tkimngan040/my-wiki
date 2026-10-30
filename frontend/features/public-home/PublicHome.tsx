"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  FormEvent,
  useCallback,
  useEffect,
  useRef,
  useState,
  useSyncExternalStore,
} from "react";
import "./public-home.css";

type IconName =
  | "book"
  | "home"
  | "compass"
  | "search"
  | "folder"
  | "globe"
  | "clock"
  | "file"
  | "plus"
  | "tree"
  | "sparkles"
  | "layers"
  | "link"
  | "eye"
  | "chevron"
  | "settings"
  | "sun"
  | "moon"
  | "lightbulb"
  | "help"
  | "question"
  | "mail"
  | "close"
  | "arrow"
  | "sidebar-left"
  | "sidebar-right";

type Workspace = {
  workspaceId: number;
  ownerUsername: string;
  name: string;
  description: string | null;
  visibility: string;
};

type SignedInProfile = {
  userId: number;
  username: string;
  email: string;
  avatarUrl: string | null;
};

type FolderRecord = {
  folderId: number;
  workspaceId: number;
  name: string;
  description: string | null;
};

type PageRecord = {
  pageId: number;
  workspaceId: number;
  title: string;
  content: string | null;
};

type Paginated<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
};

type SearchResult = {
  id: number;
  title: string;
  description: string;
  kind: "Workspace" | "Folder" | "Page";
  workspaceId?: number;
  workspaceName?: string;
};

type HelpTopic = "guide" | "faq" | "support";
type SearchScope = "Tất cả" | "Workspace" | "Folder" | "Page";

const scopes: SearchScope[] = ["Tất cả", "Workspace", "Folder", "Page"];

function subscribeToTheme(onChange: () => void) {
  window.addEventListener("storage", onChange);
  window.addEventListener("mywiki-theme-change", onChange);
  return () => {
    window.removeEventListener("storage", onChange);
    window.removeEventListener("mywiki-theme-change", onChange);
  };
}

function getThemeSnapshot(): "light" | "dark" {
  if (typeof window === "undefined") return "light";
  return window.localStorage.getItem("mywiki-theme") === "dark" ? "dark" : "light";
}

function getServerThemeSnapshot(): "light" {
  return "light";
}

function saveTheme(theme: "light" | "dark") {
  window.localStorage.setItem("mywiki-theme", theme);
  window.dispatchEvent(new Event("mywiki-theme-change"));
}

const featureList = [
  {
    icon: "layers" as const,
    title: "Tạo & quản lý Workspace",
    description: "Mỗi dự án có một không gian riêng.",
  },
  {
    icon: "folder" as const,
    title: "Tổ chức Folder và Page",
    description: "Sắp xếp nội dung theo cách của bạn.",
  },
  {
    icon: "link" as const,
    title: "Liên kết nội dung",
    description: "Kết nối những ý tưởng liên quan.",
  },
  {
    icon: "eye" as const,
    title: "View Mode & Edit Mode",
    description: "Chia sẻ và tiếp tục sáng tạo.",
  },
];

function Icon({
  name,
  size = 18,
}: {
  name: IconName;
  size?: number;
}) {
  const paths: Record<IconName, React.ReactNode> = {
    book: <><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v17H6.5A2.5 2.5 0 0 0 4 22z" /><path d="M4 5.5v14A2.5 2.5 0 0 1 6.5 17H20" /><path d="M8 7h8M8 10h7" /></>,
    home: <><path d="m3 10 9-7 9 7" /><path d="M5 9v11h14V9M9 20v-7h6v7" /></>,
    compass: <><circle cx="12" cy="12" r="9" /><path d="m15.5 8.5-2.2 4.8-4.8 2.2 2.2-4.8z" /></>,
    search: <><circle cx="10.8" cy="10.8" r="6.8" /><path d="m16 16 4.5 4.5" /></>,
    folder: <><path d="M3 6.5A1.5 1.5 0 0 1 4.5 5H10l2 2h7.5A1.5 1.5 0 0 1 21 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5z" /><path d="M3 9h18" /></>,
    globe: <><circle cx="12" cy="12" r="9" /><path d="M3 12h18M12 3a15 15 0 0 1 0 18M12 3a15 15 0 0 0 0 18" /></>,
    clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
    file: <><path d="M6 3h8l5 5v13H6z" /><path d="M14 3v6h5M9 13h7M9 17h7" /></>,
    plus: <><path d="M12 5v14M5 12h14" /></>,
    tree: <><path d="m12 3 3.2 4.5h-2l3.3 4.2h-2.2l3.5 4.3H6.2l3.5-4.3H7.5l3.3-4.2h-2zM12 16v5" /></>,
    sparkles: <><path d="m12 3 1.5 5.5L19 10l-5.5 1.5L12 17l-1.5-5.5L5 10l5.5-1.5zM19 15l.8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8z" /></>,
    layers: <><path d="m12 3 9 5-9 5-9-5zM3 12l9 5 9-5M3 16l9 5 9-5" /></>,
    link: <><path d="M10 13a5 5 0 0 0 7.1 0l2-2A5 5 0 0 0 12 3.9l-1.1 1.1" /><path d="M14 11a5 5 0 0 0-7.1 0l-2 2A5 5 0 0 0 12 20.1l1.1-1.1" /></>,
    eye: <><path d="M2.5 12s3.2-6 9.5-6 9.5 6 9.5 6-3.2 6-9.5 6-9.5-6-9.5-6z" /><circle cx="12" cy="12" r="2.5" /></>,
    chevron: <path d="m9 18 6-6-6-6" />,
    settings: <><circle cx="12" cy="12" r="3" /><path d="m19.4 15 .1.1 1.4 1.1-1.4 2.4-1.7-.7a8 8 0 0 1-1.8 1l-.3 1.8h-2.8l-.3-1.8a8 8 0 0 1-1.8-1l-1.7.7-1.4-2.4 1.4-1.1a7 7 0 0 1 0-2l-1.4-1.1 1.4-2.4 1.7.7a8 8 0 0 1 1.8-1l.3-1.8h2.8l.3 1.8a8 8 0 0 1 1.8 1l1.7-.7 1.4 2.4-1.4 1.1a7 7 0 0 1 0 2z" /></>,
    sun: <><circle cx="12" cy="12" r="4" /><path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" /></>,
    moon: <path d="M20.5 14A8.5 8.5 0 0 1 10 3.5 8.5 8.5 0 1 0 20.5 14z" />,
    lightbulb: <><path d="M9 18h6m-5 3h4m-2-19a7 7 0 0 0-4 12.7c.8.6 1 1.1 1 2.3h6c0-1.2.2-1.7 1-2.3A7 7 0 0 0 12 2z" /></>,
    help: <><circle cx="12" cy="12" r="9" /><path d="M9.5 9a2.6 2.6 0 1 1 4.5 1.8c-1.2 1.2-2 1.5-2 3.2M12 17.5v.1" /></>,
    question: <><circle cx="12" cy="12" r="9" /><path d="M9.5 9a2.6 2.6 0 1 1 4.5 1.8c-1.2 1.2-2 1.5-2 3.2M12 17.5v.1" /></>,
    mail: <><rect x="3" y="5" width="18" height="14" rx="2" /><path d="m4 7 8 6 8-6" /></>,
    close: <><path d="m6 6 12 12M18 6 6 18" /></>,
    arrow: <><path d="M5 12h14M13 6l6 6-6 6" /></>,
    "sidebar-left": <><rect x="3" y="4" width="18" height="16" rx="2" /><path d="M9 4v16" /></>,
    "sidebar-right": <><rect x="3" y="4" width="18" height="16" rx="2" /><path d="M15 4v16" /></>,
  };

  return (
    <svg
      aria-hidden="true"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {paths[name]}
    </svg>
  );
}

async function fetchPage<T>(
  type: "workspaces" | "folders" | "pages",
  page: number,
  workspaceId?: number,
): Promise<Paginated<T>> {
  const params = new URLSearchParams({ type, page: String(page), size: "100" });
  if (workspaceId !== undefined) params.set("workspaceId", String(workspaceId));
  const response = await fetch(`/api/public-content?${params}`, {
    cache: "no-store",
  });
  const data = (await response.json()) as
    | Paginated<T>
    | { message?: string };

  if (!response.ok || !("content" in data)) {
    throw new Error(
      "message" in data
        ? data.message ?? "Không tải được dữ liệu công khai."
        : "Không tải được dữ liệu công khai.",
    );
  }
  return data;
}

async function fetchAll<T>(
  type: "workspaces" | "folders" | "pages",
  workspaceId?: number,
): Promise<T[]> {
  const firstPage = await fetchPage<T>(type, 0, workspaceId);
  const remaining = await Promise.all(
    Array.from({ length: Math.max(0, firstPage.totalPages - 1) }, (_, index) =>
      fetchPage<T>(type, index + 1, workspaceId),
    ),
  );
  return [firstPage, ...remaining].flatMap((page) => page.content);
}

function Landscape() {
  return (
    <svg
      className="hero-landscape"
      viewBox="0 0 1200 390"
      preserveAspectRatio="xMidYMid slice"
      role="img"
      aria-label="Minh họa lâu đài trên núi giữa những tầng mây"
    >
      <defs>
        <linearGradient id="sky" x1="0" x2="0" y1="0" y2="1">
          <stop offset="0" stopColor="#28443e" />
          <stop offset="1" stopColor="#9cae92" />
        </linearGradient>
        <linearGradient id="mountain" x1="0" x2="1">
          <stop offset="0" stopColor="#334e45" />
          <stop offset="1" stopColor="#526d5c" />
        </linearGradient>
        <linearGradient id="castle" x1="0" x2="0" y1="0" y2="1">
          <stop offset="0" stopColor="#dfd5b9" />
          <stop offset="1" stopColor="#998f77" />
        </linearGradient>
      </defs>
      <rect width="1200" height="390" fill="url(#sky)" />
      <circle cx="904" cy="83" r="47" fill="#eee2c2" opacity=".8" />
      <path d="M0 245 170 93l126 122L448 59l209 218 151-155 188 164 93-106 111 97v113H0z" fill="#718575" opacity=".58" />
      <path d="m0 289 178-152 120 118L447 87l181 214 169-169 153 168 104-94 146 117v67H0z" fill="url(#mountain)" />
      <path d="M0 325 215 214l144 103 183-89 149 80 189-112 135 99 104-59 81 49v105H0z" fill="#293e38" />
      <path d="M0 316c143-34 210 20 332-1s229-13 355 10 242 3 513-20v85H0z" fill="#d4d0b9" opacity=".34" />
      <g fill="url(#castle)" stroke="#776f5e" strokeWidth="2">
        <path d="M744 205h188v84H744z" />
        <path d="M772 163h43v42h-43zM862 153h48v52h-48zM824 183h40v106h-40z" />
        <path d="m767 163 26-30 27 30zm88-10 31-37 33 37zm-37 30 26-38 27 38z" />
        <path d="M724 205h25v84h-25zm203-12h25v96h-25z" />
        <path d="m717 205 20-30 20 30zm201-12 21-31 20 31z" />
      </g>
      <g fill="#e8c987" opacity=".9">
        <path d="M789 174h8v13h-8zm90-8h8v14h-8zm-43 31h8v15h-8z" />
      </g>
      <g fill="#e9e3cb" opacity=".3">
        <path d="M-20 240c115-46 180 5 285-19s157-21 260 7v20c-102-20-180-2-277 19s-152-26-268 9z" />
        <path d="M841 254c114-39 177-5 265-29s115 2 133 11v19c-68-15-143-7-223 21s-106-11-175 5z" />
      </g>
    </svg>
  );
}

function getTopic(topic: HelpTopic) {
  if (topic === "guide") {
    return {
      title: "Hướng dẫn sử dụng",
      body: "Tạo tài khoản để bắt đầu một Workspace. Bên trong, bạn có thể tổ chức nội dung bằng Folder và Page, sau đó liên kết các Page liên quan với nhau.",
    };
  }
  if (topic === "faq") {
    return {
      title: "Câu hỏi thường gặp",
      body: "Workspace Public có thể được khách truy cập khám phá. Workspace Private chỉ dành cho chủ sở hữu. Khách chưa đăng nhập chỉ có thể xem và tìm kiếm nội dung công khai.",
    };
  }
  return {
    title: "Liên hệ hỗ trợ",
    body: "Bạn có thể gửi góp ý hoặc báo lỗi qua trang GitHub của dự án MyWiki. Nhóm hỗ trợ sẽ cập nhật kênh liên hệ chính thức khi dự án sẵn sàng.",
    link: "https://github.com/tkimngan040/my-wiki",
  };
}

export default function PublicHome() {
  const router = useRouter();
  const theme = useSyncExternalStore(
    subscribeToTheme,
    getThemeSnapshot,
    getServerThemeSnapshot,
  );
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [workspaceLoading, setWorkspaceLoading] = useState(true);
  const [workspaceError, setWorkspaceError] = useState("");
  const [showAllWorkspaces, setShowAllWorkspaces] = useState(false);
  const [signedInProfile, setSignedInProfile] =
    useState<SignedInProfile | null>(null);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const [accountError, setAccountError] = useState("");
  const [scope, setScope] = useState<SearchScope>("Tất cả");
  const [scopeOpen, setScopeOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState<SearchResult[] | null>(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState("");
  const [selectedWorkspace, setSelectedWorkspace] = useState<Workspace | null>(
    null,
  );
  const [workspaceFolders, setWorkspaceFolders] = useState<FolderRecord[]>([]);
  const [workspacePages, setWorkspacePages] = useState<PageRecord[]>([]);
  const [workspaceDetailsLoading, setWorkspaceDetailsLoading] = useState(false);
  const [workspaceDetailsError, setWorkspaceDetailsError] = useState("");
  const [helpTopic, setHelpTopic] = useState<HelpTopic | null>(null);
  const [showEmptyMenu, setShowEmptyMenu] = useState(false);
  const searchInput = useRef<HTMLInputElement>(null);
  const searchSection = useRef<HTMLElement>(null);
  const publicSection = useRef<HTMLElement>(null);
  const workspaceRequestId = useRef(0);

  const loadWorkspaces = useCallback(async () => {
    setWorkspaceLoading(true);
    setWorkspaceError("");
    try {
      setWorkspaces(await fetchAll<Workspace>("workspaces"));
    } catch (error) {
      setWorkspaceError(
        error instanceof Error ? error.message : "Không tải được Workspace.",
      );
    } finally {
      setWorkspaceLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;
    fetchAll<Workspace>("workspaces")
      .then((items) => {
        if (active) setWorkspaces(items);
      })
      .catch((error: unknown) => {
        if (active) {
          setWorkspaceError(
            error instanceof Error ? error.message : "Không tải được Workspace.",
          );
        }
      })
      .finally(() => {
        if (active) setWorkspaceLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    async function loadSignedInProfile() {
      try {
        const sessionResponse = await fetch("/api/account/session", {
          cache: "no-store",
        });
        if (!sessionResponse.ok) return;

        const profileResponse = await fetch("/api/account/profile", {
          cache: "no-store",
        });
        if (!profileResponse.ok) return;

        const profile = (await profileResponse.json()) as SignedInProfile;
        if (active) setSignedInProfile(profile);
      } catch {
        if (active) setAccountError("Không thể tải thông tin tài khoản.");
      }
    }

    void loadSignedInProfile();
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
  }, [theme]);

  async function openWorkspace(workspace: Workspace) {
    const requestId = ++workspaceRequestId.current;
    setSelectedWorkspace(workspace);
    setWorkspaceFolders([]);
    setWorkspacePages([]);
    setWorkspaceDetailsLoading(true);
    setWorkspaceDetailsError("");

    try {
      const [folders, pages] = await Promise.all([
        fetchAll<FolderRecord>("folders", workspace.workspaceId),
        fetchAll<PageRecord>("pages", workspace.workspaceId),
      ]);
      if (requestId === workspaceRequestId.current) {
        setWorkspaceFolders(folders);
        setWorkspacePages(pages);
      }
    } catch (error) {
      if (requestId === workspaceRequestId.current) {
        setWorkspaceDetailsError(
          error instanceof Error
            ? error.message
            : "Không tải được nội dung Workspace công khai.",
        );
      }
    } finally {
      if (requestId === workspaceRequestId.current) {
        setWorkspaceDetailsLoading(false);
      }
    }
  }

  function focusSearch() {
    searchSection.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    searchInput.current?.focus({ preventScroll: true });
  }

  function requireLogin() {
    if (signedInProfile) {
      router.push("/my-workspaces");
      return;
    }
    setShowEmptyMenu(true);
  }

  async function handleLogout() {
    setAccountError("");
    try {
      const response = await fetch("/api/auth/logout", { method: "POST" });
      if (!response.ok) {
        setAccountError("Đăng xuất chưa thành công. Vui lòng thử lại.");
        return;
      }
      setSignedInProfile(null);
      setProfileMenuOpen(false);
    } catch {
      setAccountError("Không thể kết nối để đăng xuất. Vui lòng thử lại.");
    }
  }

  async function runSearch() {
    const term = query.trim();
    if (!term) {
      searchInput.current?.focus();
      return;
    }

    setSearchLoading(true);
    setSearchError("");
    setSearchResults(null);

    try {
      const allWorkspaces = await fetchAll<Workspace>("workspaces");
      setWorkspaces(allWorkspaces);
      const normalizedTerm = term.toLocaleLowerCase("vi");
      const results: SearchResult[] = [];

      if (scope === "Tất cả" || scope === "Workspace") {
        results.push(
          ...allWorkspaces
            .filter(
              (workspace) =>
                workspace.name.toLocaleLowerCase("vi").includes(normalizedTerm) ||
                (workspace.description ?? "")
                  .toLocaleLowerCase("vi")
                  .includes(normalizedTerm),
            )
            .map((workspace) => ({
              id: workspace.workspaceId,
              title: workspace.name,
              description: workspace.description ?? "Chưa có mô tả.",
              kind: "Workspace" as const,
            })),
        );
      }

      if (scope === "Tất cả" || scope === "Folder" || scope === "Page") {
        for (let offset = 0; offset < allWorkspaces.length; offset += 8) {
          const batch = allWorkspaces.slice(offset, offset + 8);
          const nestedResults = await Promise.all(
            batch.map(async (workspace) => {
              const found: SearchResult[] = [];
              if (scope !== "Page") {
                const folders = await fetchAll<FolderRecord>(
                  "folders",
                  workspace.workspaceId,
                );
                found.push(
                  ...folders
                    .filter(
                      (folder) =>
                        folder.name.toLocaleLowerCase("vi").includes(normalizedTerm) ||
                        (folder.description ?? "")
                          .toLocaleLowerCase("vi")
                          .includes(normalizedTerm),
                    )
                    .map((folder) => ({
                      id: folder.folderId,
                      title: folder.name,
                      description: folder.description ?? "Folder công khai.",
                      kind: "Folder" as const,
                      workspaceId: workspace.workspaceId,
                      workspaceName: workspace.name,
                    })),
                );
              }
              if (scope !== "Folder") {
                const pages = await fetchAll<PageRecord>(
                  "pages",
                  workspace.workspaceId,
                );
                found.push(
                  ...pages
                    .filter(
                      (page) =>
                        page.title.toLocaleLowerCase("vi").includes(normalizedTerm) ||
                        (page.content ?? "")
                          .toLocaleLowerCase("vi")
                          .includes(normalizedTerm),
                    )
                    .map((page) => ({
                      id: page.pageId,
                      title: page.title,
                      description: (page.content ?? "Page công khai.")
                        .replace(/\s+/g, " ")
                        .slice(0, 180),
                      kind: "Page" as const,
                      workspaceId: workspace.workspaceId,
                      workspaceName: workspace.name,
                    })),
                );
              }
              return found;
            }),
          );
          results.push(...nestedResults.flat());
        }
      }

      setSearchResults(results);
    } catch (error) {
      setSearchError(
        error instanceof Error
          ? error.message
          : "Không thể tìm kiếm dữ liệu công khai.",
      );
    } finally {
      setSearchLoading(false);
    }
  }

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await runSearch();
  }

  function closeDialogs() {
    workspaceRequestId.current += 1;
    setSelectedWorkspace(null);
    setHelpTopic(null);
    setShowEmptyMenu(false);
    setWorkspaceDetailsError("");
  }

  const groupedResults = searchResults
    ? scopes
        .filter((item) => item !== "Tất cả")
        .map((kind) => ({
          kind,
          results: searchResults.filter((result) => result.kind === kind),
        }))
        .filter((group) => group.results.length > 0)
    : [];

  const searchPanel = (
    <section className="search-section" ref={searchSection} aria-label="Tìm kiếm công khai">
      <form className="search-form" role="search" onSubmit={handleSearch}>
        <label className="search-input-wrap">
          <Icon name="search" size={19} />
          <input
            ref={searchInput}
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Tìm kiếm Workspace, Folder, Page..."
            aria-label="Tìm kiếm Workspace, Folder, Page"
          />
          {query && (
            <button
              type="button"
              className="clear-search"
              aria-label="Xóa nội dung tìm kiếm"
              onClick={() => {
                setQuery("");
                setSearchResults(null);
                setSearchError("");
              }}
            >
              <Icon name="close" size={15} />
            </button>
          )}
        </label>
        <div className="search-scope-wrap">
          <button
            type="button"
            className="search-scope"
            aria-expanded={scopeOpen}
            aria-haspopup="listbox"
            onClick={() => setScopeOpen((open) => !open)}
          >
            {scope}<span className="scope-chevron">⌄</span>
          </button>
          {scopeOpen && (
            <div className="scope-menu" role="listbox" aria-label="Phạm vi tìm kiếm">
              {scopes.map((item) => (
                <button
                  type="button"
                  role="option"
                  aria-selected={scope === item}
                  className={scope === item ? "scope-option selected" : "scope-option"}
                  key={item}
                  onClick={() => {
                    setScope(item);
                    setScopeOpen(false);
                  }}
                >
                  {item}
                </button>
              ))}
            </div>
          )}
        </div>
        <button className="search-submit" type="submit" aria-label="Tìm kiếm" disabled={searchLoading}>
          <Icon name="search" size={19} />
        </button>
      </form>

      {searchLoading && (
        <div className="search-feedback" role="status">Đang tìm trong các Workspace công khai...</div>
      )}
      {searchError && (
        <div className="search-feedback error-feedback" role="alert">
          {searchError}
          <button type="button" onClick={() => void runSearch()}>
            Thử lại
          </button>
        </div>
      )}
      {searchResults && !searchLoading && (
        <section className="search-results-panel" aria-live="polite">
          <div className="results-heading">
            <div>
              <span className="section-kicker">KẾT QUẢ TÌM KIẾM</span>
              <h2>“{query.trim()}”</h2>
            </div>
            <button type="button" className="close-results" onClick={() => setSearchResults(null)}>
              Đóng <Icon name="close" size={15} />
            </button>
          </div>
          {searchResults.length === 0 ? (
            <div className="no-results">
              <span className="empty-icon"><Icon name="search" size={22} /></span>
              <strong>Không tìm thấy dữ liệu phù hợp</strong>
              <p>Thử từ khóa khác hoặc chọn phạm vi tìm kiếm khác nhé.</p>
            </div>
          ) : (
            <div className="search-result-groups">
              {groupedResults.map((group) => (
                <section className="result-group" key={group.kind}>
                  <h3>{group.kind}<span>{group.results.length}</span></h3>
                  {group.results.map((result) => (
                    <button
                      className="result-row"
                      type="button"
                      key={`${result.kind}-${result.id}-${result.workspaceId ?? ""}`}
                      onClick={() => {
                        const workspace = workspaces.find(
                          (item) =>
                            item.workspaceId ===
                            (result.kind === "Workspace"
                              ? result.id
                              : result.workspaceId),
                        );
                        if (workspace) void openWorkspace(workspace);
                      }}
                    >
                      <span className="result-icon"><Icon name={result.kind === "Workspace" ? "globe" : result.kind === "Folder" ? "folder" : "file"} size={17} /></span>
                      <span className="result-copy">
                        <strong>{result.title}</strong>
                        <small>{result.workspaceName ? `${result.workspaceName} · ` : ""}{result.description}</small>
                      </span>
                      <Icon name="chevron" size={16} />
                    </button>
                  ))}
                </section>
              ))}
            </div>
          )}
        </section>
      )}
    </section>
  );

  return (
    <div className={`public-home guest-home${signedInProfile ? " signed-in-home" : ""}`} data-theme={theme}>
      <aside className="left-sidebar">
        <Link className="app-logo" href="/" aria-label="MyWiki trang chủ">
          <span className="logo-icon"><Icon name="book" size={20} /></span>
          <span>MyWiki</span>
        </Link>

        <nav className="navigation-menu" aria-label="Điều hướng chính">
          <button className="nav-item active" type="button" aria-current="page">
            <Icon name="home" /> <span>Trang chủ</span>
          </button>
          <button
            className="nav-item"
            type="button"
            onClick={() =>
              publicSection.current?.scrollIntoView({ behavior: "smooth" })
            }
          >
            <Icon name="compass" /> <span>Khám phá</span>
          </button>
          <button className="nav-item" type="button" onClick={focusSearch}>
            <Icon name="search" /> <span>Tìm kiếm</span>
          </button>
          <button
            className="nav-item theme-nav-item"
            type="button"
            aria-label={theme === "light" ? "Chuyển sang chế độ tối" : "Chuyển sang chế độ sáng"}
            onClick={() => saveTheme(theme === "light" ? "dark" : "light")}
          >
            <Icon name={theme === "light" ? "moon" : "sun"} />
            <span>{theme === "light" ? "Chế độ tối" : "Chế độ sáng"}</span>
          </button>

          <div className="nav-group">
            <div className="nav-group-title">
              <span>Workspace</span>
              {!signedInProfile && (
                <button
                  type="button"
                  className="icon-button add-workspace"
                  aria-label="Tạo Workspace (yêu cầu đăng nhập)"
                  onClick={requireLogin}
                >
                  <Icon name="plus" size={16} />
                </button>
              )}
            </div>
            <button className="nav-item nav-subitem" type="button" onClick={requireLogin}>
              <Icon name="layers" /> <span>{signedInProfile ? "Workspace của tôi" : "Tất cả Workspace"}</span>
            </button>
            <button
              className="nav-item nav-subitem"
              type="button"
              onClick={() =>
                publicSection.current?.scrollIntoView({ behavior: "smooth" })
              }
            >
              <Icon name="globe" /> <span>Workspace công khai</span>
            </button>
          </div>

          {signedInProfile && (
            <div className="nav-group recent-group">
              <div className="nav-group-title"><span>Gần đây</span></div>
              <button className="nav-item nav-subitem" type="button" onClick={requireLogin}>
                <Icon name="file" /> <span>Page gần đây</span>
              </button>
              <button className="nav-item nav-subitem" type="button" onClick={requireLogin}>
                <Icon name="folder" /> <span>Folder gần đây</span>
              </button>
              <button className="nav-item nav-subitem" type="button" onClick={requireLogin}>
                <Icon name="clock" /> <span>Workspace gần đây</span>
              </button>
            </div>
          )}
        </nav>

        <div className="sidebar-bottom">
          <div className="sidebar-footer">
            <span className="footer-logo-icon"><Icon name="book" size={15} /></span>
            <span><strong>MyWiki</strong><small>Your knowledge, your world</small></span>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="home-topbar guest-topbar">
          {searchPanel}
          {signedInProfile ? (
            <div className="account-menu-wrap">
              <button
                className="account-profile-button"
                type="button"
                aria-expanded={profileMenuOpen}
                aria-haspopup="true"
                onClick={() => setProfileMenuOpen((open) => !open)}
              >
                <span className="account-avatar">
                  {signedInProfile.avatarUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={signedInProfile.avatarUrl} alt="" />
                  ) : (
                    signedInProfile.username.slice(0, 1).toLocaleUpperCase("vi")
                  )}
                </span>
                <span className="account-profile-name">{signedInProfile.username}</span>
              </button>
              {profileMenuOpen && (
                <div className="account-profile-menu">
                  {accountError && <p className="account-menu-error" role="alert">{accountError}</p>}
                  <button
                    className="account-menu-item"
                    type="button"
                    onClick={() => void handleLogout()}
                  >
                    <Icon name="close" size={16} />
                    Đăng xuất
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="guest-auth-actions">
              <Link className="guest-login-button" href="/login">Đăng nhập</Link>
              <Link className="guest-register-button" href="/register">Đăng ký</Link>
            </div>
          )}
        </header>

        <section className="hero-banner">
          <Landscape />
          <div className="hero-shade" />
          <div className="hero-content">
            <span className="hero-badge"><Icon name="sparkles" size={14} /> KHÔNG GIAN TRI THỨC CỦA BẠN</span>
            <h1>Khám phá tri thức, kết nối ý tưởng</h1>
            <p>MyWiki là không gian lưu trữ và tổ chức tri thức cá nhân của bạn.</p>
            <div className="hero-tags">
              <span><Icon name="lightbulb" size={14} /> Lưu trữ ý tưởng</span>
              <span><Icon name="file" size={14} /> Quản lý tài liệu</span>
              <span><Icon name="sparkles" size={14} /> Xây dựng thế giới truyện</span>
              <span><Icon name="link" size={14} /> Kết nối tri thức</span>
            </div>
          </div>
          <span className="hero-caption">Mỗi ý tưởng đều có thể trở thành một thế giới.</span>
        </section>

        <section className="public-workspaces" id="public-workspaces" ref={publicSection}>
          <div className="section-heading">
            <div className="section-heading-copy">
              <div className="section-title-row">
                <span className="section-icon"><Icon name="globe" size={19} /></span>
                <h2>Workspace công khai</h2>
              </div>
            </div>
            <button
              type="button"
              className="view-all-link"
              onClick={() => {
                if (workspaceError) {
                  void loadWorkspaces();
                } else {
                  setShowAllWorkspaces((current) => !current);
                }
              }}
            >
              {showAllWorkspaces ? "Thu gọn" : "Xem tất cả"} <Icon name="arrow" size={16} />
            </button>
          </div>

          {workspaceLoading ? (
            <div className="workspace-empty loading-empty" role="status">
              <span className="loading-spinner" />
              <strong>Đang tải Workspace công khai...</strong>
            </div>
          ) : workspaceError ? (
            <div className="workspace-empty">
              <span className="empty-icon"><Icon name="globe" size={23} /></span>
              <strong>Chưa tải được Workspace công khai</strong>
              <p>{workspaceError}</p>
              <button className="retry-button" type="button" onClick={() => void loadWorkspaces()}>Thử tải lại</button>
            </div>
          ) : workspaces.length === 0 ? (
            <div className="workspace-empty">
              <span className="empty-icon"><Icon name="globe" size={23} /></span>
              <strong>Chưa có Workspace công khai</strong>
              <p>Khi cộng đồng chia sẻ không gian tri thức, chúng sẽ xuất hiện tại đây.</p>
            </div>
          ) : (
            <div className="workspace-grid">
              {workspaces.slice(0, showAllWorkspaces ? undefined : 6).map((workspace, index) => (
                <button
                  className={`workspace-card workspace-card-${index % 4}`}
                  key={workspace.workspaceId}
                  type="button"
                  onClick={() => void openWorkspace(workspace)}
                >
                  <span className="workspace-card-art"><Icon name={index % 2 ? "sparkles" : "book"} size={25} /></span>
                  <span className="workspace-card-body">
                    <span className="workspace-card-title">{workspace.name}</span>
                    <span className="workspace-card-description">{workspace.description || "Chưa có mô tả cho Workspace này."}</span>
                    <span className="workspace-card-owner"><span className="owner-dot" /> {workspace.ownerUsername}</span>
                  </span>
                  <span className="workspace-card-arrow"><Icon name="arrow" size={15} /></span>
                </button>
              ))}
            </div>
          )}
        </section>

        <section className={`bottom-cta${signedInProfile ? "" : " guest-bottom-cta"}`}>
          <span className="bottom-cta-icon"><Icon name="lightbulb" size={21} /></span>
          <div className="bottom-cta-copy">
            <h2>{signedInProfile ? `Chào ${signedInProfile.username}, tiếp tục sáng tạo nhé!` : "Bạn muốn tạo Workspace riêng?"}</h2>
            <p>{signedInProfile ? "Mở không gian làm việc để tiếp tục quản lý các Workspace của bạn." : "Đăng ký hoặc đăng nhập để tạo và quản lý Workspace của riêng bạn."}</p>
          </div>
          <div className="bottom-cta-actions">
            {signedInProfile ? (
              <Link className="cta-primary-button" href="/my-workspaces">Workspace của tôi <Icon name="arrow" size={15} /></Link>
            ) : (
              <>
                <Link className="cta-outline-button" href="/register">Đăng ký</Link>
                <Link className="cta-primary-button" href="/login">Đăng nhập <Icon name="arrow" size={15} /></Link>
              </>
            )}
          </div>
        </section>
        <footer className="main-footer">© MyWiki <span>·</span> Your knowledge, your world</footer>
      </main>

      <aside className="right-sidebar">
        {signedInProfile ? (
          <section className="welcome-card signed-in-welcome">
            <div className="signed-in-avatar-large">
              {signedInProfile.avatarUrl ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={signedInProfile.avatarUrl} alt="" />
              ) : signedInProfile.username.slice(0, 1).toLocaleUpperCase("vi")}
            </div>
            <h2>Chào {signedInProfile.username}!</h2>
            <p>Bạn đang khám phá MyWiki với tài khoản của mình.</p>
          </section>
        ) : (
        <section className="welcome-card">
          <div className="welcome-illustration">
            <span className="welcome-sun" />
            <Icon name="book" size={35} />
            <span className="welcome-sparkle sparkle-one">✦</span>
            <span className="welcome-sparkle sparkle-two">✧</span>
          </div>
          <h2>Chào bạn!</h2>
          <p>Đăng nhập để sử dụng đầy đủ các tính năng của MyWiki.</p>
          <Link className="side-button primary-side-button" href="/login">Đăng nhập</Link>
          <Link className="side-button secondary-side-button" href="/register">Đăng ký</Link>
        </section>
        )}

        <section className="utility-section features-utility">
          <h2 className="utility-heading"><Icon name="sparkles" size={17} /> Tính năng nổi bật</h2>
          <div className="utility-features">
            {featureList.map((feature) => (
              <div className="utility-feature" key={feature.title}>
                <span className="utility-feature-icon"><Icon name={feature.icon} size={16} /></span>
                <span><strong>{feature.title}</strong><small>{feature.description}</small></span>
              </div>
            ))}
          </div>
        </section>

        <section className="utility-section help-utility">
          <h2 className="utility-heading"><Icon name="settings" size={17} /> Trợ giúp</h2>
          <div className="help-links">
            {([
              ["guide", "Hướng dẫn sử dụng", "help"],
              ["faq", "Câu hỏi thường gặp", "question"],
              ["support", "Liên hệ hỗ trợ", "mail"],
            ] as const).map(([topic, label, icon]) => (
              <button className="help-link" type="button" key={topic} onClick={() => setHelpTopic(topic)}>
                <Icon name={icon} size={16} />
                <span>{label}</span>
                <Icon name="chevron" size={15} />
              </button>
            ))}
          </div>
        </section>

        {signedInProfile && (
          <div className="right-sidebar-account-actions">
            {accountError && <p className="account-inline-error" role="alert">{accountError}</p>}
            <button className="right-sidebar-logout" type="button" onClick={() => void handleLogout()}>
              <Icon name="close" size={15} /> Đăng xuất
            </button>
          </div>
        )}
      </aside>

      {(selectedWorkspace || helpTopic || showEmptyMenu) && (
        <div className="dialog-backdrop" onMouseDown={(event) => {
          if (event.target === event.currentTarget) closeDialogs();
        }}>
          <section className="information-dialog" role="dialog" aria-modal="true" aria-labelledby="dialog-title">
            <button className="dialog-close" type="button" aria-label="Đóng" onClick={closeDialogs}>
              <Icon name="close" size={18} />
            </button>
            {selectedWorkspace ? (
              <>
                <span className="dialog-icon"><Icon name="globe" size={22} /></span>
                <span className="dialog-kicker">WORKSPACE CÔNG KHAI</span>
                <h2 id="dialog-title">{selectedWorkspace.name}</h2>
                <p>{selectedWorkspace.description || "Workspace này chưa có mô tả."}</p>
                <div className="workspace-dialog-meta"><Icon name="eye" size={15} /> Chế độ xem · Tạo bởi {selectedWorkspace.ownerUsername}</div>
                {workspaceDetailsLoading ? (
                  <div className="workspace-details-status" role="status"><span className="loading-spinner" /> Đang tải nội dung công khai...</div>
                ) : workspaceDetailsError ? (
                  <div className="workspace-details-status workspace-details-error" role="alert">
                    {workspaceDetailsError}
                    <button type="button" onClick={() => {
                      void openWorkspace(selectedWorkspace);
                    }}>Thử lại</button>
                  </div>
                ) : (
                  <div className="workspace-view-content">
                    <h3><Icon name="folder" size={15} /> Folder <span>{workspaceFolders.length}</span></h3>
                    {workspaceFolders.length ? workspaceFolders.map((folder) => (
                      <article className="workspace-view-item" key={folder.folderId}>
                        <strong>{folder.name}</strong>
                        <p>{folder.description || "Chưa có mô tả."}</p>
                      </article>
                    )) : <p className="workspace-view-empty">Workspace chưa có Folder công khai.</p>}
                    <h3><Icon name="file" size={15} /> Page <span>{workspacePages.length}</span></h3>
                    {workspacePages.length ? workspacePages.map((page) => (
                      <article className="workspace-view-item" key={page.pageId}>
                        <strong>{page.title}</strong>
                        <p>{page.content || "Page chưa có nội dung."}</p>
                      </article>
                    )) : <p className="workspace-view-empty">Workspace chưa có Page công khai.</p>}
                  </div>
                )}
              </>
            ) : helpTopic ? (
              <>
                <span className="dialog-icon"><Icon name={helpTopic === "guide" ? "help" : helpTopic === "faq" ? "question" : "mail"} size={22} /></span>
                <span className="dialog-kicker">TRUNG TÂM TRỢ GIÚP</span>
                <h2 id="dialog-title">{getTopic(helpTopic).title}</h2>
                <p>{getTopic(helpTopic).body}</p>
                {getTopic(helpTopic).link && (
                  <a className="dialog-primary" href={getTopic(helpTopic).link} target="_blank" rel="noreferrer">
                    Mở GitHub MyWiki <Icon name="arrow" size={15} />
                  </a>
                )}
              </>
            ) : (
              <>
                <span className="dialog-icon"><Icon name="tree" size={22} /></span>
                <span className="dialog-kicker">TÀI KHOẢN MYWIKI</span>
                <h2 id="dialog-title">Đăng nhập để tiếp tục</h2>
                <p>Đăng nhập hoặc tạo tài khoản để quản lý Workspace, Page và Folder của riêng bạn.</p>
                <div className="dialog-actions">
                  <Link className="dialog-primary" href="/login">Đăng nhập <Icon name="arrow" size={15} /></Link>
                  <Link className="dialog-secondary" href="/register">Đăng ký</Link>
                </div>
              </>
            )}
          </section>
        </div>
      )}
    </div>
  );
}
