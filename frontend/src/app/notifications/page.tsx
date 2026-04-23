'use client';

import { useState, useEffect, useCallback } from 'react';

const NOTI_API_URL = "http://localhost:8080/notifications";
const ITEMS_PER_PAGE = 8;

interface NotificationResponseDto {
  id: number;
  receiverId: number;
  senderId: number;
  targetId: number;
  content: string;
  isRead: boolean;
  createdAt: string;
}

export default function NotificationManagementPage() {
  const [allNotifications, setAllNotifications] = useState<NotificationResponseDto[]>([]);
  const [filter, setFilter] = useState<'ALL' | 'UNREAD' | 'READ'>('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isUpdating, setIsUpdating] = useState(false);

  const fetchNotifications = useCallback(async (isSilent = false) => {
    try {
      if (!isSilent) setIsInitialLoading(true);
      else setIsUpdating(true);

      const response = await fetch(NOTI_API_URL, { credentials: 'include' });
      const responseData = await response.json();
      setAllNotifications(responseData.data || []);
    } catch (error) {
      console.error("Fetch Error:", error);
    } finally {
      setIsInitialLoading(false);
      setIsUpdating(false);
    }
  }, []);

  useEffect(() => { fetchNotifications(); }, [fetchNotifications]);

  const handleMarkAsRead = async (id: number, targetId?: number) => {
    try {
      await fetch(`${NOTI_API_URL}/${id}`, { // API 엔드포인트는 환경에 맞게 수정하세요
        method: 'PUT',
        credentials: 'include'
      });
      
      // 상태 업데이트 후 데이터 새로고침
      fetchNotifications(true);

      // targetId가 있으면 해당 페이지로 이동
      if (targetId) {
        window.open(`http://localhost:3000/posts/${targetId}`, '_blank');
      }
    } catch (error) {
      console.error("Read Update Error:", error);
    }
  };

  const filteredNotis = allNotifications.filter(noti => {
    if (filter === 'UNREAD') return !noti.isRead;
    if (filter === 'READ') return noti.isRead;
    return true;
  });

  const totalPages = Math.max(1, Math.ceil(filteredNotis.length / ITEMS_PER_PAGE));
  const visibleNotis = filteredNotis.slice((currentPage - 1) * ITEMS_PER_PAGE, currentPage * ITEMS_PER_PAGE);

  // 메시지 분리 함수
  const splitContent = (content: string) => {
    const dotIndex = content.indexOf('.');
    if (dotIndex === -1) return { title: content, body: '' };
    return {
      title: content.substring(0, dotIndex + 1),
      body: content.substring(dotIndex + 1).trim()
    };
  };

  if (isInitialLoading) return <div className="p-10 text-center text-gray-500 font-medium">로딩 중...</div>;

  return (
    <main className="max-w-7xl mx-auto p-10 flex gap-10 bg-white min-h-screen">
      <aside className="w-[240px] flex-shrink-0 sticky top-10 h-fit">
        <h2 className="text-xs font-black text-gray-400 uppercase tracking-widest mb-6">Filter</h2>
        {/* <nav className="flex flex-col gap-2">
          {(['ALL', 'UNREAD', 'READ'] as const).map((status) => (
            <button
              key={status}
              type="button"
              onClick={() => { setFilter(status); setCurrentPage(1); }}
              className={`w-full text-left px-5 py-4 rounded-2xl transition-all border ${
                filter === status 
                ? 'bg-[#2D2D2D] text-white border-[#2D2D2D] shadow-lg' 
                : 'bg-gray-50 text-gray-500 border-transparent hover:bg-gray-100'
              }`}
            >
              <p className="font-bold text-sm">{status === 'ALL' ? '전체' : status === 'UNREAD' ? '새로운 알림' : '확인한 알림'}</p>
              <p className="text-[10px] mt-1 opacity-60">
                {allNotifications.filter(n => status === 'ALL' ? true : status === 'READ' ? n.isRead : !n.isRead).length} 건
              </p>
            </button>
          ))}
        </nav> */}
        <nav className="flex flex-col gap-2">
          {(['ALL', 'UNREAD'] as const).map((status) => (
            <button
              key={status}
              type="button"
              onClick={() => { setFilter(status); setCurrentPage(1); }}
              className={`w-full text-left px-5 py-4 rounded-2xl transition-all border ${
                filter === status 
                ? 'bg-[#2D2D2D] text-white border-[#2D2D2D] shadow-lg' 
                : 'bg-gray-50 text-gray-500 border-transparent hover:bg-gray-100'
              }`}
            >
              <p className="font-bold text-sm">{status === 'ALL' ? '전체' : status === 'UNREAD' ? '새로운 알림' : '확인한 알림'}</p>
              <p className="text-[10px] mt-1 opacity-60">
                {allNotifications.filter(n => status === 'ALL' ? true : !n.isRead).length} 건
              </p>
            </button>
          ))}
        </nav>
      </aside>

      <div className="flex-1 flex flex-col min-w-0">
        <div className="mb-8">
          <h1 className="text-2xl font-black text-gray-900 tracking-tight">알림</h1>
          <p className="text-sm text-gray-500 mt-1 font-medium"></p>
        </div>

        <div className="h-[620px] flex flex-col justify-between">
          <div className={`overflow-hidden border border-gray-100 rounded-[24px] shadow-sm bg-white ${isUpdating ? 'opacity-50' : 'opacity-100'}`}>
            <table className="w-full text-left border-collapse table-fixed">
              <thead>
                <tr className="bg-gray-50/50 border-b border-gray-100">
                  <th className="px-6 py-5 text-[11px] font-black text-gray-400 uppercase">알림</th>
                  <th className="w-[120px] px-6 py-5 text-[11px] font-black text-gray-400 uppercase text-center">날짜</th>
                  <th className="w-[80px] px-6 py-5 text-[11px] font-black text-gray-400 uppercase text-center">읽음</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {visibleNotis.map((noti) => {
                  const { title, body } = splitContent(noti.content);
                  return (
                    <tr 
                      key={noti.id} 
                      className={`h-[80px] transition-colors cursor-pointer ${noti.isRead ? 'bg-white' : 'bg-blue-50/40 hover:bg-blue-50/60'}`}
                    >
                      <td className="px-6 py-3"
                        onClick={() => handleMarkAsRead(noti.id, noti.targetId)}
                        >
                        <div className="flex items-start gap-3">
                          <span className={`text-lg mt-0.5 ${noti.isRead ? 'grayscale opacity-30' : ''}`}>🔔</span>
                          <div className="flex flex-col min-w-0">
                            <span className={`text-sm truncate ${noti.isRead ? 'text-gray-400 font-medium' : 'text-gray-900 font-bold'}`}>
                              {title}
                            </span>
                            {body && (
                              <span className={`text-[12px] truncate mt-0.5 ${noti.isRead ? 'text-gray-300' : 'text-gray-500 font-medium'}`}>
                                {body}
                              </span>
                            )}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-3 text-center text-[11px] text-gray-400 font-medium font-mono"                       
                        onClick={() => handleMarkAsRead(noti.id, noti.targetId)}
                      >
                        {noti.createdAt.split('T')[0].replace(/-/g, '.')}
                      </td>
                      <td className="px-6 py-3 text-center">
                      <div className="flex justify-center items-center">
                        {noti.isRead ? (
                        /* 1. 읽은 상태: 테두리 없는 연한 그레이 체크 */
                        <svg 
                            xmlns="http://www.w3.org/2000/svg" 
                            className="h-6 w-6 text-gray-200" 
                            fill="none" 
                            viewBox="0 0 24 24" 
                            stroke="currentColor"
                        >
                            <path 
                            strokeLinecap="round" 
                            strokeLinejoin="round" 
                            strokeWidth={2} 
                            d="M5 13l4 4L19 7" 
                            />
                        </svg>
                        ) : (
                        /* 2. 안 읽은 상태: 테두리 없는 선명한 초록색 체크 */
                        <div className="relative">
                            <svg 
                            xmlns="http://www.w3.org/2000/svg" 
                            className="h-7 w-7 text-green-500 transition-all duration-300 group-hover:scale-110" 
                            fill="none" 
                            viewBox="0 0 24 24" 
                            stroke="currentColor"
                            onClick={() => handleMarkAsRead(noti.id)}
                            >
                            <path 
                                strokeLinecap="round" 
                                strokeLinejoin="round" 
                                strokeWidth={3.5} // 안 읽은 건 조금 더 두껍게 해서 강조
                                d="M5 13l4 4L19 7" 
                            />
                            </svg>
                            {/* 새로운 알림임을 알리는 작은 녹색 점 (선택 사항) */}
                            
                        </div>
                        )}
                    </div>
                      </td>
                    </tr>
                  );
                })}
                {Array.from({ length: ITEMS_PER_PAGE - visibleNotis.length }).map((_, i) => (
                  <tr key={`empty-${i}`} className="h-[80px] border-none"><td colSpan={3} /></tr>
                ))}
              </tbody>
            </table>
          </div>

          <footer className="flex items-center justify-center gap-6 py-8">
            <button type="button" onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1} className="text-xs font-black disabled:opacity-20 hover:tracking-widest transition-all">PREV</button>
            <div className="flex gap-2">
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(num => (
                <button key={num} onClick={() => setCurrentPage(num)} className={`w-9 h-9 rounded-xl text-xs font-bold transition-all ${currentPage === num ? 'bg-gray-900 text-white shadow-lg' : 'text-gray-400 hover:bg-gray-100'}`}>{num}</button>
              ))}
            </div>
            <button type="button" onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages} className="text-xs font-black disabled:opacity-20 hover:tracking-widest transition-all">NEXT</button>
          </footer>
        </div>
      </div>
    </main>
  );
}