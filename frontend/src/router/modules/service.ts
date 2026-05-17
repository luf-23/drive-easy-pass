const ServiceLayout = () => import("@/views/service/ServiceLayout.vue");

export default {
  path: "/service",
  name: "ServicePortal",
  component: ServiceLayout,
  redirect: "/service/home",
  meta: {
    icon: "ep/shop",
    title: "报考服务",
    rank: 3,
    showLink: false
  },
  children: [
    {
      path: "/service/home",
      name: "ServiceHome",
      component: () => import("@/views/service/ServiceHomeView.vue"),
      meta: {
        title: "驾校首页",
        rank: 1
      }
    },
    {
      path: "/service/exam-booking",
      name: "ServiceExamBooking",
      component: () => import("@/views/service/ServiceExamBookingView.vue"),
      meta: {
        title: "考试预约"
      }
    },
    {
      path: "/service/signup",
      name: "ServiceSignup",
      component: () => import("@/views/service/ServiceSignupView.vue"),
      meta: {
        title: "在线报名"
      }
    },
    {
      path: "/service/profile",
      name: "ServiceProfile",
      component: () => import("@/views/service/ServiceProfileView.vue"),
      meta: {
        title: "个人中心",
        roles: ["student", "admin"]
      }
    }
  ]
} satisfies RouteConfigsTable;
