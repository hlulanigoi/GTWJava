import type { Response } from "express";

/** Matches the Android app's ApiResponse<T> Gson model: { success, message, data, error } */
export function ok<T>(res: Response, data: T, message = "OK", status = 200) {
  return res.status(status).json({ success: true, message, data, error: null });
}

export function paginated<T>(
  res: Response,
  items: T[],
  total: number,
  page: number,
  perPage: number,
  message = "OK",
) {
  return ok(res, {
    data: items,
    total,
    page,
    per_page: perPage,
    has_more: page * perPage < total,
  }, message);
}

export function fail(res: Response, error: string, status = 400) {
  return res.status(status).json({ success: false, message: error, data: null, error });
}
