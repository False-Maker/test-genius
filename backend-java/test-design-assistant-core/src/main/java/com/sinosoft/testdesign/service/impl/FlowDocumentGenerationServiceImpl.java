package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.dto.PathDiagramRequestDTO;
import com.sinosoft.testdesign.dto.PathDiagramResponseDTO;
import com.sinosoft.testdesign.dto.SceneDiagramRequestDTO;
import com.sinosoft.testdesign.dto.SceneDiagramResponseDTO;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.service.FlowDocumentGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程文档生成服务实现类
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowDocumentGenerationServiceImpl implements FlowDocumentGenerationService {
    
    private final RequirementRepository requirementRepository;
    private final TestCaseRepository testCaseRepository;
    
    @Override
    @Transactional(readOnly = true)
    public SceneDiagramResponseDTO generateSceneDiagram(SceneDiagramRequestDTO request) {
        log.info("生成场景图，请求参数：{}", request);
        
        try {
            // 1. 提取场景数据
            SceneData sceneData = extractSceneData(request);
            
            // 2. 生成Mermaid代码
            String mermaidCode = generateSceneDiagramMermaid(sceneData, request);
            
            // 3. 构建响应
            SceneDiagramResponseDTO response = new SceneDiagramResponseDTO();
            response.setMermaidCode(mermaidCode);
            response.setTitle(sceneData.getTitle());
            response.setFormat(request.getFormat());
            response.setNodeCount(sceneData.getNodes().size());
            response.setEdgeCount(sceneData.getEdges().size());
            
            // 4. 如果需要导出文件，生成文件URL
            if (!"MERMAID".equalsIgnoreCase(request.getFormat())) {
                String fileName = generateFileName(sceneData.getTitle(), request.getFormat());
                String fileUrl = exportSceneDiagramFile(mermaidCode, request.getFormat(), fileName);
                response.setFileUrl(fileUrl);
                response.setFileName(fileName);
            }
            
            log.info("场景图生成成功，节点数：{}，边数：{}", response.getNodeCount(), response.getEdgeCount());
            return response;
            
        } catch (Exception e) {
            log.error("生成场景图失败", e);
            throw new RuntimeException("生成场景图失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public PathDiagramResponseDTO generatePathDiagram(PathDiagramRequestDTO request) {
        log.info("生成路径图，请求参数：{}", request);
        
        try {
            // 1. 提取路径数据
            PathData pathData = extractPathData(request);
            
            // 2. 生成Mermaid代码
            String mermaidCode = generatePathDiagramMermaid(pathData, request);
            
            // 3. 构建响应
            PathDiagramResponseDTO response = new PathDiagramResponseDTO();
            response.setMermaidCode(mermaidCode);
            response.setTitle(pathData.getTitle());
            response.setFormat(request.getFormat());
            response.setNodeCount(pathData.getNodes().size());
            response.setEdgeCount(pathData.getEdges().size());
            response.setPathCount(pathData.getPaths().size());
            
            // 4. 如果需要导出文件，生成文件URL
            if (!"MERMAID".equalsIgnoreCase(request.getFormat())) {
                String fileName = generateFileName(pathData.getTitle(), request.getFormat());
                String fileUrl = exportPathDiagramFile(mermaidCode, request.getFormat(), fileName);
                response.setFileUrl(fileUrl);
                response.setFileName(fileName);
            }
            
            log.info("路径图生成成功，节点数：{}，边数：{}，路径数：{}", 
                    response.getNodeCount(), response.getEdgeCount(), response.getPathCount());
            return response;
            
        } catch (Exception e) {
            log.error("生成路径图失败", e);
            throw new RuntimeException("生成路径图失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public String exportSceneDiagramFile(String mermaidCode, String format, String fileName) {
        log.info("导出场景图文件，格式：{}，文件名：{}", format, fileName);
        
        // TODO: 实现文件导出逻辑
        // 方案1：使用Python服务（Mermaid CLI）将Mermaid代码转换为PNG/SVG/PDF
        // 方案2：前端使用Mermaid.js渲染后导出
        // 方案3：使用Puppeteer服务（需要Node.js）
        
        // 当前返回占位符URL，实际实现需要根据选择的方案来实现
        return "/api/v1/flow-documents/files/" + fileName;
    }
    
    @Override
    public String exportPathDiagramFile(String mermaidCode, String format, String fileName) {
        log.info("导出路径图文件，格式：{}，文件名：{}", format, fileName);
        
        // TODO: 实现文件导出逻辑（同exportSceneDiagramFile）
        return "/api/v1/flow-documents/files/" + fileName;
    }
    
    /**
     * 提取场景数据
     */
    private SceneData extractSceneData(SceneDiagramRequestDTO request) {
        SceneData sceneData = new SceneData();
        List<SceneNode> nodes = new ArrayList<>();
        List<SceneEdge> edges = new ArrayList<>();
        
        if (request.getRequirementId() != null) {
            // 基于需求生成场景图
            TestRequirement requirement = requirementRepository.findById(request.getRequirementId())
                    .orElseThrow(() -> new IllegalArgumentException("需求不存在，ID：" + request.getRequirementId()));
            
            sceneData.setTitle(StringUtils.hasText(request.getTitle()) ? 
                    request.getTitle() : requirement.getRequirementName());
            sceneData.setRequirementId(request.getRequirementId());
            
            // 查询需求下的所有用例
            List<TestCase> cases = testCaseRepository.findByRequirementId(request.getRequirementId());
            
            // 根据用例类型和组织场景
            Map<String, List<TestCase>> casesByType = cases.stream()
                    .collect(Collectors.groupingBy(c -> 
                            StringUtils.hasText(c.getCaseType()) ? c.getCaseType() : "正常"));
            
            // 创建场景节点
            int nodeIndex = 1;
            for (Map.Entry<String, List<TestCase>> entry : casesByType.entrySet()) {
                String sceneType = entry.getKey();
                List<TestCase> sceneCases = entry.getValue();
                
                SceneNode node = new SceneNode();
                node.setId("scene_" + nodeIndex);
                node.setLabel(sceneType + "场景(" + sceneCases.size() + "个用例)");
                node.setType(sceneType);
                if (request.getIncludeCaseDetails()) {
                    node.setDetails(sceneCases.stream()
                            .map(TestCase::getCaseName)
                            .collect(Collectors.toList()));
                }
                nodes.add(node);
                nodeIndex++;
            }
            
            // 创建边（场景之间的关联）
            for (int i = 0; i < nodes.size() - 1; i++) {
                SceneEdge edge = new SceneEdge();
                edge.setFrom(nodes.get(i).getId());
                edge.setTo(nodes.get(i + 1).getId());
                edge.setLabel("顺序");
                edges.add(edge);
            }
            
        } else if (request.getCaseIds() != null && !request.getCaseIds().isEmpty()) {
            // 基于用例列表生成场景图
            List<TestCase> cases = testCaseRepository.findAllById(request.getCaseIds());
            if (cases.isEmpty()) {
                throw new IllegalArgumentException("用例不存在或为空");
            }
            
            sceneData.setTitle(StringUtils.hasText(request.getTitle()) ? 
                    request.getTitle() : "测试场景图");
            
            // 为每个用例创建一个场景节点
            int nodeIndex = 1;
            for (TestCase testCase : cases) {
                SceneNode node = new SceneNode();
                node.setId("scene_" + nodeIndex);
                node.setLabel(testCase.getCaseName());
                node.setType(testCase.getCaseType());
                if (request.getIncludeCaseDetails()) {
                    node.setDetails(Arrays.asList(
                            "前置条件：" + (StringUtils.hasText(testCase.getPreCondition()) ? 
                                    testCase.getPreCondition() : "无"),
                            "测试步骤：" + (StringUtils.hasText(testCase.getTestStep()) ? 
                                    testCase.getTestStep() : "无")
                    ));
                }
                nodes.add(node);
                nodeIndex++;
            }
            
            // 创建边（用例之间的关联）
            for (int i = 0; i < nodes.size() - 1; i++) {
                SceneEdge edge = new SceneEdge();
                edge.setFrom(nodes.get(i).getId());
                edge.setTo(nodes.get(i + 1).getId());
                edge.setLabel("顺序");
                edges.add(edge);
            }
            
        } else {
            throw new IllegalArgumentException("必须提供需求ID或用例ID列表");
        }
        
        sceneData.setNodes(nodes);
        sceneData.setEdges(edges);
        return sceneData;
    }
    
    /**
     * 提取路径数据
     */
    private PathData extractPathData(PathDiagramRequestDTO request) {
        PathData pathData = new PathData();
        List<PathNode> nodes = new ArrayList<>();
        List<PathEdge> edges = new ArrayList<>();
        List<List<String>> paths = new ArrayList<>();
        
        List<TestCase> cases = new ArrayList<>();
        
        if (request.getCaseId() != null) {
            // 基于单个用例生成路径图
            TestCase testCase = testCaseRepository.findById(request.getCaseId())
                    .orElseThrow(() -> new IllegalArgumentException("用例不存在，ID：" + request.getCaseId()));
            cases.add(testCase);
            pathData.setTitle(StringUtils.hasText(request.getTitle()) ? 
                    request.getTitle() : testCase.getCaseName());
            
        } else if (request.getCaseIds() != null && !request.getCaseIds().isEmpty()) {
            // 基于多个用例生成路径图
            cases = testCaseRepository.findAllById(request.getCaseIds());
            if (cases.isEmpty()) {
                throw new IllegalArgumentException("用例不存在或为空");
            }
            pathData.setTitle(StringUtils.hasText(request.getTitle()) ? 
                    request.getTitle() : "测试路径图");
            
        } else if (request.getRequirementId() != null) {
            // 基于需求生成路径图
            TestRequirement requirement = requirementRepository.findById(request.getRequirementId())
                    .orElseThrow(() -> new IllegalArgumentException("需求不存在，ID：" + request.getRequirementId()));
            cases = testCaseRepository.findByRequirementId(request.getRequirementId());
            pathData.setTitle(StringUtils.hasText(request.getTitle()) ? 
                    request.getTitle() : requirement.getRequirementName() + "-测试路径图");
            
        } else {
            throw new IllegalArgumentException("必须提供用例ID、用例ID列表或需求ID");
        }
        
        // 从用例中提取测试步骤，生成路径
        Set<String> stepSet = new LinkedHashSet<>(); // 保持顺序
        Map<String, String> stepIdMap = new HashMap<>(); // 步骤内容 -> 节点ID映射
        
        for (TestCase testCase : cases) {
            if (StringUtils.hasText(testCase.getTestStep())) {
                // 解析测试步骤（假设每行一个步骤，或使用特定分隔符）
                String[] steps = testCase.getTestStep().split("\n|；|;");
                List<String> path = new ArrayList<>();
                
                for (String step : steps) {
                    step = step.trim();
                    if (StringUtils.hasText(step) && step.length() > 2) {
                        // 清理步骤编号（如"1."、"步骤1："等）
                        step = step.replaceAll("^\\d+[.、：:：]\\s*", "");
                        
                        if (StringUtils.hasText(step)) {
                            stepSet.add(step);
                            String stepId = stepIdMap.computeIfAbsent(step, s -> "step_" + (stepIdMap.size() + 1));
                            path.add(stepId);
                        }
                    }
                }
                
                if (!path.isEmpty()) {
                    paths.add(path);
                }
            }
        }
        
        // 创建节点
        int nodeIndex = 1;
        for (String step : stepSet) {
            String stepId = stepIdMap.get(step);
            PathNode node = new PathNode();
            node.setId(stepId);
            // 限制步骤文本长度（Mermaid节点标签不能太长）
            String shortStep = step.length() > 50 ? step.substring(0, 47) + "..." : step;
            node.setLabel(shortStep);
            node.setFullText(step);
            nodes.add(node);
        }
        
        // 创建边
        Set<String> edgeSet = new HashSet<>(); // 去重边
        for (List<String> path : paths) {
            for (int i = 0; i < path.size() - 1; i++) {
                String fromId = path.get(i);
                String toId = path.get(i + 1);
                String edgeKey = fromId + "->" + toId;
                
                if (!edgeSet.contains(edgeKey)) {
                    PathEdge edge = new PathEdge();
                    edge.setFrom(fromId);
                    edge.setTo(toId);
                    edges.add(edge);
                    edgeSet.add(edgeKey);
                }
            }
        }
        
        pathData.setNodes(nodes);
        pathData.setEdges(edges);
        pathData.setPaths(paths);
        return pathData;
    }
    
    /**
     * 生成场景图Mermaid代码
     */
    private String generateSceneDiagramMermaid(SceneData sceneData, SceneDiagramRequestDTO request) {
        StringBuilder mermaid = new StringBuilder();
        
        // Mermaid流程图定义
        mermaid.append("graph ").append(request.getDirection()).append("\n");
        mermaid.append("    %% 场景图：").append(sceneData.getTitle()).append("\n\n");
        
        // 添加节点
        for (SceneNode node : sceneData.getNodes()) {
            String nodeLabel = escapeMermaidLabel(node.getLabel());
            mermaid.append("    ").append(node.getId())
                    .append("[\"").append(nodeLabel).append("\"]\n");
        }
        
        // 添加边
        for (SceneEdge edge : sceneData.getEdges()) {
            String edgeLabel = StringUtils.hasText(edge.getLabel()) ? 
                    "|" + escapeMermaidLabel(edge.getLabel()) + "|" : "";
            mermaid.append("    ").append(edge.getFrom())
                    .append(" -->").append(edgeLabel).append(" ")
                    .append(edge.getTo()).append("\n");
        }
        
        return mermaid.toString();
    }
    
    /**
     * 生成路径图Mermaid代码
     */
    private String generatePathDiagramMermaid(PathData pathData, PathDiagramRequestDTO request) {
        StringBuilder mermaid = new StringBuilder();
        
        // Mermaid流程图定义
        mermaid.append("graph ").append(request.getDirection()).append("\n");
        mermaid.append("    %% 路径图：").append(pathData.getTitle()).append("\n\n");
        
        // 添加节点
        for (PathNode node : pathData.getNodes()) {
            String nodeLabel = escapeMermaidLabel(node.getLabel());
            mermaid.append("    ").append(node.getId())
                    .append("[\"").append(nodeLabel).append("\"]\n");
        }
        
        // 添加边
        for (PathEdge edge : pathData.getEdges()) {
            mermaid.append("    ").append(edge.getFrom())
                    .append(" --> ")
                    .append(edge.getTo()).append("\n");
        }
        
        return mermaid.toString();
    }
    
    /**
     * 转义Mermaid标签中的特殊字符
     */
    private String escapeMermaidLabel(String label) {
        if (label == null) {
            return "";
        }
        return label.replace("\"", "'")
                .replace("\n", " ")
                .replace("\r", " ");
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(String title, String format) {
        String extension = format.toLowerCase();
        String sanitizedTitle = title.replaceAll("[^\\w\\u4e00-\\u9fa5]", "_");
        return sanitizedTitle + "_" + System.currentTimeMillis() + "." + extension;
    }
    
    // 内部数据类
    
    /**
     * 场景数据
     */
    private static class SceneData {
        private String title;
        private Long requirementId;
        private List<SceneNode> nodes = new ArrayList<>();
        private List<SceneEdge> edges = new ArrayList<>();
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getRequirementId() { return requirementId; }
        public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }
        public List<SceneNode> getNodes() { return nodes; }
        public void setNodes(List<SceneNode> nodes) { this.nodes = nodes; }
        public List<SceneEdge> getEdges() { return edges; }
        public void setEdges(List<SceneEdge> edges) { this.edges = edges; }
    }
    
    /**
     * 场景节点
     */
    private static class SceneNode {
        private String id;
        private String label;
        private String type;
        private List<String> details;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public List<String> getDetails() { return details; }
        public void setDetails(List<String> details) { this.details = details; }
    }
    
    /**
     * 场景边
     */
    private static class SceneEdge {
        private String from;
        private String to;
        private String label;
        
        // Getters and Setters
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
    
    /**
     * 路径数据
     */
    private static class PathData {
        private String title;
        private List<PathNode> nodes = new ArrayList<>();
        private List<PathEdge> edges = new ArrayList<>();
        private List<List<String>> paths = new ArrayList<>();
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<PathNode> getNodes() { return nodes; }
        public void setNodes(List<PathNode> nodes) { this.nodes = nodes; }
        public List<PathEdge> getEdges() { return edges; }
        public void setEdges(List<PathEdge> edges) { this.edges = edges; }
        public List<List<String>> getPaths() { return paths; }
        public void setPaths(List<List<String>> paths) { this.paths = paths; }
    }
    
    /**
     * 路径节点
     */
    private static class PathNode {
        private String id;
        private String label;
        private String fullText;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getFullText() { return fullText; }
        public void setFullText(String fullText) { this.fullText = fullText; }
    }
    
    /**
     * 路径边
     */
    private static class PathEdge {
        private String from;
        private String to;
        
        // Getters and Setters
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
    }
}

