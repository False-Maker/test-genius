package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.KnowledgeBaseRequestDTO;
import com.sinosoft.testdesign.dto.KnowledgeBaseResponseDTO;
import com.sinosoft.testdesign.dto.KnowledgePermissionRequestDTO;
import com.sinosoft.testdesign.entity.KnowledgeBase;
import com.sinosoft.testdesign.entity.KnowledgeBasePermission;
import com.sinosoft.testdesign.entity.KnowledgeBaseSyncLog;
import com.sinosoft.testdesign.repository.KnowledgeBasePermissionRepository;
import com.sinosoft.testdesign.repository.KnowledgeBaseRepository;
import com.sinosoft.testdesign.repository.KnowledgeBaseSyncLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 知识库管理服务单元测试（第四阶段 4.1）
 * 覆盖删除文档数量检查、统计信息填充、CRUD 等核心流程。
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("知识库管理服务测试")
class KnowledgeBaseManageServiceImplTest {

    @Mock
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Mock
    private KnowledgeBasePermissionRepository permissionRepository;

    @Mock
    private KnowledgeBaseSyncLogRepository syncLogRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KnowledgeBaseManageServiceImpl knowledgeBaseManageService;

    private KnowledgeBase kb;
    private KnowledgeBaseRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(knowledgeBaseManageService, "aiServiceUrl", "http://localhost:8000");
        kb = new KnowledgeBase();
        kb.setId(1L);
        kb.setKbCode("KB-20260128-0001");
        kb.setKbName("测试知识库");
        kb.setKbDescription("描述");
        kb.setKbType("private");
        kb.setIsActive("1");
        kb.setCreatorId(100L);
        kb.setCreateTime(LocalDateTime.now());
        kb.setUpdateTime(LocalDateTime.now());

        requestDTO = new KnowledgeBaseRequestDTO();
        requestDTO.setKbName("测试知识库");
        requestDTO.setKbCode("KB-20260128-0001");
        requestDTO.setKbDescription("描述");
        requestDTO.setKbType("private");
        requestDTO.setCreatorId(100L);
    }

    @Test
    @DisplayName("创建知识库-成功")
    void createKnowledgeBase_Success() {
        when(knowledgeBaseRepository.findByKbCode(anyString())).thenReturn(Optional.empty());
        when(knowledgeBaseRepository.save(any(KnowledgeBase.class))).thenAnswer(inv -> {
            KnowledgeBase k = inv.getArgument(0);
            k.setId(1L);
            return k;
        });

        Long id = knowledgeBaseManageService.createKnowledgeBase(requestDTO);

        assertNotNull(id);
        assertEquals(1L, id);
        verify(knowledgeBaseRepository).save(any(KnowledgeBase.class));
        verify(permissionRepository).save(any(KnowledgeBasePermission.class));
    }

    @Test
    @DisplayName("创建知识库-编码已存在抛出异常")
    void createKnowledgeBase_DuplicateCode_Throws() {
        when(knowledgeBaseRepository.findByKbCode("KB-20260128-0001")).thenReturn(Optional.of(kb));

        assertThrows(BusinessException.class, () ->
                knowledgeBaseManageService.createKnowledgeBase(requestDTO));
        verify(knowledgeBaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新知识库-成功")
    void updateKnowledgeBase_Success() {
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(knowledgeBaseRepository.save(any(KnowledgeBase.class))).thenReturn(kb);
        KnowledgeBaseRequestDTO update = new KnowledgeBaseRequestDTO();
        update.setKbName("新名称");
        update.setKbCode("KB-20260128-0001");

        assertDoesNotThrow(() -> knowledgeBaseManageService.updateKnowledgeBase(1L, update));
        verify(knowledgeBaseRepository).save(argThat(k -> "新名称".equals(k.getKbName())));
    }

    @Test
    @DisplayName("更新知识库-不存在抛出异常")
    void updateKnowledgeBase_NotFound_Throws() {
        when(knowledgeBaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                knowledgeBaseManageService.updateKnowledgeBase(999L, requestDTO));
    }

    @Test
    @DisplayName("删除知识库-文档数为0时成功")
    void deleteKnowledgeBase_NoDocuments_Success() {
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        Map<String, Object> countResp = new HashMap<>();
        countResp.put("success", true);
        countResp.put("count", 0);
        when(restTemplate.postForObject(
                ArgumentMatchers.contains("/knowledge/documents/count"),
                any(), eq(Map.class))).thenReturn(countResp);
        doNothing().when(permissionRepository).deleteByKbIdAndUserId(1L, null);
        doNothing().when(knowledgeBaseRepository).deleteById(1L);

        assertDoesNotThrow(() -> knowledgeBaseManageService.deleteKnowledgeBase(1L));
        verify(knowledgeBaseRepository).deleteById(1L);
    }

    @Test
    @DisplayName("删除知识库-存在文档时阻止删除")
    void deleteKnowledgeBase_HasDocuments_Throws() {
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        Map<String, Object> countResp = new HashMap<>();
        countResp.put("success", true);
        countResp.put("count", 5);
        when(restTemplate.postForObject(
                ArgumentMatchers.contains("/knowledge/documents/count"),
                any(), eq(Map.class))).thenReturn(countResp);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgeBaseManageService.deleteKnowledgeBase(1L));
        assertTrue(ex.getMessage().contains("5"));
        assertTrue(ex.getMessage().contains("文档"));
        verify(knowledgeBaseRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("删除知识库-知识库不存在抛出异常")
    void deleteKnowledgeBase_NotFound_Throws() {
        when(knowledgeBaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                knowledgeBaseManageService.deleteKnowledgeBase(999L));
        verify(knowledgeBaseRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("获取知识库详情-成功且填充统计信息")
    void getKnowledgeBaseById_Success_WithStatistics() {
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        Map<String, Object> stats = new HashMap<>();
        stats.put("document_count", 10);
        stats.put("chunk_count", 100);
        stats.put("last_sync_time", LocalDateTime.now().toString());
        when(restTemplate.postForObject(
                ArgumentMatchers.contains("/knowledge/statistics"),
                any(), eq(Map.class))).thenReturn(stats);

        KnowledgeBaseResponseDTO dto = knowledgeBaseManageService.getKnowledgeBaseById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("KB-20260128-0001", dto.getKbCode());
        assertEquals("测试知识库", dto.getKbName());
        assertEquals(10, dto.getDocumentCount());
        assertEquals(100, dto.getChunkCount());
    }

    @Test
    @DisplayName("获取知识库详情-不存在抛出异常")
    void getKnowledgeBaseById_NotFound_Throws() {
        when(knowledgeBaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                knowledgeBaseManageService.getKnowledgeBaseById(999L));
    }

    @Test
    @DisplayName("获取知识库列表-成功")
    void getKnowledgeBaseList_Success() {
        when(knowledgeBaseRepository.findByIsActive("1")).thenReturn(List.of(kb));
        when(restTemplate.postForObject(
                ArgumentMatchers.contains("/knowledge/statistics"),
                any(), eq(Map.class))).thenReturn(Map.of("document_count", 0, "chunk_count", 0));

        List<KnowledgeBaseResponseDTO> list = knowledgeBaseManageService.getKnowledgeBaseList();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("测试知识库", list.get(0).getKbName());
    }

    @Test
    @DisplayName("检查权限-存在返回true")
    void checkPermission_Exists_True() {
        when(permissionRepository.existsByKbIdAndUserIdAndPermissionType(1L, 100L, "admin"))
                .thenReturn(true);

        assertTrue(knowledgeBaseManageService.checkPermission(1L, 100L, "admin"));
    }

    @Test
    @DisplayName("检查权限-不存在返回false")
    void checkPermission_NotExists_False() {
        when(permissionRepository.existsByKbIdAndUserIdAndPermissionType(1L, 100L, "admin"))
                .thenReturn(false);

        assertFalse(knowledgeBaseManageService.checkPermission(1L, 100L, "admin"));
    }

    @Test
    @DisplayName("生成知识库编码-无已有编码时从0001开始")
    void generateKbCode_NoExisting_StartsFrom0001() {
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        when(knowledgeBaseRepository.findByKbCodeStartingWithOrderByIdDesc("KB-" + dateStr + "-"))
                .thenReturn(Collections.emptyList());

        String code = knowledgeBaseManageService.generateKbCode();

        assertNotNull(code);
        assertTrue(code.startsWith("KB-" + dateStr + "-"));
        assertTrue(code.endsWith("0001"));
    }

    @Test
    @DisplayName("生成知识库编码-有已有编码时递增")
    void generateKbCode_WithExisting_Increments() {
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        KnowledgeBase last = new KnowledgeBase();
        last.setKbCode("KB-" + dateStr + "-0003");
        when(knowledgeBaseRepository.findByKbCodeStartingWithOrderByIdDesc("KB-" + dateStr + "-"))
                .thenReturn(List.of(last));

        String code = knowledgeBaseManageService.generateKbCode();

        assertNotNull(code);
        assertTrue(code.endsWith("0004"));
    }
}
