import { http } from "@/utils/http";
import { handleTree } from "@/utils/tree";

type BackendRoute = {
  id: number;
  path: string;
  name: string;
  title: string;
  parentId: number | null;
  redirect?: string;
  component: string;
  icon: string;
  rankNo: number;
  enabled: boolean;
};

type Result = {
  success: boolean;
  data: Array<any>;
};

/** 已下架能力对应 path；后端库里若未同步删除，这里过滤掉避免菜单仍出现 */
const DEPRECATED_ROUTE_PATHS = new Set([
  "/service/exam-sites",
  "/operation/venue-route"
]);

export const getAsyncRoutes = async (): Promise<Result> => {
  const routes = await http.request<BackendRoute[]>("get", "/api/routes");
  const filtered = routes.filter(r => !DEPRECATED_ROUTE_PATHS.has(r.path));
  return {
    success: true,
    data: handleTree(filtered.map(toRouteConfig), "id", "parentId", "children")
  };
};

/**
 * 后端与 unplugin-icons 常用 `集合/图标`（如 ep/guide），侧边栏 Iconify 需 `集合:图标`；
 * 无冒号且无 IF- 前缀时转为在线 Iconify（否则离线未 addIcon 的集合会空白）。
 */
function normalizeMenuIcon(icon: string | undefined): string | undefined {
  if (icon == null || icon === "") return undefined;
  if (icon.includes(":") || icon.startsWith("IF-")) return icon;
  const slash = icon.indexOf("/");
  if (slash > 0) {
    return `${icon.slice(0, slash)}:${icon.slice(slash + 1)}`;
  }
  return icon;
}

function toRouteConfig(route: BackendRoute) {
  return {
    id: route.id,
    path: route.path,
    name: route.name,
    parentId: route.parentId,
    redirect: route.redirect || undefined,
    component: route.component,
    meta: {
      title: route.title,
      icon: normalizeMenuIcon(route.icon),
      rank: route.rankNo
    }
  };
}
