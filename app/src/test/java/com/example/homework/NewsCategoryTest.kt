package com.example.homework

import com.example.homework.model.NewsCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class NewsCategoryTest {

    @Test
    fun fromApiValue_returnsMatchingCategory() {
        assertEquals(NewsCategory.TECHNOLOGY, NewsCategory.fromApiValue("technology"))
        assertEquals(NewsCategory.SPORTS, NewsCategory.fromApiValue("sports"))
        assertEquals(NewsCategory.INTERNATIONAL, NewsCategory.fromApiValue("international"))
    }

    @Test
    fun fromApiValue_fallsBackToRecommendForUnknown() {
        assertEquals(NewsCategory.RECOMMEND, NewsCategory.fromApiValue("unknown"))
        assertEquals(NewsCategory.RECOMMEND, NewsCategory.fromApiValue(null))
    }

    @Test
    fun everyCategoryHasJuheType() {
        NewsCategory.entries.forEach { category ->
            assert(category.juheType.isNotBlank()) {
                "category ${category.name} missing juheType"
            }
        }
    }
}
