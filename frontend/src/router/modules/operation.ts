const Layout = () => import("@/layout/index.vue");

export default {
  path: "/operation",
  name: "OperationCenter",
  component: Layout,
  redirect: "/operation/dashboard",
  meta: {
    icon: "ep/data-board",
    title: "中台管理",
    rank: 2
  },
  children: [
    {
      path: "/operation/dashboard",
      name: "AdminDashboard",
      component: () => import("@/views/welcome/index.vue"),
      meta: {
        title: "工作台",
        roles: ["admin", "coach"]
      }
    },    {
      path: "/operation/statistics",
      name: "StatisticsCenter",
      component: () => import("@/views/operation/statistics/index.vue"),
      meta: {
        title: "统计中心",
        roles: ["admin", "market"]
      }
    },    {
      path: "/operation/enrollment/intents",
      name: "Enrollment",
      component: () => import("@/views/operation/EnrollmentManagementView.vue"),
      meta: {
        title: "招生管理",
        roles: ["admin", "sales", "market", "coach"]
      }
    },
    {
      path: "/operation/teaching",
      name: "Teaching",
      component: () => import("@/views/welcome/index.vue"),
      meta: {
        title: "教学管理"
      }
    },
    {
      path: "/operation/exam-service",
      name: "ExamService",
      component: () =>
        import("@/views/operation/ExamServiceManagementView.vue"),
      meta: {
        title: "报考服务"
      }
    },
    {
      path: "/operation/reports",
      name: "Reports",
      component: () => import("@/views/welcome/index.vue"),
      meta: {
        title: "报表中心"
      }
    },
    {
      path: "/operation/system/users",
      name: "UserManagement",
      component: () => import("@/views/system/UserManagementView.vue"),
      meta: {
        title: "用户管理",
        roles: ["admin"]
      }
    },
    {
      path: "/operation/system/routes",
      name: "RouteManagement",
      component: () => import("@/views/system/RouteManagementView.vue"),
      meta: {
        title: "路由管理",
        roles: ["admin"]
      }
    },
    {
      path: "/operation/system/roles",
      name: "RoleManagement",
      component: () => import("@/views/system/RoleManagementView.vue"),
      meta: {
        title: "角色管理",
        roles: ["admin"]
      }
    }
  ]
} satisfies RouteConfigsTable;
