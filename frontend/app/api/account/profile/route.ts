import { NextResponse } from "next/server";

export async function GET(request: Request) {
  const backendUrl = process.env.BACKEND_API_URL ?? "http://localhost:8080";
  const headers = new Headers();
  const cookies = request.headers.get("cookie");

  if (cookies) headers.set("Cookie", cookies);

  let upstream: Response;
  try {
    upstream = await fetch(`${backendUrl}/api/users/me`, {
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
