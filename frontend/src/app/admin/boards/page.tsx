'use client';

import { useState, useEffect, useCallback } from 'react';

// 요청하신 Board 인터페이스 정의
interface Board {
  id: number;
  boardName: string;
  description: string;
  createdAt: string;
  modifiedAt: string;
  isDeleted: boolean;
}

interface AdminBoardListResponseDto {
  exist: Board[];
  deleted: Board[];
}

const ITEMS_PER_PAGE = 5;
const baseUrl = "http://localhost:8080/admin/boards";

export default function BoardManagementPage() {
  const [boards, setBoards] = useState<Board[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [currentPage, setCurrentPage] = useState(1);
  const [editingBoardId, setEditingBoardId] = useState<number | null>(null);
  const [editingTitle, setEditingTitle] = useState('');
  const [newBoard, setNewBoard] = useState('');

  // 게시판 목록 조회
  const fetchBoards = useCallback(async () => {
    try {
      setLoading(true);
      const response = await fetch(baseUrl,{
        credentials:'include'
      });
      if (!response.ok) throw new Error('데이터 로드 실패');
      const responseData:AdminBoardResponseDto = await response.json();
      const data: AdminBoardListDto = responseData.data;
      
      // exist와 deleted를 합쳐서 하나의 리스트로 관리
      // (순서는 exist 우선, 그 다음 deleted가 오도록 배치)
      const combinedBoards = [...(data.exist || []), ...(data.deleted || [])];
      setBoards(combinedBoards);
    } catch (error) {
      console.error("Fetch Error:", error);
      alert("데이터를 가져오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchBoards();
  }, [fetchBoards]);

  const totalPages = Math.max(1, Math.ceil(boards.length / ITEMS_PER_PAGE));
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const visibleBoards = boards.slice(startIndex, startIndex + ITEMS_PER_PAGE);

  const startEdit = (board: Board) => {
    if (board.isDeleted) return; // 삭제된 게시판은 수정, 삭제 불가
    setEditingBoardId(board.id);
    setEditingTitle(board.boardName);
  };

  const cancelEdit = () => {
    setEditingBoardId(null);
    setEditingTitle('');
  };

  // 수정 핸들러
  const handleEdit = async (id: number) => {
    const newTitle = editingTitle.trim();
    if (!newTitle) return;

    try {
      console.log(`Board ID ${id} 수정: ${newTitle}`);
      setBoards((prev) =>
        prev.map((b) => (b.id === id ? { ...b, boardName: newTitle, modifiedAt: new Date().toISOString() } : b)),
      );
      cancelEdit();
    } catch (error) {
      alert('수정 실패');
    }
  };

  // 삭제 핸들러 (실제 삭제가 아닌 isDeleted를 true로 변경하는 로직)
  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
      setBoards((prev) =>
        prev.map((b) => (b.id === id ? { ...b, isDeleted: true, modifiedAt: new Date().toISOString() } : b))
      );
    } catch (error) {
      alert('삭제 실패');
    }
  };

  const handleRegister = () => {
    if (!newBoard.trim()) return;
    
    const newItem: Board = {
      id: Date.now(), // 임시 ID
      boardName: newBoard,
      description: '',
      createdAt: new Date().toISOString(),
      modifiedAt: new Date().toISOString(),
      isDeleted: false
    };
    
    setBoards(prev => [newItem, ...prev]);
    setNewBoard('');
  };

  if (loading) return <div className="p-10 text-center text-gray-500">데이터를 불러오는 중입니다...</div>;

  return (
    <main>
      <div className="max-w-5xl mx-auto p-10 flex gap-10 bg-white min-h-screen flex-col pt-0"> 
        <div className="mb-2 h-[500px] space-y-4">
          {visibleBoards.map((board) => (
            <div 
              key={board.id} 
              className={`flex items-center justify-between p-5 border rounded-xl shadow-sm transition-all
                ${board.isDeleted ? 'bg-gray-50 border-gray-100 opacity-60 grayscale' : 'bg-white border-gray-200 hover:shadow-md'}`}
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
                    <div className="flex items-center gap-2">
                      <p className={`truncate text-lg font-semibold ${board.isDeleted ? 'text-gray-400 line-through' : 'text-gray-900'}`}>
                        {board.boardName}
                      </p>
                      {board.isDeleted && (
                        <span className="text-[10px] bg-gray-200 text-gray-500 px-2 py-0.5 rounded uppercase font-bold">Deleted</span>
                      )}
                    </div>
                    <p className="mt-1 truncate text-sm text-gray-400">{board.description || '설명이 없습니다.'}</p>
                  </div>
                )}
              </div>

              <div className="flex gap-2">
                {!board.isDeleted && (
                  <>
                    {editingBoardId === board.id ? (
                      <>
                        <button onClick={() => handleEdit(board.id)} className="px-4 py-1.5 bg-gray-100 border border-gray-300 rounded-md text-sm hover:bg-gray-200">저장</button>
                        <button onClick={cancelEdit} className="px-4 py-1.5 bg-white border border-gray-300 rounded-md text-sm hover:bg-gray-50">취소</button>
                      </>
                    ) : (
                      <button onClick={() => startEdit(board)} className="px-4 py-1.5 bg-gray-100 border border-gray-300 rounded-md text-sm hover:bg-gray-200">수정</button>
                    )}
                    <button onClick={() => handleDelete(board.id)} className="px-4 py-1.5 bg-[#2D2D2D] text-white rounded-md text-sm hover:bg-black">삭제</button>
                  </>
                )}
                
              </div>
            </div>
          ))}
        </div>

        {/* 페이지네이션 */}
        <div className="mb-4 flex items-center justify-center gap-3">
          <button onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))} disabled={currentPage === 1} className="rounded-md border border-gray-300 px-4 py-2 text-sm disabled:opacity-40">Prev</button>
          <span className="text-sm text-gray-600">{currentPage} / {totalPages}</span>
          <button onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))} disabled={currentPage === totalPages} className="rounded-md border border-gray-300 px-4 py-2 text-sm disabled:opacity-40">Next</button>
        </div>

        {/* 추가 섹션 */}
        <div>
          <h2 className="text-md font-medium text-gray-700 mb-2">게시판 추가</h2>
          <div className='flex flex-row gap-2 w-full'>
            <textarea
              value={newBoard}
              onChange={(e) => setNewBoard(e.target.value)}
              placeholder="추가할 게시판 이름을 입력하세요."
              className="w-full h-15 p-4 border border-gray-200 rounded-xl bg-gray-50 focus:outline-none focus:ring-1 focus:ring-gray-300 resize-none"
            />
            <button onClick={handleRegister} className="w-20 px-3 py-2.5 bg-[#2D2D2D] text-white rounded-lg hover:bg-black transition-colors h-13 mt-1">등록</button>
          </div>
        </div>
      </div>
    </main>
  );
}