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
    },
    {
      path: "/operation/statistics",
      name: "StatisticsCenter",
      component: () => import("@/views/operation/statistics/index.vue"),
      meta: {
        title: "统计中心",
        roles: ["admin", "market"]
      }
    },
    {
      path: "/operation/enrollment/intents",
      name: "Enrollment",
      component: () => import("@/views/operation/EnrollmentManagementView.vue"),
      meta: {
        title: "招生管理",
        roles: ["admin", "sales", "market", "coach"]
      }
    },
    {
      path: "/operation/enrollment/packages",
      name: "CoursePackageManagement",
      component: () => import("@/views/operation/enrollment/packages.vue"),
      meta: {
        title: "课程套餐",
        roles: ["admin", "sales", "market", "coach"]
      }
    },
    {
      path: "/operation/students/list",
      name: "StudentManagement",
      component: () => import("@/views/operation/StudentManagementView.vue"),
      meta: {
        title: "学员管理",
        roles: ["admin", "sales", "market", "coach"]
      }
    },
    {
      path: "/operation/students/progress",
      name: "StudentProgress",
      component: () => import("@/views/operation/students/progress.vue"),
      meta: {
        title: "学员考试进度",
        roles: ["admin", "sales", "market", "coach"]
      }
    },
    {
      path: "/operation/coaches",
      name: "CoachManagement",
      component: () => import("@/views/operation/coaches.vue"),
      meta: {
        title: "教练管理",
        roles: ["admin", "coach"]
      }
    },
    {
      path: "/operation/questions/subject1",
      name: "SubjectOneQuestions",
      component: () => import("@/views/operation/questions/subject1.vue"),
      meta: {
        title: "科目一题库",
        roles: ["admin", "coach"]
      }
    },
    {
      path: "/operation/questions/subject4",
      name: "SubjectFourQuestions",
      component: () => import("@/views/operation/questions/subject4.vue"),
      meta: {
        title: "科目四题库",
        roles: ["admin", "coach"]
      }
    },
    {
      path: "/operation/exams/rooms",
      name: "ExamRoomManagement",
      component: () => import("@/views/operation/exams/rooms.vue"),
      meta: {
        title: "考场管理",
        roles: ["admin", "coach"]
      }
    },
    {
      path: "/operation/exams/schedules",
      name: "ExamScheduleManagement",
      component: () => import("@/views/operation/exams/schedules.vue"),
      meta: {
        title: "考试场次",
        roles: ["admin", "coach"]
      }
    },
    {
      path: "/operation/exams/registrations",
      name: "ExamRegistrations",
      component: () => import("@/views/operation/exams/registrations.vue"),
      meta: {
        title: "预约审核",
        roles: ["admin", "coach"]
      }
    },
    {
      path: "/operation/exams/results",
      name: "ExamResults",
      component: () => import("@/views/operation/exams/results.vue"),
      meta: {
        title: "成绩管理",
        roles: ["admin", "coach"]
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
