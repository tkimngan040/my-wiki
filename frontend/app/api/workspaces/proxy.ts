import { NextResponse } from "next/server";

type CsrfResponse = { token?: string };

const backendUnavailable = () =>
  NextResponse.json(
    { code: "BACKEND_UNAVAILABLE", message: "Không kết nối được với máy chủ." },
    { status: 502 },
  );

function appendSetCookies(headers: Headers, response: Response) {
  response.headers.getSetCookie().forEach((cookie) => {
    headers.append("Set-Cookie", cookie);
  });
}

function errorResponse(
  code: string,
  message: string,
  status: number,
  cookies: string[] = [],
) {
  const headers = new Headers();
  cookies.forEach((cookie) => headers.append("Set-Cookie", cookie));
  return NextResponse.json({ code, message }, { status, headers });
}

function mergeCookies(requestCookies: string, csrfCookies: string[]) {
  const pairs = new Map<string, string>();

  for (const cookie of requestCookies.split(";")) {
    const pair = cookie.trim();
    const separator = pair.indexOf("=");
    if (separator > 0) pairs.set(pair.slice(0, separator).trim(), pair);
  }

  for (const cookie of csrfCookies) {
    const pair = cookie.split(";", 1)[0].trim();
    const separator = pair.indexOf("=");
    if (separator > 0) pairs.set(pair.slice(0, separator).trim(), pair);
  }

  return [...pairs.values()].join("; ");
}

export async function proxyWorkspaceRequest(
  request: Request,
  upstreamPath: string,
) {
  const backendUrl = (
    process.env.BACKEND_API_URL ?? "http://localhost:8080"
  ).replace(/\/+$/, "");
  const method = request.method.toUpperCase();
  const requestCookies = request.headers.get("cookie") ?? "";
  const isMutation = method === "POST" || method === "PUT" || method === "DELETE";
  let csrfResponse: Response | undefined;
  let csrfCookies: string[] = [];
  const headers = new Headers();

  if (isMutation) {
    try {
      csrfResponse = await fetch(`${backendUrl}/api/auth/csrf`, {
        headers: requestCookies ? { Cookie: requestCookies } : undefined,
        cache: "no-store",
      });
    } catch {
      return backendUnavailable();
    }

    csrfCookies = csrfResponse.headers.getSetCookie();
    if (!csrfResponse.ok) {
      return errorResponse(
        "CSRF_UNAVAILABLE",
        "Không thể xác thực yêu cầu.",
        502,
        csrfCookies,
      );
    }

    let csrf: CsrfResponse;
    try {
      csrf = (await csrfResponse.json()) as CsrfResponse;
    } catch {
      return errorResponse(
        "INVALID_BACKEND_RESPONSE",
        "Phản hồi bảo mật không hợp lệ.",
        502,
        csrfCookies,
      );
    }

    if (!csrf.token) {
      return errorResponse(
        "CSRF_UNAVAILABLE",
        "Máy chủ không trả về mã bảo mật.",
        502,
        csrfCookies,
      );
    }

    headers.set("X-XSRF-TOKEN", csrf.token);
    const mergedCookies = mergeCookies(requestCookies, csrfCookies);
    if (mergedCookies) headers.set("Cookie", mergedCookies);
  } else if (requestCookies) {
    headers.set("Cookie", requestCookies);
  }

  let body: string | undefined;
  if (method === "POST" || method === "PUT") {
    try {
      body = JSON.stringify(await request.json());
    } catch {
      return errorResponse(
        "VALIDATION_ERROR",
        "Dữ liệu gửi lên không hợp lệ.",
        400,
        csrfCookies,
      );
    }
    headers.set("Content-Type", "application/json");
  }

  const query = new URL(request.url).search;
  let upstream: Response;
  try {
    upstream = await fetch(`${backendUrl}${upstreamPath}${query}`, {
      method,
      headers,
      body,
      cache: "no-store",
    });
  } catch {
    return errorResponse(
      "BACKEND_UNAVAILABLE",
      "Không kết nối được với máy chủ.",
      502,
      csrfCookies,
    );
  }

  const responseHeaders = new Headers();
  if (csrfResponse) appendSetCookies(responseHeaders, csrfResponse);
  appendSetCookies(responseHeaders, upstream);

  if (upstream.status === 204 || upstream.status === 205) {
    return new Response(null, { status: upstream.status, headers: responseHeaders });
  }

  const responseBody = await upstream.text();
  if (!responseBody) {
    return new Response(null, { status: upstream.status, headers: responseHeaders });
  }

  try {
    return NextResponse.json(JSON.parse(responseBody), {
      status: upstream.status,
      headers: responseHeaders,
    });
  } catch {
    return errorResponse(
      "INVALID_BACKEND_RESPONSE",
      "Máy chủ trả về phản hồi không hợp lệ.",
      502,
      responseHeaders.getSetCookie(),
    );
  }
}

const identifierPattern = /^[A-Za-z0-9_-]+$/;

export function isWorkspaceRoute(segments: string[]) {
  if (
    segments.length === 0 ||
    segments.some((segment) => !identifierPattern.test(segment))
  ) {
    return false;
  }

  const [, resource, , subresource] = segments;
  return (
    segments.length === 1 ||
    (segments.length === 2 &&
      (resource === "folders" || resource === "pages")) ||
    (segments.length === 3 &&
      (resource === "folders" || resource === "pages")) ||
    (segments.length === 4 &&
      resource === "pages" &&
      subresource === "links") ||
    (segments.length === 5 &&
      resource === "pages" &&
      subresource === "links")
  );
}

export function resolveWorkspaceRoute(
  segments: string[],
  method: string,
): string | null {
  if (!isWorkspaceRoute(segments)) return null;

  const [workspaceId, resource, resourceId, subresource, subresourceId] = segments;
  const normalizedMethod = method.toUpperCase();

  if (segments.length === 1 && ["GET", "PUT", "DELETE"].includes(normalizedMethod)) {
    return `/api/workspaces/${workspaceId}`;
  }

  if (
    segments.length === 2 &&
    (resource === "folders" || resource === "pages") &&
    ["GET", "POST"].includes(normalizedMethod)
  ) {
    return `/api/workspaces/${workspaceId}/${resource}`;
  }

  if (
    segments.length === 3 &&
    (resource === "folders" || resource === "pages") &&
    ["GET", "PUT", "DELETE"].includes(normalizedMethod)
  ) {
    return `/api/workspaces/${workspaceId}/${resource}/${resourceId}`;
  }

  if (
    segments.length === 4 &&
    resource === "pages" &&
    subresource === "links" &&
    ["GET", "POST"].includes(normalizedMethod)
  ) {
    return `/api/workspaces/${workspaceId}/pages/${resourceId}/links`;
  }

  if (
    segments.length === 5 &&
    resource === "pages" &&
    subresource === "links" &&
    normalizedMethod === "DELETE"
  ) {
    return `/api/workspaces/${workspaceId}/pages/${resourceId}/links/${subresourceId}`;
  }

  return null;
}

export function methodNotAllowed() {
  return NextResponse.json(
    { code: "METHOD_NOT_ALLOWED", message: "Phương thức không được hỗ trợ." },
    { status: 405 },
  );
}

export function invalidWorkspaceRoute() {
  return NextResponse.json(
    { code: "NOT_FOUND", message: "Đường dẫn không hợp lệ." },
    { status: 404 },
  );
}
