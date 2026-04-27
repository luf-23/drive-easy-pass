const Layout = () => import("@/layout/index.vue");

export default {
  path: "/drive",
  name: "DriveBusiness",
  component: Layout,
  redirect: "/home",
  meta: {
    icon: "ep/guide",
    title: "驾考业务",
    rank: 1
  },
  children: [
    {
      path: "/home",
      name: "DriveHome",
      component: () => import("@/views/HomeView.vue"),
      meta: {
        title: "学员首页"
      }
    },
    {
      path: "/practice",
      name: "Practice",
      component: () => import("@/views/PracticeView.vue"),
      meta: {
        title: "顺序练习"
      }
    },
    {
      path: "/exam",
      name: "Exam",
      component: () => import("@/views/ExamView.vue"),
      meta: {
        title: "模拟考试"
      }
    },
    {
      path: "/wrong",
      name: "WrongQuestions",
      component: () => import("@/views/WrongQuestionsView.vue"),
      meta: {
        title: "错题本"
      }
    },
    {
      path: "/exam/venues",
      name: "VenueList",
      component: () => import("@/views/exam/VenueList.vue"),
      meta: {
        title: "考场信息"
      }
    }
  ]
} satisfies RouteConfigsTable;
