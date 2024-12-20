package com.example.mymove.api

/**
 * 视频信息数据模型
 */
data class VodInfo(
    val vodId: Int = 0,
    val typeId: Int = 0,
    val typeName: String = "",
    val vodName: String = "",
    val vodSub: String = "",      // 副标题
    val vodEn: String = "",       // 英文名
    val vodTime: String = "",     // 更新时间
    val vodRemarks: String = "",  // 备注信息
    val vodPlayFrom: String = "", // 播放来源
    val vodPlayUrl: String = "",  // 播放地址
    val vodPic: String = "",      // 封面图
    val vodBlurb: String = "",    // 简介
    val vodArea: String = "",     // 地区
    val vodLang: String = "",     // 语言
    val vodYear: String = "",     // 年份
    val vodActor: String = "",    // 演员
    val vodDirector: String = "", // 导演
    val vodContent: String = "",  // 详细介绍
    val vodTag: String = ""       // 标签
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): VodInfo {
            return VodInfo(
                vodId = (json["vod_id"] as? Number)?.toInt() ?: 0,
                typeId = (json["type_id"] as? Number)?.toInt() ?: 0,
                typeName = json["type_name"] as? String ?: "",
                vodName = json["vod_name"] as? String ?: "",
                vodSub = json["vod_sub"] as? String ?: "",
                vodEn = json["vod_en"] as? String ?: "",
                vodTime = json["vod_time"] as? String ?: "",
                vodRemarks = json["vod_remarks"] as? String ?: "",
                vodPlayFrom = json["vod_play_from"] as? String ?: "",
                vodPlayUrl = json["vod_play_url"] as? String ?: "",
                vodPic = json["vod_pic"] as? String ?: "",
                vodBlurb = json["vod_blurb"] as? String ?: "",
                vodArea = json["vod_area"] as? String ?: "",
                vodLang = json["vod_lang"] as? String ?: "",
                vodYear = json["vod_year"] as? String ?: "",
                vodActor = json["vod_actor"] as? String ?: "",
                vodDirector = json["vod_director"] as? String ?: "",
                vodContent = json["vod_content"] as? String ?: "",
                vodTag = json["vod_tag"] as? String ?: ""
            )
        }
    }
}

/**
 * 分类信息数据模型
 */
data class TypeInfo(
    val typeId: Int = 0,
    val typeName: String = "",
    val parentId: Int = 0,    // 父分类ID
    val level: Int = 0        // 分类层级
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): TypeInfo {
            return TypeInfo(
                typeId = (json["type_id"] as? Number)?.toInt() ?: 0,
                typeName = json["type_name"] as? String ?: "",
                parentId = (json["parent_id"] as? Number)?.toInt() ?: 0,
                level = (json["level"] as? Number)?.toInt() ?: 0
            )
        }
    }
}

/**
 * API 统一返回结果模型
 */
data class ApiResult<T>(
    val code: Int = 0,            // 状态码
    val msg: String = "",         // 消息
    val page: Int = 1,           // 当前页码
    val pageCount: Int = 1,      // 总页数
    val limit: Int = 20,         // 每页条数
    val total: Int = 0,          // 总记录数
    val list: List<T> = emptyList(), // 数据列表
    val types: List<TypeInfo> = emptyList() // 分类列表
) {
    companion object {
        fun <T> fromJson(json: Map<String, Any?>, converter: (Map<String, Any?>) -> T): ApiResult<T> {
            val list = (json["list"] as? List<*>)?.mapNotNull { item ->
                (item as? Map<String, Any?>)?.let(converter)
            } ?: emptyList()
            
            val types = (json["type"] as? List<*>)?.mapNotNull { item ->
                (item as? Map<String, Any?>)?.let { TypeInfo.fromJson(it) }
            } ?: emptyList()
            
            return ApiResult(
                code = (json["code"] as? Number)?.toInt() ?: 0,
                msg = json["msg"] as? String ?: "",
                page = (json["page"] as? Number)?.toInt() ?: 1,
                pageCount = (json["pagecount"] as? Number)?.toInt() ?: 1,
                limit = (json["limit"] as? Number)?.toInt() ?: 20,
                total = (json["total"] as? Number)?.toInt() ?: 0,
                list = list,
                types = types
            )
        }
    }
}
