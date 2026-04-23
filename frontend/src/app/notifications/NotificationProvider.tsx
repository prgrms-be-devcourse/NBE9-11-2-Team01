"use client";

import React, { createContext, useContext, useEffect, useState } from 'react';
import { XMarkIcon, BellAlertIcon } from '@heroicons/react/24/outline';
import { useRouter } from 'next/navigation';

interface Notification {
  id: string;
  title: string;
  message: string;
  targetId: number;
}

const NotificationContext = createContext({
  removeNotification: (id: string) => {},
});

export const NotificationProvider = ({ children }: { children: React.ReactNode }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const router = useRouter();

  useEffect(() => {
    // 백엔드 주소 확인 (8080)
    const eventSource = new EventSource('http://localhost:8080/subscribe', { withCredentials: true });

    eventSource.onmessage = (event) => {
      // SseEmitter에서 더미 데이터를 보낼 때 "connected!" 같은 문자열이 올 수 있으므로 예외처리
      if (!event.data.includes('{')) return;

      try {
        const data = JSON.parse(event.data);
        const newId = Date.now().toString();

        // 1. 첫 번째 마침표(.)를 기준으로 title과 message 분리
        const dotIndex = data.content.indexOf('.');
        let title = data.content;
        let message = "";

        if (dotIndex !== -1) {
          title = data.content.substring(0, dotIndex).trim();
          message = data.content.substring(dotIndex + 1).trim();
        }

        const newNotification: Notification = {
          id: newId,
          title: title,
          message: message,
          targetId: data.targetId
        };

        setNotifications((prev) => [...prev, newNotification]);

        // 4초 후 자동 삭제
        setTimeout(() => {
          setNotifications((prev) => prev.filter((n) => n.id !== newId));
        }, 4000);
      } catch (error) {
        console.error("데이터 파싱 에러:", error);
      }
    };

    eventSource.onerror = (err) => {
      console.error("SSE 연결 에러:", err);
      eventSource.close();
    };

    return () => eventSource.close();
  }, []);

  const removeNotification = (id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  return (
    <NotificationContext.Provider value={{ removeNotification }}>
      {children}
      
      {/* 알림창 컨테이너: bottom-5 대신 top-5를 사용하고, z-index를 최상단으로 설정 */}
      <div className="fixed top-5 right-5 z-[9999] flex flex-col gap-3 pointer-events-none">
        {notifications.map((noti) => (
          <div
            key={noti.id}
            className="pointer-events-auto flex w-85 min-w-[20rem] transform items-center gap-4 rounded-2xl bg-white/90 p-4 shadow-2xl ring-1 ring-black/5 backdrop-blur-xl transition-all duration-300 animate-in fade-in slide-in-from-top-2 slide-in-from-right-5 cursor-pointer hover:bg-white"
            onClick={() => router.push(`/posts/${noti.targetId}`)}
          >
            {/* 아이콘 섹션 */}
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-indigo-500 text-white shadow-indigo-200 shadow-lg">
              <BellAlertIcon className="h-6 w-6" />
            </div>

            {/* 텍스트 섹션 */}
            <div className="flex-1 overflow-hidden">
              <h4 className="text-sm font-bold text-gray-900 truncate">
                {noti.title}
              </h4>
              <p className="mt-0.5 text-xs text-gray-600 line-clamp-2">
                {noti.message}
              </p>
            </div>

            {/* 닫기 버튼 */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                removeNotification(noti.id);
              }}
              className="group relative rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
            >
              <XMarkIcon className="h-5 w-5" />
            </button>
          </div>
        ))}
      </div>
    </NotificationContext.Provider>
  );
};