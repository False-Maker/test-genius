<template>
  <div class="case-reuse">
    <div class="header">
      <h2>用例复用管理</h2>
      <div class="header-actions">
        <el-button @click="handleInit">初始化向量表</el-button>
        <el-button type="primary" @click="handleCreateSuite">
          <el-icon><Plus /></el-icon>
          创建测试套件
        </el-button>
      </div>
    </div>

    <!-- 搜索条件�?-->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="检索方式">
          <el-radio-group v-model="searchType">
            <el-radio label="semantic">语义检索</el-radio>
            <el-radio label="keyword">关键词检索</el-radio>
            <el-radio label="recommend">用例推荐</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="检索内容" v-if="searchType !== 'recommend'">
          <el-input
            v-model="searchForm.caseText"
            type="textarea"
            :rows="3"
            placeholder="请输入用例描述或关键词"
            style="width: 400px"
          />
        </el-form-item>
        <el-form-item label="用例ID" v-if="searchType === 'recommend'">
          <el-input-number
            v-model="searchForm.caseId"
            :min="1"
            placeholder="请输入用例ID"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="测试分层">
          <el-select
            v-model="searchForm.layerId"
            placeholder="请选择测试分层"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="layer in layerList"
              :key="layer.id"
              :label="layer.layerName"
              :value="layer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="测试方法">
          <el-select
            v-model="searchForm.methodId"
            placeholder="请选择测试方法"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="method in methodList"
              :key="method.id"
              :label="method.methodName"
              :value="method.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">检索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用例列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="caseList"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="caseCode" label="用例编码" width="180" />
        <el-table-column prop="caseName" label="用例名称" min-width="200" />
        <el-table-column prop="similarity" label="相似度" width="100" v-if="searchType === 'semantic' || searchType === 'recommend'">
          <template #default="scope">
            <span v-if="scope.row.similarity !== undefined">
              {{ (scope.row.similarity * 100).toFixed(1) }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="caseType" label="用例类型" width="100" />
        <el-table-column prop="casePriority" label="优先级" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button size="small" link type="success" @click="handleUpdateEmbedding(scope.row)">
              更新向量
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建测试套件对话框 -->
    <el-dialog
      v-model="suiteDialogVisible"
      title="创建测试套件"
      width="600px"
    >
      <el-form
        ref="suiteFormRef"
        :model="suiteForm"
        :rules="suiteFormRules"
        label-width="120px"
      >
        <el-form-item label="套件名称" prop="suiteName">
          <el-input
            v-model="suiteForm.suiteName"
            placeholder="请输入测试套件名称"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="已选用例">
          <div v-if="selectedCases.length > 0">
            <el-tag
              v-for="testCase in selectedCases"
              :key="testCase.caseId"
              style="margin-right: 8px; margin-bottom: 8px"
            >
              {{ testCase.caseCode }} - {{ testCase.caseName }}
            </el-tag>
          </div>
          <el-empty v-else description="请先选择用例" :image-size="80" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="suiteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="suiteLoading" @click="handleSubmitSuite">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { caseReuseApi, type SimilarCase } from '@/api/caseReuse'
import { commonApi, type TestLayer, type TestDesignMethod } from '@/api/common'

// 响应式数据表单验证规则
const suiteFormRules: FormRules = {
  suiteName: [
    { required: true, message: '请输入套件名称', trigger: 'blur' }
  ]
}

// 初始化向量表
const handleInit = async () => {
  try {
    await ElMessageBox.confirm('确定要初始化用例向量表吗？', '初始化确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await caseReuseApi.initCaseVectorTable()
    if (response.data) {
      ElMessage.success('初始化成功')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('初始化失败', error)
      ElMessage.error('初始化失败')
    }
  }
}

// 检索用例
const handleSearch = async () => {
  if (searchType.value !== 'recommend' && !searchForm.caseText.trim()) {
    ElMessage.warning('请输入检索内容')
    return
  }
  if (searchType.value === 'recommend' && !searchForm.caseId) {
    ElMessage.warning('请输入用例ID')
    return
  }

  loading.value = true
  try {
    let response
    if (searchType.value === 'semantic') {
      response = await caseReuseApi.searchSimilarCases({
        caseText: searchForm.caseText,
        layerId: searchForm.layerId,
        methodId: searchForm.methodId,
        topK: 10,
        similarityThreshold: 0.7
      })
    } else if (searchType.value === 'keyword') {
      response = await caseReuseApi.searchCasesByKeyword(searchForm.caseText, {
        layerId: searchForm.layerId,
        methodId: searchForm.methodId,
        topK: 10
      })
    } else {
      response = await caseReuseApi.recommendSimilarCases(searchForm.caseId!, 10)
    }
    
    if (response.data) {
      caseList.value = response.data
      if (caseList.value.length === 0) {
        ElMessage.info('未找到相似用例')
      }
    }
  } catch (error) {
    console.error('检索失败', error)
    ElMessage.error('检索失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
const handleReset = () => {
  searchForm.caseText = ''
  searchForm.caseId = undefined
  searchForm.layerId = undefined
  searchForm.methodId = undefined
  caseList.value = []
  selectedCases.value = []
}

// 选择变化
const handleSelectionChange = (selection: SimilarCase[]) => {
  selectedCases.value = selection
}

// 查看用例
const handleView = (row: SimilarCase) => {
  // 跳转到用例管理页面查看详�?
  window.open(`/test-case?id=${row.caseId}`, '_blank')
}

// 更新向量
const handleUpdateEmbedding = async (row: SimilarCase) => {
  try {
    const response = await caseReuseApi.updateCaseEmbedding(row.caseId)
    if (response.data) {
      ElMessage.success('向量更新成功')
    }
  } catch (error) {
    console.error('更新向量失败:', error)
    ElMessage.error('更新向量失败')
  }
}

// 创建测试套件
const handleCreateSuite = () => {
  if (selectedCases.value.length === 0) {
    ElMessage.warning('请先选择用例')
    return
  }
  suiteForm.suiteName = ''
  suiteDialogVisible.value = true
}

// 提交测试套件
const handleSubmitSuite = async () => {
  if (!suiteFormRef.value) return
  
  await suiteFormRef.value.validate(async (valid) => {
    if (valid) {
      suiteLoading.value = true
      try {
        const caseIds = selectedCases.value.map(c => c.caseId)
        await caseReuseApi.createCaseSuite(suiteForm.suiteName, caseIds)
        ElMessage.success('测试套件创建成功')
        suiteDialogVisible.value = false
        handleReset()
      } catch (error) {
        console.error('创建失败:', error)
      } finally {
        suiteLoading.value = false
      }
    }
  })
}

// 加载基础数据
const loadBaseData = async () => {
  try {
    const [layerResponse, methodResponse] = await Promise.all([
      commonApi.getTestLayerList(),
      commonApi.getTestDesignMethodList()
    ])
    
    if (layerResponse.data) {
      layerList.value = layerResponse.data
    }
    if (methodResponse.data) {
      methodList.value = methodResponse.data
    }
  } catch (error) {
    console.error('加载基础数据失败:', error)
  }
}

// 初始�?
onMounted(() => {
  loadBaseData()
})
</script>

<style scoped>
.case-reuse {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.table-card {
  margin-bottom: 20px;
}
</style>
