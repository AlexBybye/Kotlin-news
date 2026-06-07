package com.example.homework

import com.example.homework.data.mapper.JuheNewsMapper
import com.example.homework.data.remote.dto.JuheNewsItemDto
import com.example.homework.model.NewsCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JuheNewsMapperTest {

    private fun sampleItem() = JuheNewsItemDto(
        uniqueKey = "key-1",
        title = "测试新闻标题",
        date = "2026-06-07 10:00",
        category = "keji",
        authorName = "科技日报",
        url = "https://example.com/news/1",
        thumbnailPic = "https://example.com/pic.jpg"
    )

    @Test
    fun toArticleDto_mapsFieldsAndCategory() {
        val dto = JuheNewsMapper.toArticleDto(sampleItem(), NewsCategory.TECHNOLOGY)

        assertEquals("key-1", dto.id)
        assertEquals("测试新闻标题", dto.title)
        assertEquals("https://example.com/pic.jpg", dto.coverImageUrl)
        assertEquals(NewsCategory.TECHNOLOGY.apiValue, dto.category)
        assertEquals("https://example.com/news/1", dto.contentUrl)
    }

    @Test
    fun toDetailDto_buildsNonEmptyContentAndKeepsUrl() {
        val detail = JuheNewsMapper.toDetailDto(sampleItem(), NewsCategory.TECHNOLOGY)

        assertEquals("key-1", detail.id)
        assertTrue(detail.content!!.isNotEmpty())
        assertEquals("https://example.com/news/1", detail.contentUrl)
    }

    @Test
    fun toArticleDto_fallsBackToUrlWhenNoUniqueKey() {
        val item = sampleItem().copy(uniqueKey = null)
        val dto = JuheNewsMapper.toArticleDto(item, NewsCategory.RECOMMEND)
        assertEquals("https://example.com/news/1", dto.id)
    }
}
