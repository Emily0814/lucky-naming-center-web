// auth.js - JWT 인증 처리를 위한 JavaScript
document.addEventListener('DOMContentLoaded', function() {
  
  // JWT 토큰 저장 및 관리
  const authService = {
    // 토큰 저장
    setTokens(accessToken, refreshToken) {
      localStorage.setItem('accessToken', accessToken);
      if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken);
      }
    },
    
    // 액세스 토큰 가져오기
    getAccessToken() {
      return localStorage.getItem('accessToken');
    },
    
    // 리프레시 토큰 가져오기
    getRefreshToken() {
      return localStorage.getItem('refreshToken');
    },
    
    // 쿠키에서 토큰 가져오기
    getTokenFromCookie(name) {
      const value = `; ${document.cookie}`;
      const parts = value.split(`; ${name}=`);
      if (parts.length === 2) return parts.pop().split(';').shift();
      return null;
    },
    
    // 토큰 삭제 (로그아웃)
    clearTokens() {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      // 쿠키도 삭제
      document.cookie = 'jwt_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    },
    
    // 토큰 유효성 검사 (간단한 존재 여부 확인)
    isAuthenticated() {
      return !!(this.getAccessToken() || this.getTokenFromCookie('jwt_token'));
    },
    
    // 액세스 토큰 갱신
    async refreshAccessToken() {
      try {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
          throw new Error('리프레시 토큰이 없습니다.');
        }
        
        const response = await fetch('/api/auth/refresh', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ refreshToken })
        });
        
        if (!response.ok) {
          throw new Error('토큰 갱신에 실패했습니다.');
        }
        
        const data = await response.json();
        if (data.success === "true" && data.accessToken) {
          localStorage.setItem('accessToken', data.accessToken);
          return data.accessToken;
        } else {
          throw new Error('액세스 토큰 갱신에 실패했습니다.');
        }
      } catch (error) {
        console.error('토큰 갱신 오류:', error);
        // 갱신 실패 시 로그아웃 처리
        this.clearTokens();
        // 로그아웃 이벤트 발생
        document.dispatchEvent(new CustomEvent('auth:logout'));
        return null;
      }
    }
  };
  
  // API 요청에 인증 헤더 추가하는 함수
  async function fetchWithAuth(url, options = {}) {
    // 기본 옵션 설정
    const defaultOptions = {
      headers: {
        'Content-Type': 'application/json'
      }
    };
    
    // 옵션 병합
    const mergedOptions = { ...defaultOptions, ...options };
    
    // 헤더 병합
    mergedOptions.headers = { ...defaultOptions.headers, ...options.headers };
    
    // 액세스 토큰이 있으면 헤더에 추가
    const accessToken = authService.getAccessToken();
    if (accessToken) {
      mergedOptions.headers['Authorization'] = `Bearer ${accessToken}`;
    }
    
    // 요청 전송
    let response = await fetch(url, mergedOptions);
    
    // 401 Unauthorized 응답이면 토큰 갱신 시도
    if (response.status === 401 && authService.getRefreshToken()) {
      const newAccessToken = await authService.refreshAccessToken();
      
      // 토큰 갱신 성공 시 요청 재시도
      if (newAccessToken) {
        mergedOptions.headers['Authorization'] = `Bearer ${newAccessToken}`;
        response = await fetch(url, mergedOptions);
      } else {
        // 토큰 갱신 실패 시 로그인 페이지로 리디렉션
        window.location.href = '/?needLogin=true&redirectUrl=' + encodeURIComponent(window.location.pathname);
      }
    }
    
    return response;
  }
  
  // 전역 변수로 노출
  window.authService = authService;
  window.fetchWithAuth = fetchWithAuth;
  
  // 권한이 필요한 페이지 체크
  const requiresAuth = document.body.hasAttribute('data-requires-auth');
  if (requiresAuth && !authService.isAuthenticated()) {
    // 인증이 필요한 페이지에 접근 시 토큰이 없으면 로그인 페이지로 리디렉션
    window.location.href = '/?needLogin=true&redirectUrl=' + encodeURIComponent(window.location.pathname);
  }
  
  // 로그아웃 처리
  const logoutLink = document.querySelector('a[href="/logout"]');
  if (logoutLink) {
    logoutLink.addEventListener('click', function(e) {
      // JWT 토큰 삭제
      authService.clearTokens();
      
      // 기본 동작 허용 (서버 측 로그아웃 처리)
      // 로그아웃 성공 시 홈페이지로 리디렉션됨
    });
  }
});