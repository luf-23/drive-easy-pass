import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '../stores/auth'
import AuthView from '../views/AuthView.vue'
import ExamView from '../views/ExamView.vue'
import HomeView from '../views/HomeView.vue'
import PracticeView from '../views/PracticeView.vue'
import WrongQuestionsView from '../views/WrongQuestionsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/home', name: 'home', component: HomeView },
    { path: '/practice', name: 'practice', component: PracticeView },
    { path: '/exam', name: 'exam', component: ExamView, meta: { requiresAuth: true } },
    { path: '/wrong', name: 'wrong', component: WrongQuestionsView, meta: { requiresAuth: true } },
    { path: '/login', name: 'login', component: AuthView, props: { mode: 'login' } },
    { path: '/register', name: 'register', component: AuthView, props: { mode: 'register' } },
    { path: '/:pathMatch(.*)*', redirect: '/home' },
  ],
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isLoggedIn.value) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if ((to.name === 'login' || to.name === 'register') && isLoggedIn.value) {
    return { name: 'home' }
  }
})

export default router
