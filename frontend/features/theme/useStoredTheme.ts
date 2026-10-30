"use client";

import { useEffect, useSyncExternalStore } from "react";

type Theme = "light" | "dark";

function subscribeToTheme(onChange: () => void) {
  window.addEventListener("storage", onChange);
  window.addEventListener("mywiki-theme-change", onChange);
  return () => {
    window.removeEventListener("storage", onChange);
    window.removeEventListener("mywiki-theme-change", onChange);
  };
}

function getThemeSnapshot(): Theme {
  return window.localStorage.getItem("mywiki-theme") === "dark" ? "dark" : "light";
}

function getServerThemeSnapshot(): Theme {
  return "light";
}

export function useStoredTheme(): Theme {
  const theme = useSyncExternalStore(
    subscribeToTheme,
    getThemeSnapshot,
    getServerThemeSnapshot,
  );
  useEffect(() => {
    document.documentElement.dataset.theme = theme;
  }, [theme]);

  return theme;
}
