import { NextResponse } from "next/server";

type RegisterPayload = {
  username?: string;
  email?: string;
  password?: string;
};

export async function POST(request: Request) {
  let payload: RegisterPayload;

  try {
    payload = (await request.json()) as RegisterPayload;
  } catch {
    return NextResponse.json(
      { code: "VALIDATION_ERROR", message: "Dữ liệu gửi lên không hợp lệ." },
      { status: 400 },
    );
  }

  const backendUrl = process.env.BACKEND_API_URL ?? "http://localhost:8080";
  let upstream: Response;

  try {
    upstream = await fetch(`${backendUrl}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
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
