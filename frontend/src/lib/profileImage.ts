/** 업로드된 프로필이 없을 때 사용하는 기본 이미지 (public 정적 파일) */
export const DEFAULT_AVATAR = "/default-avatar.svg";

export function profileImageUrl(url: string | null | undefined): string {
  if (url != null && url.trim().length > 0) {
    return url;
  }
  return DEFAULT_AVATAR;
}
