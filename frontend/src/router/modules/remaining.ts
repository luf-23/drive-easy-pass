const Layout = () => import("@/layout/index.vue");

export default [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/AuthLoginView.vue"),
    meta: {
      title: "登录",
      showLink: false
    }
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/views/AuthRegisterView.vue"),
    meta: {
      title: "注册",
      showLink: false
    }
  },
  // 全屏403（无权访问）页面
  {
    path: "/access-denied",
    name: "AccessDenied",
    component: () => import("@/views/error/403.vue"),
    meta: {
      title: "403",
      showLink: false
    }
  },
  // 全屏500（服务器出错）页面
  {
    path: "/server-error",
    name: "ServerError",
    component: () => import("@/views/error/500.vue"),
    meta: {
      title: "500",
      showLink: false
    }
  },
  {
    path: "/redirect",
    component: Layout,
    meta: {
      title: "加载中...",
      showLink: false
    },
    children: [
      {
        path: "/redirect/:path(.*)",
        name: "Redirect",
        component: () => import("@/layout/redirect.vue")
      }
    ]
  },
  {
    path: "/exam/venue/:id",
    name: "ExamVenueDetailPage",
    component: () => import("@/views/exam/VenueDetail.vue"),
    meta: {
      title: "考场详情",
      showLink: false
    }
  },
] satisfies Array<RouteConfigsTable>;
