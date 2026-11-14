export type User = { id: string; username: string; email: string };
export type Loadout = {
  id: string; title: string; description: string | null; category: string | null;
  ownerId: string; ownerUsername: string; createdAt: string; draft: boolean; publiclyVisible: boolean;
  knownTotal: number | null; pricedCount: number; missingPriceCount: number;
};
export type ProductFields = {
  name: string; url: string | null; note: string | null; store: string | null;
  price: number | null; availability: string | null;
};
export type Product = ProductFields & { id: string };
export type Feedback = { id: string; username: string; body: string; createdAt: string };
export type Community = { likes: number; liked: boolean; bookmarked: boolean; comments: Feedback[]; reviews: Feedback[] };
export type Profile = { id: string; username: string; displayName: string | null; bio: string | null; privateProfile: boolean; loadouts: Loadout[] };
export const categories = ['Cooking', 'Outdoors', 'Technology', 'Photography', 'Fitness', 'Other'];
export const usd = (amount: number) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);

export class ApiError extends Error {
  constructor(public status: number, message: string) { super(message); }
}
export async function api<T>(path: string, method = 'GET', body?: object, signal?: AbortSignal): Promise<T> {
  let response: Response;
  try {
    response = await fetch('/api' + path, {
      method, signal, credentials: 'same-origin',
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (error) {
    if (signal?.aborted) throw error;
    throw new ApiError(0, 'Could not reach Loadouts. Check that the backend is running.');
  }
  if (!response.ok) {
    const json = await response.json().catch(() => null);
    const fallback = response.status >= 500 ? 'The server could not complete this request. Try again in a moment.'
      : response.status === 404 ? 'This page is unavailable.' : 'This action could not be completed.';
    throw new ApiError(response.status, typeof json?.error === 'string' && json.error !== 'Not Found' ? json.error : fallback);
  }
  return response.status === 204 ? undefined as T : response.json();
}
export const byId = (id: string) => '/loadouts/' + encodeURIComponent(id);
export const message = (error: unknown) => error instanceof Error ? error.message : 'Something went wrong.';

