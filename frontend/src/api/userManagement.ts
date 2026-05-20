import { http } from "@/utils/http";

export type BaseResult<T = any> = {
  success: boolean;
  message?: string;
  data: T;
};

// 获取用户列表
export const getUserList = (params?: object) => {
  return http.request<BaseResult>("get", "/api/admin/users", { params });
};

// 创建用户
export const createUser = (data?: object) => {
  return http.request<BaseResult>("post", "/api/admin/users", { data });
};

// 更新用户
export const updateUser = (id: number, data?: object) => {
  return http.request<BaseResult>("put", `/api/admin/users/${id}`, { data });
};

// 删除用户
export const deleteUser = (id: number) => {
  return http.request<BaseResult>("delete", `/api/admin/users/${id}`);
};

// 更新用户状态 (启用/禁用)
export const updateUserStatus = (id: number, status: number) => {
  return http.request<BaseResult>("put", `/api/admin/users/${id}/status`, {
    data: { status }
  });
};

// 重置用户密码
export const resetUserPassword = (id: number, password?: string) => {
  return http.request<BaseResult>("put", `/api/admin/users/${id}/password`, {
    data: { password }
  });
};
