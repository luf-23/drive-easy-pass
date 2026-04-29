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

export const getAsyncRoutes = async (): Promise<Result> => {
  const routes = await http.request<BackendRoute[]>("get", "/api/routes");
  return {
    success: true,
    data: handleTree(routes.map(toRouteConfig), "id", "parentId", "children")
  };
};

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
      icon: route.icon,
      rank: route.rankNo
    }
  };
}
