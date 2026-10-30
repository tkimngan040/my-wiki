"use client";

import Link from "next/link";
import {
  useEffect,
  useMemo,
  useRef,
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

type Folder = {
  folderId: number;
  workspaceId: number;
  parentFolderId: number | null;
  name: string;
  description: string | null;
};

type Page = {
  pageId: number;
  workspaceId: number;
  folderId: number | null;
  title: string;
  content: string;
};

type PageLink = {
  linkId: number;
  sourcePageId: number;
  targetPageId: number;
  targetPageTitle: string;
  anchorText: string;
};

type Filter = "Tất cả" | "Cá nhân" | "Công khai";

const filters: Filter[] = ["Tất cả", "Cá nhân", "Công khai"];

type DialogField = {
  key: string;
  label: string;
  value: string;
  maxLength: number;
  required?: boolean;
  multiline?: boolean;
  options?: { value: string; label: string }[];
};

async function requestApi<T>(
  path: string,
  method = "GET",
  body?: Record<string, unknown>,
): Promise<T> {
  const response = await fetch(path, {
    method,
    cache: "no-store",
    headers: body ? { "Content-Type": "application/json" } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await response.text();
  let result: Record<string, unknown> = {};
  if (text) {
    try {
      result = JSON.parse(text) as Record<string, unknown>;
    } catch {
      result = {};
    }
  }
  if (!response.ok) {
    throw new Error(
      `${response.status}: ${typeof result.message === "string" ? result.message : "Yêu cầu chưa thành công. Vui lòng thử lại."}`,
    );
  }
  return result as T;
}

function asList<T>(value: unknown): T[] {
  if (Array.isArray(value)) return value as T[];
  if (value && typeof value === "object" && Array.isArray((value as { content?: unknown }).content)) {
    return (value as { content: T[] }).content;
  }
  return [];
}

function ResourceDialog({
  title,
  fields,
  saving,
  error,
  onClose,
  onSubmit,
}: {
  title: string;
  fields: DialogField[];
  saving: boolean;
  error: string;
  onClose: () => void;
  onSubmit: (values: Record<string, string>) => void;
}) {
  const [values, setValues] = useState<Record<string, string>>(
    () => Object.fromEntries(fields.map((field) => [field.key, field.value])),
  );
  return (
    <div className="dashboard-modal-backdrop" onMouseDown={(event) => {
      if (event.target === event.currentTarget && !saving) onClose();
    }}>
      <section className="dashboard-modal management-dialog" role="dialog" aria-modal="true" aria-labelledby="resource-dialog-title">
        <button className="dashboard-modal-close" type="button" aria-label="Đóng" disabled={saving} onClick={onClose}><DashboardIcon name="close" size={17} /></button>
        <span className="dashboard-modal-icon"><DashboardIcon name="layers" size={22} /></span>
        <span className="dashboard-section-kicker">MYWIKI WORKSPACE</span>
        <h2 id="resource-dialog-title">{title}</h2>
        <form onSubmit={(event) => {
          event.preventDefault();
          onSubmit(values);
        }}>
          {fields.map((field) => (
            <label className="management-field" key={field.key} htmlFor={`management-${field.key}`}>
              <span>{field.label}{field.required ? " *" : ""}</span>
              {field.options ? (
                <select id={`management-${field.key}`} value={values[field.key] ?? ""} required={field.required} onChange={(event) => setValues((current) => ({ ...current, [field.key]: event.target.value }))}>
                  {field.options.map((option) => <option value={option.value} key={option.value}>{option.label}</option>)}
                </select>
              ) : field.multiline ? (
                <textarea id={`management-${field.key}`} value={values[field.key] ?? ""} maxLength={field.maxLength} required={field.required} rows={5} onChange={(event) => setValues((current) => ({ ...current, [field.key]: event.target.value }))} />
              ) : (
                <input id={`management-${field.key}`} value={values[field.key] ?? ""} maxLength={field.maxLength} required={field.required} onChange={(event) => setValues((current) => ({ ...current, [field.key]: event.target.value }))} />
              )}
              {field.multiline && <small>{(values[field.key] ?? "").length}/{field.maxLength}</small>}
            </label>
          ))}
          {error && <p className="dashboard-error" role="alert">{error}</p>}
          <div className="management-dialog-actions">
            <button type="button" className="management-secondary-button" disabled={saving} onClick={onClose}>Hủy</button>
            <button type="submit" className="dashboard-create-button" disabled={saving}>{saving ? "Đang lưu..." : "Lưu"}</button>
          </div>
        </form>
      </section>
    </div>
  );
}

function WorkspaceManager({
  workspace,
  onClose,
  onCreated,
  onUpdated,
  onDeleted,
}: {
  workspace: Workspace | null;
  onClose: () => void;
  onCreated: (workspace: Workspace) => void;
  onUpdated: (workspace: Workspace) => void;
  onDeleted: (workspaceId: number) => void;
}) {
  const [folders, setFolders] = useState<Folder[]>([]);
  const [pages, setPages] = useState<Page[]>([]);
  const [links, setLinks] = useState<PageLink[]>([]);
  const [activePage, setActivePage] = useState<Page | null>(null);
  const [currentFolderId, setCurrentFolderId] = useState<number | null>(null);
  const [editMode, setEditMode] = useState(false);
  const [dialog, setDialog] = useState<"workspace" | "folder" | "page" | "link" | null>(workspace ? null : "workspace");
  const [editingFolder, setEditingFolder] = useState<Folder | null>(null);
  const [editingPage, setEditingPage] = useState<Page | null>(null);
  const [loading, setLoading] = useState(Boolean(workspace));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const base = workspace ? `/api/workspaces/${workspace.workspaceId}` : "/api/workspaces";
  const currentFolder = folders.find((folder) => folder.folderId === currentFolderId) ?? null;

  function getFolderAncestors(folder: Folder | null): Folder[] {
    const ancestors: Folder[] = [];
    const visited = new Set<number>();
    let parentId = folder?.parentFolderId ?? null;
    while (parentId !== null && !visited.has(parentId)) {
      visited.add(parentId);
      const parent = folders.find((item) => item.folderId === parentId);
      if (!parent) break;
      ancestors.unshift(parent);
      parentId = parent.parentFolderId;
    }
    return ancestors;
  }

  function isFolderDescendant(candidateId: number, folderId: number): boolean {
    let parentId = folders.find((folder) => folder.folderId === candidateId)?.parentFolderId ?? null;
    const visited = new Set<number>();
    while (parentId !== null && !visited.has(parentId)) {
      if (parentId === folderId) return true;
      visited.add(parentId);
      parentId = folders.find((folder) => folder.folderId === parentId)?.parentFolderId ?? null;
    }
    return false;
  }

  function getFolderSubtreeIds(folderId: number): Set<number> {
    const subtreeIds = new Set([folderId]);
    let addedFolder = true;
    while (addedFolder) {
      addedFolder = false;
      folders.forEach((folder) => {
        if (folder.parentFolderId !== null && subtreeIds.has(folder.parentFolderId) && !subtreeIds.has(folder.folderId)) {
          subtreeIds.add(folder.folderId);
          addedFolder = true;
        }
      });
    }
    return subtreeIds;
  }

  useEffect(() => {
    if (!workspace) return;
    let active = true;
    Promise.all([
      requestApi<unknown>(`${base}/folders`),
      requestApi<unknown>(`${base}/pages`),
    ]).then(([folderResult, pageResult]) => {
      if (!active) return;
      setFolders(asList<Folder>(folderResult));
      setPages(asList<Page>(pageResult));
      setError("");
    }).catch((loadError: unknown) => {
      if (active) setError(loadError instanceof Error ? loadError.message : "Không thể tải nội dung Workspace.");
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [base, workspace]);

  async function showPage(page: Page) {
    setError("");
    setSuccess("");
    setActivePage(page);
    setLinks([]);
    try {
      const [details, linkResults] = await Promise.all([
        requestApi<Page>(`${base}/pages/${page.pageId}`),
        requestApi<unknown>(`${base}/pages/${page.pageId}/links`),
      ]);
      setActivePage(details);
      setLinks(asList<PageLink>(linkResults));
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Không thể tải Page và liên kết.");
    }
  }

  function fieldsForDialog(): DialogField[] {
    if (dialog === "workspace") {
      return [
        { key: "name", label: "Tên Workspace", value: workspace?.name ?? "", maxLength: 100, required: true },
        { key: "description", label: "Mô tả", value: workspace?.description ?? "", maxLength: 500, multiline: true },
        { key: "visibility", label: "Chế độ hiển thị", value: workspace?.visibility ?? "Private", maxLength: 20, required: true, options: [{ value: "Private", label: "Riêng tư" }, { value: "Public", label: "Công khai" }] },
      ];
    }
    if (dialog === "folder") {
      return [
        { key: "name", label: "Tên Folder", value: editingFolder?.name ?? "", maxLength: 100, required: true },
        { key: "description", label: "Mô tả", value: editingFolder?.description ?? "", maxLength: 500, multiline: true },
        { key: "parentFolderId", label: "Folder cha", value: editingFolder?.parentFolderId ? String(editingFolder.parentFolderId) : currentFolderId ? String(currentFolderId) : "", maxLength: 20, options: [{ value: "", label: "Không có" }, ...folders.filter((folder) => folder.folderId !== editingFolder?.folderId && (!editingFolder || !isFolderDescendant(folder.folderId, editingFolder.folderId))).map((folder) => ({ value: String(folder.folderId), label: [...getFolderAncestors(folder), folder].map((item) => item.name).join(" / ") }))] },
      ];
    }
    if (dialog === "page") {
      return [
        { key: "title", label: "Tiêu đề Page", value: editingPage?.title ?? "", maxLength: 200, required: true },
        { key: "content", label: "Nội dung", value: editingPage?.content ?? "", maxLength: 10000, multiline: true },
        { key: "folderId", label: "Folder", value: editingPage?.folderId ? String(editingPage.folderId) : currentFolderId ? String(currentFolderId) : "", maxLength: 20, options: [{ value: "", label: "Không có" }, ...folders.map((folder) => ({ value: String(folder.folderId), label: [...getFolderAncestors(folder), folder].map((item) => item.name).join(" / ") }))] },
      ];
    }
    return [
      { key: "targetPageId", label: "Page đích", value: "", maxLength: 20, required: true, options: [{ value: "", label: "Chọn Page" }, ...pages.filter((page) => page.pageId !== activePage?.pageId).map((page) => ({ value: String(page.pageId), label: page.title }))] },
      { key: "anchorText", label: "Văn bản liên kết", value: "", maxLength: 200, required: true },
    ];
  }

  async function submitDialog(values: Record<string, string>) {
    if (fieldsForDialog().some((field) => values[field.key].length > field.maxLength)) {
      setError("Một hoặc nhiều trường vượt quá độ dài cho phép.");
      return;
    }
    if (fieldsForDialog().some((field) => field.required && !values[field.key].trim())) {
      setError("Vui lòng điền đầy đủ các trường bắt buộc.");
      return;
    }
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      if (dialog === "workspace") {
        const body = { name: values.name.trim(), description: values.description.trim(), visibility: values.visibility };
        if (!body.name) throw new Error("Tên Workspace là trường bắt buộc.");
        const saved = await requestApi<Workspace>(base, workspace ? "PUT" : "POST", body);
        if (workspace) onUpdated(saved);
        else onCreated(saved);
      } else if (dialog === "folder") {
        const body = { name: values.name.trim(), description: values.description.trim(), parentFolderId: values.parentFolderId ? Number(values.parentFolderId) : null };
        if (!body.name) throw new Error("Tên Folder là trường bắt buộc.");
        const saved = await requestApi<Folder>(editingFolder ? `${base}/folders/${editingFolder.folderId}` : `${base}/folders`, editingFolder ? "PUT" : "POST", body);
        setFolders((current) => editingFolder ? current.map((item) => item.folderId === saved.folderId ? saved : item) : [...current, saved]);
        setSuccess(editingFolder ? "Đã cập nhật Folder." : "Đã tạo Folder.");
      } else if (dialog === "page") {
        const body = { title: values.title.trim(), content: values.content, folderId: values.folderId ? Number(values.folderId) : null };
        if (!body.title) throw new Error("Tiêu đề Page là trường bắt buộc.");
        const saved = await requestApi<Page>(editingPage ? `${base}/pages/${editingPage.pageId}` : `${base}/pages`, editingPage ? "PUT" : "POST", body);
        setPages((current) => editingPage ? current.map((item) => item.pageId === saved.pageId ? saved : item) : [...current, saved]);
        if (activePage?.pageId === saved.pageId) setActivePage(saved);
        setSuccess(editingPage ? "Đã cập nhật Page." : "Đã tạo Page.");
      } else if (dialog === "link" && activePage) {
        const saved = await requestApi<PageLink>(`${base}/pages/${activePage.pageId}/links`, "POST", { targetPageId: Number(values.targetPageId), anchorText: values.anchorText.trim() });
        setLinks((current) => [...current, saved]);
        setSuccess("Đã thêm liên kết Page.");
      }
      setDialog(null);
      setEditingFolder(null);
      setEditingPage(null);
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "Không thể lưu thay đổi.");
    } finally {
      setSaving(false);
    }
  }

  async function deleteResource(path: string, label: string, afterDelete: () => void) {
    if (!window.confirm(`Bạn có chắc muốn xóa ${label}?`)) return;
    setError("");
    setSuccess("");
    try {
      await requestApi<unknown>(path, "DELETE");
      afterDelete();
      setSuccess(`Đã xóa ${label}.`);
    } catch (deleteError) {
      setError(deleteError instanceof Error ? deleteError.message : `Không thể xóa ${label}.`);
    }
  }

  const visibleFolders = folders
    .filter((folder) => folder.parentFolderId === currentFolderId)
    .sort((left, right) => left.name.localeCompare(right.name, "vi"));
  const visiblePages = pages.filter((page) => page.folderId === currentFolderId);
  const openFolderDialog = (folder?: Folder) => {
    setEditingFolder(folder ?? null);
    setDialog("folder");
    setError("");
  };
  const openPageDialog = (page?: Page) => {
    setEditingPage(page ?? null);
    setDialog("page");
    setError("");
  };

  return (
    <section className="workspace-manager" aria-label={workspace ? `Quản lý ${workspace.name}` : "Tạo Workspace"}>
      {workspace && <>
        <div className="manager-mode-bar">
          <button type="button" className="manager-back-button" onClick={onClose}>← Workspace của tôi</button>
          <div className="manager-mode-controls">
            <span>Chế độ hiển thị</span>
            <div
              className="manager-mode-toggle"
              role="group"
              aria-label="Chế độ Workspace"
              onClick={(event) => {
                const target = event.target;
                const modeButton = target instanceof HTMLElement
                  ? target.closest<HTMLButtonElement>("[data-mode]")
                  : null;
                if (modeButton?.dataset.mode) {
                  setEditMode(modeButton.dataset.mode === "edit");
                  if (modeButton.dataset.mode === "view") setDialog(null);
                } else {
                  setEditMode((current) => !current);
                }
              }}
            >
              <button type="button" data-mode="view" aria-pressed={!editMode} className={!editMode ? "active" : ""}>View</button>
              <button type="button" data-mode="edit" aria-pressed={editMode} className={editMode ? "active" : ""}>Edit</button>
            </div>
          </div>
        </div>
        <div className="manager-heading">
          <div>
            <h2>{workspace.name}</h2>
            {workspace.description && <p>{workspace.description}</p>}
          </div>
          <div className="manager-toolbar">
            {editMode && <>
              <button className="management-secondary-button" type="button" onClick={() => { setDialog("workspace"); setError(""); }}>Chỉnh sửa Workspace</button>
              <button className="management-danger-button" type="button" onClick={() => void deleteResource(base, "Workspace", () => onDeleted(workspace.workspaceId))}>Xóa Workspace</button>
            </>}
          </div>
        </div>
        {error && <div className="dashboard-error" role="alert">{error}</div>}
        {success && <div className="dashboard-success" role="status">{success}</div>}
        {loading ? <div className="manager-loading"><span className="dashboard-spinner" /> Đang tải Folder và Page...</div> : (
          <>
          <div className="manager-panel manager-browser">
            <div className="manager-panel-heading manager-browser-heading">
              <nav className="manager-breadcrumbs" aria-label="Đường dẫn Folder">
                <button type="button" onClick={() => { setCurrentFolderId(null); setActivePage(null); setLinks([]); }}>{workspace.name}</button>
                {getFolderAncestors(currentFolder).map((folder) => (
                  <span key={folder.folderId} className="manager-breadcrumb-part">
                    <span aria-hidden="true">/</span>
                    <button type="button" onClick={() => { setCurrentFolderId(folder.folderId); setActivePage(null); setLinks([]); }}>{folder.name}</button>
                  </span>
                ))}
                {currentFolder && <span className="manager-breadcrumb-part" aria-current="location"><span aria-hidden="true">/</span><strong>{currentFolder.name}</strong></span>}
              </nav>
              {editMode && <div className="manager-browser-actions">
                  <button className="management-secondary-button" type="button" onClick={() => openFolderDialog()}>＋ Folder</button>
                  <button className="dashboard-create-button" type="button" onClick={() => openPageDialog()}>＋ Page</button>
                </div>}
            </div>
            {visibleFolders.length || visiblePages.length ? (
              <div className="manager-item-grid">
                {visibleFolders.map((folder) => (
                  <article className="manager-item-card" key={folder.folderId}>
                    <button className="manager-item-open" type="button" onClick={() => { setCurrentFolderId(folder.folderId); setActivePage(null); setLinks([]); }} aria-label={`Mở Folder ${folder.name}`}>
                      <span className="manager-item-icon manager-folder-icon"><DashboardIcon name="folder" size={38} /></span>
                      <strong>{folder.name}</strong>
                      <small>Folder · {folders.filter((child) => child.parentFolderId === folder.folderId).length} Folder con</small>
                    </button>
                    {editMode && <span className="manager-item-actions">
                      <button type="button" aria-label={`Sửa ${folder.name}`} onClick={() => openFolderDialog(folder)}>Sửa</button>
                      <button type="button" aria-label={`Xóa ${folder.name}`} onClick={() => void deleteResource(`${base}/folders/${folder.folderId}`, `Folder “${folder.name}”`, () => {
                        const deletedFolderIds = getFolderSubtreeIds(folder.folderId);
                        const deletedPageIds = new Set(pages.filter((page) => page.folderId !== null && deletedFolderIds.has(page.folderId)).map((page) => page.pageId));
                        setFolders((current) => current.filter((item) => !deletedFolderIds.has(item.folderId)));
                        setPages((current) => current.filter((page) => !deletedPageIds.has(page.pageId)));
                        if (activePage && deletedPageIds.has(activePage.pageId)) {
                          setActivePage(null);
                          setLinks([]);
                        }
                      })}>Xóa</button>
                    </span>}
                  </article>
                ))}
                {visiblePages.map((page) => (
                  <article className={`manager-item-card ${activePage?.pageId === page.pageId ? "active" : ""}`} key={page.pageId}>
                    <button className="manager-item-open" type="button" onClick={() => void showPage(page)} aria-label={`Mở Page ${page.title}`}>
                      <span className="manager-item-icon manager-page-icon"><DashboardIcon name="file" size={36} /></span>
                      <strong>{page.title}</strong>
                      <small>Page</small>
                    </button>
                    {editMode && <span className="manager-item-actions">
                      <button type="button" aria-label={`Sửa ${page.title}`} onClick={() => openPageDialog(page)}>Sửa</button>
                      <button type="button" aria-label={`Xóa ${page.title}`} onClick={() => void deleteResource(`${base}/pages/${page.pageId}`, `Page “${page.title}”`, () => {
                        setPages((current) => current.filter((item) => item.pageId !== page.pageId));
                        if (activePage?.pageId === page.pageId) { setActivePage(null); setLinks([]); }
                      })}>Xóa</button>
                    </span>}
                  </article>
                ))}
              </div>
            ) : (
              <p className="manager-hint">{folders.length || pages.length ? "Folder này chưa có nội dung." : "Chưa có Folder hoặc Page. Tạo mới để bắt đầu sắp xếp nội dung."}</p>
            )}
          </div>
          {activePage && <section className="manager-panel manager-page-detail">
              <div className="manager-panel-heading"><div><span className="dashboard-section-kicker">PAGE</span><h3>{activePage.title}</h3></div><button type="button" className="manager-detail-close" aria-label="Đóng Page" onClick={() => { setActivePage(null); setLinks([]); }}>×</button></div>
              <div className="manager-page-content">{activePage.content || "Page chưa có nội dung."}</div>
              <div className="manager-panel-heading manager-links-heading"><h4>Liên kết Page</h4>{editMode && <button type="button" className="management-secondary-button" onClick={() => { setDialog("link"); setError(""); }}>＋ Thêm liên kết</button>}</div>
              {links.map((link) => <div className="manager-link-row" key={link.linkId}><span><strong>{link.anchorText}</strong><small>→ {link.targetPageTitle}</small></span>{editMode && <button type="button" aria-label={`Xóa liên kết ${link.anchorText}`} onClick={() => void deleteResource(`${base}/pages/${activePage.pageId}/links/${link.linkId}`, "liên kết", () => setLinks((current) => current.filter((item) => item.linkId !== link.linkId)))}>Xóa</button>}</div>)}
              {!links.length && <p className="manager-hint">Page này chưa có liên kết.</p>}
            </section>}
          </>
        )}
      </>}
      {dialog && <ResourceDialog
        key={`${dialog}-${editingFolder?.folderId ?? ""}-${editingPage?.pageId ?? ""}-${workspace?.workspaceId ?? "new"}`}
        title={`${dialog === "workspace" ? workspace ? "Chỉnh sửa Workspace" : "Tạo Workspace" : dialog === "folder" ? editingFolder ? "Chỉnh sửa Folder" : "Tạo Folder" : dialog === "page" ? editingPage ? "Chỉnh sửa Page" : "Tạo Page" : "Thêm liên kết Page"}`}
        fields={fieldsForDialog()}
        saving={saving}
        error={error}
        onClose={() => { if (!saving) { setDialog(null); setError(""); } }}
        onSubmit={(values) => void submitDialog(values)}
      />}
    </section>
  );
}

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

function DashboardIcon({
  name,
  size = 18,
}: {
  name: "book" | "home" | "compass" | "search" | "layers" | "globe" | "plus" | "clock" | "folder" | "file" | "settings" | "help" | "sun" | "moon" | "chevron" | "arrow" | "close" | "sparkles" | "dots" | "lightbulb";
  size?: number;
}) {
  const paths: Record<typeof name, React.ReactNode> = {
    book: <><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v17H6.5A2.5 2.5 0 0 0 4 22z" /><path d="M4 5.5v14A2.5 2.5 0 0 1 6.5 17H20" /><path d="M8 7h8" /></>,
    home: <><path d="m3 10 9-7 9 7" /><path d="M5 9v11h14V9M9 20v-7h6v7" /></>,
    compass: <><circle cx="12" cy="12" r="9" /><path d="m15.5 8.5-2.2 4.8-4.8 2.2 2.2-4.8z" /></>,
    search: <><circle cx="10.8" cy="10.8" r="6.8" /><path d="m16 16 4.5 4.5" /></>,
    layers: <><path d="m12 3 9 5-9 5-9-5zM3 12l9 5 9-5M3 16l9 5 9-5" /></>,
    globe: <><circle cx="12" cy="12" r="9" /><path d="M3 12h18M12 3a15 15 0 0 1 0 18M12 3a15 15 0 0 0 0 18" /></>,
    plus: <><path d="M12 5v14M5 12h14" /></>,
    clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
    folder: <><path d="M7 4h10v13H7zM4 7h10v13H4z" /><path d="M4 10h10" /><path d="M7 8h10a1.5 1.5 0 0 1 1.5 1.5v9A1.5 1.5 0 0 1 17 20H5.5A1.5 1.5 0 0 1 4 18.5V10a2 2 0 0 1 2-2z" /><path d="M4 12h14.5" /></>,
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
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const [workspaceManager, setWorkspaceManager] = useState<Workspace | "create" | null>(null);
  const [workspaceNotice, setWorkspaceNotice] = useState("");
  const workspaceHistoryEntry = useRef(false);

  function openWorkspaceManager(target: Workspace | "create") {
    if (!workspaceHistoryEntry.current) {
      window.history.pushState(
        { ...window.history.state, myWikiWorkspaceDetail: true },
        "",
        window.location.href,
      );
      workspaceHistoryEntry.current = true;
    }
    setWorkspaceManager(target);
  }

  function closeWorkspaceManager() {
    setWorkspaceManager(null);
    if (workspaceHistoryEntry.current) {
      workspaceHistoryEntry.current = false;
      window.history.back();
    }
  }

  useEffect(() => {
    function handlePopState() {
      if (!workspaceHistoryEntry.current) return;
      workspaceHistoryEntry.current = false;
      setWorkspaceManager(null);
    }
    window.addEventListener("popstate", handlePopState);
    return () => window.removeEventListener("popstate", handlePopState);
  }, []);

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
  const scrollToWorkspaces = () => {
    document.getElementById("my-workspaces")?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <div className="account-dashboard" data-theme={theme}>
      <aside className="dashboard-left">
        <Link className="dashboard-logo" href="/" aria-label="MyWiki trang chủ">
          <span><DashboardIcon name="book" size={20} /></span>
          <span>MyWiki</span>
        </Link>
        <nav className="dashboard-nav" aria-label="Điều hướng chính">
          <Link className="dashboard-nav-item" href="/">
            <DashboardIcon name="home" /> <span>Trang chủ</span>
          </Link>
          <button className="dashboard-nav-item" type="button" onClick={scrollToWorkspaces}>
            <DashboardIcon name="compass" /> <span>Khám phá</span>
          </button>
          <button className="dashboard-nav-item" type="button" onClick={() => {
            scrollToWorkspaces();
            window.setTimeout(() => document.getElementById("workspace-search")?.focus(), 350);
          }}>
            <DashboardIcon name="search" /> <span>Tìm kiếm</span>
          </button>
          <button
            className="dashboard-nav-item theme-nav-item"
            type="button"
            aria-label={theme === "light" ? "Chuyển sang chế độ tối" : "Chuyển sang chế độ sáng"}
            onClick={() => saveTheme(theme === "light" ? "dark" : "light")}
          >
            <DashboardIcon name={theme === "light" ? "moon" : "sun"} />
            <span>{theme === "light" ? "Chế độ tối" : "Chế độ sáng"}</span>
          </button>

          <div className="dashboard-nav-group">
            <div className="dashboard-nav-heading"><span>Workspace</span></div>
            <button className="dashboard-nav-item dashboard-nav-subitem selected" type="button" aria-current="page" onClick={() => {
              setFilter("Tất cả");
              setQuery("");
              scrollToWorkspaces();
            }}>
              <DashboardIcon name="layers" /> <span>Workspace của tôi</span>
            </button>
            <Link
              className="dashboard-nav-item dashboard-nav-subitem"
              href="/#public-workspaces"
            >
              <DashboardIcon name="globe" /> <span>Workspace công khai</span>
            </Link>
          </div>

          <div className="dashboard-nav-group recent-group">
            <div className="dashboard-nav-heading"><span>Gần đây</span></div>
            <button className="dashboard-nav-item dashboard-nav-subitem" type="button" onClick={scrollToWorkspaces}>
              <DashboardIcon name="file" /> <span>Page gần đây</span>
            </button>
            <button className="dashboard-nav-item dashboard-nav-subitem" type="button" onClick={scrollToWorkspaces}>
              <DashboardIcon name="folder" /> <span>Folder gần đây</span>
            </button>
            <button className="dashboard-nav-item dashboard-nav-subitem" type="button" onClick={scrollToWorkspaces}>
              <DashboardIcon name="clock" /> <span>Workspace gần đây</span>
            </button>
          </div>
        </nav>
        <div className="dashboard-left-bottom">
          <div className="dashboard-sidebar-footer">
            <span className="dashboard-footer-logo"><DashboardIcon name="book" size={15} /></span>
            <span><strong>MyWiki</strong><small>Your knowledge, your world</small></span>
          </div>
        </div>
      </aside>

      <main className="dashboard-main">
        <header className="dashboard-topbar">
          <div className="dashboard-top-actions">
            <button
              className="dashboard-profile-chip"
              type="button"
              aria-expanded={profileMenuOpen}
              aria-haspopup="true"
              aria-label={`Tài khoản ${profile.username}`}
              onClick={() => setProfileMenuOpen((open) => !open)}
            >
              <span className="dashboard-profile-avatar">
                {profile.avatarUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img src={profile.avatarUrl} alt="" />
                ) : initial}
              </span>
              <span>{profile.username}</span>
            </button>
            {profileMenuOpen && (
              <div className="dashboard-profile-menu">
                {error && <p role="alert">{error}</p>}
                <button type="button" onClick={() => void logout()}>
                  <DashboardIcon name="close" size={15} /> Đăng xuất
                </button>
              </div>
            )}
          </div>
        </header>
        {workspaceNotice && <div className="dashboard-success" role="status">{workspaceNotice}</div>}

        {!workspaceManager && <section className="dashboard-welcome">
          <div>
            <span className="dashboard-eyebrow"><DashboardIcon name="sparkles" size={14} /> MYWIKI WORKSPACE</span>
            <h1>Chào {profile.username} <span aria-hidden="true">✦</span></h1>
          </div>
          <button className="dashboard-create-button" type="button" onClick={() => {
            openWorkspaceManager("create");
          }}>
            <DashboardIcon name="plus" size={17} /> Tạo Workspace
          </button>
        </section>}

        {!workspaceManager && <section className="dashboard-stats" aria-label="Tổng quan Workspace">
          <article><span className="stat-icon stat-icon-green"><DashboardIcon name="layers" size={17} /></span><span><small>Tổng Workspace</small><strong>{workspaces.length}</strong></span></article>
          <article><span className="stat-icon stat-icon-gold"><DashboardIcon name="globe" size={17} /></span><span><small>Đang công khai</small><strong>{publicCount}</strong></span></article>
          <article><span className="stat-icon stat-icon-blue"><DashboardIcon name="folder" size={17} /></span><span><small>Chế độ riêng tư</small><strong>{workspaces.length - publicCount}</strong></span></article>
        </section>}

        {!workspaceManager && <section className="my-workspaces" id="my-workspaces">
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
                      <button className="workspace-more" type="button" aria-label={`Quản lý ${workspace.name}`} onClick={() => openWorkspaceManager(workspace)}><DashboardIcon name="dots" size={17} /></button>
                    </div>
                    <button className="workspace-card-content workspace-open-button" type="button" onClick={() => openWorkspaceManager(workspace)} aria-label={`Mở Workspace ${workspace.name}`}>
                      <h3>{workspace.name}</h3>
                      <p>{workspace.description || "Chưa có mô tả cho Workspace này."}</p>
                      <div className="workspace-card-meta">
                        <span><DashboardIcon name="clock" size={13} /> Cập nhật {new Date(workspace.updatedAt).toLocaleDateString("vi-VN")}</span>
                        <span className="owner-avatar">{initial}</span>
                      </div>
                    </button>
                  </article>
                );
              })}
            </div>
          ) : (
            <div className="dashboard-empty">
              <span className="dashboard-empty-icon"><DashboardIcon name="layers" size={24} /></span>
              <h3>{query || filter !== "Tất cả" ? "Không tìm thấy Workspace phù hợp" : "Bắt đầu với Workspace đầu tiên"}</h3>
              <p>{query || filter !== "Tất cả" ? "Thử từ khóa hoặc bộ lọc khác." : "Tạo một không gian để sắp xếp ý tưởng, tài liệu và các Page của bạn."}</p>
              {!query && filter === "Tất cả" && <button className="dashboard-create-button" type="button" onClick={() => openWorkspaceManager("create")}><DashboardIcon name="plus" size={16} /> Tạo Workspace</button>}
            </div>
          )}
        </section>
        }

        {workspaceManager && <WorkspaceManager
          key={workspaceManager === "create" ? "create-workspace" : workspaceManager.workspaceId}
          workspace={workspaceManager === "create" ? null : workspaceManager}
          onClose={closeWorkspaceManager}
          onCreated={(created) => {
            setWorkspaces((current) => [...current, created]);
            setWorkspaceNotice("Đã tạo Workspace.");
            setWorkspaceManager(created);
          }}
          onUpdated={(updated) => {
            setWorkspaces((current) => current.map((item) => item.workspaceId === updated.workspaceId ? updated : item));
            setWorkspaceManager(updated);
          }}
          onDeleted={(workspaceId) => {
            setWorkspaces((current) => current.filter((item) => item.workspaceId !== workspaceId));
            setWorkspaceNotice("Đã xóa Workspace.");
            closeWorkspaceManager();
          }}
        />}

      </main>
    </div>
  );
}
