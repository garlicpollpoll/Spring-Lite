<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.springlite.demo.model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User Detail - Spring Lite MVC</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .header {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
            padding: 20px;
            margin: -30px -30px 30px -30px;
            border-radius: 8px 8px 0 0;
        }
        .header h1 {
            margin: 0;
            color: white;
        }
        .user-info {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 6px;
            border-left: 4px solid #28a745;
        }
        .field {
            margin-bottom: 15px;
        }
        .field-label {
            font-weight: bold;
            color: #495057;
            display: inline-block;
            width: 80px;
        }
        .field-value {
            color: #212529;
        }
        .back-link {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 20px;
            background-color: #6c757d;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            transition: background-color 0.3s;
        }
        .back-link:hover {
            background-color: #5a6268;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>👤 사용자 상세 정보</h1>
        </div>
        
        <%
            User user = (User) request.getAttribute("user");
            if (user != null) {
        %>
            <div class="user-info">
                <div class="field">
                    <span class="field-label">ID:</span>
                    <span class="field-value"><%= user.getId() %></span>
                </div>
                <div class="field">
                    <span class="field-label">이름:</span>
                    <span class="field-value"><%= user.getName() %></span>
                </div>
                <div class="field">
                    <span class="field-label">이메일:</span>
                    <span class="field-value"><%= user.getEmail() %></span>
                </div>
            </div>
        <%
            } else {
        %>
            <div style="text-align: center; color: #dc3545;">
                <h3>❌ 사용자를 찾을 수 없습니다.</h3>
            </div>
        <%
            }
        %>
        
        <a href="/users/view" class="back-link">← 목록으로 돌아가기</a>
        
        <div style="margin-top: 30px; text-align: center;">
            <p style="color: #6c757d; font-size: 0.9em;">
                🎨 <strong>View</strong> 계층이 추가된 완전한 MVC 패턴
            </p>
        </div>
    </div>
</body>
</html> 