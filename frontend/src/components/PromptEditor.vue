<template>
  <div class="prompt-editor">
    <div class="editor-container">
      <div class="editor-header">
        <span class="editor-title">提示词编辑器</span>
        <div class="editor-actions">
          <el-button size="small" @click="handleFormat">格式化</el-button>
          <el-button size="small" @click="handleValidate">验证</el-button>
        </div>
      </div>
      <div ref="editorContainer" class="monaco-editor"></div>
    </div>
    
    <!-- 变量配置面板 -->
    <div class="variables-panel" v-if="showVariables">
      <div class="panel-header">
        <span>变量配置</span>
        <el-button size="small" text @click="showVariables = false">收起</el-button>
      </div>
      <div class="variables-list">
        <div v-for="(value, key) in variables" :key="key" class="variable-item">
          <el-input
            v-model="variables[key]"
            :placeholder="`请输入${key}的值`"
            size="small"
            @input="handleVariableChange"
          >
            <template #prepend>{{ key }}</template>
          </el-input>
        </div>
        <el-button size="small" type="primary" @click="handleAddVariable">添加变量</el-button>
      </div>
    </div>
    
    <!-- 实时预览面板 -->
    <div class="preview-panel" v-if="showPreview">
      <div class="panel-header">
        <span>实时预览</span>
        <el-button size="small" text @click="showPreview = false">收起</el-button>
      </div>
      <div class="preview-content">
        <pre>{{ previewContent }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { loader } from '@monaco-editor/loader'
import * as monaco from 'monaco-editor'
import { ElMessage } from 'element-plus'

interface Props {
  modelValue: string
  variables?: Record<string, string>
  readonly?: boolean
  height?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  variables: () => ({}),
  readonly: false,
  height: '400px'
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'variable-change': [variables: Record<string, string>]
}>()

const editorContainer = ref<HTMLElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null
let monacoInstance: typeof monaco | null = null

const showVariables = ref(true)
const showPreview = ref(true)
const variables = ref<Record<string, string>>({ ...props.variables })
const previewContent = ref('')

// 变量占位符正则：匹配 {变量名} 格式
const VARIABLE_PATTERN = /\{([^}]+)\}/g

// 初始化Monaco Editor
const initEditor = async () => {
  if (!editorContainer.value) return

  try {
    // 加载Monaco Editor
    monacoInstance = await loader.init()
    
    // 注册自定义语言（用于变量高亮）
    monacoInstance.languages.register({ id: 'prompt-template' })
    
    // 定义变量高亮规则
    monacoInstance.languages.setMonarchTokensProvider('prompt-template', {
      tokenizer: {
        root: [
          [/\{[^}]+\}/, 'variable'], // 变量占位符
          [/./, 'text'] // 普通文本
        ]
      }
    })
    
    // 定义主题
    monacoInstance.editor.defineTheme('prompt-theme', {
      base: 'vs',
      inherit: true,
      rules: [
        { token: 'variable', foreground: '0066cc', fontStyle: 'bold' },
        { token: 'text', foreground: '000000' }
      ],
      colors: {}
    })
    
    // 创建编辑器实例
    editor = monacoInstance.editor.create(editorContainer.value, {
      value: props.modelValue,
      language: 'prompt-template',
      theme: 'prompt-theme',
      readOnly: props.readonly,
      minimap: { enabled: true },
      lineNumbers: 'on',
      scrollBeyondLastLine: false,
      automaticLayout: true,
      wordWrap: 'on',
      fontSize: 14,
      lineHeight: 24,
      suggestOnTriggerCharacters: true,
      quickSuggestions: true,
      formatOnPaste: true,
      formatOnType: true
    })
    
    // 监听内容变化
    editor.onDidChangeModelContent(() => {
      const value = editor?.getValue() || ''
      emit('update:modelValue', value)
      updatePreview()
      extractVariables(value)
    })
    
    // 配置自动补全
    setupAutoComplete()
    
    // 初始预览
    updatePreview()
    extractVariables(props.modelValue)
    
  } catch (error) {
    console.error('初始化Monaco Editor失败:', error)
    ElMessage.error('编辑器初始化失败')
  }
}

// 设置自动补全
const setupAutoComplete = () => {
  if (!monacoInstance) return
  
  monacoInstance.languages.registerCompletionItemProvider('prompt-template', {
    provideCompletionItems: (model, position) => {
      const textUntilPosition = model.getValueInRange({
        startLineNumber: 1,
        startColumn: 1,
        endLineNumber: position.lineNumber,
        endColumn: position.column
      })
      
      // 检测是否在输入变量占位符
      const match = textUntilPosition.match(/\{([^}]*)$/)
      if (match) {
        const prefix = match[1]
        const suggestions: monaco.languages.CompletionItem[] = []
        
        // 从当前变量列表生成建议
        Object.keys(variables.value).forEach(varName => {
          if (varName.toLowerCase().includes(prefix.toLowerCase())) {
            suggestions.push({
              label: varName,
              kind: monacoInstance!.languages.CompletionItemKind.Variable,
              insertText: varName,
              detail: `变量: ${varName}`,
              range: {
                startLineNumber: position.lineNumber,
                startColumn: position.column - prefix.length - 1,
                endLineNumber: position.lineNumber,
                endColumn: position.column
              }
            })
          }
        })
        
        return { suggestions }
      }
      
      // 提供插入变量占位符的建议
      const variableNames = Object.keys(variables.value)
      if (variableNames.length > 0) {
        return {
          suggestions: variableNames.map(varName => ({
            label: `{${varName}}`,
            kind: monacoInstance!.languages.CompletionItemKind.Snippet,
            insertText: `{${varName}}`,
            detail: `插入变量: ${varName}`,
            range: {
              startLineNumber: position.lineNumber,
              startColumn: position.column,
              endLineNumber: position.lineNumber,
              endColumn: position.column
            }
          }))
        }
      }
      
      return { suggestions: [] }
    }
  })
}

// 提取模板中的变量
const extractVariables = (content: string) => {
  const extracted: Record<string, string> = {}
  const matches = content.matchAll(VARIABLE_PATTERN)
  
  for (const match of matches) {
    const varName = match[1].trim()
    if (varName && !extracted[varName]) {
      extracted[varName] = variables.value[varName] || ''
    }
  }
  
  // 合并现有变量，保留已有值
  const merged: Record<string, string> = { ...variables.value }
  Object.keys(extracted).forEach(key => {
    if (!(key in merged)) {
      merged[key] = ''
    }
  })
  
  variables.value = merged
  emit('variable-change', variables.value)
}

// 更新预览内容
const updatePreview = () => {
  if (!editor) return
  
  const template = editor.getValue()
  let preview = template
  
  // 替换变量占位符
  Object.keys(variables.value).forEach(key => {
    const value = variables.value[key] || `{${key}}`
    const regex = new RegExp(`\\{${key}\\}`, 'g')
    preview = preview.replace(regex, value)
  })
  
  previewContent.value = preview
}

// 处理变量变化
const handleVariableChange = () => {
  emit('variable-change', variables.value)
  updatePreview()
}

// 添加变量
const handleAddVariable = () => {
  const varName = prompt('请输入变量名:')
  if (varName && varName.trim()) {
    const trimmed = varName.trim()
    if (!variables.value[trimmed]) {
      variables.value[trimmed] = ''
      emit('variable-change', variables.value)
    } else {
      ElMessage.warning('变量已存在')
    }
  }
}

// 格式化
const handleFormat = () => {
  if (!editor) return
  
  editor.getAction('editor.action.formatDocument')?.run()
}

// 验证
const handleValidate = () => {
  if (!editor) return
  
  const content = editor.getValue()
  const matches = content.matchAll(VARIABLE_PATTERN)
  const variablesInTemplate = new Set<string>()
  
  for (const match of matches) {
    variablesInTemplate.add(match[1].trim())
  }
  
  // 检查未定义的变量
  const undefinedVars: string[] = []
  variablesInTemplate.forEach(varName => {
    if (!variables.value[varName]) {
      undefinedVars.push(varName)
    }
  })
  
  if (undefinedVars.length > 0) {
    ElMessage.warning(`以下变量未定义: ${undefinedVars.join(', ')}`)
  } else {
    ElMessage.success('验证通过')
  }
}

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (editor && editor.getValue() !== newValue) {
    editor.setValue(newValue)
    extractVariables(newValue)
    updatePreview()
  }
})

// 监听变量变化
watch(() => props.variables, (newVars) => {
  variables.value = { ...newVars }
  updatePreview()
}, { deep: true })

onMounted(() => {
  nextTick(() => {
    initEditor()
  })
})

onBeforeUnmount(() => {
  if (editor) {
    editor.dispose()
  }
})
</script>

<style scoped lang="scss">
.prompt-editor {
  display: flex;
  flex-direction: column;
  gap: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
  background: #fff;

  .editor-container {
    display: flex;
    flex-direction: column;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;

    .editor-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      background: #f5f7fa;
      border-bottom: 1px solid #dcdfe6;

      .editor-title {
        font-weight: 500;
        color: #303133;
      }

      .editor-actions {
        display: flex;
        gap: 8px;
      }
    }

    .monaco-editor {
      height: v-bind(height);
      width: 100%;
    }
  }

  .variables-panel,
  .preview-panel {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #fff;

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      background: #f5f7fa;
      border-bottom: 1px solid #dcdfe6;
      font-weight: 500;
      color: #303133;
    }

    .variables-list {
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 8px;

      .variable-item {
        display: flex;
        align-items: center;
      }
    }

    .preview-content {
      padding: 12px;
      max-height: 300px;
      overflow-y: auto;
      background: #fafafa;
      border-radius: 4px;

      pre {
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;
        font-family: 'Courier New', monospace;
        font-size: 13px;
        line-height: 1.6;
        color: #303133;
      }
    }
  }
}
</style>
