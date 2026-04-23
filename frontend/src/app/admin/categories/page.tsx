'use client';

import { useState, useEffect, useCallback } from 'react';

const CATEGORY_URL = "http://localhost:8080/admin/categories";
const BOARD_URL = "http://localhost:8080/admin/boards";
const ITEMS_PER_PAGE = 4; // 카테고리 페이지당 노출 개수

export default function CategoryManagementPage() {
  const [boards, setBoards] = useState<any[]>([]);
  const [allCategories, setAllCategories] = useState<any[]>([]);
  const [selectedBoardId, setSelectedBoardId] = useState<number | null>(null);
  const [isInitialLoading, setIsInitialLoading] = useState(true);

  // 카테고리용 페이징 상태
  const [categoryPage, setCategoryPage] = useState(1);
  
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState('');
  const [newName, setNewName] = useState('');

  const validateCategoryInput = (name: string) => {
    if (name.length < 2) {
      alert('이름은 2자 이상이어야 합니다.');
      return false;
    }
    return true;
  };

  const fetchData = useCallback(async (isSilent = false) => {
    try {
      if (!isSilent) setIsInitialLoading(true);
      const [boardRes, catRes] = await Promise.all([
        fetch(BOARD_URL, { credentials: 'include' }),
        fetch(CATEGORY_URL, { credentials: 'include' })
      ]);
      const boardData = await boardRes.json();
      const catData = await catRes.json();

      const combinedBoards = [...(boardData.data.exist || []), ...(boardData.data.deleted || [])];
      setBoards(combinedBoards);
      setAllCategories(catData.data || []);

      if (combinedBoards.length > 0 && selectedBoardId === null) {
        setSelectedBoardId(combinedBoards[0].id);
      }
    } catch (error) {
      console.error("Fetch Error:", error);
    } finally {
      setIsInitialLoading(false);
    }
  }, [selectedBoardId]);

  useEffect(() => { fetchData(); }, []);

  // 선택된 게시판 정보 및 필터링/페이징 로직
  const currentBoard = boards.find(b => b.id === selectedBoardId);
  const isBoardDeleted = currentBoard?.isDeleted;
  
  const filteredCategories = allCategories.filter(cat => cat.boardId === selectedBoardId);
  const totalPages = Math.max(1, Math.ceil(filteredCategories.length / ITEMS_PER_PAGE));
  const startIndex = (categoryPage - 1) * ITEMS_PER_PAGE;
  const visibleCategories = filteredCategories.slice(startIndex, startIndex + ITEMS_PER_PAGE);

  // 핸들러
  const handleRegister = async () => {
    if (!validateCategoryInput(newName.trim()) || !selectedBoardId || isBoardDeleted) return;
    try {
      await fetch(CATEGORY_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: newName, boardId: selectedBoardId }),
        credentials: 'include'
      });
      setNewName('');
      fetchData(true);
    } catch (error) { alert('등록 실패'); }
  };

  const handleUpdate = async (id: number) => {
    if (isBoardDeleted || !validateCategoryInput(editingName.trim())) return;
    try {
      await fetch(`${CATEGORY_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: editingName, boardId: selectedBoardId }),
        credentials: 'include'
      });
      setEditingId(null);
      fetchData(true);
    } catch (error) { alert('수정 실패'); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('카테고리를 삭제하시겠습니까?') || isBoardDeleted) return;
    await fetch(`${CATEGORY_URL}/${id}`, { method: 'DELETE', credentials: 'include' });
    fetchData(true);
  };

  if (isInitialLoading) return <div className="p-10 text-center text-gray-500">데이터를 불러오는 중...</div>;

  return (
    <main className="max-w-6xl mx-auto p-10 flex gap-8 bg-white min-h-screen rounded-2xl border border-gray-200">
      {/* 왼쪽: 게시판 목록 - 고정 높이 지정 */}
      <div className="w-1/3 border-r pr-6 sticky top-10 h-fit">
        <h2 className="text-sm font-bold text-gray-400 mb-4 uppercase tracking-wider">게시판</h2>
        <div className="space-y-2 h-[500px] overflow-y-auto pr-2 custom-scrollbar">
          {boards.map((board) => (
            <button
              key={board.id}
              onClick={() => {
                setSelectedBoardId(board.id);
                setCategoryPage(1); // 게시판 변경 시 페이징 초기화
                setEditingId(null);
              }}
              className={`w-full text-left p-4 rounded-xl transition-all border ${
                selectedBoardId === board.id 
                ? 'bg-black text-white border-black' 
                : board.isDeleted 
                  ? 'bg-gray-100 text-gray-400 border-gray-200 opacity-60 grayscale' 
                  : 'bg-blue-50 text-gray-600 border-gray-200 hover:bg-blue-100'
              }`}
            >
              <div className="flex justify-between items-center">
                <p className="font-semibold truncate mr-2">{board.boardName}</p>
                {board.isDeleted && <span className="flex-shrink-0 text-[9px] bg-gray-300 text-white px-1.5 py-0.5 rounded uppercase">Deleted</span>}
              </div>
              <p className={`text-xs mt-1 ${selectedBoardId === board.id ? 'text-gray-300' : 'text-gray-400'}`}>ID: {board.id}</p>
            </button>
          ))}
        </div>
      </div>

      {/* 오른쪽: 카테고리 관리 영역 */}
      <div className="flex-1 flex flex-col">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-900">카테고리 관리</h2>
          <p className={`text-sm mt-1 ${isBoardDeleted ? 'text-red-400' : 'text-gray-500'}`}>
            {isBoardDeleted ? "삭제된 게시판은 카테고리 편집이 불가능합니다." : "선택된 게시판의 카테고리를 구성합니다."}
          </p>
        </div>

        {/* 카테고리 목록 - 고정 높이(h-[450px]) 지정 */}
        <div className="h-[450px] flex flex-col justify-between">
          <div className={`space-y-3 transition-opacity ${isBoardDeleted ? 'pointer-events-none opacity-40' : 'opacity-100'}`}>
            {visibleCategories.map((cat) => (
              <div key={cat.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-xl bg-white shadow-sm h-[72px]">
                <div className="flex-1">
                  {editingId === cat.id ? (
                    <input
                      value={editingName}
                      onChange={(e) => setEditingName(e.target.value)}
                      className="w-full max-w-xs border-b border-gray-900 outline-none font-medium bg-transparent"
                      autoFocus
                    />
                  ) : (
                    <span className="font-medium text-gray-800">{cat.name}</span>
                  )}
                </div>

                {!isBoardDeleted && (
                  <div className="flex gap-2">
                    {editingId === cat.id ? (
                      <button onClick={() => handleUpdate(cat.id)} className="px-3 py-1.5 bg-black text-white rounded-xl text-xs font-semibold border border-black">저장</button>
                    ) : (
                      <button onClick={() => { setEditingId(cat.id); setEditingName(cat.name); }} className="px-3 py-1.5 bg-blue-50 border border-gray-200 rounded-xl text-xs font-semibold">수정</button>
                    )}
                    {/* <button onClick={() => handleDelete(cat.id)} className="px-3 py-1.5 bg-red-50 text-red-600 rounded-lg text-xs font-semibold hover:bg-red-100">삭제</button> */}
                  </div>
                )}
              </div>
            ))}

            {filteredCategories.length === 0 && (
              <div className="h-[300px] flex items-center justify-center border-2 border-dashed border-gray-100 rounded-2xl text-gray-400">
                등록된 카테고리가 없습니다.
              </div>
            )}
          </div>

          {/* 카테고리 페이지네이션 */}
          {filteredCategories.length > 0 && (
            <div className="flex items-center justify-center gap-4 py-4">
              <button 
                onClick={() => setCategoryPage(p => Math.max(1, p - 1))} 
                disabled={categoryPage === 1}
                className="p-2 text-sm disabled:opacity-30 font-bold"
              >
                &lt; Prev
              </button>
              <span className="text-sm font-medium text-gray-600">{categoryPage} / {totalPages}</span>
              <button 
                onClick={() => setCategoryPage(p => Math.min(totalPages, p + 1))} 
                disabled={categoryPage === totalPages}
                className="p-2 text-sm disabled:opacity-30 font-bold"
              >
                Next &gt;
              </button>
            </div>
          )}
        </div>

        {/* 하단 고정: 추가 섹션 */}
        <div className="mt-4">
        {!isBoardDeleted && (
          <div className="mt-8 p-6 bg-blue-50/50 rounded-2xl border border-gray-200">
            <h3 className="text-sm font-bold mb-3 text-gray-700">새 카테고리 추가</h3>
            <div className="flex gap-2">
              <input
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                placeholder="카테고리 이름을 입력하세요"
                className="flex-1 p-3 rounded-xl border border-gray-200 outline-none bg-white text-sm"
                onKeyDown={(e) => e.key === 'Enter' && handleRegister()}
              />
              <button 
                onClick={handleRegister}
                className="px-6 py-3 bg-black text-white rounded-xl font-bold hover:bg-gray-800 transition-all text-sm border border-black"
              >
                등록
              </button>
            </div>
          </div>
        )}
        </div>
      </div>
    </main>
  );
}