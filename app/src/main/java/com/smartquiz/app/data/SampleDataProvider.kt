package com.smartquiz.app.data

import com.smartquiz.app.data.entities.Question

object SampleDataProvider {

    fun getMathQuestions(): List<Question> {
        return listOf(
            Question(
                subject = "Toán học",
                difficulty = "easy",
                questionText = "2 + 3 = ?",
                optionA = "4",
                optionB = "5",
                optionC = "6",
                optionD = "7",
                correctAnswer = "B",
                explanation = "2 + 3 = 5"
            ),
            Question(
                subject = "Toán học",
                difficulty = "easy",
                questionText = "10 - 4 = ?",
                optionA = "5",
                optionB = "6",
                optionC = "7",
                optionD = "8",
                correctAnswer = "B",
                explanation = "10 - 4 = 6"
            ),
            Question(
                subject = "Toán học",
                difficulty = "medium",
                questionText = "Giải phương trình: 2x + 5 = 11",
                optionA = "x = 2",
                optionB = "x = 3",
                optionC = "x = 4",
                optionD = "x = 5",
                correctAnswer = "B",
                explanation = "2x + 5 = 11 => 2x = 6 => x = 3"
            ),
            Question(
                subject = "Toán học",
                difficulty = "medium",
                questionText = "Tính diện tích hình vuông có cạnh 5cm",
                optionA = "20 cm²",
                optionB = "25 cm²",
                optionC = "30 cm²",
                optionD = "35 cm²",
                correctAnswer = "B",
                explanation = "Diện tích hình vuông = cạnh × cạnh = 5 × 5 = 25 cm²"
            ),
            Question(
                subject = "Toán học",
                difficulty = "hard",
                questionText = "Tìm đạo hàm của hàm số f(x) = x² + 3x + 2",
                optionA = "f'(x) = 2x + 3",
                optionB = "f'(x) = x + 3",
                optionC = "f'(x) = 2x + 2",
                optionD = "f'(x) = x² + 3",
                correctAnswer = "A",
                explanation = "Đạo hàm của x² là 2x, đạo hàm của 3x là 3, đạo hàm của hằng số là 0"
            )
        )
    }

    fun getPhysicsQuestions(): List<Question> {
        return listOf(
            Question(
                subject = "Vật lý",
                difficulty = "easy",
                questionText = "Đơn vị đo vận tốc trong hệ SI là gì?",
                optionA = "km/h",
                optionB = "m/s",
                optionC = "cm/s",
                optionD = "mm/s",
                correctAnswer = "B",
                explanation = "Trong hệ SI, đơn vị đo vận tốc là mét trên giây (m/s)"
            ),
            Question(
                subject = "Vật lý",
                difficulty = "medium",
                questionText = "Công thức tính động năng là gì?",
                optionA = "Ek = mv",
                optionB = "Ek = ½mv",
                optionC = "Ek = ½mv²",
                optionD = "Ek = mv²",
                correctAnswer = "C",
                explanation = "Động năng được tính bằng công thức Ek = ½mv²"
            ),
            Question(
                subject = "Vật lý",
                difficulty = "hard",
                questionText = "Định luật bảo toàn năng lượng phát biểu như thế nào?",
                optionA = "Năng lượng không thể tạo ra hoặc tiêu hủy",
                optionB = "Năng lượng luôn tăng",
                optionC = "Năng lượng luôn giảm",
                optionD = "Năng lượng không đổi",
                correctAnswer = "A",
                explanation = "Định luật bảo toàn năng lượng: Năng lượng không thể tạo ra hoặc tiêu hủy, chỉ có thể chuyển đổi từ dạng này sang dạng khác"
            )
        )
    }

    fun getChemistryQuestions(): List<Question> {
        return listOf(
            Question(
                subject = "Hóa học",
                difficulty = "easy",
                questionText = "Ký hiệu hóa học của nước là gì?",
                optionA = "H2O",
                optionB = "HO2",
                optionC = "H2O2",
                optionD = "HO",
                correctAnswer = "A",
                explanation = "Nước có công thức hóa học là H2O"
            ),
            Question(
                subject = "Hóa học",
                difficulty = "medium",
                questionText = "Số proton trong nguyên tử carbon là bao nhiêu?",
                optionA = "5",
                optionB = "6",
                optionC = "7",
                optionD = "8",
                correctAnswer = "B",
                explanation = "Carbon có số hiệu nguyên tử là 6, tức là có 6 proton"
            )
        )
    }

    fun getBiologyQuestions(): List<Question> {
        return listOf(
            Question(
                subject = "Sinh học",
                difficulty = "easy",
                questionText = "Cơ quan nào trong cơ thể người có chức năng bơm máu?",
                optionA = "Phổi",
                optionB = "Tim",
                optionC = "Gan",
                optionD = "Thận",
                correctAnswer = "B",
                explanation = "Tim là cơ quan có chức năng bơm máu đi khắp cơ thể"
            ),
            Question(
                subject = "Sinh học",
                difficulty = "medium",
                questionText = "Quá trình quang hợp xảy ra ở đâu trong tế bào thực vật?",
                optionA = "Nhân tế bào",
                optionB = "Ty thể",
                optionC = "Lục lạp",
                optionD = "Màng tế bào",
                correctAnswer = "C",
                explanation = "Quá trình quang hợp xảy ra trong lục lạp của tế bào thực vật"
            )
        )
    }

    fun getAllSampleQuestions(): List<Question> {
        return getMathQuestions() + getPhysicsQuestions() + getChemistryQuestions() + getBiologyQuestions()
    }
}
