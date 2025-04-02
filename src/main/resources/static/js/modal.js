// modal.js - 모달 관련 JavaScript 코드
document.addEventListener('DOMContentLoaded', function() {
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
  
  // 로그인 폼 제출 처리
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', function(e) {
      e.preventDefault();
      // AJAX를 사용하여 로그인 처리
      const email = document.getElementById('login-email').value;
      const password = document.getElementById('login-password').value;
      
      // 여기에 로그인 로직 추가
      console.log('로그인 시도:', email);
      
      // 예시: 서버로 데이터 전송 (fetch API 사용)
      fetch('/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('네트워크 응답이 올바르지 않습니다');
        }
        return response.json();
      })
      .then(data => {
        if (data.success) {
          window.location.reload();
        } else {
          alert('로그인 실패: ' + data.message);
        }
      })
      .catch(error => {
        console.error('로그인 오류:', error);
        // 실제 API가 없으므로 임시로 성공 처리하여 테스트
        loginModal.hide();
        alert('로그인 성공 (테스트)');
      });
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
     
  
});