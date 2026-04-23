'use client';

import { useState, useEffect, useCallback } from 'react';

const ITEMS_PER_PAGE = 4;
const baseUrl = "http://localhost:8080/admin/boards";

export default function BoardManagementPage() {
  const [boards, setBoards] = useState<Board[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [currentPage, setCurrentPage] = useState(1);
  const [editingBoardId, setEditingBoardId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState('');
  const [editingDescription, setEditingDescription] = useState('');
  const [newBoardName, setNewBoardName] = useState('');
  const [newBoardDescription, setNewBoardDescription] = useState('');

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

  const validateBoardInput = (name: string, description: string) => {
    if (name.length < 2) {
      alert('이름은 2자 이상이어야 합니다.');
      return false;
    }
    if (description.length < 5) {
      alert('설명은 5자 이상이어야 합니다.');
      return false;
    }
    return true;
  };

  const startEdit = (board: Board) => { //수정 폼 작성 중 
    if (board.isDeleted) return; // 삭제된 게시판은 수정, 삭제 불가
    setEditingBoardId(board.id);
    setEditingName(board.boardName);
    setEditingDescription(board.description ?? '');
  };

  const cancelEdit = () => { //현재 수정하고 있는 게시판 없음 (종료)
    setEditingBoardId(null);
    setEditingName('');
    setEditingDescription('');
  };

  // 수정 핸들러
  const handleEdit = async (id: number) => { //실제 수정 요청 
    const name = editingName.trim();
    const description = editingDescription.trim();
    if (!validateBoardInput(name, description)) return;

    try {
      await fetch(`${baseUrl}/${id}`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name, description }),
      });
      fetchBoards();

      cancelEdit();
    } catch (error) {
      alert('수정 실패');
    }
  };

  // 삭제 핸들러 (실제 삭제가 아닌 isDeleted를 true로 변경하는 로직)
  const softDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
      await fetch(`${baseUrl}/${id}`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        }
      });
      fetchBoards();
    } catch (error) {
      alert('삭제 실패');
    }
  };

  const handleRegister = async () => {
    const name = newBoardName.trim();
    const description = newBoardDescription.trim();
    if (!validateBoardInput(name, description)) return;

    try {
      await fetch(baseUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name, description }),
      });
    
      fetchBoards();

    } catch (error) {
      alert('등록 실패');
    }
  };

  if (loading) return <div className="p-10 text-center text-gray-500">데이터를 불러오는 중입니다...</div>;

  return (
    <main>
      <div className="max-w-5xl mx-auto p-10 flex gap-10 bg-white min-h-screen flex-col pt-0 rounded-2xl border border-gray-200"> 
        <div className="mb-2 h-[470px] space-y-4">
          {visibleBoards.map((board) => (
            <div 
              key={board.id} 
              className={`flex items-center justify-between p-5 border rounded-xl shadow-sm transition-all
                ${board.isDeleted ? 'bg-gray-50 border-gray-100 opacity-60 grayscale' : 'bg-white border-gray-200 hover:shadow-md'}`}
            >
              <div className="min-w-0">
                {editingBoardId === board.id ? (
                  <div className="flex w-full max-w-md flex-col gap-2">
                    <input
                      value={editingName}
                      onChange={(e) => setEditingName(e.target.value)}
                      placeholder="게시판 이름"
                      className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm font-semibold outline-none focus:ring-2 focus:ring-blue-200"
                    />
                    <input
                      value={editingDescription}
                      onChange={(e) => setEditingDescription(e.target.value)}
                      placeholder="게시판 설명"
                      className="w-full rounded-xl border border-gray-200 px-3 py-2 text-xs text-gray-600 outline-none focus:ring-2 focus:ring-blue-100"
                    />
                  </div>
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
                        <button onClick={() => handleEdit(board.id)} className="px-4 py-1.5 bg-blue-50 border border-gray-200 rounded-xl text-sm hover:bg-blue-100">저장</button>
                        <button onClick={cancelEdit} className="px-4 py-1.5 bg-white border border-gray-200 rounded-xl text-sm hover:bg-blue-50">취소</button>
                      </>
                    ) : (
                      <button onClick={() => startEdit(board)} className="px-4 py-1.5 bg-blue-50 border border-gray-200 rounded-xl text-sm hover:bg-blue-100">수정</button>
                    )}
                    <button onClick={() => softDelete(board.id)} className="px-4 py-1.5 bg-black text-white rounded-xl text-sm hover:bg-gray-800">삭제</button>
                  </>
                )}
                
              </div>
            </div>
          ))}
        </div>

        {/* 페이지네이션 */}
        <div className="mb-4 flex items-center justify-center gap-3">
          <button onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))} disabled={currentPage === 1} className="rounded-xl border border-gray-200 px-4 py-2 text-sm disabled:opacity-40">Prev</button>
          <span className="text-sm text-gray-600">{currentPage} / {totalPages}</span>
          <button onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))} disabled={currentPage === totalPages} className="rounded-xl border border-gray-200 px-4 py-2 text-sm disabled:opacity-40">Next</button>
        </div>

        {/* 추가 섹션 */}
        <div>
          <h2 className="text-md font-medium text-gray-700 mb-2">게시판 추가</h2>
          <div className="w-full space-y-2">
            <input
              value={newBoardName}
              onChange={(e) => setNewBoardName(e.target.value)}
              placeholder="게시판 이름을 입력하세요."
              className="w-full rounded-xl border border-gray-200 bg-gray-50 p-3 text-sm font-medium outline-none focus:ring-1 focus:ring-gray-300"
            />
            <textarea
              value={newBoardDescription}
              onChange={(e) => setNewBoardDescription(e.target.value)}
              placeholder="게시판 설명을 입력하세요."
              className="w-full h-20 resize-none rounded-xl border border-gray-200 bg-gray-50 p-3 text-sm outline-none focus:ring-1 focus:ring-gray-300"
            />
            <div className="flex justify-end">
              <button onClick={handleRegister} className="w-20 px-3 py-2.5 bg-black text-white rounded-xl hover:bg-gray-800 transition-colors border border-black">등록</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}