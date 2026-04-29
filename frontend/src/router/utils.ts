import {
  type RouterHistory,
  type RouteRecordRaw,
  type RouteComponent,
  createWebHistory,
  createWebHashHistory
} from "vue-router";
import { router } from "./index";
import { isProxy, toRaw } from "vue";
import { useTimeoutFn } from "@vueuse/core";
import {
  isString,
  cloneDeep,
  isAllEmpty,
  intersection,
  storageLocal,
  isIncludeAllChildren
} from "@pureadmin/utils";
import { getConfig } from "@/config";
import { buildHierarchyTree } from "@/utils/tree";
import { userKey, type DataInfo } from "@/utils/auth";
import { type menuType, routerArrays } from "@/layout/types";
import { useMultiTagsStoreHook } from "@/store/modules/multiTags";
import { usePermissionStoreHook } from "@/store/modules/permission";
import { getAsyncRoutes } from "@/api/routes";
const Layout = () => import("@/layout/index.vue");
const IFrame = () => import("@/layout/frame.vue");
// https://cn.vitejs.dev/guide/features.html#glob-import
const modulesRoutes = import.meta.glob("/src/views/**/*.{vue,tsx}");


function handRank(routeInfo: any) {
  const { name, path, parentId, meta } = routeInfo;
  return isAllEmpty(parentId)
    ? isAllEmpty(meta?.rank) ||
      (meta?.rank === 0 && name !== "Home" && path !== "/")
      ? true
      : false
    : false;
}

/** 按路由 meta 的 rank 升序排序 */
function ascending(arr: any[]) {
  arr.forEach((v, index) => {
    // rank 不存在时按顺序自动生成，首页路由固定排在前面
    if (handRank(v)) v.meta.rank = index + 2;
  });
  return arr.sort(
    (a: { meta: { rank: number } }, b: { meta: { rank: number } }) => {
      return a?.meta.rank - b?.meta.rank;
    }
  );
}

/** 过滤 meta 中 showLink 为 false 的菜单 */
function filterTree(data: RouteComponent[]) {
  const newTree = cloneDeep(data).filter(
    (v: { meta: { showLink: boolean } }) => v.meta?.showLink !== false
  );
  newTree.forEach(
    (v: { children }) => v.children && (v.children = filterTree(v.children))
  );
  return newTree;
}

/** 过滤 children 为空的目录；只要子菜单中有可见项就保留目录 */
function filterChildrenTree(data: RouteComponent[]) {
  const newTree = cloneDeep(data).filter((v: any) => v?.children?.length !== 0);
  newTree.forEach(
    (v: { children }) => v.children && (v.children = filterTree(v.children))
  );
  return newTree;
}

/** 判断两个数组是否存在相同值 */
function isOneOfArray(a: Array<string>, b: Array<string>) {
  return Array.isArray(a) && Array.isArray(b)
    ? intersection(a, b).length > 0
      ? true
      : false
    : true;
}

/** 从 localStorage 读取当前用户角色，过滤无权限菜单 */
function filterNoPermissionTree(data: RouteComponent[]) {
  const currentRoles =
    storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [];
  const newTree = cloneDeep(data).filter((v: any) =>
    isOneOfArray(v.meta?.roles, currentRoles)
  );
  newTree.forEach(
    (v: any) => v.children && (v.children = filterNoPermissionTree(v.children))
  );
  return filterChildrenTree(newTree);
}

/** 根据指定 key 获取父级路径集合，默认 key 为 path */
function getParentPaths(value: string, routes: RouteRecordRaw[], key = "path") {
  const parents: string[] = [];

  // 深度优先查找
  function dfs(routes: RouteRecordRaw[] = []): boolean {
    if (!Array.isArray(routes) || routes.length === 0) return false;
    for (let i = 0; i < routes.length; i++) {
      const item = routes[i];
      // 找到目标节点时结束查找
      if (item[key] === value) return true;
      // children 不存在或为空时跳过
      if (!Array.isArray(item.children) || !item.children.length) continue;
      // 向下查找前先把当前 path 压栈
      parents.push(item.path);

      if (dfs(item.children)) return true;
      // 没找到时弹出当前 path
      parents.pop();
    }
    // 未找到
    return false;
  }

  dfs(Array.isArray(routes) ? routes : []);
  return parents;
}

/** 查找对应 path 的路由信息 */
function findRouteByPath(path: string, routes: RouteRecordRaw[]) {
  let res = routes.find((item: { path: string }) => item.path == path);
  if (res) {
    return isProxy(res) ? toRaw(res) : res;
  } else {
    for (let i = 0; i < routes.length; i++) {
      if (
        routes[i].children instanceof Array &&
        routes[i].children.length > 0
      ) {
        res = findRouteByPath(path, routes[i].children);
        if (res) {
          return isProxy(res) ? toRaw(res) : res;
        }
      }
    }
    return null;
  }
}

/** 动态路由注册完成后补充全屏 404 路由，避免刷新时报错 */
function addPathMatch() {
  if (!router.hasRoute("pathMatch")) {
    router.addRoute({
      path: "/:pathMatch(.*)*",
      name: "PageNotFound",
      component: () => import("@/views/error/404.vue"),
      meta: {
        title: "404",
        showLink: false
      }
    });
  }
}

/** 处理后端返回的动态路由 */
function handleAsyncRoutes(routeList) {
  if (routeList.length === 0) {
    usePermissionStoreHook().handleWholeMenus(routeList);
  } else {
    formatFlatteningRoutes(addAsyncRoutes(routeList)).map(
      (v: RouteRecordRaw) => {
        // 防止重复添加路由
        if (
          router.options.routes[0].children.findIndex(
            value => value.path === v.path
          ) !== -1
        ) {
          return;
        } else {
          // push 到 routes 后还要同步 addRoute，路由才能正常跳转
          router.options.routes[0].children.push(v);
          // 最终路由排序
          ascending(router.options.routes[0].children);
          if (!router.hasRoute(v?.name)) router.addRoute(v);
          const flattenRouters: any = router
            .getRoutes()
            .find(n => n.path === "/");
          // 保持 children 与根路由一致，避免数据不一致
          flattenRouters.children = router.options.routes[0].children;
          router.addRoute(flattenRouters);
        }
      }
    );
    usePermissionStoreHook().handleWholeMenus(routeList);
  }
  if (!useMultiTagsStoreHook().getMultiTagsCache) {
    useMultiTagsStoreHook().handleTags("equal", [
      ...routerArrays,
      ...usePermissionStoreHook().flatteningRoutes.filter(
        v => v?.meta?.fixedTag
      )
    ]);
  }
  addPathMatch();
}

function initRouter() {
  const key = "async-routes";
  if (getConfig()?.CachingAsyncRoutes) {
    const asyncRouteList = storageLocal().getItem(key) as any;
    if (asyncRouteList && asyncRouteList?.length > 0) {
      return new Promise(resolve => {
        handleAsyncRoutes(asyncRouteList);
        resolve(router);
      });
    }
  }
  return new Promise(resolve => {
    getAsyncRoutes().then(({ data }) => {
      handleAsyncRoutes(cloneDeep(data));
      if (getConfig()?.CachingAsyncRoutes) {
        storageLocal().setItem(key, data);
      }
      resolve(router);
    });
  });
}

/**
 * 灏嗗绾у祵濂楄矾鐢卞鐞嗘垚涓€缁存暟缁? * @param routesList 浼犲叆璺敱
 * @returns 杩斿洖澶勭悊鍚庣殑涓€缁磋矾鐢? */
function formatFlatteningRoutes(routesList: RouteRecordRaw[]) {
  if (routesList.length === 0) return routesList;
  let hierarchyList = buildHierarchyTree(routesList);
  for (let i = 0; i < hierarchyList.length; i++) {
    if (hierarchyList[i].children) {
      hierarchyList = hierarchyList
        .slice(0, i + 1)
        .concat(hierarchyList[i].children, hierarchyList.slice(i + 1));
    }
  }
  return hierarchyList;
}

/**
 * 涓€缁存暟缁勫鐞嗘垚澶氱骇宓屽鏁扮粍锛堜笁绾у強浠ヤ笂鐨勮矾鐢卞叏閮ㄦ媿鎴愪簩绾э紝keep-alive 鍙敮鎸佸埌浜岀骇缂撳瓨锛? * https://github.com/pure-admin/vue-pure-admin/issues/67
 * @param routesList 澶勭悊鍚庣殑涓€缁磋矾鐢辫彍鍗曟暟缁? * @returns 杩斿洖灏嗕竴缁存暟缁勯噸鏂板鐞嗘垚瑙勫畾璺敱鐨勬牸寮? */
function formatTwoStageRoutes(routesList: RouteRecordRaw[]) {
  if (routesList.length === 0) return routesList;
  const newRoutesList: RouteRecordRaw[] = [];
  routesList.forEach((v: RouteRecordRaw) => {
    if (v.path === "/") {
      newRoutesList.push({
        component: v.component,
        name: v.name,
        path: v.path,
        redirect: v.redirect,
        meta: v.meta,
        children: []
      });
    } else {
      newRoutesList[0]?.children.push({ ...v });
    }
  });
  return newRoutesList;
}

/** 澶勭悊缂撳瓨璺敱锛堟坊鍔犮€佸垹闄ゃ€佸埛鏂帮級 */
function handleAliveRoute({ name }: ToRouteType, mode?: string) {
  switch (mode) {
    case "add":
      usePermissionStoreHook().cacheOperate({
        mode: "add",
        name
      });
      break;
    case "delete":
      usePermissionStoreHook().cacheOperate({
        mode: "delete",
        name
      });
      break;
    case "refresh":
      usePermissionStoreHook().cacheOperate({
        mode: "refresh",
        name
      });
      break;
    default:
      usePermissionStoreHook().cacheOperate({
        mode: "delete",
        name
      });
      useTimeoutFn(() => {
        usePermissionStoreHook().cacheOperate({
          mode: "add",
          name
        });
      }, 100);
  }
}

/** 杩囨护鍚庣浼犳潵鐨勫姩鎬佽矾鐢?閲嶆柊鐢熸垚瑙勮寖璺敱 */
function addAsyncRoutes(arrRoutes: Array<RouteRecordRaw>) {
  if (!arrRoutes || !arrRoutes.length) return;
  const modulesRoutesKeys = Object.keys(modulesRoutes);
  arrRoutes.forEach((v: RouteRecordRaw) => {
    const children = Array.isArray(v.children) ? v.children : [];
    // 灏哹ackstage灞炴€у姞鍏eta锛屾爣璇嗘璺敱涓哄悗绔繑鍥炶矾鐢?    v.meta.backstage = true;
    // 鐖剁骇鐨剅edirect灞炴€у彇鍊硷細濡傛灉瀛愮骇瀛樺湪涓旂埗绾х殑redirect灞炴€т笉瀛樺湪锛岄粯璁ゅ彇绗竴涓瓙绾х殑path锛涘鏋滃瓙绾у瓨鍦ㄤ笖鐖剁骇鐨剅edirect灞炴€у瓨鍦紝鍙栧瓨鍦ㄧ殑redirect灞炴€э紝浼氳鐩栭粯璁ゅ€?    if (v?.children && v.children.length && !v.redirect)
    if (children.length && !v.redirect) v.redirect = children[0].path;
    // 鐖剁骇鐨刵ame灞炴€у彇鍊硷細濡傛灉瀛愮骇瀛樺湪涓旂埗绾х殑name灞炴€т笉瀛樺湪锛岄粯璁ゅ彇绗竴涓瓙绾х殑name锛涘鏋滃瓙绾у瓨鍦ㄤ笖鐖剁骇鐨刵ame灞炴€у瓨鍦紝鍙栧瓨鍦ㄧ殑name灞炴€э紝浼氳鐩栭粯璁ゅ€硷紙娉ㄦ剰锛氭祴璇曚腑鍙戠幇鐖剁骇鐨刵ame涓嶈兘鍜屽瓙绾ame閲嶅锛屽鏋滈噸澶嶄細閫犳垚閲嶅畾鍚戞棤鏁堬紙璺宠浆404锛夛紝鎵€浠ヨ繖閲岀粰鐖剁骇鐨刵ame璧峰悕鐨勬椂鍊欏悗闈細鑷姩鍔犱笂`Parent`锛岄伩鍏嶉噸澶嶏級
    if (children.length && !v.name)
      v.name = (children[0].name as string) + "Parent";
    if ((v.component as any) === "Layout") {
      v.component = Layout;
    } else if (v.meta?.frameSrc) {
      v.component = IFrame;
    } else {
      // 瀵瑰悗绔紶component缁勪欢璺緞鍜屼笉浼犲仛鍏煎锛堝鏋滃悗绔紶component缁勪欢璺緞锛岄偅涔坧ath鍙互闅忎究鍐欙紝濡傛灉涓嶄紶锛宑omponent缁勪欢璺緞浼氳窡path淇濇寔涓€鑷达級
      const index = v?.component
        ? modulesRoutesKeys.findIndex(ev => ev.includes(v.component as any))
        : modulesRoutesKeys.findIndex(ev => ev.includes(v.path));
      v.component = index >= 0 ? modulesRoutes[modulesRoutesKeys[index]] : modulesRoutes["/src/views/welcome/index.vue"];
    }
    if (children.length) {
      addAsyncRoutes(children as Array<RouteRecordRaw>);
    }
  });
  return arrRoutes;
}

/** 鑾峰彇璺敱鍘嗗彶妯″紡 https://next.router.vuejs.org/zh/guide/essentials/history-mode.html */
function getHistoryMode(routerHistory): RouterHistory {
  // len涓? 浠ｈ〃鍙湁鍘嗗彶妯″紡 涓? 浠ｈ〃鍘嗗彶妯″紡涓瓨鍦╞ase鍙傛暟 https://next.router.vuejs.org/zh/api/#%E5%8F%82%E6%95%B0-1
  const historyMode = routerHistory.split(",");
  const leftMode = historyMode[0];
  const rightMode = historyMode[1];
  // no param
  if (historyMode.length === 1) {
    if (leftMode === "hash") {
      return createWebHashHistory("");
    } else if (leftMode === "h5") {
      return createWebHistory("");
    }
  } //has param
  else if (historyMode.length === 2) {
    if (leftMode === "hash") {
      return createWebHashHistory(rightMode);
    } else if (leftMode === "h5") {
      return createWebHistory(rightMode);
    }
  }
}

/** 鑾峰彇褰撳墠椤甸潰鎸夐挳绾у埆鐨勬潈闄?*/
function getAuths(): Array<string> {
  return router.currentRoute.value.meta.auths as Array<string>;
}

/** route auth helper */
function hasAuth(value: string | Array<string>): boolean {
  if (!value) return false;
  /** 浠庡綋鍓嶈矾鐢辩殑`meta`瀛楁閲岃幏鍙栨寜閽骇鍒殑鎵€鏈夎嚜瀹氫箟`code`鍊?*/
  const metaAuths = getAuths();
  if (!metaAuths) return false;
  const isAuths = isString(value)
    ? metaAuths.includes(value)
    : isIncludeAllChildren(value, metaAuths);
  return isAuths ? true : false;
}

function handleTopMenu(route) {
  if (route?.children && route.children.length > 1) {
    if (route.redirect) {
      return route.children.filter(cur => cur.path === route.redirect)[0];
    } else {
      return route.children[0];
    }
  } else {
    return route;
  }
}

/** top menu helper */
function getTopMenu(tag = false): menuType {
  const topMenu = handleTopMenu(
    usePermissionStoreHook().wholeMenus[0]?.children[0]
  );
  tag && useMultiTagsStoreHook().handleTags("push", topMenu);
  return topMenu;
}

export {
  hasAuth,
  getAuths,
  ascending,
  filterTree,
  initRouter,
  getTopMenu,
  addPathMatch,
  isOneOfArray,
  getHistoryMode,
  addAsyncRoutes,
  getParentPaths,
  findRouteByPath,
  handleAliveRoute,
  formatTwoStageRoutes,
  formatFlatteningRoutes,
  filterNoPermissionTree
};
