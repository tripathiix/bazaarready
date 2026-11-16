export type SessionStatus = "RUNNING" | "WAITING_OWNER" | "WAITING_APPROVAL" | "DONE" | "FAILED";

export type ActionStatus = "DONE" | "WAITING_OWNER" | "WAITING_APPROVAL" | "APPROVED" | "DECLINED" | "FAILED";

export interface ActionView {
  stepNo: number;
  tool: string;
  text: string;
  status: ActionStatus;
  data: { question?: string; answer?: string; items?: number; faqs?: number; messages?: number; customers?: number } | null;
}

export interface SessionView {
  id: string;
  status: SessionStatus;
  question: string | null;
  questionType: "text" | "phone" | null;
  approvalRequest: string | null;
  finalMessage: string | null;
  businessId: string | null;
  shopPath: string | null;
  ownerPath: string | null;
  published: boolean;
  actions: ActionView[];
}

async function post<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
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
  const response = await fetch("/api/agent/sessions/whatsapp", { method: "POST", body: form });
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
  if (!response.ok) throw new Error(`Could not load that session (${response.status}).`);
  return response.json() as Promise<SessionView>;
}

export const sendReply = (id: string, answer: string) =>
  post<SessionView>(`/api/agent/sessions/${id}/reply`, { answer });

export const sendApproval = (id: string, approved: boolean) =>
  post<SessionView>(`/api/agent/sessions/${id}/approval`, { approved });
