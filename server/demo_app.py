"""
Smart Quiz App - Demo Server
Simplified version for demonstration without database dependencies
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import json
import random
import time
from datetime import datetime

app = Flask(__name__)
CORS(app)

# Sample data for demonstration
SAMPLE_QUESTIONS = {
    "math": {
        "easy": [
            {
                "id": "math_easy_1",
                "question_text": "Tính 2 + 3 = ?",
                "options": ["4", "5", "6", "7"],
                "correct_answer_index": 1,
                "explanation": "2 + 3 = 5. Đây là phép cộng cơ bản.",
                "hints": ["Đếm trên tay", "2 + 3 nghĩa là bắt đầu từ 2 và đếm thêm 3 số"],
                "tags": ["cộng", "số_tự_nhiên"]
            },
            {
                "id": "math_easy_2", 
                "question_text": "Trong các số sau, số nào là số chẵn?",
                "options": ["3", "7", "8", "9"],
                "correct_answer_index": 2,
                "explanation": "8 là số chẵn vì chia hết cho 2.",
                "hints": ["Số chẵn chia hết cho 2", "Số chẵn có tận cùng là 0, 2, 4, 6, 8"],
                "tags": ["số_chẵn", "chia_hết"]
            }
        ],
        "medium": [
            {
                "id": "math_medium_1",
                "question_text": "Giải phương trình: 2x + 5 = 13",
                "options": ["x = 3", "x = 4", "x = 5", "x = 6"],
                "correct_answer_index": 1,
                "explanation": "2x + 5 = 13 → 2x = 8 → x = 4",
                "hints": ["Chuyển 5 sang vế phải", "Chia cả hai vế cho 2"],
                "tags": ["phương_trình", "đại_số"]
            }
        ],
        "hard": [
            {
                "id": "math_hard_1",
                "question_text": "Tính đạo hàm của f(x) = x³ + 2x² - 5x + 1",
                "options": ["3x² + 4x - 5", "x² + 4x - 5", "3x² + 2x - 5", "3x² + 4x + 5"],
                "correct_answer_index": 0,
                "explanation": "f'(x) = 3x² + 4x - 5 theo quy tắc đạo hàm cơ bản.",
                "hints": ["Đạo hàm của x^n là n*x^(n-1)", "Đạo hàm của hằng số là 0"],
                "tags": ["đạo_hàm", "giải_tích"]
            }
        ]
    },
    "physics": {
        "easy": [
            {
                "id": "physics_easy_1",
                "question_text": "Vận tốc ánh sáng trong chân không là bao nhiêu?",
                "options": ["3×10⁸ m/s", "3×10⁶ m/s", "3×10⁷ m/s", "3×10⁹ m/s"],
                "correct_answer_index": 0,
                "explanation": "Vận tốc ánh sáng trong chân không là c = 3×10⁸ m/s.",
                "hints": ["Đây là hằng số vật lý cơ bản", "Ký hiệu là c"],
                "tags": ["ánh_sáng", "hằng_số"]
            }
        ]
    }
}

SUBJECTS = [
    {"id": "math", "name": "Toán học", "icon": "🔢"},
    {"id": "physics", "name": "Vật lý", "icon": "⚛️"},
    {"id": "chemistry", "name": "Hóa học", "icon": "🧪"},
    {"id": "biology", "name": "Sinh học", "icon": "🧬"}
]

@app.route('/api/v1/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'timestamp': datetime.utcnow().isoformat(),
        'version': '1.0.0-demo',
        'services': {
            'api': 'healthy',
            'ai_simulation': 'healthy'
        }
    })

@app.route('/api/v1/subjects', methods=['GET'])
def get_subjects():
    """Get available subjects"""
    return jsonify({'subjects': SUBJECTS})

@app.route('/api/v1/questions/generate', methods=['POST'])
def generate_questions():
    """Generate questions (simulated AI)"""
    try:
        data = request.get_json()
        subject = data.get('subject', 'math')
        difficulty = data.get('difficulty', 'easy')
        count = min(data.get('count', 5), 10)  # Max 10 questions
        
        # Simulate AI processing time
        time.sleep(1)
        
        # Get questions from sample data
        subject_questions = SAMPLE_QUESTIONS.get(subject, {})
        difficulty_questions = subject_questions.get(difficulty, [])
        
        if not difficulty_questions:
            return jsonify({'error': f'No questions available for {subject}/{difficulty}'}), 404
        
        # Select random questions
        selected_questions = random.sample(
            difficulty_questions, 
            min(count, len(difficulty_questions))
        )
        
        # Add generation metadata
        response = {
            'questions': selected_questions,
            'generated_at': datetime.utcnow().isoformat(),
            'cached': False,
            'metadata': {
                'subject': subject,
                'difficulty': difficulty,
                'count': len(selected_questions),
                'ai_model': 'demo-simulation',
                'generation_time': '1.0s'
            }
        }
        
        return jsonify(response)
        
    except Exception as e:
        return jsonify({'error': f'Question generation failed: {str(e)}'}), 500

@app.route('/api/v1/feedback/generate', methods=['POST'])
def generate_feedback():
    """Generate AI feedback (simulated)"""
    try:
        data = request.get_json()
        quiz_data = data.get('quiz_data', {})
        answers = data.get('answers', [])
        
        if not answers:
            return jsonify({'error': 'No answers provided'}), 400
        
        # Simulate AI processing
        time.sleep(0.5)
        
        # Calculate basic stats
        correct_count = sum(1 for answer in answers if answer.get('is_correct', False))
        total_questions = len(answers)
        accuracy = (correct_count / total_questions) * 100 if total_questions > 0 else 0
        
        # Generate simulated feedback based on performance
        if accuracy >= 80:
            overall = "Xuất sắc! Bạn đã thể hiện sự hiểu biết vững chắc về chủ đề này."
            performance_level = "excellent"
            strengths = [
                "Nắm vững kiến thức cơ bản",
                "Tư duy logic tốt",
                "Khả năng áp dụng công thức chính xác"
            ]
            weaknesses = ["Có thể thử thách bản thân với độ khó cao hơn"]
            next_difficulty = "hard" if quiz_data.get('difficulty') != 'hard' else 'hard'
        elif accuracy >= 60:
            overall = "Tốt! Bạn đã nắm được phần lớn kiến thức, cần cải thiện một số điểm."
            performance_level = "good"
            strengths = [
                "Hiểu được các khái niệm cơ bản",
                "Có khả năng giải quyết vấn đề"
            ]
            weaknesses = [
                "Cần ôn tập thêm một số chủ đề",
                "Chú ý đọc kỹ đề bài"
            ]
            next_difficulty = quiz_data.get('difficulty', 'medium')
        else:
            overall = "Cần cố gắng thêm! Hãy ôn tập kỹ lại kiến thức cơ bản."
            performance_level = "needs_improvement"
            strengths = ["Có tinh thần học hỏi"]
            weaknesses = [
                "Cần nắm vững kiến thức cơ bản",
                "Luyện tập thêm các bài tập",
                "Tìm hiểu thêm về lý thuyết"
            ]
            next_difficulty = "easy"
        
        feedback = {
            'overall_assessment': overall,
            'performance_level': performance_level,
            'strengths': strengths,
            'weaknesses': weaknesses,
            'recommendations': [
                f"Ôn tập thêm về {quiz_data.get('subject', 'môn học này')}",
                "Làm thêm bài tập tương tự",
                "Tìm hiểu sâu hơn về các khái niệm chưa rõ",
                "Thực hành đều đặn mỗi ngày",
                "Tham khảo thêm tài liệu học tập"
            ],
            'next_difficulty': next_difficulty,
            'study_time_minutes': max(30, 60 - int(accuracy/2)),
            'focus_areas': [quiz_data.get('subject', 'toán học')],
            'confidence_score': 0.85,
            'motivational_message': "Hãy tiếp tục cố gắng! Mỗi bài quiz là một bước tiến trong hành trình học tập của bạn."
        }
        
        return jsonify({
            'feedback': feedback,
            'generated_at': datetime.utcnow().isoformat()
        })
        
    except Exception as e:
        return jsonify({'error': f'Feedback generation failed: {str(e)}'}), 500

@app.route('/api/v1/auth/demo-login', methods=['POST'])
def demo_login():
    """Demo login endpoint"""
    data = request.get_json()
    username = data.get('username', 'demo_user')
    
    # Simulate user data
    user_data = {
        'id': 'demo_user_123',
        'username': username,
        'email': f'{username}@demo.com',
        'display_name': f'Demo User ({username})',
        'level': 5,
        'total_xp': 1250,
        'current_streak': 7,
        'created_at': datetime.utcnow().isoformat()
    }
    
    return jsonify({
        'message': 'Demo login successful',
        'user': user_data,
        'access_token': 'demo_token_12345'
    })

@app.route('/api/v1/analytics/demo-stats', methods=['GET'])
def get_demo_stats():
    """Get demo user statistics"""
    stats = {
        'total_quizzes': 25,
        'total_questions': 250,
        'total_correct': 180,
        'average_accuracy': 72.0,
        'total_time_spent': 7200000,  # milliseconds
        'favorite_subject': 'math',
        'weekly_progress': [
            {'date': '2024-01-01', 'questions': 15, 'correct': 12},
            {'date': '2024-01-02', 'questions': 20, 'correct': 16},
            {'date': '2024-01-03', 'questions': 18, 'correct': 14},
            {'date': '2024-01-04', 'questions': 22, 'correct': 18},
            {'date': '2024-01-05', 'questions': 25, 'correct': 20},
            {'date': '2024-01-06', 'questions': 30, 'correct': 24},
            {'date': '2024-01-07', 'questions': 28, 'correct': 22}
        ]
    }
    
    return jsonify({'stats': stats})

@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Endpoint not found'}), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({'error': 'Internal server error'}), 500

if __name__ == '__main__':
    print("🚀 Starting Smart Quiz Demo Server...")
    print("📊 Available endpoints:")
    print("   GET  /api/v1/health - Health check")
    print("   GET  /api/v1/subjects - Get subjects")
    print("   POST /api/v1/questions/generate - Generate questions")
    print("   POST /api/v1/feedback/generate - Generate feedback")
    print("   POST /api/v1/auth/demo-login - Demo login")
    print("   GET  /api/v1/analytics/demo-stats - Demo statistics")
    print("\n🌐 Server running at: http://localhost:5000")
    print("📱 Ready for Android app connection!")
    
    app.run(host='0.0.0.0', port=5000, debug=True)