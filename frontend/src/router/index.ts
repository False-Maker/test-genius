import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/login/Login.vue'),
      meta: {
        title: '登录',
        public: true
      }
    },
    {
      path: '/',
      redirect: '/requirement'
    },
    {
      path: '/requirement',
      name: 'Requirement',
      component: () => import('../views/requirement/RequirementList.vue'),
      meta: {
        title: '需求ID管理',
        icon: 'Document',
        permission: ['requirement:read'],
        keepAlive: true
      }
    },
    {
      path: '/test-case',
      name: 'TestCase',
      component: () => import('../views/test-case/TestCaseList.vue'),
      meta: {
        title: '用例管理',
        icon: 'Tickets',
        permission: ['testcase:read'],
        keepAlive: true
      }
    },
    {
      path: '/case-generation',
      name: 'CaseGeneration',
      component: () => import('../views/case-generation/CaseGeneration.vue'),
      meta: {
        title: '智能用例生成',
        icon: 'MagicStick',
        permission: ['case:generate'],
        keepAlive: false
      }
    },
    {
      path: '/prompt-template',
      name: 'PromptTemplate',
      component: () => import('../views/prompt-template/PromptTemplateList.vue'),
      meta: {
        title: 'Prompt模板',
        icon: 'EditPen',
        permission: ['prompt:read'],
        keepAlive: true
      }
    },
    {
      path: '/model-config',
      name: 'ModelConfig',
      component: () => import('../views/model-config/ModelConfigList.vue'),
      meta: {
        title: '模型配置',
        icon: 'Setting',
        permission: ['model:read'],
        keepAlive: true
      }
    },
    {
      path: '/knowledge-base',
      name: 'KnowledgeBase',
      component: () => import('../views/knowledge-base/KnowledgeBaseList.vue'),
      meta: {
        title: '知识库',
        icon: 'Collection',
        permission: ['knowledge:read'],
        keepAlive: true
      }
    },
    {
      path: '/case-reuse',
      name: 'CaseReuse',
      component: () => import('../views/case-reuse/CaseReuse.vue'),
      meta: {
        title: '智能复用',
        icon: 'Refresh',
        permission: ['case:reuse'],
        keepAlive: false
      }
    },
    {
      path: '/ui-script-generation',
      name: 'UIScriptGeneration',
      component: () => import('../views/test-execution/UIScriptGeneration.vue'),
      meta: {
        title: 'UI脚本生成',
        icon: 'VideoPlay',
        permission: ['script:generate'],
        keepAlive: false
      }
    },
    {
      path: '/ui-script-repair',
      name: 'UIScriptRepair',
      component: () => import('../views/test-execution/UIScriptRepair.vue'),
      meta: {
        title: 'UI脚本修复',
        icon: 'Tools',
        permission: ['script:repair'],
        keepAlive: false
      }
    },
    {
      path: '/test-execution',
      name: 'TestExecutionManagement',
      component: () => import('../views/test-execution/TestExecutionManagement.vue'),
      meta: {
        title: '执行管理',
        icon: 'VideoCamera',
        permission: ['execution:read'],
        keepAlive: true
      }
    },
    {
      path: '/test-report',
      name: 'TestReport',
      component: () => import('../views/test-report/TestReportList.vue'),
      meta: {
        title: '测试报告',
        icon: 'DocumentCopy',
        permission: ['report:read'],
        keepAlive: true
      }
    },
    {
      path: '/test-report-template',
      name: 'TestReportTemplate',
      component: () => import('../views/test-report-template/TestReportTemplateList.vue'),
      meta: {
        title: '报告模板',
        icon: 'Notebook',
        permission: ['template:read'],
        keepAlive: true
      }
    },
    {
      path: '/test-coverage',
      name: 'TestCoverageAnalysis',
      component: () => import('../views/test-coverage/TestCoverageAnalysis.vue'),
      meta: {
        title: '覆盖率分析',
        icon: 'PieChart',
        permission: ['coverage:read'],
        keepAlive: true
      }
    },
    {
      path: '/test-risk-assessment',
      name: 'TestRiskAssessment',
      component: () => import('../views/test-risk-assessment/TestRiskAssessment.vue'),
      meta: {
        title: '风险评估',
        icon: 'Warning',
        permission: ['risk:read'],
        keepAlive: true
      }
    },
    {
      path: '/page-element',
      name: 'PageElementList',
      component: () => import('../views/page-element/PageElementList.vue'),
      meta: {
        title: '页面元素',
        icon: 'Grid',
        permission: ['element:read'],
        keepAlive: true
      }
    },
    {
      path: '/ui-script-template',
      name: 'UIScriptTemplateList',
      component: () => import('../views/ui-script-template/UIScriptTemplateList.vue'),
      meta: {
        title: '脚本模板',
        icon: 'Memo',
        permission: ['script:read'],
        keepAlive: true
      }
    },
    {
      path: '/test-specification',
      name: 'TestSpecificationList',
      component: () => import('../views/test-specification/TestSpecificationList.vue'),
      meta: {
        title: '测试规约',
        icon: 'DocumentChecked',
        permission: ['specification:read'],
        keepAlive: true
      }
    },
    {
      path: '/specification-check',
      name: 'SpecificationCheck',
      component: () => import('../views/specification-check/SpecificationCheck.vue'),
      meta: {
        title: '规约检查',
        icon: 'CircleCheck',
        permission: ['specification:check'],
        keepAlive: false
      }
    },
    {
      path: '/test-case-quality',
      name: 'TestCaseQuality',
      component: () => import('../views/test-case-quality/TestCaseQuality.vue'),
      meta: {
        title: '质量评估',
        icon: 'Star',
        permission: ['quality:read'],
        keepAlive: true
      }
    },
    {
      path: '/flow-document',
      name: 'FlowDocumentGeneration',
      component: () => import('../views/flow-document/FlowDocumentGeneration.vue'),
      meta: {
        title: '流程文档生成',
        icon: 'Share',
        permission: ['document:generate'],
        keepAlive: false
      }
    },
    {
      path: '/data-document',
      name: 'DataDocumentGeneration',
      component: () => import('../views/data-document/DataDocumentGeneration.vue'),
      meta: {
        title: '数据文档生成',
        icon: 'DataBoard',
        permission: ['document:generate'],
        keepAlive: false
      }
    },
    {
      path: '/workflow',
      name: 'WorkflowEditor',
      component: () => import('../views/workflow/WorkflowEditor.vue'),
      meta: {
        title: '工作流编辑器',
        icon: 'Connection',
        permission: ['workflow:edit'],
        keepAlive: false
      }
    },
    {
      path: '/monitoring',
      name: 'MonitoringDashboard',
      component: () => import('../views/monitoring/MonitoringDashboard.vue'),
      meta: {
        title: '监控Dashboard',
        icon: 'Monitor',
        permission: ['monitoring:read'],
        keepAlive: true
      }
    },
    {
      path: '/agent',
      name: 'AgentManagement',
      component: () => import('../views/agent/AgentManagement.vue'),
      meta: {
        title: 'Agent管理',
        icon: 'Robot',
        permission: ['agent:read'],
        keepAlive: true
      }
    },
    {
      path: '/agent/chat/:agentId',
      name: 'AgentChat',
      component: () => import('../views/agent/AgentChat.vue'),
      meta: {
        title: 'Agent对话',
        icon: 'ChatDotRound',
        permission: ['agent:chat'],
        keepAlive: false
      }
    },
    {
      path: '/agent/sessions',
      name: 'AgentSessionHistory',
      component: () => import('../views/agent/AgentSessionHistory.vue'),
      meta: {
        title: 'Agent会话历史',
        icon: 'Clock',
        permission: ['agent:history'],
        keepAlive: true
      }
    },
    {
      path: '/model-comparison',
      name: 'ModelPerformanceComparison',
      component: () => import('../views/model-comparison/ModelPerformanceComparison.vue'),
      meta: {
        title: '模型性能对比',
        icon: 'TrendCharts',
        permission: ['model:compare'],
        keepAlive: true
      }
    },
    {
      path: '/intelligent-model',
      name: 'IntelligentModelSelection',
      component: () => import('../views/intelligent-model/IntelligentModelSelection.vue'),
      meta: {
        title: '智能模型选择',
        icon: 'MagicStick',
        permission: ['model:select'],
        keepAlive: true
      }
    }
  ]
})

// 全局前置守卫（临时禁用登录和权限检查）
router.beforeEach((_to, _from, next) => {
  // TODO: 正式环境需要恢复登录和权限检查
  // const userStore = useUserStore()

  // // 检查是否是公开路由
  // if (to.meta.public) {
  //   // 如果已登录且访问登录页，直接跳转到首页
  //   if (to.name === 'Login' && userStore.isLoggedIn) {
  //     next({ path: '/' })
  //     return
  //   }
  //   next()
  //   return
  // }

  // // 检查登录状态
  // if (!userStore.isLoggedIn) {
  //   // 保存原始目标路径
  //   next({
  //     name: 'Login',
  //     query: { redirect: to.fullPath }
  //   })
  // } else {
  //   // 检查权限
  //   if (to.meta.permission && Array.isArray(to.meta.permission)) {
  //     const hasPermission = userStore.hasAnyPermission(to.meta.permission as string[])
  //     if (!hasPermission) {
  //       // 权限不足，跳转到首页（如果没有权限则停留在当前页或显示提示）
  //       console.warn(`权限不足，无法访问: ${to.path}, 需要权限: ${to.meta.permission}`)
  //       next({ path: '/' })  // 跳转到首页重定向
  //       return
  //     }
  //   }
  //   next()
  // }

  // 临时：直接放行所有请求
  next()
})

// 全局后置钩子
router.afterEach((to) => {
  // 设置页面标题
  const title = to.meta.title || '测试设计助手系统'
  document.title = `${title} - 测试设计助手`
})

export default router

