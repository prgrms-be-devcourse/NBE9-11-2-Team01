interface AdminBoardResponseDto{
    data:AdminBoardListDto,
    success:boolean
}
interface Board {
    id: number;
    boardName: string;
    description: string;
    createdAt: string;
    modifiedAt: string;
    isDeleted: boolean;
  }
  
  interface AdminBoardListDto {
    exist: Board[];
    deleted: Board[];
  }