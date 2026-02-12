export interface TextWithPermission {
  id: string;
  memberId: number;
  text: string;
  createdAt: string;
  canEdit: boolean;
  canDelete: boolean;
  isCreator: boolean;
}

export interface TextResponse {
  id: string;
  memberId: number;
  text: string;
  createdAt: string;
}
