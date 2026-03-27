import { Buffer } from 'node:buffer'
import { expect, test, type APIRequestContext, type Page } from '@playwright/test'

async function loginThroughUi(page: Page) {
  await page.goto('/login')
  await expect(page.getByRole('button', { name: '进入系统' })).toBeVisible()

  await page.getByPlaceholder('用户名').fill('smoke-user')
  await page.getByPlaceholder('密码').fill('smoke-pass')

  await Promise.all([
    page.waitForURL('**/requirement'),
    page.getByRole('button', { name: '进入系统' }).click()
  ])

  await expect(page.locator('.sidebar-menu')).toBeVisible()
}

async function seedCaseReuseEmbeddings(request: APIRequestContext) {
  const initResponse = await request.post('/api/v1/case-reuse/init')
  expect(initResponse.ok()).toBeTruthy()

  const keywordResponse = await request.get('/api/v1/case-reuse/cases/search/keyword/投保', {
    params: {
      topK: 5
    }
  })
  expect(keywordResponse.ok()).toBeTruthy()

  const keywordPayload = await keywordResponse.json()
  const cases = Array.isArray(keywordPayload.data) ? keywordPayload.data : []
  expect(cases.length).toBeGreaterThan(0)

  for (const caseItem of cases.slice(0, 3) as Array<{ id?: number; caseId?: number }>) {
    const caseId = caseItem.caseId ?? caseItem.id
    expect(caseId).toBeTruthy()

    const embeddingResponse = await request.post(`/api/v1/case-reuse/cases/${caseId}/embedding`)
    expect(embeddingResponse.ok()).toBeTruthy()
  }
}

test('login flow reaches the main shell', async ({ page }) => {
  await loginThroughUi(page)
  await expect(page).toHaveURL(/\/requirement$/)
  await expect(page.locator('.sidebar-menu')).toContainText('知识库')
})

test('case reuse supports semantic search and embedding update', async ({ page, request }) => {
  await loginThroughUi(page)
  await seedCaseReuseEmbeddings(request)
  await page.goto('/case-reuse')

  await expect(page.getByRole('heading', { name: '用例复用管理' })).toBeVisible()

  const searchResponsePromise = page.waitForResponse((response) => {
    return response.request().method() === 'POST' &&
      response.url().includes('/api/v1/case-reuse/cases/search/similar')
  })

  await page.getByPlaceholder('请输入用例描述或关键词').fill('正常投保流程')
  await page.getByRole('button', { name: '检索' }).click()

  const searchResponse = await searchResponsePromise
  expect(searchResponse.ok()).toBeTruthy()

  const searchPayload = await searchResponse.json()
  const similarCases = Array.isArray(searchPayload.data) ? searchPayload.data : []
  expect(similarCases.length).toBeGreaterThan(0)

  const firstCase = similarCases[0] as {
    id?: number
    caseId?: number
    case_name?: string
    caseName?: string
  }
  const caseId = firstCase.caseId ?? firstCase.id
  const caseName = firstCase.caseName ?? firstCase.case_name

  expect(caseId).toBeTruthy()
  expect(caseName).toBeTruthy()

  const resultRow = page.locator('.el-table__row', { hasText: caseName! }).first()
  await expect(resultRow).toBeVisible()

  const updateResponsePromise = page.waitForResponse((response) => {
    return response.request().method() === 'POST' &&
      response.url().includes(`/api/v1/case-reuse/cases/${caseId}/embedding`)
  })

  await resultRow.getByText('更新向量').click()

  const updateResponse = await updateResponsePromise
  expect(updateResponse.ok()).toBeTruthy()

  const updatePayload = await updateResponse.json()
  expect(updatePayload.data).toBe(true)
  await expect(page.getByText('向量更新成功')).toBeVisible()

  await resultRow.locator('.el-checkbox').first().click()
  await page.getByRole('button', { name: '创建测试套件' }).click()
  const suiteDialog = page.getByRole('dialog', { name: '创建测试套件' })
  await expect(suiteDialog).toBeVisible()

  const suiteName = `smoke-suite-${Date.now()}`
  const createSuiteResponsePromise = page.waitForResponse((response) => {
    return response.request().method() === 'POST' &&
      response.url().includes('/api/v1/case-reuse/suites')
  })

  await suiteDialog.getByPlaceholder('请输入测试套件名称').fill(suiteName)
  await suiteDialog.getByRole('button', { name: '确定' }).click()

  const createSuiteResponse = await createSuiteResponsePromise
  expect(createSuiteResponse.ok()).toBeTruthy()

  const createSuitePayload = await createSuiteResponse.json()
  expect(createSuitePayload.data).toBeGreaterThan(0)
  await expect(page.getByText('测试套件创建成功')).toBeVisible()
  await expect(suiteDialog).toBeHidden()
})

test('knowledge base supports upload and semantic search', async ({ page }) => {
  await loginThroughUi(page)
  await page.goto('/knowledge-base')

  await expect(page.getByRole('heading', { name: '知识库' })).toBeVisible()

  const fileStamp = Date.now()
  const fileName = `kb-upload-smoke-${fileStamp}.txt`
  const fileContent = `knowledge upload semantic smoke ${fileStamp}`

  const uploadResponsePromise = page.waitForResponse((response) => {
    return response.request().method() === 'POST' &&
      /\/api\/v1\/knowledge-base\/\d+\/upload/.test(response.url())
  })

  await page.locator('.header-actions').getByRole('button', { name: 'Upload' }).click()

  const uploadDialog = page.getByRole('dialog', { name: 'Upload Document' })
  await expect(uploadDialog).toBeVisible()
  await uploadDialog.locator('input[type="file"]').setInputFiles({
    name: fileName,
    mimeType: 'text/plain',
    buffer: Buffer.from(fileContent, 'utf-8')
  })
  await uploadDialog.locator('.el-dialog__footer').getByRole('button', { name: 'Upload', exact: true }).click()

  const uploadResponse = await uploadResponsePromise
  expect(uploadResponse.ok()).toBeTruthy()

  const uploadPayload = await uploadResponse.json()
  expect(uploadPayload.data).toBeTruthy()
  await expect(page.getByText('文档上传成功')).toBeVisible()
  await expect(page.locator('.document-grid')).toContainText(fileName)

  const semanticResponsePromise = page.waitForResponse((response) => {
    return response.request().method() === 'POST' &&
      response.url().includes('/api/v1/knowledge-base/documents/search')
  })

  await page.getByPlaceholder('Search knowledge base...').fill(fileContent)
  await page.getByPlaceholder('Search knowledge base...').press('Enter')

  const semanticResponse = await semanticResponsePromise
  expect(semanticResponse.ok()).toBeTruthy()

  const isUploadedDocumentHit = (candidateDocuments: Array<{ doc_name?: string; docName?: string }>) => {
    return candidateDocuments.some((doc) => (doc.doc_name ?? doc.docName) === fileName)
  }

  let semanticPayload = await semanticResponse.json()
  let documents = Array.isArray(semanticPayload.data) ? semanticPayload.data : []

  for (let attempt = 0; attempt < 15 && !isUploadedDocumentHit(documents); attempt += 1) {
    await page.waitForTimeout(2_000)

    const retryResponsePromise = page.waitForResponse((response) => {
      return response.request().method() === 'POST' &&
        response.url().includes('/api/v1/knowledge-base/documents/search')
    })

    await page.getByPlaceholder('Search knowledge base...').fill(fileContent)
    await page.getByPlaceholder('Search knowledge base...').press('Enter')

    const retryResponse = await retryResponsePromise
    expect(retryResponse.ok()).toBeTruthy()

    semanticPayload = await retryResponse.json()
    documents = Array.isArray(semanticPayload.data) ? semanticPayload.data : []
  }

  expect(documents.length).toBeGreaterThan(0)

  expect(isUploadedDocumentHit(documents)).toBeTruthy()
  await expect(page.locator('.document-grid')).toContainText(fileName)
})

test('knowledge base supports adding a document from Add New', async ({ page }) => {
  await loginThroughUi(page)
  await page.goto('/knowledge-base')

  await expect(page.getByRole('heading', { name: '知识库' })).toBeVisible()

  const stamp = Date.now()
  const docCode = `KB-SMOKE-${stamp}`
  const docName = `kb-add-smoke-${stamp}`
  const docContent = `knowledge add smoke ${stamp}`

  await page.locator('.header-actions').getByRole('button', { name: 'Add New' }).click()

  const addDialog = page.getByRole('dialog', { name: 'Add Document' })
  await expect(addDialog).toBeVisible()

  await addDialog.getByPlaceholder('DOC-001').fill(docCode)
  await addDialog.locator('.el-select').click()
  await page.keyboard.press('ArrowDown')
  await page.keyboard.press('Enter')
  await addDialog.getByPlaceholder('Document Name').fill(docName)
  await addDialog.getByPlaceholder('Category (Optional)').fill('smoke')
  await addDialog.getByPlaceholder('Document content...').fill(docContent)

  const addResponsePromise = page.waitForResponse((response) => {
    return response.request().method() === 'POST' &&
      response.url().includes('/api/v1/knowledge-base/documents')
  })

  await addDialog.getByRole('button', { name: 'Confirm' }).click()

  const addResponse = await addResponsePromise
  expect(addResponse.ok()).toBeTruthy()

  const addPayload = await addResponse.json()
  expect(addPayload.data).toBeGreaterThan(0)
  await expect(page.getByText('添加成功')).toBeVisible()
  await expect(addDialog).toBeHidden()
  await expect(page.locator('.document-grid')).toContainText(docName)
})
