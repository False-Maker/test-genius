import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/requirement'
    },
    {
      path: '/requirement',
      name: 'Requirement',
      component: () => import('../views/requirement/RequirementList.vue')
    },
    {
      path: '/test-case',
      name: 'TestCase',
      component: () => import('../views/test-case/TestCaseList.vue')
    },
    {
      path: '/case-generation',
      name: 'CaseGeneration',
      component: () => import('../views/case-generation/CaseGeneration.vue')
    },
    {
      path: '/prompt-template',
      name: 'PromptTemplate',
      component: () => import('../views/prompt-template/PromptTemplateList.vue')
    }
  ]
})

export default router

