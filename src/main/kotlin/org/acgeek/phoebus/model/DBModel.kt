package org.acgeek.phoebus.model

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import java.io.Serializable as Seria
import org.acgeek.phoebus.service.LocalDateTimeSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 站点用户
 */
@Document("site_user")
@Serializable
data class UserDo(
        @SerialId(1) @Id var id: String? = null,
        @SerialId(2) @Indexed(unique = true) val uid: String,
        @SerialId(3) @Indexed(unique = true) var mail: String,
        @SerialId(4) val nick: String,
        @SerialId(5) @JsonIgnore var password: String,
        @SerialId(6) val avatar: String,
        @SerialId(7) val credit: Long,
        @SerialId(8) val desc: String,
        @SerialId(9) @Indexed val status: Int,
        @Serializable(LocalDateTimeSerializer::class)
        @SerialId(10) val create: LocalDateTime,
        @Serializable(LocalDateTimeSerializer::class)
        @SerialId(11) val active: LocalDateTime
): Seria

/**
 * 站点管理员
 */
@Document("site_admin")
data class AdminDo(
        @SerialId(1) @Id var id: String? = null,
        @Indexed(unique = true) val uid: String,
        val status: Int,
        val create: LocalDateTime,
        val update: LocalDateTime
)

/**
 * 用户Follow
 */
@Document("user_follow")
@CompoundIndexes(
        CompoundIndex(name = "user_follow", def = "{'uid': 1, 'fid': 1}", unique = true),
        CompoundIndex(name = "followed_by_user", def = "{'fid': 1, 'uid': 1}", unique = true)
)
data class UserFollowDo(
        @Indexed val uid: String,
        @Indexed val fid: String,
        val create: LocalDateTime,
        val update: LocalDateTime
)

/**
 * 作品属性
 *
 * @property bid 作品ID UUID
 * @property uid 上传者ID
 * @property author 作者名
 * @property title 作品名
 * @property origin 原作名
 * @property cp CP名称列表
 * @property tag 标签列表
 * @property area 地区
 * @property orient 性向
 * @property create 创建时间
 * @property update 修改时间
 * @property status 更新状态
 * @property cover 封面
 * @property desc 简介
 * @property warn 警告
 * @property dl 是否允许下载
 * @property copy 是否允许复制
 * @property comment 评论数量
 * @property good 点赞数量
 * @property read 观看数量
 * @property count 字数
 */
@Document("book_serials")
@CompoundIndexes(
        CompoundIndex(name = "user_books", def = "{'uid': 1, 'update': -1}", unique = true)
)
data class BookSerialsDo(
        @Indexed(unique = true) val bid: String,
        val uid: String,
        val author: String,
        val title: String,
        val origin: String,
        val cp: List<String>,
        val tag: List<String>,
        val area: Int,
        val orient: Int,
        val create: LocalDateTime,
        val update: LocalDateTime,
        val status: Int,
        val cover: String,
        val desc: String,
        val warn: String,
        val dl: String,
        val copy: Int,
        val comment: Int,
        val good: Int,
        val read: Int,
        val count: Long
)

/**
 * 书的章节
 */
@Document("chapter")
@CompoundIndexes(
        CompoundIndex(name = "chapter_order", def = "{'bid': 1, 'order': 1}", unique = true)
)
data class ChapterDo(
        @Indexed(unique = true) val cid: String,
        val bid: String,
        val order: Int,
        val author: String,
        val tag: List<String>,
        val create: LocalDateTime,
        @Indexed val lastUpdate: LocalDateTime,
        val desc: String,
        val comment: Int,
        val title: String,
        val content: String,
        val good: Int,
        val read: Int,
        val count: Long
)


/**
 * 书本的订阅关系
 */
@Document("bookmark")
@CompoundIndexes(
        CompoundIndex(name = "user_subscribe", def = "{'bid': 1, 'uid': 1}", unique = true),
        CompoundIndex(name = "book_subscribe", def = "{'uid': 1, 'bid': 1}", unique = true),
        CompoundIndex(name = "user_sub_desc", def = "{'uid': 1, 'update': -1}"),
        CompoundIndex(name = "book_sub_desc", def = "{'bid': 1, 'update': -1}")
)
data class BookMark(
        val bid: String,
        val uid: String,
        val process: String,
        val update: LocalDateTime
)

/**
 * 用户赞的书
 */
@Document("user_good")
@CompoundIndexes(
        CompoundIndex(name = "user_good", def = "{'uid': 1, 'bid': 1}", unique = true),
        CompoundIndex(name = "user_order", def = "{'uid': 1, 'create': -1}")
)
data class UserGoodDo(
        val uid: String,
        val bid: String,
        val create: LocalDateTime
)

/**
 * 用户的评论
 *
 */
@Document("comment")
@CompoundIndexes(
        CompoundIndex(name = "comment_base", def = "{'type': 1, 'source': 1, 'order': -1}")
)
data class CommentDo(
        @Indexed(unique = true) val cid: String,
        val type: Int,
        val source: String,
        @Indexed val reply: String,
        val create: String,
        val order: Int,
        val content: String
)

/**
 * 聊天内容
 *
 */
@Document
data class Chat(
        val from: String,
        val to: String,
        val create: LocalDateTime,
        val content: String,
        val readFlag: Int
)

/**
 * 举报
 *
 * @property type 举报对象的类型
 * @property from 举报人ID
 * @property title 标题
 * @property create 创建时间
 * @property reason 举报理由
 * @property link 链接
 *
 */
@Document
data class Report(
        val type: Int,
        val from: String,
        val title: String,
        val create: String,
        val reason: String,
        val link: String
)

/**
 * 回收站
 *
 * @property type 类型， 对应可回收的类型
 * @property content 回收站内容
 * @property create 创建时间
 */
@Document("recycle_bin")
data class RecycleBin(
        val type: String,
        val content: String,
        val create: LocalDateTime
)

/**
 * 用户黑名单
 */
@Document("block_list")
data class blockList(
        val uid: String,
        val to: String,
        val create: LocalDateTime
)


/**
 * 管理员操作日志
 *
 * @property action 动作ID，增、删、改
 * @property time 操作时间
 * @property type 操作对象的类型： 书目、章节、用户
 * @property desc 操作具体内容
 */
data class Log(
        val action: Int,
        val time: LocalDateTime,
        val type: Int,
        val desc: String
)