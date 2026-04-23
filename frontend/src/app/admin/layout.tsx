import Sidebar from "./page";

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="mx-auto flex min-h-screen w-[1280px] justify-center bg-blue-50/30">
      <div className="sticky top-0 self-start shrink-0 pl-12 pr-6 py-10">
        <Sidebar />
      </div>
      <main className="min-w-0 flex-1 py-10 pr-10">{children}</main>
    </div>
  );
}