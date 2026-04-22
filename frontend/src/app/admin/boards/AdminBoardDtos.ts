interface AdminBoardResponseDto{
    data:AdminBoardListDto,
    success:boolean
}
interface AdminBoardDto {
    id: number;
    boardName: string;
    description: string;
    createdAt: string;
    modifiedAt: string;
    isDeleted: boolean;
  }
  
  interface AdminBoardListDto {
    exist: AdminBoardDto[];
    deleted: AdminBoardDto[];
  }