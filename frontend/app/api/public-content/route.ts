import { NextResponse } from "next/server";

const pageTypes = new Set(["workspaces", "folders", "pages"]);

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const type = searchParams.get("type");
  const page = Number(searchParams.get("page") ?? "0");
  const size = Number(searchParams.get("size") ?? "100");
  const workspaceId = Number(searchParams.get("workspaceId"));

  if (
    !type ||
    !pageTypes.has(type) ||
    !Number.isInteger(page) ||
    page < 0 ||
    !Number.isInteger(size) ||
    size < 1 ||
    size > 100 ||
    (type !== "workspaces" &&
      (!Number.isInteger(workspaceId) || workspaceId < 1))
  ) {
    return NextResponse.json(
      { code: "VALIDATION_ERROR", message: "Tham số tìm kiếm không hợp lệ." },
      { status: 400 },
    );
  }

  const backendUrl = process.env.BACKEND_API_URL ?? "http://localhost:8080";
  const path =
    type === "workspaces"
      ? "/api/workspaces"
      : `/api/workspaces/${workspaceId}/${type}`;
  const query = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort:
      type === "workspaces"
        ? "updatedAt"
        : type === "pages"
          ? "title"
          : "name",
    direction: type === "workspaces" ? "desc" : "asc",
  });

  let upstream: Response;

  try {
    upstream = await fetch(`${backendUrl}${path}?${query}`, {
      cache: "no-store",
    });
  } catch {
    return NextResponse.json(
      {
        code: "BACKEND_UNAVAILABLE",
        message:
          "Không kết nối được với máy chủ. Hãy kiểm tra backend đang chạy rồi thử lại.",
      },
      { status: 502 },
    );
  }

  const responseBody = await upstream.text();

  try {
    return NextResponse.json(JSON.parse(responseBody), {
      status: upstream.status,
    });
  } catch {
    return NextResponse.json(
      {
        code: "INVALID_BACKEND_RESPONSE",
        message: "Máy chủ trả về phản hồi không hợp lệ.",
      },
      { status: 502 },
    );
  }
}
