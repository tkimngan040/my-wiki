import {
  methodNotAllowed,
  proxyWorkspaceRequest,
} from "./proxy";

export function GET(request: Request) {
  return proxyWorkspaceRequest(request, "/api/workspaces");
}

export function POST(request: Request) {
  return proxyWorkspaceRequest(request, "/api/workspaces");
}

export function PUT() {
  return methodNotAllowed();
}

export function DELETE() {
  return methodNotAllowed();
}
