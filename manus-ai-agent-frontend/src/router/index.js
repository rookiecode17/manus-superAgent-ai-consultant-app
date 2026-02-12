import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: 'Home - AI Super Agent Application Platform',
      description: 'The AI super agent application platform provides AI love guru and AI super agent services to meet your various AI conversation needs'
    }
  },
  {
    path: '/love-master',
    name: 'LoveMaster',
    component: () => import('../views/LoveMaster.vue'),
    meta: {
      title: 'AI Love Guru - AI Super Agent Application Platform',
      description: 'AI恋爱大师是AI超级智能体应用平台的专业情感顾问，帮你解答各种恋爱问题，提供情感建议AI Love Master is a professional emotional consultant on the AI super intelligence application platform, helping you answer various love questions and provide emotional advice'
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'AI Super Agent - AI Super Agent Application Platform',
      description: 'AI super agent is an all round assistant of the AI super agent application platform, which can answer various professional questions and provide accurate suggestions and solutions'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Global navigation guard, set document title
router.beforeEach((to, from, next) => {
  // Set the page title
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router 