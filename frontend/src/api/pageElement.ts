import request from './request'
import type { PageResult } from './testReport'

export interface PageElementInfoRequestDTO {
    pageUrl: string
    elementType?: string
    elementLocatorType?: string
    elementLocatorValue?: string
    elementText?: string
    elementAttributes?: string
    pageStructure?: string
    screenshotUrl?: string
}

export interface PageElementInfoResponseDTO {
    id: number
    elementCode: string
    pageUrl: string
    elementType?: string
    elementLocatorType?: string
    elementLocatorValue?: string
    elementText?: string
    elementAttributes?: string
    pageStructure?: string
    screenshotUrl?: string
    createTime?: string
    updateTime?: string
}

export const pageElementApi = {
    // Create page element
    createPageElement(data: PageElementInfoRequestDTO) {
        return request.post<any, PageElementInfoResponseDTO>('/v1/page-elements', data)
    },

    // Get page element list
    getPageElementList(page: number = 0, size: number = 10, pageUrl?: string, elementType?: string) {
        return request.get<any, PageResult<PageElementInfoResponseDTO>>('/v1/page-elements', {
            params: { page, size, pageUrl, elementType }
        })
    },

    // Get page element by ID
    getPageElementById(id: number) {
        return request.get<any, PageElementInfoResponseDTO>(`/v1/page-elements/${id}`)
    },

    // Get page element by code
    getPageElementByCode(elementCode: string) {
        return request.get<any, PageElementInfoResponseDTO>(`/v1/page-elements/code/${elementCode}`)
    },

    // Get page elements by URL
    getPageElementsByUrl(pageUrl: string) {
        return request.get<any, PageElementInfoResponseDTO[]>('/v1/page-elements/by-url', {
            params: { pageUrl }
        })
    },

    // Update page element
    updatePageElement(id: number, data: PageElementInfoRequestDTO) {
        return request.put<any, PageElementInfoResponseDTO>(`/v1/page-elements/${id}`, data)
    },

    // Delete page element
    deletePageElement(id: number) {
        return request.delete<any, void>(`/v1/page-elements/${id}`)
    }
}
