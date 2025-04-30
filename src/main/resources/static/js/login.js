// login.js - 로그인 폼 처리 및 OAuth2 로그인 성공 감지
document.addEventListener('DOMContentLoaded', function() {

  // OAuth2 로그인 성공 처리
  function handleOAuth2LoginSuccess() {
    // URL 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const loginSuccess = urlParams.get('login_success');
    
    if (loginSuccess === 'true') {
      console.log('OAuth2 로그인 성공 감지');
      
      // 쿠키에서 JWT 토큰 확인
      let jwtToken = null;
      const value = `; ${document.cookie}`;
      const parts = value.split(`; jwt_token=`);
      if (parts.length === 2) jwtToken = parts.pop().split(';').shift();
      
      if (jwtToken) {
        // 토큰 저장
        if (window.authService) {
          window.authService.setTokens(jwtToken);
          console.log('JWT 토큰이 저장됨');
        } else {
          localStorage.setItem('accessToken', jwtToken);
        }
        
        // 즉시 UI 업데이트 (사용자 경험 향상)
        updateAuthUI(true);
        
        // URL에서 파라미터 제거하고 페이지 새로고침
        const cleanUrl = window.location.pathname;
        
        // 지연시간을 주어 토큰이 브라우저에 저장되도록 함
        setTimeout(() => {
          window.location.href = cleanUrl; // 페이지 강제 새로고침
        }, 500);
      }
    }
  }
  
  // UI 업데이트 함수
  function updateAuthUI(isLoggedIn) {
    console.log('UI 업데이트 중: 로그인 상태 =', isLoggedIn);
    
    // 로그인/로그아웃 상태에 따라 요소 표시/숨김
    document.querySelectorAll('.auth-hide-logged-in').forEach(el => {
      el.style.display = isLoggedIn ? 'none' : '';
    });
    
    document.querySelectorAll('.auth-hide-logged-out').forEach(el => {
      el.style.display = isLoggedIn ? '' : 'none';
    });
  }
  
  // 인증 상태 확인 및 UI 업데이트
  function checkAuthAndUpdateUI() {
    // JWT 토큰이 있는지 확인
    let isLoggedIn = false;
    
    // 인증 상태 확인 방법 1: authService 사용
    if (window.authService && window.authService.isAuthenticated()) {
      isLoggedIn = true;
    } 
    // 인증 상태 확인 방법 2: 로컬 스토리지 확인
    else if (localStorage.getItem('accessToken')) {
      isLoggedIn = true;
    }
    // 인증 상태 확인 방법 3: 쿠키 확인
    else {
      const value = `; ${document.cookie}`;
      const parts = value.split(`; jwt_token=`);
      if (parts.length === 2 && parts.pop().split(';').shift()) {
        isLoggedIn = true;
      }
    }
    
    updateAuthUI(isLoggedIn);
  }
  
  // 로그인 폼 처리
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', async function(e) {
      e.preventDefault();
      
      const email = document.getElementById('login-email').value;
      const password = document.getElementById('login-password').value;
      
      try {
        const response = await fetch('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ email, password })
        });
        
        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || '로그인에 실패했습니다.');
        }
        
        const data = await response.json();
        
        // 토큰 저장
        if (window.authService) {
          window.authService.setTokens(data.accessToken, data.refreshToken);
        } else {
          localStorage.setItem('accessToken', data.accessToken);
          if (data.refreshToken) {
            localStorage.setItem('refreshToken', data.refreshToken);
          }
        }
        
        // UI 업데이트
        updateAuthUI(true);
        
        // 리다이렉트 URL 확인
        const redirectUrl = document.getElementById('redirect-url')?.value || 
                          sessionStorage.getItem('redirectUrl') || '/';
        
        // 리다이렉트 URL 세션 스토리지에서 제거
        sessionStorage.removeItem('redirectUrl');
        
        // 페이지 이동
        window.location.href = redirectUrl;
        
      } catch (error) {
        console.error('로그인 오류:', error);
        alert(error.message || '로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
      }
    });
  }
  
  // 소셜 로그인 버튼 이벤트 리스너 (있는 경우)
  const googleLoginBtn = document.getElementById('google-login-btn');
  if (googleLoginBtn) {
    googleLoginBtn.addEventListener('click', function() {
      // 리다이렉트 URL이 있으면 세션 스토리지에 저장
      const redirectUrl = document.getElementById('redirect-url')?.value;
      if (redirectUrl) {
        sessionStorage.setItem('redirectUrl', redirectUrl);
      }
    });
  }
  
  // 인증 상태 확인 및 UI 업데이트
  checkAuthAndUpdateUI();
  
  // OAuth2 로그인 성공 감지
  handleOAuth2LoginSuccess();
});