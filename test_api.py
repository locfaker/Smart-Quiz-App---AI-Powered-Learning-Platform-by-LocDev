#!/usr/bin/env python3
"""
Test script for Smart Quiz API
"""

import requests
import json
import time

BASE_URL = "http://localhost:5000/api/v1"

def test_health():
    """Test health endpoint"""
    try:
        response = requests.get(f"{BASE_URL}/health")
        print(f"✅ Health Check: {response.status_code}")
        print(f"   Response: {response.json()}")
        return True
    except Exception as e:
        print(f"❌ Health Check Failed: {e}")
        return False

def test_subjects():
    """Test subjects endpoint"""
    try:
        response = requests.get(f"{BASE_URL}/subjects")
        print(f"✅ Subjects: {response.status_code}")
        data = response.json()
        print(f"   Found {len(data['subjects'])} subjects")
        for subject in data['subjects']:
            print(f"   - {subject['icon']} {subject['name']}")
        return True
    except Exception as e:
        print(f"❌ Subjects Failed: {e}")
        return False

def test_generate_questions():
    """Test question generation"""
    try:
        payload = {
            "subject": "math",
            "difficulty": "easy",
            "count": 3
        }
        response = requests.post(f"{BASE_URL}/questions/generate", json=payload)
        print(f"✅ Generate Questions: {response.status_code}")
        data = response.json()
        print(f"   Generated {len(data['questions'])} questions")
        for i, q in enumerate(data['questions'], 1):
            print(f"   {i}. {q['question_text']}")
            print(f"      Options: {q['options']}")
            print(f"      Correct: {q['options'][q['correct_answer_index']]}")
        return True
    except Exception as e:
        print(f"❌ Generate Questions Failed: {e}")
        return False

def test_generate_feedback():
    """Test feedback generation"""
    try:
        payload = {
            "quiz_data": {
                "subject": "math",
                "difficulty": "easy"
            },
            "answers": [
                {"is_correct": True, "time_spent": 5000},
                {"is_correct": False, "time_spent": 8000},
                {"is_correct": True, "time_spent": 3000}
            ]
        }
        response = requests.post(f"{BASE_URL}/feedback/generate", json=payload)
        print(f"✅ Generate Feedback: {response.status_code}")
        data = response.json()
        feedback = data['feedback']
        print(f"   Assessment: {feedback['overall_assessment']}")
        print(f"   Performance: {feedback['performance_level']}")
        print(f"   Strengths: {', '.join(feedback['strengths'])}")
        return True
    except Exception as e:
        print(f"❌ Generate Feedback Failed: {e}")
        return False

def test_demo_login():
    """Test demo login"""
    try:
        payload = {"username": "test_user"}
        response = requests.post(f"{BASE_URL}/auth/demo-login", json=payload)
        print(f"✅ Demo Login: {response.status_code}")
        data = response.json()
        user = data['user']
        print(f"   User: {user['display_name']} (Level {user['level']})")
        print(f"   XP: {user['total_xp']}, Streak: {user['current_streak']}")
        return True
    except Exception as e:
        print(f"❌ Demo Login Failed: {e}")
        return False

def main():
    print("🚀 Testing Smart Quiz API...")
    print("=" * 50)
    
    # Wait for server to start
    print("⏳ Waiting for server to start...")
    time.sleep(3)
    
    tests = [
        test_health,
        test_subjects,
        test_generate_questions,
        test_generate_feedback,
        test_demo_login
    ]
    
    passed = 0
    total = len(tests)
    
    for test in tests:
        print()
        if test():
            passed += 1
        time.sleep(1)
    
    print()
    print("=" * 50)
    print(f"📊 Test Results: {passed}/{total} tests passed")
    
    if passed == total:
        print("🎉 All tests passed! Smart Quiz API is working perfectly!")
    else:
        print("⚠️  Some tests failed. Check server logs for details.")

if __name__ == "__main__":
    main()