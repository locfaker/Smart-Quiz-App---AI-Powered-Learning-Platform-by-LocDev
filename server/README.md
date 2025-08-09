# SmartQuiz Server

## Yêu cầu
- Python 3.8+
- pip

## Cài đặt
1. Tạo môi trường ảo:
```bash
python -m venv venv
source venv/bin/activate  # Trên Windows: venv\Scripts\activate
```

2. Cài đặt dependencies:
```bash
pip install -r requirements.txt
```

## Chạy server
```bash
python app.py
```

Server sẽ chạy tại `http://localhost:5000`

## Endpoints
- `/api/generate-questions`: Tạo câu hỏi
- `/api/generate-feedback`: Tạo phản hồi



