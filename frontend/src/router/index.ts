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
    },
    {
      path: '/model-config',
      name: 'ModelConfig',
      component: () => import('../views/model-config/ModelConfigList.vue')
    },
    {
      path: '/knowledge-base',
      name: 'KnowledgeBase',
      component: () => import('../views/knowledge-base/KnowledgeBaseList.vue')
    },
    {
      path: '/case-reuse',
      name: 'CaseReuse',
      component: () => import('../views/case-reuse/CaseReuse.vue')
    }
  ]
})

export default router

