import { setAuthTokenGetter } from "@workspace/api-client-react";

export function initAuth() {
  setAuthTokenGetter(() => localStorage.getItem("auth_token"));
}

export const setToken = (t: string | null) => {
  if (t) {
    localStorage.setItem("auth_token", t);
  } else {
    localStorage.removeItem("auth_token");
  }
};

export const getToken = () => localStorage.getItem("auth_token");
