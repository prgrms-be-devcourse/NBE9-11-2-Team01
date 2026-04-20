package com.team01.backend.domain.board.dto;

import java.util.List;

public record AdminBoardListResponseDto(
        List<AdminBoardResponseDto> exist,
        List<AdminBoardResponseDto> deleted
) {
   public AdminBoardListResponseDto(List<AdminBoardResponseDto> exist,List<AdminBoardResponseDto> deleted){
      this.deleted = deleted;
      this.exist = exist;

   }
}
