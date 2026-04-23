"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { apiGet, apiPostEmpty } from "@/lib/api";
import type { MyPageUser } from "@/lib/types";

type AuthState = {
  user: MyPageUser | null;
  loading: boolean;
  refresh: () => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<MyPageUser | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      const r = await apiGet<MyPageUser>("/users/me");
      setUser(r.data ?? null);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await apiPostEmpty("/auth/logout");
    } catch { /* ignore */ }
    setUser(null);
  }, []);

  useEffect(() => {
    void refresh();
    const handleAuthRefreshed = () => { void refresh(); };
    window.addEventListener("auth-refreshed", handleAuthRefreshed);
    return () => window.removeEventListener("auth-refreshed", handleAuthRefreshed);
  }, [refresh]);

  const value = useMemo(() => ({ user, loading, refresh, logout }), [user, loading, refresh, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};