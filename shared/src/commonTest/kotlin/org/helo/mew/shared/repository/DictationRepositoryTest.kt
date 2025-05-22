package org.helo.mew.shared.repository

import org.helo.mew.shared.model.DictationRecord
import org.helo.mew.shared.model.MisspelledWordEntry
import kotlin.test.*
import kotlinx.coroutines.test.runTest

class DictationRepositoryTest {

    private val repository: DictationRepository = InMemoryDictationRepository()
    private val contentRepository: ContentRepository = InMemoryContentRepository() // For word text if needed

    private suspend fun getSampleWordText(wordId: String): String {
        return contentRepository.getWord(wordId)?.text ?: "Unknown Word"
    }

    @Test
    fun testAddAndGetDictationRecord() = runTest {
        val recordTime = System.currentTimeMillis()
        val record1 = DictationRecord(
            id = "rec1",
            lessonId = "en_l1",
            lessonName = "Fruit Vocabulary",
            correctWords = 1,
            totalWords = 2,
            timeTakenMillis = 60000,
            timestamp = recordTime,
            markedWords = mapOf("en_w1" to true, "en_w2" to false) // apple correct, banana incorrect
        )
        repository.addDictationRecord(record1)

        val retrievedRecord = repository.getDictationRecord("rec1")
        assertNotNull(retrievedRecord)
        assertEquals("rec1", retrievedRecord.id)
        assertEquals(1, retrievedRecord.correctWords)

        val allRecords = repository.getDictationRecords()
        assertTrue(allRecords.isNotEmpty())
        assertEquals(1, allRecords.size)
        assertEquals("rec1", allRecords[0].id)
    }

    @Test
    fun testGetDictationRecordsSortedByTimestamp() = runTest {
        val time1 = System.currentTimeMillis()
        val time2 = time1 + 1000

        val record1 = DictationRecord("rec_old", "l1", "les1", 0, 0, 0, time1, emptyMap())
        val record2 = DictationRecord("rec_new", "l2", "les2", 0, 0, 0, time2, emptyMap())

        repository.addDictationRecord(record1) // Add older first
        repository.addDictationRecord(record2) // Add newer second

        val records = repository.getDictationRecords()
        assertEquals(2, records.size)
        assertEquals("rec_new", records[0].id, "Newer record should be first")
        assertEquals("rec_old", records[1].id, "Older record should be second")
    }

    @Test
    fun testUpdateDictationRecord() = runTest {
        val recordTime = System.currentTimeMillis()
        val originalRecord = DictationRecord(
            id = "rec_update",
            lessonId = "en_l1",
            lessonName = "Fruit Vocabulary",
            correctWords = 0,
            totalWords = 1,
            timeTakenMillis = 30000,
            timestamp = recordTime,
            markedWords = mapOf("en_w1" to false)
        )
        repository.addDictationRecord(originalRecord)

        val updatedRecord = originalRecord.copy(correctWords = 1, markedWords = mapOf("en_w1" to true))
        repository.updateDictationRecord(updatedRecord)

        val retrievedRecord = repository.getDictationRecord("rec_update")
        assertNotNull(retrievedRecord)
        assertEquals(1, retrievedRecord.correctWords)
    }

    @Test
    fun testDeleteDictationRecord() = runTest {
        val record = DictationRecord("rec_delete", "l_del", "les_del", 0, 0, 0, System.currentTimeMillis(), emptyMap())
        repository.addDictationRecord(record)
        assertNotNull(repository.getDictationRecord("rec_delete"))

        repository.deleteDictationRecord("rec_delete")
        assertNull(repository.getDictationRecord("rec_delete"))
    }

    @Test
    fun testAddDictationRecordUpdatesMisspelledWords() = runTest {
        val recordTime = System.currentTimeMillis()
        val record = DictationRecord(
            id = "rec_misspell_test",
            lessonId = "en_l1",
            lessonName = "Fruit Vocabulary", // Used as placeholder for wordText in InMemoryDictationRepository
            correctWords = 1,
            totalWords = 2,
            timeTakenMillis = 60000,
            timestamp = recordTime,
            markedWords = mapOf(
                "en_w1" to true,  // apple - correct
                "en_w2" to false // banana - incorrect
            )
        )
        repository.addDictationRecord(record)

        val bananaEntry = repository.getMisspelledWordEntry("en_w2")
        assertNotNull(bananaEntry, "Banana should be in misspelled words")
        assertEquals(0, bananaEntry.correctStreak)
        assertEquals(1, bananaEntry.incorrectCount)
        assertTrue(bananaEntry.nextPracticeDueTimestamp > recordTime)

        val appleEntry = repository.getMisspelledWordEntry("en_w1")
        assertNotNull(appleEntry, "Apple should also have an entry due to practice")
        assertEquals(1, appleEntry.correctStreak)
        assertEquals(0, appleEntry.incorrectCount)
        assertTrue(appleEntry.nextPracticeDueTimestamp > recordTime)
    }

    @Test
    fun testGetMisspelledWordEntriesSortedByNextPracticeTime() = runTest {
        val now = System.currentTimeMillis()
        val entry1 = MisspelledWordEntry("word1", "Word One", now - 1000, now + 1000, 0, 1)
        val entry2 = MisspelledWordEntry("word2", "Word Two", now - 2000, now + 500, 0, 1) // Due sooner

        repository.addOrUpdateMisspelledWordEntry(entry1)
        repository.addOrUpdateMisspelledWordEntry(entry2)

        val entries = repository.getMisspelledWordEntries()
        assertEquals(2, entries.size)
        assertEquals("word2", entries[0].wordId, "Entry due sooner should be first")
        assertEquals("word1", entries[1].wordId)
    }

    @Test
    fun testGetDueMisspelledWords() = runTest {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val dueEntry = MisspelledWordEntry("due_word", "Due Word", now - dayInMillis, now - 1000, 0, 1) // Due yesterday
        val notDueEntry = MisspelledWordEntry("not_due_word", "Not Due Word", now - dayInMillis, now + dayInMillis, 1, 0) // Due tomorrow
        val alsoDueEntry = MisspelledWordEntry("also_due_word", "Also Due", now - dayInMillis, now - 500, 0, 2) // Due yesterday, but later than due_word


        repository.addOrUpdateMisspelledWordEntry(dueEntry)
        repository.addOrUpdateMisspelledWordEntry(notDueEntry)
        repository.addOrUpdateMisspelledWordEntry(alsoDueEntry)

        val dueWords = repository.getDueMisspelledWords(now)
        assertEquals(2, dueWords.size, "Should find two due words")
        assertTrue(dueWords.any { it.wordId == "due_word" })
        assertTrue(dueWords.any { it.wordId == "also_due_word" })
        assertFalse(dueWords.any { it.wordId == "not_due_word" })
        // Check sorting (earliest due first)
        assertEquals("due_word", dueWords[0].wordId)
        assertEquals("also_due_word", dueWords[1].wordId)
    }

    @Test
    fun testEbbinghausLogicForNextPracticeTime() = runTest {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        // Word just got wrong (streak 0)
        val recordWrong = DictationRecord("r_wrong", "l", "l", 0,1,1, now, mapOf("w_eb1" to false))
        repository.addDictationRecord(recordWrong)
        val entryWrong = repository.getMisspelledWordEntry("w_eb1")!!
        assertEquals(now + (dayInMillis / 24), entryWrong.nextPracticeDueTimestamp, "Practice in 1 hour for 0 streak")

        // Word correct once (streak 1)
        val recordCorrect1 = DictationRecord("r_corr1", "l", "l", 1,1,1, now, mapOf("w_eb2" to true))
        repository.addDictationRecord(recordCorrect1)
        val entryCorrect1 = repository.getMisspelledWordEntry("w_eb2")!!
        assertEquals(now + dayInMillis, entryCorrect1.nextPracticeDueTimestamp, "Practice in 1 day for 1 streak")

        // Simulate getting it correct again after 1 day
        val nextTime = entryCorrect1.nextPracticeDueTimestamp
        val recordCorrect2 = DictationRecord("r_corr2", "l", "l", 1,1,1, nextTime, mapOf("w_eb2" to true))
        repository.addDictationRecord(recordCorrect2) // This will update w_eb2 entry
        val entryCorrect2 = repository.getMisspelledWordEntry("w_eb2")!!
        assertEquals(2, entryCorrect2.correctStreak)
        assertEquals(nextTime + (2 * dayInMillis), entryCorrect2.nextPracticeDueTimestamp, "Practice in 2 days for 2 streak")
    }

    @Test
    fun testDeleteMisspelledWordEntry() = runTest {
        val entry = MisspelledWordEntry("del_mis", "Del Mis", System.currentTimeMillis(), System.currentTimeMillis() + 1000, 0, 1)
        repository.addOrUpdateMisspelledWordEntry(entry)
        assertNotNull(repository.getMisspelledWordEntry("del_mis"))

        repository.deleteMisspelledWordEntry("del_mis")
        assertNull(repository.getMisspelledWordEntry("del_mis"))
    }
}