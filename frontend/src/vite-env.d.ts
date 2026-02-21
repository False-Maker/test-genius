/// <reference types="vite/client" />

declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<Record<string, never>, Record<string, never>, unknown>
    export default component
}

declare module '@/config/workflow-nodes' {
  export const NODE_TYPES: Record<string, string>
  export const NODE_CONFIGS: Record<string, { name: string; category: string; color: string; description: string }>
  export const NODE_CATEGORIES: Array<{ key: string; title: string }>
}
