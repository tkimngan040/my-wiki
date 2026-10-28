import { NextResponse } from "next/server";

type CsrfResponse = { token?: string };

export async function POST(request: Request) {
  const backendUrl = process.env.BACKEND_API_URL ?? "http://localhost:8080";
  const requestCookies = request.headers.get("cookie") ?? "";
  let csrfResponse: Response;

  try {
    csrfResponse = await fetch(`${backendUrl}/api/auth/csrf`, {
      headers: requestCookies ? { Cookie: requestCookies } : undefined,
      cache: "no-store",
    });
  } catch {
    return NextResponse.json(
      { code: "BACKEND_UNAVAILABLE", message: "Không kết nối được với máy chủ." },
      { status: 502 },
    );
  }

  if (!csrfResponse.ok) {
    return NextResponse.json(
      { code: "CSRF_UNAVAILABLE", message: "Không thể xác thực yêu cầu đăng xuất." },
      { status: 502 },
    );
  }

  let csrf: CsrfResponse;
  try {
    csrf = (await csrfResponse.json()) as CsrfResponse;
  } catch {
    return NextResponse.json(
      { code: "INVALID_BACKEND_RESPONSE", message: "Phản hồi bảo mật không hợp lệ." },
      { status: 502 },
    );
  }

  if (!csrf.token) {
    return NextResponse.json(
      { code: "CSRF_UNAVAILABLE", message: "Máy chủ không trả về mã bảo mật." },
      { status: 502 },
    );
  }

  const updatedCookies = [...csrfResponse.headers.getSetCookie()]
    .map((cookie) => cookie.split(";")[0])
    .filter(Boolean);
  const cookieHeader = [requestCookies, ...updatedCookies]
    .filter(Boolean)
    .join("; ");
  const headers = new Headers({ "X-XSRF-TOKEN": csrf.token });
  if (cookieHeader) headers.set("Cookie", cookieHeader);

  let upstream: Response;
  try {
    upstream = await fetch(`${backendUrl}/api/auth/logout`, {
      method: "POST",
      headers,
      cache: "no-store",
    });
  } catch {
    return NextResponse.json(
      { code: "BACKEND_UNAVAILABLE", message: "Không kết nối được với máy chủ." },
      { status: 502 },
    );
  }

  const responseHeaders = new Headers();
  csrfResponse.headers.getSetCookie().forEach((cookie) => {
    responseHeaders.append("Set-Cookie", cookie);
  });
  upstream.headers.getSetCookie().forEach((cookie) => {
    responseHeaders.append("Set-Cookie", cookie);
  });

  if (!upstream.ok) {
    const body = await upstream.text();
    return new Response(body || null, {
      status: upstream.status,
      headers: responseHeaders,
    });
  }

  return new Response(null, { status: upstream.status, headers: responseHeaders });
}
