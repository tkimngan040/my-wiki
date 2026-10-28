"use client";

import Link from "next/link";
import {
  useEffect,
  useMemo,
  useState,
  useSyncExternalStore,
} from "react";
import { useRouter } from "next/navigation";
import "./account-dashboard.css";

type Profile = {
  userId: number;
  username: string;
  email: string;
  avatarUrl: string | null;
  bio: string | null;
};

type Workspace = {
  workspaceId: number;
  ownerUsername: string;
  name: string;
  description: string | null;
  visibility: string;
  updatedAt: string;
};

type WorkspaceResponse = {
  content: Workspace[];
  totalElements: number;
  totalPages: number;
};

type Filter = "Tất cả" | "Cá nhân" | "Công khai";

const filters: Filter[] = ["Tất cả", "Cá nhân", "Công khai"];

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

function subscribeToSidebarState(onChange: () => void) {
  window.addEventListener("storage", onChange);
  window.addEventListener("mywiki-sidebar-change", onChange);
  return () => {
    window.removeEventListener("storage", onChange);
    window.removeEventListener("mywiki-sidebar-change", onChange);
  };
}

function getSidebarSnapshot(side: "left" | "right"): "true" | "false" {
  if (typeof window === "undefined") return "false";
  return window.localStorage.getItem(`mywiki-${side}-sidebar-collapsed`) === "true"
    ? "true"
    : "false";
}

function getServerSidebarSnapshot(): "false" {
  return "false";
}

function saveSidebarState(side: "left" | "right", collapsed: boolean) {
  window.localStorage.setItem(`mywiki-${side}-sidebar-collapsed`, String(collapsed));
  window.dispatchEvent(new Event("mywiki-sidebar-change"));
}

function DashboardIcon({
  name,
  size = 18,
}: {
  name: "book" | "home" | "search" | "layers" | "globe" | "plus" | "clock" | "folder" | "file" | "settings" | "help" | "sun" | "moon" | "chevron" | "arrow" | "close" | "sparkles" | "dots" | "lightbulb" | "sidebar-left" | "sidebar-right";
  size?: number;
}) {
  const paths: Record<typeof name, React.ReactNode> = {
    book: <><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v17H6.5A2.5 2.5 0 0 0 4 22z" /><path d="M4 5.5v14A2.5 2.5 0 0 1 6.5 17H20" /><path d="M8 7h8" /></>,
    home: <><path d="m3 10 9-7 9 7" /><path d="M5 9v11h14V9M9 20v-7h6v7" /></>,
    search: <><circle cx="10.8" cy="10.8" r="6.8" /><path d="m16 16 4.5 4.5" /></>,
    layers: <><path d="m12 3 9 5-9 5-9-5zM3 12l9 5 9-5M3 16l9 5 9-5" /></>,
    globe: <><circle cx="12" cy="12" r="9" /><path d="M3 12h18M12 3a15 15 0 0 1 0 18M12 3a15 15 0 0 0 0 18" /></>,
    plus: <><path d="M12 5v14M5 12h14" /></>,
    clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
    folder: <><path d="M3 6.5A1.5 1.5 0 0 1 4.5 5H10l2 2h7.5A1.5 1.5 0 0 1 21 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5z" /><path d="M3 9h18" /></>,
    file: <><path d="M6 3h8l5 5v13H6z" /><path d="M14 3v6h5M9 13h7M9 17h7" /></>,
    settings: <><circle cx="12" cy="12" r="3" /><path d="m19.4 15 1.5 1.2-1.5 2.5-1.8-.7a8 8 0 0 1-1.8 1l-.3 2h-3l-.3-2a8 8 0 0 1-1.8-1l-1.8.7-1.5-2.5L6.6 15a7 7 0 0 1 0-2l-1.5-1.2 1.5-2.5 1.8.7a8 8 0 0 1 1.8-1l.3-2h3l.3 2a8 8 0 0 1 1.8 1l1.8-.7 1.5 2.5-1.5 1.2a7 7 0 0 1 0 2z" /></>,
    help: <><circle cx="12" cy="12" r="9" /><path d="M9.5 9a2.6 2.6 0 1 1 4.5 1.8c-1.2 1.2-2 1.5-2 3.2M12 17.5v.1" /></>,
    sun: <><circle cx="12" cy="12" r="4" /><path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" /></>,
    moon: <path d="M20.5 14A8.5 8.5 0 0 1 10 3.5 8.5 8.5 0 1 0 20.5 14z" />,
    chevron: <path d="m9 18 6-6-6-6" />,
    arrow: <><path d="M5 12h14M13 6l6 6-6 6" /></>,
    close: <><path d="m6 6 12 12M18 6 6 18" /></>,
    sparkles: <><path d="m12 3 1.5 5.5L19 10l-5.5 1.5L12 17l-1.5-5.5L5 10l5.5-1.5z" /></>,
    dots: <><circle cx="5" cy="12" r="1" /><circle cx="12" cy="12" r="1" /><circle cx="19" cy="12" r="1" /></>,
    lightbulb: <><path d="M9 18h6m-5 3h4m-2-19a7 7 0 0 0-4 12.7c.8.6 1 1.1 1 2.3h6c0-1.2.2-1.7 1-2.3A7 7 0 0 0 12 2z" /></>,
    "sidebar-left": <><rect x="3" y="4" width="18" height="16" rx="2" /><path d="M9 4v16" /></>,
    "sidebar-right": <><rect x="3" y="4" width="18" height="16" rx="2" /><path d="M15 4v16" /></>,
  };

  return (
    <svg aria-hidden="true" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      {paths[name]}
    </svg>
  );
}

async function loadAccount<T>(path: string): Promise<T> {
  const response = await fetch(path, { cache: "no-store" });
  const result = (await response.json()) as Record<string, unknown>;
  if (!response.ok) {
    throw new Error(
      `${response.status}: ${typeof result.message === "string" ? result.message : "Không tải được dữ liệu tài khoản."}`,
    );
  }
  return result as T;
}

async function loadWorkspaces(): Promise<Workspace[]> {
  const firstPage = await loadAccount<WorkspaceResponse>(
    "/api/account/workspaces?page=0",
  );
  const remainingPages = await Promise.all(
    Array.from({ length: Math.max(0, firstPage.totalPages - 1) }, (_, index) =>
      loadAccount<WorkspaceResponse>(
        `/api/account/workspaces?page=${index + 1}`,
      ),
    ),
  );
  return [firstPage, ...remainingPages].flatMap((page) => page.content);
}

export default function AccountDashboard() {
  const router = useRouter();
  const theme = useSyncExternalStore(
    subscribeToTheme,
    getThemeSnapshot,
    getServerThemeSnapshot,
  );
  const [profile, setProfile] = useState<Profile | null>(null);
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState<Filter>("Tất cả");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retryCount, setRetryCount] = useState(0);
  const [showCreateNotice, setShowCreateNotice] = useState(false);
  const leftCollapsed =
    useSyncExternalStore(subscribeToSidebarState, () => getSidebarSnapshot("left"), getServerSidebarSnapshot) === "true";
  const rightCollapsed =
    useSyncExternalStore(subscribeToSidebarState, () => getSidebarSnapshot("right"), getServerSidebarSnapshot) === "true";
  const [modalTitle, setModalTitle] = useState("Tạo Workspace");
  const [modalMessage, setModalMessage] = useState(
    "Giao diện quản lý Workspace đang được hoàn thiện. Hiện tại bạn có thể xem các Workspace đã có; thao tác tạo và chỉnh sửa sẽ được kết nối ở bước tiếp theo.",
  );

  useEffect(() => {
    let active = true;
    Promise.all([
      loadAccount<Profile>("/api/account/profile"),
      loadWorkspaces(),
    ])
      .then(([currentProfile, accountWorkspaces]) => {
        if (!active) return;
        setProfile(currentProfile);
        setWorkspaces(accountWorkspaces);
        setError("");
      })
      .catch((loadError: unknown) => {
        if (!active) return;
        setError(
          loadError instanceof Error
            ? loadError.message
            : "Không thể tải dữ liệu tài khoản.",
        );
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [retryCount]);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.body.dataset.theme = theme;
  }, [theme]);

  const visibleWorkspaces = useMemo(() => {
    const normalizedQuery = query.trim().toLocaleLowerCase("vi");
    return workspaces.filter((workspace) => {
      const isPublic = workspace.visibility.toLowerCase() === "public";
      const matchesFilter =
        filter === "Tất cả" ||
        (filter === "Cá nhân" && workspace.ownerUsername === profile?.username) ||
        (filter === "Công khai" && isPublic);
      const matchesQuery =
        !normalizedQuery ||
        workspace.name.toLocaleLowerCase("vi").includes(normalizedQuery) ||
        (workspace.description ?? "")
          .toLocaleLowerCase("vi")
          .includes(normalizedQuery);
      return matchesFilter && matchesQuery;
    });
  }, [filter, profile?.username, query, workspaces]);

  async function logout() {
    try {
      const response = await fetch("/api/auth/logout", { method: "POST" });
      if (!response.ok) {
        setError("Đăng xuất chưa thành công. Vui lòng thử lại.");
        return;
      }
      router.push("/");
    } catch {
      setError("Không thể kết nối để đăng xuất. Vui lòng thử lại.");
    }
  }

  if (loading) {
    return (
      <main className="dashboard-loading">
        <span className="dashboard-spinner" />
        <p>Đang mở không gian của bạn...</p>
      </main>
    );
  }

  if (!profile) {
    const unauthenticated = error.includes("401") || error.toLowerCase().includes("unauthorized");
    return (
      <main className="dashboard-gate">
        <span className="dashboard-gate-icon"><DashboardIcon name="book" size={25} /></span>
        <h1>{unauthenticated ? "Bạn cần đăng nhập" : "Chưa tải được không gian"}</h1>
        <p>{unauthenticated ? "Đăng nhập để xem và quản lý Workspace của bạn." : error}</p>
        {unauthenticated ? (
          <Link className="dashboard-primary-button" href="/login">Đăng nhập</Link>
        ) : (
          <button className="dashboard-primary-button" type="button" onClick={() => {
            setLoading(true);
            setRetryCount((count) => count + 1);
          }}>Thử lại</button>
        )}
      </main>
    );
  }

  const initial = profile.username.trim().charAt(0).toLocaleUpperCase("vi") || "M";
  const publicCount = workspaces.filter((workspace) => workspace.visibility.toLowerCase() === "public").length;

  return (
    <div className={`account-dashboard${leftCollapsed ? " left-collapsed" : ""}${rightCollapsed ? " right-collapsed" : ""}`} data-theme={theme}>
      <aside className="dashboard-left">
        <Link className="dashboard-logo" href="/">
          <span><DashboardIcon name="book" size={20} /></span> MyWiki
        </Link>
        <nav className="dashboard-nav" aria-label="Điều hướng workspace">
          <span className="dashboard-nav-label">KHÔNG GIAN CỦA BẠN</span>
          <Link className="dashboard-nav-item" href="/">
            <DashboardIcon name="home" /> Trang chủ khám phá
          </Link>
          <button className="dashboard-nav-item" type="button" onClick={() => {
            setFilter("Tất cả");
            document.getElementById("my-workspaces")?.scrollIntoView({ behavior: "smooth" });
          }}>
            <DashboardIcon name="layers" /> Workspace của tôi
          </button>
          <button className="dashboard-nav-item" type="button" onClick={() => {
            setFilter("Công khai");
            document.getElementById("my-workspaces")?.scrollIntoView({ behavior: "smooth" });
          }}>
            <DashboardIcon name="globe" /> Workspace công khai
          </button>
          <div className="dashboard-nav-heading">
            <span>Workspace</span>
            <button type="button" aria-label="Tạo Workspace" onClick={() => {
              setModalTitle("Tạo Workspace");
              setModalMessage("Giao diện quản lý Workspace đang được hoàn thiện. Hiện tại bạn có thể xem các Workspace đã có; thao tác tạo và chỉnh sửa sẽ được kết nối ở bước tiếp theo.");
              setShowCreateNotice(true);
            }}>
              <DashboardIcon name="plus" size={16} />
            </button>
          </div>
          {workspaces.slice(0, 5).map((workspace) => (
            <button className="dashboard-nav-item dashboard-nav-workspace" type="button" key={workspace.workspaceId} onClick={() => {
              setQuery(workspace.name);
              document.getElementById("my-workspaces")?.scrollIntoView({ behavior: "smooth" });
            }}>
              <span className="workspace-color-dot" />
              <span>{workspace.name}</span>
            </button>
          ))}
          {workspaces.length === 0 && <p className="dashboard-nav-empty">Workspace của bạn sẽ hiển thị tại đây.</p>}
          <div className="dashboard-nav-heading recent-heading"><span>Gần đây</span></div>
          <div className="recent-empty"><DashboardIcon name="clock" size={15} /> Chưa có hoạt động gần đây</div>
        </nav>
        <div className="dashboard-left-bottom">
          <button className="dashboard-nav-item" type="button" onClick={() => {
            setModalTitle("Tạo Workspace");
            setModalMessage("Giao diện quản lý Workspace đang được hoàn thiện. Hiện tại bạn có thể xem các Workspace đã có; thao tác tạo và chỉnh sửa sẽ được kết nối ở bước tiếp theo.");
            setShowCreateNotice(true);
          }}>
            <DashboardIcon name="plus" /> Tạo Workspace
          </button>
          <div className="dashboard-account-chip">
            {profile.avatarUrl ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img src={profile.avatarUrl} alt="" />
            ) : <span className="user-avatar">{initial}</span>}
            <span className="account-chip-copy"><strong>{profile.username}</strong><small>{profile.email}</small></span>
            <button type="button" aria-label="Đăng xuất" onClick={() => void logout()}><DashboardIcon name="dots" size={17} /></button>
          </div>
        </div>
      </aside>

      <main className="dashboard-main">
        <header className="dashboard-topbar">
          <div className="dashboard-top-title">
            <button className="dashboard-panel-button" type="button" aria-label={leftCollapsed ? "Mở thanh bên trái" : "Thu gọn thanh bên trái"} aria-expanded={!leftCollapsed} onClick={() => {
              saveSidebarState("left", !leftCollapsed);
            }}><DashboardIcon name="sidebar-left" size={17} /></button>
            <strong>MYWIKI WORKSPACE</strong>
          </div>
          <div className="dashboard-top-actions">
            <button className="dashboard-panel-button" type="button" aria-label={rightCollapsed ? "Mở thanh bên phải" : "Thu gọn thanh bên phải"} aria-expanded={!rightCollapsed} onClick={() => {
              saveSidebarState("right", !rightCollapsed);
            }}><DashboardIcon name="sidebar-right" size={17} /></button>
            <button className="dashboard-theme-button" type="button" aria-label={theme === "light" ? "Chuyển sang chế độ tối" : "Chuyển sang chế độ sáng"} onClick={() => saveTheme(theme === "light" ? "dark" : "light")}>
              <DashboardIcon name={theme === "light" ? "moon" : "sun"} size={17} />
            </button>
          </div>
        </header>

        <section className="dashboard-welcome">
          <div>
            <span className="dashboard-eyebrow"><DashboardIcon name="sparkles" size={14} /> MYWIKI WORKSPACE</span>
            <h1>Chào {profile.username} <span aria-hidden="true">✦</span></h1>
          </div>
          <button className="dashboard-create-button" type="button" onClick={() => {
            setModalTitle("Tạo Workspace");
            setModalMessage("Giao diện quản lý Workspace đang được hoàn thiện. Hiện tại bạn có thể xem các Workspace đã có; thao tác tạo và chỉnh sửa sẽ được kết nối ở bước tiếp theo.");
            setShowCreateNotice(true);
          }}>
            <DashboardIcon name="plus" size={17} /> Tạo Workspace
          </button>
        </section>

        <section className="dashboard-stats" aria-label="Tổng quan Workspace">
          <article><span className="stat-icon stat-icon-green"><DashboardIcon name="layers" size={17} /></span><span><small>Tổng Workspace</small><strong>{workspaces.length}</strong></span></article>
          <article><span className="stat-icon stat-icon-gold"><DashboardIcon name="globe" size={17} /></span><span><small>Đang công khai</small><strong>{publicCount}</strong></span></article>
          <article><span className="stat-icon stat-icon-blue"><DashboardIcon name="folder" size={17} /></span><span><small>Chế độ riêng tư</small><strong>{workspaces.length - publicCount}</strong></span></article>
        </section>

        <section className="my-workspaces" id="my-workspaces">
          <div className="workspace-list-heading">
            <div>
              <span className="dashboard-section-kicker">SÁNG TẠO CỦA BẠN</span>
              <h2>Workspace của bạn</h2>
            </div>
            <label className="workspace-search">
              <DashboardIcon name="search" size={16} />
              <input id="workspace-search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Tìm Workspace..." />
            </label>
          </div>
          <div className="workspace-filter-bar">
            <div className="workspace-filters" role="tablist" aria-label="Lọc Workspace">
              {filters.map((item) => (
                <button className={filter === item ? "workspace-filter active" : "workspace-filter"} type="button" role="tab" aria-selected={filter === item} key={item} onClick={() => setFilter(item)}>{item}</button>
              ))}
            </div>
            <span className="workspace-total">{visibleWorkspaces.length} Workspace</span>
          </div>

          {error && <div className="dashboard-error" role="alert">{error}</div>}
          {visibleWorkspaces.length ? (
            <div className="dashboard-workspace-grid">
              {visibleWorkspaces.map((workspace, index) => {
                const isPublic = workspace.visibility.toLowerCase() === "public";
                return (
                  <article className={`dashboard-workspace-card workspace-tone-${index % 4}`} key={workspace.workspaceId}>
                    <div className="workspace-cover">
                      <span className="cover-orb" />
                      <span className="cover-icon"><DashboardIcon name="book" size={23} /></span>
                      <span className={isPublic ? "visibility-pill is-public" : "visibility-pill"}><DashboardIcon name={isPublic ? "globe" : "settings"} size={12} /> {isPublic ? "Công khai" : "Riêng tư"}</span>
                      <button className="workspace-more" type="button" aria-label={`Tùy chọn ${workspace.name}`} onClick={() => setShowCreateNotice(true)}><DashboardIcon name="dots" size={17} /></button>
                    </div>
                    <div className="workspace-card-content">
                      <h3>{workspace.name}</h3>
                      <p>{workspace.description || "Chưa có mô tả cho Workspace này."}</p>
                      <div className="workspace-card-meta">
                        <span><DashboardIcon name="clock" size={13} /> Cập nhật {new Date(workspace.updatedAt).toLocaleDateString("vi-VN")}</span>
                        <span className="owner-avatar">{initial}</span>
                      </div>
                    </div>
                  </article>
                );
              })}
            </div>
          ) : (
            <div className="dashboard-empty">
              <span className="dashboard-empty-icon"><DashboardIcon name="layers" size={24} /></span>
              <h3>{query || filter !== "Tất cả" ? "Không tìm thấy Workspace phù hợp" : "Bắt đầu với Workspace đầu tiên"}</h3>
              <p>{query || filter !== "Tất cả" ? "Thử từ khóa hoặc bộ lọc khác." : "Tạo một không gian để sắp xếp ý tưởng, tài liệu và các Page của bạn."}</p>
              {!query && filter === "Tất cả" &&               <button className="dashboard-create-button" type="button" onClick={() => {
                setModalTitle("Tạo Workspace");
                setModalMessage("Giao diện quản lý Workspace đang được hoàn thiện. Hiện tại bạn có thể xem các Workspace đã có; thao tác tạo và chỉnh sửa sẽ được kết nối ở bước tiếp theo.");
                setShowCreateNotice(true);
              }}><DashboardIcon name="plus" size={16} /> Tạo Workspace</button>}
            </div>
          )}
        </section>

        <section className="dashboard-bottom-note">
          <span><DashboardIcon name="lightbulb" size={18} /></span>
          <p><strong>Một ý tưởng hay có thể bắt đầu từ bất cứ đâu.</strong><small>Tạo Workspace để sắp xếp những mảnh ghép tiếp theo của bạn.</small></p>
          <button type="button" onClick={() => setShowCreateNotice(true)}>Tìm hiểu thêm <DashboardIcon name="arrow" size={14} /></button>
        </section>
      </main>

      <aside className="dashboard-right">
        <div className="profile-card">
          <div className="profile-card-top">
            <span className="profile-avatar">{profile.avatarUrl ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img src={profile.avatarUrl} alt="" />
            ) : initial}</span>
            <span className="profile-status"><i /> Đang hoạt động</span>
            <button type="button" aria-label="Đăng xuất" onClick={() => void logout()}><DashboardIcon name="dots" size={17} /></button>
          </div>
          <h2>{profile.username}</h2>
          <p>{profile.email}</p>
          <div className="profile-divider" />
          <div className="profile-summary"><span><DashboardIcon name="layers" size={15} /> Workspace</span><strong>{workspaces.length}</strong></div>
          <Link className="profile-settings-link" href="/dashboard">Quản lý tài khoản <DashboardIcon name="arrow" size={14} /></Link>
        </div>

        <section className="dashboard-quick-section">
          <h2><DashboardIcon name="clock" size={16} /> Gần đây</h2>
          <div className="dashboard-quick-empty">Các Workspace bạn mở gần đây sẽ xuất hiện ở đây.</div>
        </section>

        <section className="dashboard-quick-section">
          <h2><DashboardIcon name="help" size={16} /> Trợ giúp</h2>
          <button type="button" onClick={() => window.open("https://github.com/tkimngan040/my-wiki", "_blank", "noopener,noreferrer")}>Hướng dẫn sử dụng <DashboardIcon name="chevron" size={15} /></button>
          <button type="button" onClick={() => window.open("https://github.com/tkimngan040/my-wiki", "_blank", "noopener,noreferrer")}>Câu hỏi thường gặp <DashboardIcon name="chevron" size={15} /></button>
          <button type="button" onClick={() => window.open("https://github.com/tkimngan040/my-wiki", "_blank", "noopener,noreferrer")}>Liên hệ hỗ trợ <DashboardIcon name="chevron" size={15} /></button>
        </section>

        <button className="dashboard-logout" type="button" onClick={() => void logout()}><DashboardIcon name="close" size={15} /> Đăng xuất</button>
        <div className="dashboard-right-footer">Your knowledge, your world</div>
      </aside>

      {showCreateNotice && (
        <div className="dashboard-modal-backdrop" onMouseDown={(event) => {
          if (event.target === event.currentTarget) setShowCreateNotice(false);
        }}>
          <section className="dashboard-modal" role="dialog" aria-modal="true" aria-labelledby="create-dialog-title">
            <button className="dashboard-modal-close" type="button" aria-label="Đóng" onClick={() => setShowCreateNotice(false)}><DashboardIcon name="close" size={17} /></button>
            <span className="dashboard-modal-icon"><DashboardIcon name="layers" size={22} /></span>
            <span className="dashboard-section-kicker">MYWIKI WORKSPACE</span>
            <h2 id="create-dialog-title">{modalTitle}</h2>
            <p>{modalMessage}</p>
            <button className="dashboard-create-button" type="button" onClick={() => setShowCreateNotice(false)}>Đã hiểu</button>
          </section>
        </div>
      )}
    </div>
  );
}
