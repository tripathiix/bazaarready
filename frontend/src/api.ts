export type SessionStatus = "RUNNING" | "WAITING_OWNER" | "WAITING_APPROVAL" | "DONE" | "FAILED";

export type ActionStatus = "DONE" | "WAITING_OWNER" | "WAITING_APPROVAL" | "APPROVED" | "DECLINED" | "FAILED";

export interface ActionView {
  stepNo: number;
  tool: string;
  text: string;
  status: ActionStatus;
  data: { question?: string; answer?: string; items?: number; faqs?: number; messages?: number; customers?: number } | null;
}

export interface FieldView {
  label: string;
  kind: "text" | "long" | "choice" | "email" | "date" | "time" | "number";
  placeholder: string;
  options: string[];
}

export interface SessionView {
  id: string;
  status: SessionStatus;
  question: string | null;
  questionType: "text" | "phone" | null;
  fields: FieldView[];
  approvalRequest: string | null;
  finalMessage: string | null;
  businessId: string | null;
  shopPath: string | null;
  ownerPath: string | null;
  published: boolean;
  actions: ActionView[];
}

/** The CSRF token the server put in a cookie; sent back on every change so other sites can't act as you. */
export function csrfToken(): string {
  const m = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]+)/);
  return m ? decodeURIComponent(m[1]) : "";
}

/** When the server says "sign in first", go to the sign-in page. */
export function signInIfNeeded(response: Response) {
  if (response.status === 401) {
    window.location.href = "/login";
    throw new Error("Please sign in first.");
  }
}

export interface Me {
  signedIn: boolean;
  name: string | null;
  authForBuilding: boolean;
}

export async function getMe(): Promise<Me> {
  try {
    const response = await fetch("/api/me");
    return response.ok ? ((await response.json()) as Me) : { signedIn: false, name: null, authForBuilding: false };
  } catch {
    return { signedIn: false, name: null, authForBuilding: false };
  }
}

async function post<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": csrfToken() },
    body: JSON.stringify(body),
  });
  signInIfNeeded(response);
  if (!response.ok) {
    let detail = `The server answered with an error (${response.status}).`;
    try {
      const problem = await response.json();
      if (typeof problem.detail === "string") detail = problem.detail;
    } catch {
      // keep the generic message
    }
    throw new Error(detail);
  }
  return response.json() as Promise<T>;
}

export const startSession = (description: string, language: string) =>
  post<SessionView>("/api/agent/sessions", { description, language });

export async function startFromChats(files: File[], language: string, ownerName: string): Promise<SessionView> {
  const form = new FormData();
  files.forEach((file) => form.append("files", file));
  form.append("language", language);
  form.append("ownerName", ownerName);
  const response = await fetch("/api/agent/sessions/whatsapp", { method: "POST", body: form, headers: { "X-XSRF-TOKEN": csrfToken() } });
  signInIfNeeded(response);
  if (!response.ok) {
    let detail = `The server answered with an error (${response.status}).`;
    try {
      const problem = await response.json();
      if (typeof problem.detail === "string") detail = problem.detail;
    } catch {
      // keep the generic message
    }
    throw new Error(detail);
  }
  return response.json() as Promise<SessionView>;
}

export const startUpdate = (ownerKey: string, feedback: string, language: string) =>
  post<SessionView>("/api/agent/sessions/update", { ownerKey, feedback, language });

export async function getSession(id: string): Promise<SessionView> {
  const response = await fetch(`/api/agent/sessions/${id}`);
  signInIfNeeded(response);
  if (!response.ok) throw new Error(`Could not load that session (${response.status}).`);
  return response.json() as Promise<SessionView>;
}

export const sendReply = (id: string, answer: string) =>
  post<SessionView>(`/api/agent/sessions/${id}/reply`, { answer });

export const sendApproval = (id: string, approved: boolean) =>
  post<SessionView>(`/api/agent/sessions/${id}/approval`, { approved });
