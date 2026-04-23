'use client';

import { useState, useEffect, useCallback } from 'react';
import { apiGet, apiPostJson, apiPutJson, apiDelete } from '@/lib/api';

const ITEMS_PER_PAGE = 4;

export default function BoardManagementPage() {
  const [boards, setBoards] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [currentPage, setCurrentPage] = useState(1);
  const [editingBoardId, setEditingBoardId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState('');
  const [editingDescription, setEditingDescription] = useState('');
  const [newBoardName, setNewBoardName] = useState('');
  const [newBoardDescription, setNewBoardDescription] = useState('');

  const fetchBoards = useCallback(async () => {
    try {
      setLoading(true);
      const responseData = await apiGet<any>("/admin/boards");
      const data = responseData.data;
      if (data) {
        const combinedBoards = [...(data.exist || []), ...(data.deleted || [])];
        setBoards(combinedBoards);
      }
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

  const startEdit = (board: any) => {
    if (board.isDeleted) return; 
    setEditingBoardId(board.id);
    setEditingName(board.boardName);
    setEditingDescription(board.description ?? '');
  };

  const cancelEdit = () => {
    setEditingBoardId(null);
    setEditingName('');
    setEditingDescription('');
  };

  const handleEdit = async (id: number) => {
    const name = editingName.trim();
    const description = editingDescription.trim();
    if (!validateBoardInput(name, description)) return;

    try {
      await apiPutJson(`/admin/boards/${id}`, { name, description });
      fetchBoards();
      cancelEdit();
    } catch (error) {
      alert('수정 실패');
    }
  };

  const softDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      await apiDelete(`/admin/boards/${id}`);
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
      await apiPostJson("/admin/boards", { name, description });
      setNewBoardName('');
      setNewBoardDescription('');
      fetchBoards();
    } catch (error) {
      alert('등록 실패');
    }
  };

  if (loading) return <div className="p-10 text-center text-gray-500">데이터를 불러오는 중입니다...</div>;

  return (
    <main className="max-w-5xl mx-auto p-10 bg-white min-h-screen rounded-2xl border border-gray-200">
      {/* 상단 제목 섹션 (카테고리 관리와 통일) */}
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-gray-900">게시판 관리</h2>
        <p className="text-sm text-gray-500 mt-1">
          서비스의 전체 게시판 목록을 조회하고 수정하거나 새 게시판을 생성합니다.
        </p>
      </div>

      {/* 게시판 목록 영역 */}
      <div className="mb-2 h-[480px] flex flex-col justify-between">
        <div className="space-y-4">
          {visibleBoards.map((board) => (
            <div 
              key={board.id} 
              className={`flex items-center justify-between p-5 border rounded-xl shadow-sm transition-all
                ${board.isDeleted ? 'bg-gray-50 border-gray-100 opacity-60 grayscale' : 'bg-white border-gray-200 hover:shadow-md'}`}
            >
              <div className="min-w-0 flex-1 mr-4">
                {editingBoardId === board.id ? (
                  <div className="flex flex-col gap-2">
                    <input
                      value={editingName}
                      onChange={(e) => setEditingName(e.target.value)}
                      className="w-full max-w-md rounded-lg border border-gray-300 px-3 py-2 text-sm font-semibold outline-none focus:ring-2 focus:ring-blue-200"
                    />
                    <input
                      value={editingDescription}
                      onChange={(e) => setEditingDescription(e.target.value)}
                      className="w-full max-w-md rounded-lg border border-gray-200 px-3 py-2 text-xs text-gray-600 outline-none focus:ring-2 focus:ring-blue-100"
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

              <div className="flex gap-2 flex-shrink-0">
                {!board.isDeleted && (
                  <>
                    {editingBoardId === board.id ? (
                      <>
                        <button onClick={() => handleEdit(board.id)} className="px-4 py-1.5 bg-black text-white rounded-xl text-sm font-semibold">저장</button>
                        <button onClick={cancelEdit} className="px-4 py-1.5 bg-white border border-gray-200 rounded-xl text-sm font-semibold hover:bg-gray-50">취소</button>
                      </>
                    ) : (
                      <button onClick={() => startEdit(board)} className="px-4 py-1.5 bg-blue-50 border border-gray-200 rounded-xl text-sm font-semibold">수정</button>
                    )}
                    <button onClick={() => softDelete(board.id)} className="px-4 py-1.5 bg-black text-white rounded-xl text-sm font-semibold hover:bg-gray-800">삭제</button>
                  </>
                )}
              </div>
            </div>
          ))}
          
          {boards.length === 0 && (
            <div className="h-[300px] flex items-center justify-center border-2 border-dashed border-gray-100 rounded-2xl text-gray-400">
              등록된 게시판이 없습니다.
            </div>
          )}
        </div>

        {/* 페이지네이션 */}
        <div className="flex items-center justify-center gap-4 py-4">
          <button onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))} disabled={currentPage === 1} className="p-2 text-sm disabled:opacity-30 font-bold hover:text-black">
            &lt; Prev
          </button>
          <span className="text-sm font-medium text-gray-600">{currentPage} / {totalPages}</span>
          <button onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))} disabled={currentPage === totalPages} className="p-2 text-sm disabled:opacity-30 font-bold hover:text-black">
            Next &gt;
          </button>
        </div>
      </div>

      {/* 하단 추가 섹션 (간격 및 디자인 조정) */}
      <div className="mt-12 p-8 bg-blue-50/50 rounded-2xl border border-gray-200">
        <h3 className="text-sm font-bold text-gray-700 mb-4 uppercase tracking-wider">새 게시판 추가</h3>
        <div className="space-y-3">
          <input
            value={newBoardName}
            onChange={(e) => setNewBoardName(e.target.value)}
            placeholder="게시판 이름을 입력하세요 (예: 자유게시판)"
            className="w-full rounded-xl border border-gray-200 bg-white p-3 text-sm font-medium outline-none focus:ring-2 focus:ring-blue-100"
          />
          <textarea
            value={newBoardDescription}
            onChange={(e) => setNewBoardDescription(e.target.value)}
            placeholder="게시판에 대한 간단한 설명을 입력하세요."
            className="w-full h-24 resize-none rounded-xl border border-gray-200 bg-white p-3 text-sm outline-none focus:ring-2 focus:ring-blue-100"
          />
          <div className="flex justify-end pt-2">
            <button 
              onClick={handleRegister} 
              className="px-8 py-3 bg-black text-white rounded-xl font-bold hover:bg-gray-800 transition-all text-sm border border-black"
            >
              게시판 등록
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}