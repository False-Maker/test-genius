import request from './request'
import type { PageResult } from './testReport'

// --- Test Specification ---

export interface TestSpecificationRequestDTO {
    specName: string
    specType: 'APPLICATION' | 'PUBLIC' | string
    specCategory?: string
    specDescription?: string
    specContent?: string
    applicableModules?: string
    applicableLayers?: string
    applicableMethods?: string
    effectiveDate?: string
    expireDate?: string
    creatorId?: number
    creatorName?: string
}

export interface TestSpecificationResponseDTO {
    id: number
    specCode: string
    specName: string
    specType: string
    specCategory: string
    specDescription: string
    specContent: string
    applicableModules: string
    applicableLayers: string
    applicableMethods: string
    currentVersion: string
    isActive: string
    effectiveDate: string
    expireDate: string
    creatorId: number
    creatorName: string
    createTime: string
    updateTime: string
    version: number
}

export interface SpecVersionResponseDTO {
    id: number
    specId: number
    versionNumber: string
    versionName: string
    versionDescription: string
    changeLog: string
    creatorId: number
    createTime: string
}

// --- Field Test Point ---

export interface FieldTestPointRequestDTO {
    fieldName: string
    specId?: number
    ruleType?: string
    ruleContent?: string
    ruleDescription?: string
    isActive?: string
}

export interface FieldTestPointResponseDTO {
    id: number
    fieldCode: string
    fieldName: string
    specId: number
    ruleType: string
    ruleContent: string
    ruleDescription: string
    isActive: string
    createTime: string
    updateTime: string
}

// --- Logic Test Point ---

export interface LogicTestPointRequestDTO {
    logicName: string
    specId?: number
    logicType?: string
    logicContent?: string
    logicDescription?: string
    isActive?: string
}

export interface LogicTestPointResponseDTO {
    id: number
    logicCode: string
    logicName: string
    specId: number
    logicType: string
    logicContent: string
    logicDescription: string
    isActive: string
    createTime: string
    updateTime: string
}

export const testSpecificationApi = {
    // --- Specification Management ---

    createSpecification(data: TestSpecificationRequestDTO) {
        return request.post<any, TestSpecificationResponseDTO>('/v1/specifications', data)
    },

    getSpecificationList(page: number = 0, size: number = 10, specName?: string, specType?: string, isActive?: string) {
        return request.get<any, PageResult<TestSpecificationResponseDTO>>('/v1/specifications', {
            params: { page, size, specName, specType, isActive }
        })
    },

    getSpecificationById(id: number) {
        return request.get<any, TestSpecificationResponseDTO>(`/v1/specifications/${id}`)
    },

    getSpecificationByCode(specCode: string) {
        return request.get<any, TestSpecificationResponseDTO>(`/v1/specifications/code/${specCode}`)
    },

    updateSpecification(id: number, data: TestSpecificationRequestDTO) {
        return request.put<any, TestSpecificationResponseDTO>(`/v1/specifications/${id}`, data)
    },

    deleteSpecification(id: number) {
        return request.delete<any, void>(`/v1/specifications/${id}`)
    },

    updateSpecificationStatus(id: number, isActive: string) {
        return request.put<any, TestSpecificationResponseDTO>(`/v1/specifications/${id}/status`, null, {
            params: { isActive }
        })
    },

    getApplicationSpecifications() {
        return request.get<any, TestSpecificationResponseDTO[]>('/v1/specifications/application')
    },

    getPublicSpecifications() {
        return request.get<any, TestSpecificationResponseDTO[]>('/v1/specifications/public')
    },

    // --- Version Management ---

    createVersion(specId: number, versionNumber: string, versionName?: string, versionDescription?: string, changeLog?: string) {
        return request.post<any, TestSpecificationResponseDTO>(`/v1/specifications/${specId}/versions`, null, {
            params: { versionNumber, versionName, versionDescription, changeLog }
        })
    },

    switchVersion(specId: number, versionNumber: string) {
        return request.put<any, TestSpecificationResponseDTO>(`/v1/specifications/${specId}/versions/${versionNumber}/switch`)
    },

    getVersionList(specId: number) {
        return request.get<any, SpecVersionResponseDTO[]>(`/v1/specifications/${specId}/versions`)
    },


    // --- Field Test Point Management ---

    createFieldTestPoint(data: FieldTestPointRequestDTO) {
        return request.post<any, FieldTestPointResponseDTO>('/v1/specifications/field-points', data)
    },

    getFieldTestPointList(page: number = 0, size: number = 10, fieldName?: string, specId?: number, isActive?: string) {
        return request.get<any, PageResult<FieldTestPointResponseDTO>>('/v1/specifications/field-points', {
            params: { page, size, fieldName, specId, isActive }
        })
    },

    getFieldTestPointById(id: number) {
        return request.get<any, FieldTestPointResponseDTO>(`/v1/specifications/field-points/${id}`)
    },

    getFieldTestPointsBySpecId(specId: number) {
        return request.get<any, FieldTestPointResponseDTO[]>(`/v1/specifications/${specId}/field-points`)
    },

    updateFieldTestPoint(id: number, data: FieldTestPointRequestDTO) {
        return request.put<any, FieldTestPointResponseDTO>(`/v1/specifications/field-points/${id}`, data)
    },

    deleteFieldTestPoint(id: number) {
        return request.delete<any, void>(`/v1/specifications/field-points/${id}`)
    },

    updateFieldTestPointStatus(id: number, isActive: string) {
        return request.put<any, FieldTestPointResponseDTO>(`/v1/specifications/field-points/${id}/status`, null, {
            params: { isActive }
        })
    },

    // --- Logic Test Point Management ---

    createLogicTestPoint(data: LogicTestPointRequestDTO) {
        return request.post<any, LogicTestPointResponseDTO>('/v1/specifications/logic-points', data)
    },

    getLogicTestPointList(page: number = 0, size: number = 10, logicName?: string, specId?: number, isActive?: string) {
        return request.get<any, PageResult<LogicTestPointResponseDTO>>('/v1/specifications/logic-points', {
            params: { page, size, logicName, specId, isActive }
        })
    },

    getLogicTestPointById(id: number) {
        return request.get<any, LogicTestPointResponseDTO>(`/v1/specifications/logic-points/${id}`)
    },

    getLogicTestPointsBySpecId(specId: number) {
        return request.get<any, LogicTestPointResponseDTO[]>(`/v1/specifications/${specId}/logic-points`)
    },

    updateLogicTestPoint(id: number, data: LogicTestPointRequestDTO) {
        return request.put<any, LogicTestPointResponseDTO>(`/v1/specifications/logic-points/${id}`, data)
    },

    deleteLogicTestPoint(id: number) {
        return request.delete<any, void>(`/v1/specifications/logic-points/${id}`)
    },

    updateLogicTestPointStatus(id: number, isActive: string) {
        return request.put<any, LogicTestPointResponseDTO>(`/v1/specifications/logic-points/${id}/status`, null, {
            params: { isActive }
        })
    }
}
