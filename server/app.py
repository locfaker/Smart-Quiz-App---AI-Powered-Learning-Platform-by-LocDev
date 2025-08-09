"""
Smart Quiz App - Production-Level Backend Server
Enterprise-grade Flask application with AI integration
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from werkzeug.security import generate_password_hash, check_password_hash
import os
import openai
import google.generativeai as genai
from datetime import datetime, timedelta
import logging
import redis
import json
import uuid
from typing import List, Dict, Any, Optional
import re
from functools import wraps

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app, origins=["http://localhost:3000", "https://smartquiz.app"])

# Configuration
app.config.update({
    'SECRET_KEY': os.environ.get('SECRET_KEY', 'dev-secret-key-change-in-production'),
    'SQLALCHEMY_DATABASE_URI': os.environ.get('DATABASE_URL', 'postgresql://localhost/smartquiz_db'),
    'SQLALCHEMY_TRACK_MODIFICATIONS': False,
    'JWT_SECRET_KEY': os.environ.get('JWT_SECRET_KEY', 'jwt-secret-change-in-production'),
    'JWT_ACCESS_TOKEN_EXPIRES': timedelta(hours=24),
    'MAX_CONTENT_LENGTH': 16 * 1024 * 1024  # 16MB max file size
})

# Initialize extensions
db = SQLAlchemy(app)
migrate = Migrate(app, db)
jwt = JWTManager(app)

# Redis for caching and rate limiting
try:
    redis_client = redis.Redis(
        host=os.environ.get('REDIS_HOST', 'localhost'),
        port=int(os.environ.get('REDIS_PORT', 6379)),
        db=0,
        decode_responses=True,
        socket_connect_timeout=5,
        socket_timeout=5
    )
    redis_client.ping()
    logger.info("Redis connected successfully")
except Exception as e:
    redis_client = None
    logger.warning(f"Redis not available: {e}")

# AI Configuration
openai.api_key = os.environ.get('OPENAI_API_KEY')
if os.environ.get('GEMINI_API_KEY'):
    genai.configure(api_key=os.environ.get('GEMINI_API_KEY'))

# Database Models
class User(db.Model):
    __tablename__ = 'users'
    
    id = db.Column(db.String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    username = db.Column(db.String(80), unique=True, nullable=False, index=True)
    email = db.Column(db.String(120), unique=True, nullable=False, index=True)
    password_hash = db.Column(db.String(255), nullable=False)
    display_name = db.Column(db.String(100), nullable=False)
    avatar_url = db.Column(db.String(255))
    level = db.Column(db.Integer, default=1)
    total_xp = db.Column(db.Integer, default=0)
    current_streak = db.Column(db.Integer, default=0)
    longest_streak = db.Column(db.Integer, default=0)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    last_active_at = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    is_active = db.Column(db.Boolean, default=True)
    
    # Relationships
    quizzes = db.relationship('Quiz', backref='user', lazy='dynamic', cascade='all, delete-orphan')

class Quiz(db.Model):
    __tablename__ = 'quizzes'
    
    id = db.Column(db.String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False, index=True)
    subject = db.Column(db.String(50), nullable=False, index=True)
    difficulty = db.Column(db.String(20), nullable=False, index=True)
    total_questions = db.Column(db.Integer, nullable=False)
    correct_answers = db.Column(db.Integer, default=0)
    score = db.Column(db.Float, default=0.0)
    percentage = db.Column(db.Float, default=0.0)
    time_spent = db.Column(db.Integer, default=0)  # milliseconds
    time_limit = db.Column(db.Integer, default=900)  # seconds
    ai_feedback = db.Column(db.Text)
    suggestions = db.Column(db.JSON)
    started_at = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    completed_at = db.Column(db.DateTime, index=True)
    is_completed = db.Column(db.Boolean, default=False, index=True)
    metadata = db.Column(db.JSON)

class Question(db.Model):
    __tablename__ = 'questions'
    
    id = db.Column(db.String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    subject = db.Column(db.String(50), nullable=False, index=True)
    difficulty = db.Column(db.String(20), nullable=False, index=True)
    question_type = db.Column(db.String(30), default='multiple_choice')
    question_text = db.Column(db.Text, nullable=False)
    options = db.Column(db.JSON, nullable=False)  # List of options
    correct_answer_index = db.Column(db.Integer, nullable=False)
    explanation = db.Column(db.Text)
    hints = db.Column(db.JSON, default=list)  # List of hints
    tags = db.Column(db.JSON, default=list)  # List of tags
    points = db.Column(db.Integer, default=1)
    time_limit = db.Column(db.Integer)  # seconds
    created_at = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    created_by = db.Column(db.String(20), default='ai')  # 'ai' or 'human'
    source = db.Column(db.String(50), default='ai_generated')
    is_active = db.Column(db.Boolean, default=True, index=True)
    usage_count = db.Column(db.Integer, default=0)
    success_rate = db.Column(db.Float, default=0.0)

# Utility Functions
def cache_key(prefix: str, *args) -> str:
    """Generate standardized cache key"""
    return f"smartquiz:{prefix}:{':'.join(map(str, args))}"

def get_from_cache(key: str) -> Optional[Any]:
    """Safely get value from Redis cache"""
    if not redis_client:
        return None
    try:
        value = redis_client.get(key)
        return json.loads(value) if value else None
    except Exception as e:
        logger.warning(f"Cache get failed for key {key}: {e}")
        return None

def set_cache(key: str, value: Any, ttl: int = 3600) -> None:
    """Safely set value in Redis cache"""
    if not redis_client:
        return
    try:
        redis_client.setex(key, ttl, json.dumps(value, default=str))
    except Exception as e:
        logger.warning(f"Cache set failed for key {key}: {e}")

def rate_limit(max_requests: int = 100, window: int = 3600):
    """Advanced rate limiting decorator with Redis"""
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            if not redis_client:
                return f(*args, **kwargs)
            
            client_ip = request.headers.get('X-Forwarded-For', request.remote_addr)
            key = f"rate_limit:{client_ip}:{f.__name__}"
            
            try:
                pipe = redis_client.pipeline()
                pipe.incr(key)
                pipe.expire(key, window)
                results = pipe.execute()
                
                if results[0] > max_requests:
                    return jsonify({
                        'error': 'Rate limit exceeded',
                        'retry_after': window
                    }), 429
                    
            except Exception as e:
                logger.warning(f"Rate limiting failed: {e}")
            
            return f(*args, **kwargs)
        return decorated_function
    return decorator

def validate_request_data(required_fields: List[str]):
    """Decorator to validate required fields in request JSON"""
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            data = request.get_json()
            if not data:
                return jsonify({'error': 'JSON data required'}), 400
            
            missing_fields = [field for field in required_fields if not data.get(field)]
            if missing_fields:
                return jsonify({
                    'error': f'Missing required fields: {", ".join(missing_fields)}'
                }), 400
            
            return f(*args, **kwargs)
        return decorated_function
    return decorator

# AI Service Classes
class AIQuestionGenerator:
    """Advanced AI question generation with multiple providers"""
    
    @staticmethod
    def generate_with_openai(subject: str, difficulty: str, count: int, 
                           topics: List[str] = None, user_level: int = 1) -> List[Dict]:
        """Generate questions using OpenAI GPT-4"""
        if not openai.api_key:
            raise ValueError("OpenAI API key not configured")
        
        topics_context = f" focusing on {', '.join(topics)}" if topics else ""
        difficulty_mapping = {
            'easy': 'cơ bản, phù hợp cho người mới bắt đầu',
            'medium': 'trung bình, yêu cầu hiểu biết vững chắc',
            'hard': 'nâng cao, thử thách tư duy phản biện'
        }
        
        prompt = f"""
        Tạo {count} câu hỏi trắc nghiệm chất lượng cao cho môn {subject} ở mức độ {difficulty_mapping.get(difficulty, difficulty)}{topics_context}.
        
        Yêu cầu chuyên nghiệp:
        - Mỗi câu hỏi có đúng 4 lựa chọn (A, B, C, D)
        - Đáp án phải chính xác 100% và có cơ sở khoa học
        - Giải thích chi tiết, dễ hiểu cho đáp án đúng
        - 2-3 gợi ý thông minh giúp học sinh tư duy
        - Câu hỏi phải có tính ứng dụng thực tế
        - Sử dụng tiếng Việt chuẩn, thuật ngữ chính xác
        - Tránh câu hỏi mơ hồ hoặc có nhiều đáp án đúng
        
        Trả về JSON array chính xác:
        [
            {{
                "question_text": "Câu hỏi rõ ràng và cụ thể",
                "options": ["Lựa chọn A", "Lựa chọn B", "Lựa chọn C", "Lựa chọn D"],
                "correct_answer_index": 0,
                "explanation": "Giải thích chi tiết với lý do khoa học",
                "hints": ["Gợi ý 1 hướng dẫn tư duy", "Gợi ý 2 liên kết kiến thức"],
                "tags": ["chủ_đề_chính", "kỹ_năng_cần_thiết"],
                "difficulty_score": 0.7
            }}
        ]
        """
        
        try:
            response = openai.ChatCompletion.create(
                model="gpt-4" if "gpt-4" in str(openai.Model.list()) else "gpt-3.5-turbo",
                messages=[
                    {
                        "role": "system", 
                        "content": "Bạn là chuyên gia giáo dục với 20 năm kinh nghiệm, chuyên tạo câu hỏi chất lượng cao cho học sinh Việt Nam."
                    },
                    {"role": "user", "content": prompt}
                ],
                temperature=0.7,
                max_tokens=3000,
                presence_penalty=0.1,
                frequency_penalty=0.1
            )
            
            content = response.choices[0].message.content.strip()
            
            # Extract and validate JSON
            json_match = re.search(r'\[.*\]', content, re.DOTALL)
            if not json_match:
                raise ValueError("No valid JSON array found in response")
            
            questions = json.loads(json_match.group())
            
            # Validate question structure
            for i, q in enumerate(questions):
                required_fields = ['question_text', 'options', 'correct_answer_index', 'explanation']
                for field in required_fields:
                    if field not in q:
                        raise ValueError(f"Question {i+1} missing required field: {field}")
                
                if len(q['options']) != 4:
                    raise ValueError(f"Question {i+1} must have exactly 4 options")
                
                if not (0 <= q['correct_answer_index'] <= 3):
                    raise ValueError(f"Question {i+1} has invalid correct_answer_index")
            
            return questions
            
        except json.JSONDecodeError as e:
            logger.error(f"JSON parsing failed: {e}")
            raise ValueError("Invalid JSON response from AI")
        except Exception as e:
            logger.error(f"OpenAI question generation failed: {e}")
            raise

    @staticmethod
    def generate_feedback(quiz_data: Dict, answers: List[Dict], user_profile: Dict = None) -> Dict:
        """Generate comprehensive AI feedback"""
        if not openai.api_key:
            raise ValueError("OpenAI API key not configured")
        
        correct_count = sum(1 for answer in answers if answer.get('is_correct', False))
        total_questions = len(answers)
        accuracy = (correct_count / total_questions) * 100 if total_questions > 0 else 0
        
        # Analyze answer patterns
        wrong_answers = [a for a in answers if not a.get('is_correct', False)]
        avg_time = sum(a.get('time_spent', 0) for a in answers) / len(answers) if answers else 0
        
        user_context = ""
        if user_profile:
            user_context = f"""
            Thông tin học sinh:
            - Cấp độ: {user_profile.get('level', 1)}
            - Tổng XP: {user_profile.get('total_xp', 0)}
            - Chuỗi ngày học: {user_profile.get('current_streak', 0)}
            """
        
        prompt = f"""
        Phân tích chi tiết kết quả bài quiz và đưa ra phản hồi cá nhân hóa:
        
        {user_context}
        
        Kết quả bài quiz:
        - Môn học: {quiz_data.get('subject')}
        - Độ khó: {quiz_data.get('difficulty')}
        - Điểm số: {correct_count}/{total_questions} ({accuracy:.1f}%)
        - Thời gian trung bình/câu: {avg_time/1000:.1f} giây
        - Số câu sai: {len(wrong_answers)}
        
        Chi tiết từng câu trả lời:
        {json.dumps(answers, indent=2, ensure_ascii=False)}
        
        Yêu cầu phân tích chuyên nghiệp:
        1. Đánh giá tổng quan về năng lực hiện tại
        2. Xác định 3 điểm mạnh cụ thể
        3. Phân tích 3 điểm cần cải thiện với lý do
        4. Đưa ra 5 khuyến nghị học tập cụ thể và khả thi
        5. Gợi ý độ khó phù hợp cho lần học tiếp theo
        6. Ước tính thời gian ôn tập cần thiết
        7. Đánh giá mức độ tin cậy của phân tích
        
        Trả về JSON:
        {{
            "overall_assessment": "Đánh giá tổng quan chi tiết",
            "performance_level": "excellent|good|average|needs_improvement",
            "strengths": ["điểm mạnh 1", "điểm mạnh 2", "điểm mạnh 3"],
            "weaknesses": ["điểm yếu 1", "điểm yếu 2", "điểm yếu 3"],
            "recommendations": [
                "khuyến nghị cụ thể 1",
                "khuyến nghị cụ thể 2", 
                "khuyến nghị cụ thể 3",
                "khuyến nghị cụ thể 4",
                "khuyến nghị cụ thể 5"
            ],
            "next_difficulty": "easy|medium|hard",
            "study_time_minutes": 45,
            "focus_areas": ["chủ đề cần tập trung 1", "chủ đề cần tập trung 2"],
            "confidence_score": 0.92,
            "motivational_message": "Lời động viên tích cực"
        }}
        """
        
        try:
            response = openai.ChatCompletion.create(
                model="gpt-4" if "gpt-4" in str(openai.Model.list()) else "gpt-3.5-turbo",
                messages=[
                    {
                        "role": "system",
                        "content": "Bạn là chuyên gia tâm lý giáo dục và phân tích học tập, có khả năng đưa ra phản hồi cá nhân hóa giúp học sinh cải thiện hiệu quả học tập."
                    },
                    {"role": "user", "content": prompt}
                ],
                temperature=0.6,
                max_tokens=2000
            )
            
            content = response.choices[0].message.content.strip()
            json_match = re.search(r'\{.*\}', content, re.DOTALL)
            
            if not json_match:
                raise ValueError("No valid JSON found in feedback response")
            
            feedback = json.loads(json_match.group())
            
            # Validate feedback structure
            required_fields = ['overall_assessment', 'strengths', 'weaknesses', 'recommendations']
            for field in required_fields:
                if field not in feedback:
                    logger.warning(f"Feedback missing field: {field}")
            
            return feedback
            
        except Exception as e:
            logger.error(f"AI feedback generation failed: {e}")
            raise

# API Routes
@app.route('/api/v1/health', methods=['GET'])
def health_check():
    """Comprehensive health check"""
    services = {}
    
    # Database check
    try:
        db.session.execute('SELECT 1')
        services['database'] = {'status': 'healthy', 'response_time': 0}
    except Exception as e:
        services['database'] = {'status': 'unhealthy', 'error': str(e)}
    
    # Redis check
    if redis_client:
        try:
            start_time = time.time()
            redis_client.ping()
            response_time = (time.time() - start_time) * 1000
            services['redis'] = {'status': 'healthy', 'response_time': f'{response_time:.2f}ms'}
        except Exception as e:
            services['redis'] = {'status': 'unhealthy', 'error': str(e)}
    else:
        services['redis'] = {'status': 'not_configured'}
    
    # AI services check
    services['openai'] = {
        'status': 'configured' if openai.api_key else 'not_configured'
    }
    services['gemini'] = {
        'status': 'configured' if os.environ.get('GEMINI_API_KEY') else 'not_configured'
    }
    
    overall_status = 'healthy' if all(
        s.get('status') in ['healthy', 'configured'] for s in services.values()
    ) else 'degraded'
    
    return jsonify({
        'status': overall_status,
        'timestamp': datetime.utcnow().isoformat(),
        'version': '2.0.0',
        'environment': os.environ.get('FLASK_ENV', 'production'),
        'services': services
    })

@app.route('/api/v1/auth/register', methods=['POST'])
@rate_limit(max_requests=10, window=3600)
@validate_request_data(['username', 'email', 'password', 'display_name'])
def register():
    """Enhanced user registration with validation"""
    try:
        data = request.get_json()
        
        # Enhanced validation
        username = data['username'].strip().lower()
        email = data['email'].strip().lower()
        password = data['password']
        display_name = data['display_name'].strip()
        
        # Validation rules
        if len(username) < 3 or len(username) > 30:
            return jsonify({'error': 'Username must be 3-30 characters'}), 400
        
        if not re.match(r'^[a-zA-Z0-9_]+$', username):
            return jsonify({'error': 'Username can only contain letters, numbers, and underscores'}), 400
        
        if not re.match(r'^[^\s@]+@[^\s@]+\.[^\s@]+$', email):
            return jsonify({'error': 'Invalid email format'}), 400
        
        if len(password) < 8:
            return jsonify({'error': 'Password must be at least 8 characters'}), 400
        
        if len(display_name) < 2 or len(display_name) > 50:
            return jsonify({'error': 'Display name must be 2-50 characters'}), 400
        
        # Check existing users
        if User.query.filter_by(username=username).first():
            return jsonify({'error': 'Username already exists'}), 409
        
        if User.query.filter_by(email=email).first():
            return jsonify({'error': 'Email already registered'}), 409
        
        # Create user
        user = User(
            username=username,
            email=email,
            password_hash=generate_password_hash(password),
            display_name=display_name
        )
        
        db.session.add(user)
        db.session.commit()
        
        # Generate JWT token
        access_token = create_access_token(
            identity=user.id,
            additional_claims={'username': user.username}
        )
        
        logger.info(f"New user registered: {username}")
        
        return jsonify({
            'message': 'Registration successful',
            'user': {
                'id': user.id,
                'username': user.username,
                'email': user.email,
                'display_name': user.display_name,
                'level': user.level,
                'total_xp': user.total_xp,
                'created_at': user.created_at.isoformat()
            },
            'access_token': access_token
        }), 201
        
    except Exception as e:
        db.session.rollback()
        logger.error(f"Registration failed: {e}")
        return jsonify({'error': 'Registration failed'}), 500

@app.route('/api/v1/questions/generate', methods=['POST'])
@jwt_required()
@rate_limit(max_requests=20, window=3600)
@validate_request_data(['subject', 'difficulty'])
def generate_questions():
    """Advanced AI question generation with caching and validation"""
    try:
        data = request.get_json()
        user_id = get_jwt_identity()
        
        subject = data['subject']
        difficulty = data['difficulty']
        count = min(data.get('count', 10), 20)  # Max 20 questions
        topics = data.get('topics', [])
        
        # Validate inputs
        valid_subjects = ['math', 'physics', 'chemistry', 'biology', 'history', 'geography', 'literature', 'english']
        valid_difficulties = ['easy', 'medium', 'hard']
        
        if subject not in valid_subjects:
            return jsonify({'error': f'Invalid subject. Must be one of: {valid_subjects}'}), 400
        
        if difficulty not in valid_difficulties:
            return jsonify({'error': f'Invalid difficulty. Must be one of: {valid_difficulties}'}), 400
        
        # Check cache first
        cache_key_str = cache_key('questions', subject, difficulty, count, hash(tuple(sorted(topics))))
        cached_result = get_from_cache(cache_key_str)
        
        if cached_result:
            logger.info(f"Serving cached questions for {subject}/{difficulty}")
            return jsonify({
                'questions': cached_result,
                'cached': True,
                'generated_at': datetime.utcnow().isoformat()
            })
        
        # Get user profile for personalization
        user = User.query.get(user_id)
        user_level = user.level if user else 1
        
        # Generate questions with AI
        questions = []
        generation_start = time.time()
        
        try:
            if openai.api_key:
                questions = AIQuestionGenerator.generate_with_openai(
                    subject, difficulty, count, topics, user_level
                )
            else:
                return jsonify({'error': 'AI service not available'}), 503
                
        except Exception as ai_error:
            logger.error(f"AI generation failed: {ai_error}")
            return jsonify({'error': 'Failed to generate questions'}), 500
        
        generation_time = time.time() - generation_start
        
        # Save questions to database for analytics
        saved_questions = []
        for q_data in questions:
            question = Question(
                subject=subject,
                difficulty=difficulty,
                question_text=q_data['question_text'],
                options=q_data['options'],
                correct_answer_index=q_data['correct_answer_index'],
                explanation=q_data.get('explanation', ''),
                hints=q_data.get('hints', []),
                tags=q_data.get('tags', []),
                points=q_data.get('points', 1)
            )
            db.session.add(question)
            saved_questions.append(question)
        
        db.session.commit()
        
        # Add database IDs to questions
        for i, question in enumerate(saved_questions):
            questions[i]['id'] = question.id
        
        # Cache the results
        set_cache(cache_key_str, questions, ttl=1800)  # 30 minutes
        
        logger.info(f"Generated {len(questions)} questions for {subject}/{difficulty} in {generation_time:.2f}s")
        
        return jsonify({
            'questions': questions,
            'cached': False,
            'generated_at': datetime.utcnow().isoformat(),
            'generation_time': f'{generation_time:.2f}s',
            'metadata': {
                'subject': subject,
                'difficulty': difficulty,
                'count': len(questions),
                'user_level': user_level
            }
        })
        
    except Exception as e:
        db.session.rollback()
        logger.error(f"Question generation failed: {e}")
        return jsonify({'error': 'Question generation failed'}), 500

if __name__ == '__main__':
    # Create tables
    with app.app_context():
        db.create_all()
        logger.info("Database tables created")
    
    # Run application
    port = int(os.environ.get('PORT', 5000))
    debug = os.environ.get('FLASK_ENV') == 'development'
    
    logger.info(f"Starting Smart Quiz API server on port {port}")
    app.run(host='0.0.0.0', port=port, debug=debug, threaded=True)