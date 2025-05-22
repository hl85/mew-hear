package org.helo.mew.shared.repository

import org.helo.mew.shared.model.CustomWordList
import org.helo.mew.shared.model.Word
import org.helo.mew.shared.model.WordType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ContentRepositoryTest {

    private val repository: ContentRepository = InMemoryContentRepository()

    @Test
    fun testLoadInitialDataAndGetTextbookVersions() = runTest {
        val versions = repository.getTextbookVersions()
        assertTrue(versions.isNotEmpty(), "Textbook versions should be loaded")
        // Based on InMemoryContentRepository.loadInitialData
        assertEquals(2, versions.size, "Should load PEP English and 人教版语文")
        assertNotNull(versions.find { it.id == "tb_eng_pep" }, "PEP English should exist")
        assertNotNull(versions.find { it.id == "tb_chn_rj" }, "人教版语文 should exist")
    }

    @Test
    fun testGetTextbookVersionById() = runTest {
        val version = repository.getTextbookVersion("tb_eng_pep")
        assertNotNull(version, "PEP English should be found by ID")
        assertEquals("PEP English", version.name)
    }

    @Test
    fun testGetGrade() = runTest {
        val grade = repository.getGrade("tb_eng_pep", "en_g1")
        assertNotNull(grade, "English Grade 1 should be found")
        assertEquals("English Grade 1", grade.name)
    }

    @Test
    fun testGetUnit() = runTest {
        val unit = repository.getUnit("tb_chn_rj", "ch_g1", "ch_u1")
        assertNotNull(unit, "Chinese Unit 1 should be found")
        assertEquals("第一单元：基础", unit.name)
    }

    @Test
    fun testGetLesson() = runTest {
        val lesson = repository.getLesson("tb_eng_pep", "en_g1", "en_u1", "en_l1")
        assertNotNull(lesson, "English Lesson 1 (Fruit Vocabulary) should be found")
        assertEquals("Fruit Vocabulary", lesson.name)
        assertTrue(lesson.words.any { it.id == "en_w1" }, "Lesson should contain 'apple'")
    }

    @Test
    fun testGetWord() = runTest {
        val word = repository.getWord("en_w1") // apple
        assertNotNull(word, "Word 'apple' should be found by ID")
        assertEquals("apple", word.text)
        assertEquals(WordType.ENGLISH, word.type)

        val customWord = repository.getWord("en_w5") // lion (from custom list)
        assertNotNull(customWord, "Custom word 'lion' should be found by ID")
        assertEquals("lion", customWord.text)
    }

    @Test
    fun testGetCustomWordLists() = runTest {
        val lists = repository.getCustomWordLists()
        assertTrue(lists.isNotEmpty(), "Custom word lists should be loaded")
        // Based on InMemoryContentRepository.loadInitialData
        assertEquals(1, lists.size, "Should load 'My Favorite Animals'")
        assertNotNull(lists.find { it.id == "custom_1" }, "'My Favorite Animals' list should exist")
    }

    @Test
    fun testAddAndGetCustomWordList() = runTest {
        val newList = CustomWordList(
            id = "custom_2",
            name = "Test Custom List",
            words = listOf(Word("test_w1", "test", WordType.ENGLISH)),
            creationTimestamp = System.currentTimeMillis()
        )
        repository.addCustomWordList(newList)

        val retrievedList = repository.getCustomWordList("custom_2")
        assertNotNull(retrievedList, "Newly added custom list should be found")
        assertEquals("Test Custom List", retrievedList.name)
        assertEquals(1, retrievedList.words.size)

        val allLists = repository.getCustomWordLists()
        // Initial 1 + newly added 1
        assertTrue(allLists.size >= 2, "Total custom lists should include the new one")
    }

    @Test
    fun testUpdateCustomWordList() = runTest {
        val originalList = repository.getCustomWordList("custom_1")
        assertNotNull(originalList)

        val updatedList = originalList.copy(name = "My Favorite Animals Updated")
        repository.updateCustomWordList(updatedList)

        val retrievedList = repository.getCustomWordList("custom_1")
        assertNotNull(retrievedList)
        assertEquals("My Favorite Animals Updated", retrievedList.name)
    }

    @Test
    fun testDeleteCustomWordList() = runTest {
        val listToDelete = CustomWordList(
            id = "custom_to_delete",
            name = "To Delete",
            words = emptyList(),
            creationTimestamp = System.currentTimeMillis()
        )
        repository.addCustomWordList(listToDelete)
        assertNotNull(repository.getCustomWordList("custom_to_delete"))

        repository.deleteCustomWordList("custom_to_delete")
        val retrievedList = repository.getCustomWordList("custom_to_delete")
        assertEquals(null, retrievedList, "Deleted custom list should not be found")
    }
}