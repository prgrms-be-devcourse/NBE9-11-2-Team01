// components/Sidebar.tsx
import Link from 'next/link';

export default function Sidebar() {
  const menus = [
    { name: '카테고리 관리', href: '/admin/categories' },
    { name: '게시판 관리', href: '/admin/boards' },
    { name: '회원 관리', href: '/admin/users' },
  ];

  return (
    <aside className="w-64 min-h-[500px] bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
      <nav className="space-y-6">
        {menus.map((menu) => (
          <Link 
            key={menu.name} 
            href={menu.href}
            className="block rounded-xl px-3 py-2 text-gray-600 transition-colors hover:bg-blue-50 hover:text-black"
          >
            {menu.name}
          </Link>
        ))}
      </nav>
    </aside>
  );
}