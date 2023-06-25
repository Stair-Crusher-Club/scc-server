import React from "react";

const ACCESS_TOKEN_KEY = "SCC_ADMIN_ACCESS_TOKEN"

interface AuthContextType {
  isAuthenticated: () => boolean;
}

const accessToken = getSavedAccessToken();
export default React.createContext<AuthContextType>({
  isAuthenticated: () => false,
});

export function getSavedAccessToken(): string | null {
  return window.localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function saveAccessToken(accessToken: string) {
  window.localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
}

export function clearSavedAccessToken() {
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
}
