const Layout = () => import("@/layout/index.vue");

export default {
  path: "/operation",
  name: "OperationCenter",
  component: Layout,
  redirect: "/welcome",
  meta: {
    icon: "ep/data-board",
    title: "中台管理",
    rank: 2
  },
  children: [
    {
      path: "/operation/enrollment",
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
      component: () => import("@/views/welcome/index.vue"),
      meta: {
        title: "报考服务"
      }
    },
    {
      path: "/operation/venue-route",
      name: "VenueRoute",
      component: () => import("@/views/welcome/index.vue"),
      meta: {
        title: "考场线路"
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
