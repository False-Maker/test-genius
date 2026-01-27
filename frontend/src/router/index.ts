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
    },
    {
      path: '/ui-script-generation',
      name: 'UIScriptGeneration',
      component: () => import('../views/test-execution/UIScriptGeneration.vue')
    },
    {
      path: '/ui-script-repair',
      name: 'UIScriptRepair',
      component: () => import('../views/test-execution/UIScriptRepair.vue')
    },
    {
      path: '/test-execution',
      name: 'TestExecutionManagement',
      component: () => import('../views/test-execution/TestExecutionManagement.vue')
    },
    {
      path: '/test-report',
      name: 'TestReport',
      component: () => import('../views/test-report/TestReportList.vue')
    },
    {
      path: '/test-report-template',
      name: 'TestReportTemplate',
      component: () => import('../views/test-report-template/TestReportTemplateList.vue')
    },
    {
      path: '/test-coverage',
      name: 'TestCoverageAnalysis',
      component: () => import('../views/test-coverage/TestCoverageAnalysis.vue')
    },
    {
      path: '/test-risk-assessment',
      name: 'TestRiskAssessment',
      component: () => import('../views/test-risk-assessment/TestRiskAssessment.vue')
    },
    {
      path: '/page-element',
      name: 'PageElementList',
      component: () => import('../views/page-element/PageElementList.vue')
    },
    {
      path: '/ui-script-template',
      name: 'UIScriptTemplateList',
      component: () => import('../views/ui-script-template/UIScriptTemplateList.vue')
    },
    {
      path: '/test-specification',
      name: 'TestSpecificationList',
      component: () => import('../views/test-specification/TestSpecificationList.vue')
    },
    {
      path: '/specification-check',
      name: 'SpecificationCheck',
      component: () => import('../views/specification-check/SpecificationCheck.vue')
    },
    {
      path: '/test-case-quality',
      name: 'TestCaseQuality',
      component: () => import('../views/test-case-quality/TestCaseQuality.vue')
    },
    {
      path: '/flow-document',
      name: 'FlowDocumentGeneration',
      component: () => import('../views/flow-document/FlowDocumentGeneration.vue')
    },
    {
      path: '/data-document',
      name: 'DataDocumentGeneration',
      component: () => import('../views/data-document/DataDocumentGeneration.vue')
    },
    {
      path: '/workflow',
      name: 'WorkflowEditor',
      component: () => import('../views/workflow/WorkflowEditor.vue')
    },
    {
      path: '/monitoring',
      name: 'MonitoringDashboard',
      component: () => import('../views/monitoring/MonitoringDashboard.vue')
    },
    {
      path: '/agent',
      name: 'AgentManagement',
      component: () => import('../views/agent/AgentManagement.vue')
    },
    {
      path: '/agent/chat/:agentId',
      name: 'AgentChat',
      component: () => import('../views/agent/AgentChat.vue')
    },
    {
      path: '/agent/sessions',
      name: 'AgentSessionHistory',
      component: () => import('../views/agent/AgentSessionHistory.vue')
    },
    {
      path: '/model-comparison',
      name: 'ModelPerformanceComparison',
      component: () => import('../views/model-comparison/ModelPerformanceComparison.vue')
    },
    {
      path: '/intelligent-model',
      name: 'IntelligentModelSelection',
      component: () => import('../views/intelligent-model/IntelligentModelSelection.vue')
    }
  ]
})

export default router

