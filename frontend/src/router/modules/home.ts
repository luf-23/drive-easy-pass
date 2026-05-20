import { getTopMenu } from "@/router/utils";

const Layout = () => import("@/layout/index.vue");

export default {
  path: "/",
  name: "Home",
  component: Layout,
  redirect: () => getTopMenu()?.path || "/welcome",
  meta: {
    icon: "ep/home-filled",
    title: "驾校运营后台",
    rank: 0,
    showLink: false
  },
  children: [
    {
      path: "/operation/dashboard",
      name: "AdminDashboard",
      component: () => import("@/views/welcome/index.vue"),
      meta: {
        title: "工作台",
        showLink: false
      }
    },
    {
      path: "/welcome",
      name: "Welcome",
      component: () => import("@/views/welcome/index.vue"),
      meta: {
        title: "工作台",
        showLink: false
      }
    }
  ]
} satisfies RouteConfigsTable;
