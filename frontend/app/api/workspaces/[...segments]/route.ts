import {
  invalidWorkspaceRoute,
  isWorkspaceRoute,
  methodNotAllowed,
  proxyWorkspaceRequest,
  resolveWorkspaceRoute,
} from "../proxy";

async function handle(request: Request) {
  const pathname = new URL(request.url).pathname;
  const prefix = "/api/workspaces/";
  if (!pathname.startsWith(prefix)) return invalidWorkspaceRoute();

  let segments: string[];
  try {
    segments = pathname
      .slice(prefix.length)
      .split("/")
      .map((segment) => decodeURIComponent(segment));
  } catch {
    return invalidWorkspaceRoute();
  }

  const upstreamPath = resolveWorkspaceRoute(segments, request.method);
  if (!upstreamPath) {
    return isWorkspaceRoute(segments)
      ? methodNotAllowed()
      : invalidWorkspaceRoute();
  }
  return proxyWorkspaceRequest(request, upstreamPath);
}

export function GET(request: Request) {
  return handle(request);
}

export function POST(request: Request) {
  return handle(request);
}

export function PUT(request: Request) {
  return handle(request);
}

export function DELETE(request: Request) {
  return handle(request);
}

export function PATCH() {
  return methodNotAllowed();
}
