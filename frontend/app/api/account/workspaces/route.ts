import { NextResponse } from "next/server";

export async function GET(request: Request) {
  const backendUrl = process.env.BACKEND_API_URL ?? "http://localhost:8080";
  const requestUrl = new URL(request.url);
  const query = requestUrl.searchParams;
  const page = Number(query.get("page") ?? "0");
  const size = Number(query.get("size") ?? "100");

  if (
    !Number.isInteger(page) ||
    page < 0 ||
    !Number.isInteger(size) ||
    size < 1 ||
    size > 100
  ) {
    return NextResponse.json(
      { code: "VALIDATION_ERROR", message: "Tham số phân trang không hợp lệ." },
      { status: 400 },
    );
  }

  const headers = new Headers();
  const cookies = request.headers.get("cookie");
  if (cookies) headers.set("Cookie", cookies);
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort: "updatedAt",
    direction: "desc",
  });

  let upstream: Response;
  try {
    upstream = await fetch(`${backendUrl}/api/workspaces?${params}`, {
      headers,
      cache: "no-store",
    });
  } catch {
    return NextResponse.json(
      { code: "BACKEND_UNAVAILABLE", message: "Không kết nối được với máy chủ." },
      { status: 502 },
    );
  }

  const body = await upstream.text();
  try {
    return NextResponse.json(JSON.parse(body), { status: upstream.status });
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
