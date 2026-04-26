import { http } from "@/utils/http";
import type { AuthResponse } from "@/types";

export type UserResult = {
  success: boolean;
  data: {
    /** 头像 */
    avatar: string;
    /** 用户名 */
    username: string;
    /** 昵称 */
    nickname: string;
    /** 当前登录用户的角色 */
    roles: Array<string>;
    /** 按钮级别权限 */
    permissions: Array<string>;
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

export type RefreshTokenResult = {
  success: boolean;
  data: {
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

/** 登录 */
export const getLogin = async (data?: object) => {
  const result = await http.request<AuthResponse>("post", "/api/auth/login", {
    data
  });

  return {
    success: true,
    data: {
      avatar: "",
      username: result.user.username,
      nickname: result.user.nickname,
      roles: ["admin"],
      permissions: ["*:*:*"],
      accessToken: result.token,
      refreshToken: result.token,
      expires: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
    }
  } satisfies UserResult;
};

/** 刷新`token` */
export const refreshTokenApi = (data?: object) => {
  return http.request<RefreshTokenResult>("post", "/refresh-token", { data });
};
