'use client';

import { useState, useEffect, useCallback } from 'react';

const USER_API_URL = "http://localhost:8080/admin/users";
const ITEMS_PER_PAGE = 7;

interface UserResponseDto {
  email: string;
  nickname: string;
  profileImage: string | null;
  role: 'USER' | 'ADMIN' | 'MANAGER';
}

export default function UserManagementPage() {
  const [allUsers, setAllUsers] = useState<UserResponseDto[]>([]);
  const [selectedRole, setSelectedRole] = useState<string>('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isUpdating, setIsUpdating] = useState(false);

  // 1. fetchData의 의존성을 비워 버튼 클릭 시 함수가 바뀌지 않도록 설정
  const fetchUsers = useCallback(async (isSilent = false) => {
    try {
      if (!isSilent) setIsInitialLoading(true);
      else setIsUpdating(true);

      const response = await fetch(USER_API_URL, { credentials: 'include' });
      const responseData = await response.json();
      setAllUsers(responseData.data || []);
    } catch (error) {
      console.error("Fetch Error:", error);
    } finally {
      setIsInitialLoading(false);
      setIsUpdating(false);
    }
  }, []); // 의존성 배열을 비워둠으로써 함수 참조값 고정

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // 계산 로직 (렌더링 시 수행)
  const filteredUsers = allUsers.filter(user => 
    selectedRole === 'ALL' ? true : user.role === selectedRole
  );
  const totalPages = Math.max(1, Math.ceil(filteredUsers.length / ITEMS_PER_PAGE));
  const visibleUsers = filteredUsers.slice((currentPage - 1) * ITEMS_PER_PAGE, currentPage * ITEMS_PER_PAGE);

  if (isInitialLoading) return <div className="p-10 text-center text-gray-500">로딩 중...</div>;

  return (
    <main className="max-w-6xl mx-auto p-10 bg-white min-h-screen">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">사용자 관리</h2>
        <p className="text-sm text-gray-500 mt-1">회원 목록을 조회하고 권한을 확인할 수 있습니다.</p>
        
        {/* 가로형 필터: 로딩 중일 때도 버튼 클릭이 막히지 않도록 유지 */}
        <div className="flex gap-2 mt-6 pb-2">
          {['ALL', 'USER', 'MANAGER', 'ADMIN'].map((role) => (
            <button
              key={role}
              type="button"
              disabled={isUpdating} // 업데이트 중에만 잠시 비활성화 (선택 사항)
              onClick={() => {
                setSelectedRole(role);
                setCurrentPage(1);
              }}
              className={`px-4 py-1.5 rounded-full text-xs font-bold transition-all border ${
                selectedRole === role 
                ? 'bg-[#2D2D2D] text-white border-[#2D2D2D]' 
                : 'bg-white text-gray-400 border-gray-200 hover:border-gray-400'
              } ${isUpdating ? 'opacity-50 cursor-not-allowed' : ''}`}
            >
              {role} ({allUsers.filter(u => role === 'ALL' ? true : u.role === role).length})
            </button>
          ))}
        </div>
      </div>

      <div className="h-[600px] flex flex-col justify-between relative">
        {/* 테이블 업데이트 시 살짝 흐리게 처리 (번쩍임 대신 부드러운 효과) */}
        <div className={`overflow-hidden border border-gray-200 rounded-2xl shadow-sm bg-white transition-opacity ${isUpdating ? 'opacity-50' : 'opacity-100'}`}>
          <table className="w-full text-left border-collapse table-fixed">
            <thead>
              <tr className="bg-gray-50 border-b border-gray-200">
                <th className="w-[100px] px-6 py-4 text-[11px] font-bold text-gray-400 uppercase">Profile</th>
                <th className="w-[200px] px-6 py-4 text-[11px] font-bold text-gray-400 uppercase">Nickname</th>
                <th className="px-6 py-4 text-[11px] font-bold text-gray-400 uppercase">Email</th>
                <th className="w-[120px] px-6 py-4 text-[11px] font-bold text-gray-400 uppercase text-center">Role</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {visibleUsers.length > 0 ? (
                visibleUsers.map((user, idx) => (
                  <tr key={`${user.email}-${idx}`} className="h-[62px]">
                    <td className="px-6 py-3">
                      <div className="w-9 h-9 rounded-full bg-gray-100 overflow-hidden border border-gray-200">
                        {user.profileImage && <img src={user.profileImage} alt="" className="w-full h-full object-cover" />}
                      </div>
                    </td>
                    <td className="px-6 py-3 font-bold text-gray-800 truncate text-sm">{user.nickname}</td>
                    <td className="px-6 py-3 text-sm text-gray-500 truncate">{user.email}</td>
                    <td className="px-6 py-3 text-center">
                      <span className={`inline-block min-w-[70px] px-2 py-1 rounded-md text-[10px] font-black border ${
                        user.role === 'ADMIN' ? 'bg-purple-50 text-purple-600 border-purple-100' : 
                        user.role === 'MANAGER' ? 'bg-blue-50 text-blue-600 border-blue-100' : 
                        'bg-gray-50 text-gray-500 border-gray-200'
                      }`}>
                        {user.role}
                      </span>
                    </td>
                  </tr>
                ))
              ) : null}
              {/* 공백 노드 방지를 위해 tr로만 채움 */}
              {Array.from({ length: ITEMS_PER_PAGE - visibleUsers.length }).map((_, i) => (
                <tr key={`empty-${i}`} className="h-[62px] border-none"><td colSpan={4} /></tr>
              ))}
            </tbody>
          </table>
          
          {filteredUsers.length === 0 && !isUpdating && (
            <div className="absolute inset-0 flex items-center justify-center text-gray-300 text-sm pointer-events-none">
              검색된 사용자가 없습니다.
            </div>
          )}
        </div>

        {/* 페이징 섹션 */}
        <div className="flex items-center justify-center gap-6 py-6 mt-2">
          <button 
            type="button"
            onClick={() => setCurrentPage(p => Math.max(1, p - 1))} 
            disabled={currentPage === 1 || isUpdating}
            className="text-xs font-black disabled:opacity-20 hover:text-black"
          >
            PREV
          </button>
          <div className="flex items-center gap-2">
            {Array.from({ length: totalPages }, (_, i) => i + 1).map(num => (
              <button
                key={num}
                type="button"
                onClick={() => setCurrentPage(num)}
                className={`w-8 h-8 rounded-lg text-xs font-bold ${
                  currentPage === num ? 'bg-gray-900 text-white' : 'text-gray-400 hover:bg-gray-100'
                }`}
              >
                {num}
              </button>
            ))}
          </div>
          <button 
            type="button"
            onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))} 
            disabled={currentPage === totalPages || isUpdating}
            className="text-xs font-black disabled:opacity-20 hover:text-black"
          >
            NEXT
          </button>
        </div>
      </div>
    </main>
  );
}