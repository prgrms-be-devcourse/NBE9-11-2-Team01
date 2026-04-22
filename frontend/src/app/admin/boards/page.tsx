// app/admin/boards/page.tsx
'use client';

import { useState } from 'react';

interface Board {
  id: number;
  title: string;
  description: string;
}

const ITEMS_PER_PAGE = 5;

export default function BoardManagementPage() {
  // 예시 데이터 (실제로는 API에서 fetch해오게 됩니다)
  const [boards, setBoards] = useState<Board[]>([
    { id: 1, title: '게시판 1', description: '공지/안내를 공유하는 게시판' },
    { id: 2, title: '게시판 2', description: '자유롭게 이야기하는 공간' },
    { id: 3, title: '게시판 3', description: '질문과 답변을 남기는 게시판' },
    { id: 4, title: '게시판 4', description: '프로젝트 진행 상황을 기록' },
    { id: 5, title: '게시판 5', description: '자료/링크를 모아두는 게시판' },
    { id: 6, title: '게시판 6', description: '이벤트/모임 소식을 공유' },
    { id: 7, title: '게시판 7', description: '버그/이슈를 제보하는 공간' },
    { id: 8, title: '게시판 8', description: '회고/피드백을 남기는 게시판' },
  ]);
  const [currentPage, setCurrentPage] = useState(1);
  const [editingBoardId, setEditingBoardId] = useState<number | null>(null);
  const [editingTitle, setEditingTitle] = useState('');

  const [newBoard, setNewBoard] = useState('');

  const totalPages = Math.max(1, Math.ceil(boards.length / ITEMS_PER_PAGE));
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const visibleBoards = boards.slice(startIndex, startIndex + ITEMS_PER_PAGE);

  const startEdit = (board: Board) => {
    setEditingBoardId(board.id);
    setEditingTitle(board.title);
  };

  const cancelEdit = () => {
    setEditingBoardId(null);
    setEditingTitle('');
  };

  // 수정 핸들러 (인라인 편집 저장)
  const handleEdit = async (id: number) => {
    const newTitle = editingTitle.trim();
    if (!newTitle) return;

    try {
      // const res = await fetch(`/api/boards/${id}/update`, { method: 'POST', body: JSON.stringify({ title: newTitle }) });
      console.log(`Board ID ${id} 수정 요청: ${newTitle}`);
      setBoards((prevBoards) =>
        prevBoards.map((b) => (b.id === id ? { ...b, title: newTitle } : b)),
      );
      cancelEdit();
    } catch (error) {
      alert('수정 실패');
    }
  };

  // 삭제 핸들러
  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
      // const res = await fetch(`/api/boards/${id}/delete`, { method: 'POST' });
      console.log(`Board ID ${id} 삭제 요청`);
      setBoards((prevBoards) => {
        const nextBoards = prevBoards.filter((b) => b.id !== id);
        const nextTotalPages = Math.max(1, Math.ceil(nextBoards.length / ITEMS_PER_PAGE));
        setCurrentPage((prevPage) => Math.min(prevPage, nextTotalPages));
        return nextBoards;
      });
    } catch (error) {
      alert('삭제 실패');
    }
  };

  // 등록 핸들러
  const handleRegister = () => {
    console.log('새 게시판 등록:', newBoard);
    setNewBoard('');
  };

  return (
    <main>
    <div className="max-w-5xl mx-auto p-10 flex gap-10 bg-white min-h-screen flex-col pt-0"> 
        {/* 게시판 리스트 */}
        <div className="mb-2 h-[500px] space-y-4">
          {visibleBoards.map((board) => (
            <div 
              key={board.id} 
              className="flex items-center justify-between p-5 bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-md transition-shadow"
            >
              <div className="min-w-0">
                {editingBoardId === board.id ? (
                  <input
                    value={editingTitle}
                    onChange={(e) => setEditingTitle(e.target.value)}
                    className="w-full max-w-md rounded-md border border-gray-300 px-3 py-2 text-sm font-semibold outline-none focus:ring-2 focus:ring-gray-300"
                  />
                ) : (
                  <div className="min-w-0">
                    <p className="truncate text-lg font-semibold text-gray-900">{board.title}</p>
                    <p className="mt-1 truncate text-sm text-gray-400">{board.description}</p>
                  </div>
                )}
              </div>
              <div className="flex gap-2">
                {editingBoardId === board.id ? (
                  <>
                    <button 
                      onClick={() => handleEdit(board.id)}
                      disabled={!editingTitle.trim()}
                      className="px-4 py-1.5 bg-gray-100 border border-gray-300 rounded-md text-sm hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-40"
                    >
                      저장
                    </button>
                    <button 
                      onClick={cancelEdit}
                      className="px-4 py-1.5 bg-white border border-gray-300 rounded-md text-sm hover:bg-gray-50"
                    >
                      취소
                    </button>
                  </>
                ) : (
                  <button 
                    onClick={() => startEdit(board)}
                    className="px-4 py-1.5 bg-gray-100 border border-gray-300 rounded-md text-sm hover:bg-gray-200"
                  >
                    수정
                  </button>
                )}
                <button 
                  onClick={() => handleDelete(board.id)}
                  className="px-4 py-1.5 bg-[#2D2D2D] text-white rounded-md text-sm hover:bg-black"
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>
        <div className="mb-4 flex items-center justify-center gap-3">
          <button
            onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
            disabled={currentPage === 1}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm disabled:cursor-not-allowed disabled:opacity-40"
          >
            Prev
          </button>
          <span className="text-sm text-gray-600">
            {currentPage} / {totalPages}
          </span>
          <button
            onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
            disabled={currentPage === totalPages}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm disabled:cursor-not-allowed disabled:opacity-40"
          >
            Next
          </button>
        </div>

        {/* 카테고리 추가 섹션 */}
        <div className="">
          <h2 className="text-md font-medium text-gray-700 mb-2">게시판 추가</h2>
          <div className='flex flex-row gap-2 w-full '>
            <textarea
              value={newBoard}
              onChange={(e) => setNewBoard(e.target.value)}
              placeholder="추가할 게시판 이름을 입력하세요."
              className="w-full h-15 p-4 border border-gray-200 rounded-xl bg-gray-50 focus:outline-none focus:ring-1 focus:ring-gray-300 resize-none"
            />
              <button 
                onClick={handleRegister}
                className="w-20 px-3 py-2.5 bg-[#2D2D2D] text-white rounded-lg hover:bg-black transition-colors h-13 mt-1"
                name='등록'
                type='submit'
              >
              등록
              </button>
          </div>
          
        </div>
    </div>
    </main>
  );
}