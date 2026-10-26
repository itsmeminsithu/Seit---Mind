import type { Metadata } from "next";
import "./globals.css";
export const metadata: Metadata = { title: "စိတ် - Mind | Meditation", description: "Your space for peace, focus & better sleep.", manifest: "/manifest.webmanifest", icons: { icon: "/app-icon.svg" } };
export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) { return <html lang="my"><body>{children}</body></html>; }
