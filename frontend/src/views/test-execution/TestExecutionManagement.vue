<template>







  <div class="test-execution-management">







    <div class="header">







      <h2>测试执行管理</h2>







      <div class="header-actions">







        <el-button type="primary" @click="handleCreateTask">







          <el-icon><Plus /></el-icon>







          新建执行任务







        </el-button>







      </div>







    </div>















    <!-- 统计卡片 -->







    <el-row :gutter="20" style="margin-bottom: 20px">







      <el-col :span="6">







        <el-card shadow="hover">







          <div class="stat-card">







            <div class="stat-value">{{ statistics.totalTasks || 0 }}</div>







            <div class="stat-label">总任务数</div>







          </div>







        </el-card>







      </el-col>







      <el-col :span="6">







        <el-card shadow="hover">







          <div class="stat-card">







            <div class="stat-value success">{{ statistics.successTasks || 0 }}</div>







            <div class="stat-label">成功任务</div>







          </div>







        </el-card>







      </el-col>







      <el-col :span="6">







        <el-card shadow="hover">







          <div class="stat-card">







            <div class="stat-value warning">{{ statistics.processingTasks || 0 }}</div>







            <div class="stat-label">执行中</div>







          </div>







        </el-card>







      </el-col>







      <el-col :span="6">







        <el-card shadow="hover">







          <div class="stat-card">







            <div class="stat-value danger">{{ statistics.failedTasks || 0 }}</div>







            <div class="stat-label">失败任务</div>







          </div>







        </el-card>







      </el-col>







    </el-row>















    <!-- 搜索-->







    <el-card class="search-card" shadow="never">







      <el-form :inline="true" :model="searchForm" class="search-form">







        <el-form-item label="任务名称">







          <el-input







            v-model="searchForm.taskName"







            placeholder="请输入任务名称称称称"







            clearable







            @clear="handleSearch"







            @keyup.enter="handleSearch"







          />







        </el-form-item>







        <el-form-item label="任务状态">







          <el-select







            v-model="searchForm.taskStatus"







            placeholder="请选择状态"







            clearable







            @change="handleSearch"







          >







            <el-option label="等待中" value="PENDING" />







            <el-option label="处理中" value="PROCESSING" />







            <el-option label="成功" value="SUCCESS" />







            <el-option label="失败" value="FAILED" />







          </el-select>







        </el-form-item>







        <el-form-item label="任务类型">







          <el-select







            v-model="searchForm.taskType"







            placeholder="请选择类型"







            clearable







            @change="handleSearch"







          >







            <el-option label="脚本生成" value="AUTO_SCRIPT_GENERATION" />







            <el-option label="脚本修复" value="AUTO_SCRIPT_REPAIR" />







            <el-option label="手动执行中" value="MANUAL_EXECUTION" />







          </el-select>







        </el-form-item>







        <el-form-item>







          <el-button type="primary" @click="handleSearch">查询</el-button>







          <el-button @click="handleReset">重置</el-button>







        </el-form-item>







      </el-form>







    </el-card>















    <!-- 任务列表 -->







    <el-card class="table-card" shadow="never">







      <el-tabs v-model="activeTab" @tab-change="handleTabChange">







        <el-tab-pane label="执行中任务" name="tasks">







          <el-table







            v-loading="loading"







            :data="taskList"







            stripe







            style="width: 100%"







          >







            <el-table-column prop="taskCode" label="任务编码" width="180" />







            <el-table-column prop="taskName" label="任务名称" min-width="200" show-overflow-tooltip />







            <el-table-column prop="taskType" label="任务类型" width="120">







              <template #default="scope">







                <el-tag>{{ getTaskTypeText(scope.row.taskType) }}</el-tag>







              </template>







            </el-table-column>







            <el-table-column prop="taskStatus" label="状态" width="100">







              <template #default="scope">







                <el-tag :type="getStatusType(scope.row.taskStatus)">







                  {{ getStatusText(scope.row.taskStatus) }}







                </el-tag>







              </template>







            </el-table-column>







            <el-table-column prop="progress" label="进度" width="120">







              <template #default="scope">







                <el-progress







                  v-if="scope.row.progress !== undefined"







                  :percentage="scope.row.progress"







                  :status="scope.row.taskStatus === 'SUCCESS' ? 'success' : scope.row.taskStatus === 'FAILED' ? 'exception' : undefined"







                />







                <span v-else>-</span>







              </template>







            </el-table-column>







            <el-table-column prop="successCount" label="成功" width="80" />







            <el-table-column prop="failCount" label="失败" width="80" />







            <el-table-column prop="creatorName" label="创建人" width="120" />







            <el-table-column prop="createTime" label="创建时间" width="180" />







            <el-table-column label="操作" width="250" fixed="right">







              <template #default="scope">







                <el-button size="small" link type="primary" @click="handleViewTask(scope.row)">







                  查看







                </el-button>







                <el-button size="small" link type="info" @click="handleViewRecords(scope.row)">







                  执行中记录







                </el-button>







                <el-button







                  size="small"







                  link







                  type="danger"







                  :disabled="scope.row.taskStatus === 'PROCESSING'"







                  @click="handleDeleteTask(scope.row)"







                >







                  删除







                </el-button>







              </template>







            </el-table-column>







          </el-table>















          <!-- 分页 -->







          <div class="pagination">







            <el-pagination







              v-model:current-page="pagination.page"







              v-model:page-size="pagination.size"







              :total="pagination.total"







              :page-sizes="[10, 20, 50, 100]"







              layout="total, sizes, prev, pager, next, jumper"







              @size-change="handleSizeChange"







              @current-change="handlePageChange"







            />







          </div>







        </el-tab-pane>















        <el-tab-pane label="执行中记录" name="records">







          <el-table







            v-loading="recordLoading"







            :data="recordList"







            stripe







            style="width: 100%"







          >







            <el-table-column prop="recordCode" label="记录编码" width="180" />







            <el-table-column prop="taskId" label="任务ID" width="100" />







            <el-table-column prop="executionType" label="执行中类型" width="100">







              <template #default="scope">







                <el-tag :type="scope.row.executionType === 'AUTOMATED' ? 'success' : 'info'">







                  {{ scope.row.executionType === 'AUTOMATED' ? '自动' : '手动' }}







                </el-tag>







              </template>







            </el-table-column>







            <el-table-column prop="executionStatus" label="执行状态" width="100">







              <template #default="scope">







                <el-tag :type="getExecutionStatusType(scope.row.executionStatus)">







                  {{ getExecutionStatusText(scope.row.executionStatus) }}







                </el-tag>







              </template>







            </el-table-column>







            <el-table-column prop="executionDuration" label="耗时(ms)" width="120" />







            <el-table-column prop="executedByName" label="执行人" width="120" />







            <el-table-column prop="executionTime" label="执行时间" width="180" />







            <el-table-column label="操作" width="150" fixed="right">







              <template #default="scope">







                <el-button size="small" link type="primary" @click="handleViewRecord(scope.row)">







                  查看详情







                </el-button>







              </template>







            </el-table-column>







          </el-table>















          <!-- 分页 -->







          <div class="pagination">







            <el-pagination







              v-model:current-page="recordPagination.page"







              v-model:page-size="recordPagination.size"







              :total="recordPagination.total"







              :page-sizes="[10, 20, 50, 100]"







              layout="total, sizes, prev, pager, next, jumper"







              @size-change="handleRecordSizeChange"







              @current-change="handleRecordPageChange"







            />







          </div>







        </el-tab-pane>







      </el-tabs>







    </el-card>















    <!-- 任务详情对话框-->







    <el-dialog







      v-model="taskDialogVisible"







      title="任务详情"







      width="900px"







    >







      <el-descriptions :column="2" border v-if="currentTask">







        <el-descriptions-item label="任务编码">{{ currentTask.taskCode }}</el-descriptions-item>







        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>







        <el-descriptions-item label="任务类型">{{ getTaskTypeText(currentTask.taskType) }}</el-descriptions-item>







        <el-descriptions-item label="任务状'">







          <el-tag :type="getStatusType(currentTask.taskStatus)">







            {{ getStatusText(currentTask.taskStatus) }}







          </el-tag>







        </el-descriptions-item>







        <el-descriptions-item label="进度" :span="2">







          <el-progress







            v-if="currentTask.progress !== undefined"







            :percentage="currentTask.progress"







          />







          <span v-else>-</span>







        </el-descriptions-item>







        <el-descriptions-item label="成功数量">{{ currentTask.successCount || 0 }}</el-descriptions-item>







        <el-descriptions-item label="失败数量">{{ currentTask.failCount || 0 }}</el-descriptions-item>







        <el-descriptions-item label="脚本类型">{{ currentTask.scriptType || '-' }}</el-descriptions-item>







        <el-descriptions-item label="脚本语言">{{ currentTask.scriptLanguage || '-' }}</el-descriptions-item>







        <el-descriptions-item label="创建人">{{ currentTask.creatorName || '-' }}</el-descriptions-item>







        <el-descriptions-item label="创建时间">{{ currentTask.createTime || '-' }}</el-descriptions-item>







        <el-descriptions-item label="完成时间" :span="2">{{ currentTask.finishTime || '-' }}</el-descriptions-item>







        <el-descriptions-item label="错误信息" :span="2" v-if="currentTask.errorMessage">







          <el-alert type="error" :closable="false">







            {{ currentTask.errorMessage }}







          </el-alert>







        </el-descriptions-item>







        <el-descriptions-item label="脚本内容" :span="2" v-if="currentTask.scriptContent">







          <el-input







            :model-value="currentTask.scriptContent"







            type="textarea"







            :rows="10"







            readonly







            style="font-family: 'Courier New', monospace"







          />







        </el-descriptions-item>







      </el-descriptions>







    </el-dialog>















    <!-- 执行记录详情对话-->







    <el-dialog







      v-model="recordDialogVisible"







      title="执行记录详情"







      width="900px"







    >







      <el-descriptions :column="2" border v-if="currentRecord">







        <el-descriptions-item label="记录编码">{{ currentRecord.recordCode }}</el-descriptions-item>







        <el-descriptions-item label="任务ID">{{ currentRecord.taskId }}</el-descriptions-item>







        <el-descriptions-item label="执行中类型">







          <el-tag :type="currentRecord.executionType === 'AUTOMATED' ? 'success' : 'info'">







            {{ currentRecord.executionType === 'AUTOMATED' ? '自动' : '手动' }}







          </el-tag>







        </el-descriptions-item>







        <el-descriptions-item label="执行状态">







          <el-tag :type="getExecutionStatusType(currentRecord.executionStatus)">







            {{ getExecutionStatusText(currentRecord.executionStatus) }}







          </el-tag>







        </el-descriptions-item>







        <el-descriptions-item label="执行耗时">{{ currentRecord.executionDuration || '-' }} ms</el-descriptions-item>







        <el-descriptions-item label="执行人">{{ currentRecord.executedByName || '-' }}</el-descriptions-item>







        <el-descriptions-item label="执行时间">{{ currentRecord.executionTime || '-' }}</el-descriptions-item>







        <el-descriptions-item label="完成时间">{{ currentRecord.finishTime || '-' }}</el-descriptions-item>







        <el-descriptions-item label="执行结果" :span="2" v-if="currentRecord.executionResult">







          <el-input







            :model-value="currentRecord.executionResult"







            type="textarea"







            :rows="5"







            readonly







          />







        </el-descriptions-item>







        <el-descriptions-item label="执行日志" :span="2" v-if="currentRecord.executionLog">







          <el-input







            :model-value="currentRecord.executionLog"







            type="textarea"







            :rows="10"







            readonly







            style="font-family: 'Courier New', monospace"







          />







        </el-descriptions-item>







        <el-descriptions-item label="错误信息" :span="2" v-if="currentRecord.errorMessage">







          <el-alert type="error" :closable="false">







            {{ currentRecord.errorMessage }}







          </el-alert>







        </el-descriptions-item>







        <el-descriptions-item label="截图" :span="2" v-if="currentRecord.screenshotUrl">







          <el-image







            :src="currentRecord.screenshotUrl"







            :preview-src-list="[currentRecord.screenshotUrl]"







            style="max-width: 500px; max-height: 300px"







          />







        </el-descriptions-item>







      </el-descriptions>







    </el-dialog>







  </div>







</template>















<script setup lang="ts">







import { ref, reactive, onMounted } from 'vue'







import { ElMessage, ElMessageBox } from 'element-plus'







import { Plus } from '@element-plus/icons-vue'







import { testExecutionApi, type TestExecutionTaskResponse, type TestExecutionRecordResponse, type TestExecutionStatistics } from '@/api/testExecution'















// 响应式数据







const loading = ref(false)







const recordLoading = ref(false)







const activeTab = ref('tasks')















const searchForm = reactive({







  taskName: '',







  taskStatus: '',







  taskType: ''







})















const taskList = ref<TestExecutionTaskResponse[]>([])







const recordList = ref<TestExecutionRecordResponse[]>([])







const statistics = ref<TestExecutionStatistics>({})















const pagination = reactive({







  page: 1,







  size: 10,







  total: 0







})















const recordPagination = reactive({







  page: 1,







  size: 10,







  total: 0







})















const taskDialogVisible = ref(false)







const recordDialogVisible = ref(false)







const currentTask = ref<TestExecutionTaskResponse | null>(null)







const currentRecord = ref<TestExecutionRecordResponse | null>(null)















// 获取状态文







const getStatusText = (status?: string) => {







  const statusMap: Record<string, string> = {







    PENDING: '等待中',







    PROCESSING: '处理中',







    SUCCESS: '成功',







    FAILED: '失败'







  }







  return statusMap[status || ''] || status || '-'







}















// 获取状态类型'







const getStatusType = (status?: string) => {







  const typeMap: Record<string, string> = {







    PENDING: 'info',







    PROCESSING: 'warning',







    SUCCESS: 'success',







    FAILED: 'danger'







  }







  return typeMap[status || ''] || '' 







}















// 获取执行中状态文







const getExecutionStatusText = (status?: string) => {







  const statusMap: Record<string, string> = {







    PENDING: '等待中',







    RUNNING: '执行中',







    SUCCESS: '成功',







    FAILED: '失败',







    SKIPPED: '已跳过'







  }







  return statusMap[status || ''] || status || '-'







}















// 获取执行中状态类







const getExecutionStatusType = (status?: string) => {







  const typeMap: Record<string, string> = {







    PENDING: 'info',







    RUNNING: 'warning',







    SUCCESS: 'success',







    FAILED: 'danger',







    SKIPPED: 'info'







  }







  return typeMap[status || ''] || '' 







}















// 获取任务类型文本







const getTaskTypeText = (type?: string) => {







  const typeMap: Record<string, string> = {







    AUTO_SCRIPT_GENERATION: '脚本生成',







    AUTO_SCRIPT_REPAIR: '脚本修复',







    MANUAL_EXECUTION: '手动执行'







  }







  return typeMap[type || ''] || type || '-'







}















// 加载任务列表







const loadTaskList = async () => {







  loading.value = true







  try {







    const response = await testExecutionApi.getExecutionTaskList({







      page: pagination.page - 1, // 后端开







      size: pagination.size,







      taskName: searchForm.taskName || undefined,







      taskStatus: searchForm.taskStatus || undefined,







      taskType: searchForm.taskType || undefined







    })







    if (response.data) {







      taskList.value = response.data.content || []







      pagination.total = response.data.totalElements || 0







    }







  } catch (error: any) {







    console.error('加载任务列表失败:', error)







    ElMessage.error(error.message || '加载任务列表失败')







  } finally {







    loading.value = false







  }







}















// 加载执行中记录列表







const loadRecordList = async () => {







  recordLoading.value = true







  try {







    const response = await testExecutionApi.getExecutionRecordList({







      page: recordPagination.page - 1,







      size: recordPagination.size







    })







    if (response.data) {







      recordList.value = response.data.content || []







      recordPagination.total = response.data.totalElements || 0







    }







  } catch (error: any) {







    console.error('加载执行中记录列表失败:', error)







    ElMessage.error(error.message || '加载执行中记录列表失败')







  } finally {







    recordLoading.value = false







  }







}















// 加载统计信息







const loadStatistics = async () => {







  try {







    const response = await testExecutionApi.getExecutionStatistics()







    if (response.data) {







      statistics.value = response.data







    }







  } catch (error: any) {







    console.error('加载统计信息失败:', error)







  }







}















// 查询







const handleSearch = () => {







  pagination.page = 1







  loadTaskList()







}















// 重置







const handleReset = () => {







  searchForm.taskName = 







  searchForm.taskStatus = 







  searchForm.taskType = 







  handleSearch()







}















// 分页大小变化







const handleSizeChange = (size: number) => {







  pagination.size = size







  pagination.page = 1







  loadTaskList()







}















// 页码变化







const handlePageChange = (page: number) => {







  pagination.page = page







  loadTaskList()







}















// 记录分页大小变化







const handleRecordSizeChange = (size: number) => {







  recordPagination.size = size







  recordPagination.page = 1







  loadRecordList()







}















// 记录页码变化







const handleRecordPageChange = (page: number) => {







  recordPagination.page = page







  loadRecordList()







}















// Tab切换







const handleTabChange = (tab: string) => {







  if (tab === 'records' && recordList.value.length === 0) {







    loadRecordList()







  }







}















// 创建人任务







const handleCreateTask = () => {







  ElMessage.info('创建人任务功能待实现，可通过UI脚本生成或脚本修复页面创建人任务')







}















// 查看任务详情







const handleViewTask = async (task: TestExecutionTaskResponse) => {







  if (task.id) {







    try {







      const response = await testExecutionApi.getExecutionTaskById(task.id)







      if (response.data) {







        currentTask.value = response.data







        taskDialogVisible.value = true







      }







    } catch (error: any) {







      ElMessage.error(error.message || '获取任务详情失败')







    }







  }







}















// 查看执行中记录







const handleViewRecords = async (task: TestExecutionTaskResponse) => {







  if (task.id) {







    activeTab.value = 'records'







    searchForm.taskName = task.taskName







    // 可以添加任务ID筛







    loadRecordList()







  }







}















// 查看记录详情







const handleViewRecord = async (record: TestExecutionRecordResponse) => {







  if (record.id) {







    try {







      const response = await testExecutionApi.getExecutionRecordById(record.id)







      if (response.data) {







        currentRecord.value = response.data







        recordDialogVisible.value = true







      }







    } catch (error: any) {







      ElMessage.error(error.message || '获取记录详情失败')







    }







  }







}















// 删除任务







const handleDeleteTask = async (task: TestExecutionTaskResponse) => {







  if (!task.id) return















  try {







    await ElMessageBox.confirm('确定要删除该任务吗？', '提示', {







      type: 'warning'







    })















    await testExecutionApi.deleteExecutionTask(task.id)







    ElMessage.success('删除成功')







    loadTaskList()







    loadStatistics()







  } catch (error: any) {







    if (error !== 'cancel') {







      console.error('删除任务失败:', error)







      ElMessage.error(error.message || '删除任务失败')







    }







  }







}















// 初始







onMounted(() => {







  loadTaskList()







  loadStatistics()







})







</script>















<style scoped lang="scss">







.test-execution-management {







  padding: 20px;















  .header {







    display: flex;







    justify-content: space-between;







    align-items: center;







    margin-bottom: 20px;















    h2 {







      margin: 0;







    }















    .header-actions {







      display: flex;







      gap: 10px;







    }







  }















  .stat-card {







    text-align: center;















    .stat-value {







      font-size: 32px;







      font-weight: bold;







      margin-bottom: 8px;















      &.success {







        color: #67c23a;







      }















      &.warning {







        color: #e6a23c;







      }















      &.danger {







        color: #f56c6c;







      }







    }















    .stat-label {







      font-size: 14px;







      color: #909399;







    }







  }















  .search-card {







    margin-bottom: 20px;















    .search-form {







      margin: 0;







    }







  }















  .table-card {







    .pagination {







      margin-top: 20px;







      display: flex;







      justify-content: flex-end;







    }







  }







}







</style>