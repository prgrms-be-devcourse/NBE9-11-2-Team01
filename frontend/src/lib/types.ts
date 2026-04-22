export type MyPageUser = {
  email: string;
  nickname: string;
  profileImage: string | null;
  role: string;
  joinedAt?: string | null;
};

export type Board = {
  id: number;
  boardName: string;
  description: string;
  postCount: number;
  createdAt: string;
  modifiedAt: string;
};
