package com.smartquiz.app

import com.smartquiz.app.data.SampleDataProvider
import org.junit.Test
import org.junit.Assert.*

class SampleDataProviderTest {

    @Test
    fun testMathQuestions() {
        val mathQuestions = SampleDataProvider.getMathQuestions()
        
        assertFalse("Math questions should not be empty", mathQuestions.isEmpty())
        
        mathQuestions.forEach { question ->
            assertEquals("All questions should be Math", "Toán học", question.subject)
            assertFalse("Question text should not be empty", question.questionText.isEmpty())
            assertFalse("Option A should not be empty", question.optionA.isEmpty())
            assertFalse("Option B should not be empty", question.optionB.isEmpty())
            assertFalse("Option C should not be empty", question.optionC.isEmpty())
            assertFalse("Option D should not be empty", question.optionD.isEmpty())
            assertTrue("Correct answer should be A, B, C, or D", 
                question.correctAnswer in listOf("A", "B", "C", "D"))
            assertFalse("Explanation should not be empty", question.explanation.isEmpty())
        }
    }

    @Test
    fun testPhysicsQuestions() {
        val physicsQuestions = SampleDataProvider.getPhysicsQuestions()
        
        assertFalse("Physics questions should not be empty", physicsQuestions.isEmpty())
        
        physicsQuestions.forEach { question ->
            assertEquals("All questions should be Physics", "Vật lý", question.subject)
        }
    }

    @Test
    fun testAllSampleQuestions() {
        val allQuestions = SampleDataProvider.getAllSampleQuestions()
        
        assertFalse("All questions should not be empty", allQuestions.isEmpty())
        
        val subjects = allQuestions.map { it.subject }.distinct()
        assertTrue("Should have multiple subjects", subjects.size > 1)
        
        val difficulties = allQuestions.map { it.difficulty }.distinct()
        assertTrue("Should have multiple difficulties", difficulties.size > 1)
    }

    @Test
    fun testQuestionDifficulties() {
        val allQuestions = SampleDataProvider.getAllSampleQuestions()
        
        allQuestions.forEach { question ->
            assertTrue("Difficulty should be easy, medium, or hard",
                question.difficulty in listOf("easy", "medium", "hard"))
        }
    }
}
