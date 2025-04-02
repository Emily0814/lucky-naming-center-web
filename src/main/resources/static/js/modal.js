// modal.js - 모달 관련 JavaScript 코드
document.addEventListener('DOMContentLoaded', function() {
  // URL 파라미터 확인
  const urlParams = new URLSearchParams(window.location.search);
  const error = urlParams.get('error');
  const login = urlParams.get('login');
  
  // 로그인 실패 메시지 표시
  if (error === 'true' && login === 'failed') {
    alert('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
    // URL에서 파라미터 제거 (브라우저 히스토리 깔끔하게)
    window.history.replaceState({}, document.title, '/');
    
    // 로그인 모달 표시
    if (document.getElementById('login-modal')) {
      const loginModal = new bootstrap.Modal(document.getElementById('login-modal'));
      loginModal.show();
    }
  }
  // 모달 요소 찾기
  const loginModalEl = document.getElementById('login-modal');
  const signupModalEl = document.getElementById('signup-modal');
  
  // 모달이 없으면 함수 종료
  if (!loginModalEl || !signupModalEl) return;
  
  // Bootstrap 모달 인스턴스 생성
  const loginModal = new bootstrap.Modal(loginModalEl);
  const signupModal = new bootstrap.Modal(signupModalEl);
  
  // 포커스 관련 접근성 개선
  loginModalEl.addEventListener('hidden.bs.modal', function() {
    // 모달이 닫힐 때 포커스를 body로 이동
    document.body.focus();
  });
  
  signupModalEl.addEventListener('hidden.bs.modal', function() {
    // 모달이 닫힐 때 포커스를 body로 이동
    document.body.focus();
  });
  
  // 네비게이션 링크 이벤트 리스너 추가 - 콘텐츠 유지를 위해
  const signupNavLink = document.querySelector('a[data-bs-target="#signup-modal"]');
  if (signupNavLink) {
    signupNavLink.addEventListener('click', function(e) {
      e.preventDefault(); // 기본 동작 방지 (페이지 이동 방지)
      signupModal.show();
    });
  }
  
  const loginNavLink = document.querySelector('a[data-bs-target="#login-modal"]');
  if (loginNavLink) {
    loginNavLink.addEventListener('click', function(e) {
      e.preventDefault(); // 기본 동작 방지 (페이지 이동 방지)
      loginModal.show();
    });
  }
  
  // 모달 간 전환 - 로그인 → 회원가입
  const showSignupModalBtn = document.getElementById('showSignupModal');
  if (showSignupModalBtn) {
    showSignupModalBtn.addEventListener('click', function(e) {
      e.preventDefault();
      loginModal.hide();
      setTimeout(() => {
        signupModal.show();
      }, 300);
    });
  }
  
  // 모달 간 전환 - 회원가입 → 로그인
  const showLoginModalBtn = document.getElementById('showLoginModal');
  if (showLoginModalBtn) {
    showLoginModalBtn.addEventListener('click', function(e) {
      e.preventDefault();
      signupModal.hide();
      setTimeout(() => {
        loginModal.show();
      }, 300);
    });
  }

  // 로그인 폼이 제출되기 전에 리다이렉트 URL 설정
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    // 저장된 리다이렉트 URL이 있으면 사용
    const redirectUrl = sessionStorage.getItem('redirectUrl');
    const redirectInput = document.getElementById('redirect-param');
    
    if (redirectUrl && redirectInput) {
      redirectInput.value = redirectUrl;
    }
    
    // 기존 AJAX 로그인은 제거하고 폼이 직접 제출되도록 변경
    // 세션 스토리지에서 리다이렉트 URL 삭제
    loginForm.addEventListener('submit', function() {
      sessionStorage.removeItem('redirectUrl');
    });
  }
  
  // 회원가입 폼 제출 처리
  const signupForm = document.getElementById('signupForm');
  if (signupForm) {
    signupForm.addEventListener('submit', function(e) {
      e.preventDefault();
      // 비밀번호 확인 검증
      const password = document.getElementById('userPassword').value;
      const confirmPassword = document.getElementById('confirmPassword').value;
      
      if (password !== confirmPassword) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
      }
      
      // 사용자 정보 수집
      const email = document.getElementById('userEmail').value;
      const nickname = document.getElementById('nickname').value || '';
      
      // FormData 객체 생성
      const formData = new FormData();
      
      // String 형태로 JSON 데이터 추가 (Blob 대신)
      formData.append('userDTO', new Blob([JSON.stringify({
        email: email,
        password: password,
        nickname: nickname
      })], {
        type: 'application/json'
      }));
      
      // 프로필 이미지 파일 추가 (있는 경우)
      const profileFile = document.getElementById('confirm-profile').files[0];
      if (profileFile) {
        formData.append('profileFile', profileFile);
      }
      
      console.log('회원가입 시도:', email);
      
      // 서버로 FormData 전송
      fetch('/api/signup', {
        method: 'POST',
        body: formData
      })
      .then(response => {
        if (!response.ok) {
          return response.json().then(data => {
            throw new Error(data.message || '회원가입 처리 중 오류가 발생했습니다');
          });
        }
        return response.json();
      })
      .then(data => {
        console.log('회원가입 응답:', data);
        if (data.success === "true") {
          alert('회원가입이 완료되었습니다. 로그인해주세요.');
          signupModal.hide();
          setTimeout(() => {
            loginModal.show();
          }, 300);
        } else {
          alert('회원가입 실패: ' + data.message);
        }
      })
      .catch(error => {
        console.error('회원가입 오류:', error);
        alert('회원가입 중 오류가 발생했습니다: ' + error.message);
      });
    });
  }
  
  // 프로필 이미지 미리보기 처리
  const profileInput = document.getElementById('confirm-profile');
  const profileImage = document.getElementById('profile-image');
  
  if (profileInput && profileImage) {
    profileInput.addEventListener('change', function(e) {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
          profileImage.src = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    });
  }
});