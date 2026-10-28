import { NextResponse } from "next/server";

type LoginPayload = {
  email?: string;
  password?: string;
};

export async function POST(request: Request) {
  let payload: LoginPayload;

  try {
    payload = (await request.json()) as LoginPayload;
  } catch {
    return NextResponse.json(
      { code: "VALIDATION_ERROR", message: "Dữ liệu gửi lên không hợp lệ." },
      { status: 400 },
    );
  }

  const backendUrl = process.env.BACKEND_API_URL ?? "http://localhost:8080";
  const headers = new Headers({ "Content-Type": "application/json" });
  const cookies = request.headers.get("cookie");

  if (cookies) {
    headers.set("Cookie", cookies);
  }

  let upstream: Response;

  try {
    upstream = await fetch(`${backendUrl}/api/auth/login`, {
      method: "POST",
      headers,
      body: JSON.stringify(payload),
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
    const responseHeaders = new Headers();
    upstream.headers.getSetCookie().forEach((cookie) => {
      responseHeaders.append("Set-Cookie", cookie);
    });

    return NextResponse.json(JSON.parse(responseBody), {
      status: upstream.status,
      headers: responseHeaders,
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
